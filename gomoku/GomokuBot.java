package gomoku;

import java.util.*;

public class GomokuBot {

    public int sight = 1;
    public int depth = 2;
    public int findWinSight = 1;
    public int preventionSight = 1;
    public Gomoku color;
    private final GomokuGame game;

    private final boolean dynamic;
    public int dynamicLargeSight = 2;
    public int dynamicCloseSight = 1;
    public int dynamicShallowDepth = 1;
    public int dynamicDeepDepth = 2;
    public int dynamicMovePool = 2;
    public int dynamicMovePoolDifference = 150;
    public int dynamicLargeSightCutOff = 20;
    public int dynamicDeepDepthCutOff = 32;

    public int totalReach;
    public long totalThinkingTime;
    public int minReach;
    public int maxReach;
    public long shortestThinkingTime;
    public long longestThinkingTime;
    public int moveMadeIndex;
    public static int reach;
    boolean debugPattern = true;

    private static final Map<Long, Integer> transpositionTable = new HashMap<>();

    public GomokuBot(GomokuGame game, boolean dynamic) {
        this.game = game;
        this.dynamic = dynamic;
        if (dynamic) depth = 1;
        reach = 0;
        shortestThinkingTime = Long.MAX_VALUE;
        longestThinkingTime = Long.MIN_VALUE;
        minReach = Integer.MAX_VALUE;
        maxReach = Integer.MIN_VALUE;
        totalThinkingTime = 0;
        transpositionTable.clear();
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setSight(int sight) {
        this.sight = sight;
    }

    public void setColor(Gomoku color) {
        this.color = color;
    }

    public void makeMove() {
        if (!game.isOver()) {
            long startTime = System.currentTimeMillis();

            if (findWinningMove()) return;
            else if (preventImmediateWin()) return;
            else if (findTheStraightFour()) return;
            else if (preventTheStraightFour()) return;

            if (dynamic) {
                makeDynamicMove();
            } else {
                makeStandardMove();
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            if (duration > longestThinkingTime) longestThinkingTime = duration;
            if (duration < shortestThinkingTime) shortestThinkingTime = duration;
            if (reach > maxReach) maxReach = reach;
            if (reach < minReach) minReach = reach;
            totalReach += reach;
            totalThinkingTime += duration;
            int eval = GomokuEvaluatorV1.evaluateGomokuGame(game);

            System.out.println("Depth: " + (!dynamic ? depth : "- ") + "; Sight: " + (!dynamic ? sight : "- ") + "; Move:" + game.moveMade);
            System.out.println("Move made at: " + moveMadeIndex);
            System.out.println(reach + " Positions calculated in " + duration + " ms");
            System.out.println("Evaluation: " + eval + "\n");

            reach = 0;
        }
    }

    private void makeStandardMove() {
        int bestScore = game.turn ? GomokuEvaluatorV1.LOWEST_EVALUATION + 10 : GomokuEvaluatorV1.HIGHEST_EVALUATION - 10;
        int bestMoveIndex = -1;
        boolean botTurn = game.turn;
        int[] allIndex = GomokuUtility.getAllSquaresAroundOccupied(game.getBoard(), sight);

        for (int index : allIndex) {
            int[] move = GomokuUtility.indexToCoordinates(index);
            GomokuGame afterMove = game.makeMoveToNewGame(move[0], move[1]);
            int score = minimax(afterMove, GomokuEvaluatorV1.LOWEST_EVALUATION + 10,
                    GomokuEvaluatorV1.HIGHEST_EVALUATION - 10, depth, afterMove.turn, sight);
            if (botTurn && score > bestScore) {
                bestScore = score;
                bestMoveIndex = index;
            } else if (!botTurn && score < bestScore) {
                bestScore = score;
                bestMoveIndex = index;
            }
        }
        if (bestMoveIndex != -1) {
            int[] bestMove = GomokuUtility.indexToCoordinates(bestMoveIndex);
            game.makeMove(bestMove[0], bestMove[1]);
            moveMadeIndex = bestMoveIndex;
        } else {
            makeRandomMoveInRange();
        }
    }

    private void makeDynamicMove() {
        int bestScore = game.turn ? GomokuEvaluatorV1.LOWEST_EVALUATION + 10 : GomokuEvaluatorV1.HIGHEST_EVALUATION - 10;
        List<Integer> bestMoves = new ArrayList<>();
        List<Integer> bestScores = new ArrayList<>();
        boolean botTurn = game.turn;
        boolean largeSightCutOff = false;
        boolean deepDepthCutOff = false;

        int[] allIndex = GomokuUtility.getAllSquaresAroundOccupied(game.getBoard(), dynamicLargeSight);
        int[] allIndexInnerRing = GomokuUtility.getAllSquaresAroundOccupied(game.getBoard(), dynamicCloseSight);
        int[] allIndexOuterRing = GomokuUtility.removeAll(allIndex, allIndexInnerRing);

        if (game.moveMade >= dynamicLargeSightCutOff) {
            largeSightCutOff = true;
            allIndex = GomokuUtility.removeAll(allIndex, allIndexOuterRing);
        }

        if (game.moveMade >= dynamicDeepDepthCutOff) {
            deepDepthCutOff = true;
            depth = 1;
        }

        if (game.moveMade < 2) {
            allIndex = GomokuUtility.removeAll(allIndex, allIndexOuterRing);
        }

        for (int index : allIndex) {
            if (depth != 0) {
                if (game.moveMade < dynamicLargeSightCutOff && GomokuUtility.contains(allIndexOuterRing, index))
                    depth = dynamicShallowDepth;
                else depth = dynamicDeepDepth;
            }

            int[] move = GomokuUtility.indexToCoordinates(index);
            GomokuGame afterMove = game.makeMoveToNewGame(move[0], move[1]);
            int score = minimax(afterMove, GomokuEvaluatorV1.LOWEST_EVALUATION + 10,
                    GomokuEvaluatorV1.HIGHEST_EVALUATION - 10, depth, afterMove.turn, dynamicCloseSight);

            if ((botTurn && score > bestScore) || (!botTurn && score < bestScore)) {
                bestScore = score;
                bestMoves.clear();
                bestScores.clear();
                bestMoves.add(index);
                bestScores.add(score);
            } else if (score == bestScore) {
                bestMoves.add(index);
                bestScores.add(score);
            }
        }

        List<Integer> validMoves = new ArrayList<>();
        for (int i = 0; i < bestMoves.size(); i++) {
            if (Math.abs(bestScores.get(i) - bestScore) <= dynamicMovePoolDifference) {
                validMoves.add(bestMoves.get(i));
            }
        }

        if (!validMoves.isEmpty()) {
            Random rand = new Random();
            int randomIndex = rand.nextInt(Math.min(validMoves.size(), dynamicMovePool));
            int bestMoveIndex = validMoves.get(randomIndex);
            int[] bestMove = GomokuUtility.indexToCoordinates(bestMoveIndex);
            System.out.println("Dynamic move pool (" + validMoves.size() + "/" + dynamicMovePool + ") option: " + (randomIndex + 1));
            if (largeSightCutOff) {
                System.out.println("Notice: Large sight cut off after move: " + dynamicLargeSightCutOff);
            }
            if (deepDepthCutOff) {
                System.out.println("Notice: Deep depth cut off after move: " + dynamicDeepDepthCutOff);
            }
            game.makeMove(bestMove[0], bestMove[1]);
            moveMadeIndex = bestMoveIndex;
        } else {
            makeRandomMoveInRange();
        }
    }

    public boolean preventImmediateWin() {
        int opponentWon = game.turn ? GomokuEvaluatorV1.LOWEST_EVALUATION : GomokuEvaluatorV1.HIGHEST_EVALUATION;
        int preventionMove = -1;
        boolean botTurn = game.turn;
        int[] allIndex = GomokuUtility.getAllSquaresAroundOccupied(game.getBoard(), preventionSight);

        for (int index : allIndex) {
            int[] move = GomokuUtility.indexToCoordinates(index);
            GomokuGame afterMove = game.makeHypotheticalMoveToNewGame(move[0], move[1]);
            int score = GomokuEvaluatorV1.evaluateGomokuGame(afterMove);
            if (botTurn && score == opponentWon) {
                preventionMove = index;
            } else if (!botTurn && score == opponentWon) {
                preventionMove = index;
            }
        }
        if (preventionMove != -1) {
            int[] bestMove = GomokuUtility.indexToCoordinates(preventionMove);
            game.makeMove(bestMove[0], bestMove[1]);
            moveMadeIndex = preventionMove;
            System.out.println("Move " + game.moveMade + " made by Preventing Winning Move");
            System.out.println("Move made at: " + moveMadeIndex);
            System.out.println("Evaluation: " + GomokuEvaluatorV1.evaluateGomokuGame(game) + "\n");
            return true;
        } else {
            return false;
        }
    }

    public boolean preventTheStraightFour() {
        int bestPreventionScore = game.turn ? GomokuEvaluatorV1.LOWEST_EVALUATION : GomokuEvaluatorV1.HIGHEST_EVALUATION;
        List<Integer> preventionMoves = new ArrayList<>();
        boolean botTurn = game.turn;
        int[] allIndex = GomokuUtility.getAllSquaresAroundOccupied(game.getBoard(), preventionSight);

        for (int index : allIndex) {
            int[] move = GomokuUtility.indexToCoordinates(index);
            GomokuGame afterMove = game.makeHypotheticalMoveToNewGame(move[0], move[1]);
            int score = GomokuEvaluatorV1.evaluateGomokuGame(afterMove);

            if ((botTurn && score < -GomokuEvaluatorV1.WIN_GUARANTEED) ||
                    (!botTurn && score > GomokuEvaluatorV1.WIN_GUARANTEED)) {
                preventionMoves.add(index);
            }
        }

        System.out.println(preventionMoves.size() + " Straight Four potential found");

        if (!preventionMoves.isEmpty()) {
            int bestMoveIndex = -1;
            for (int moveIndex : preventionMoves) {
                int[] move = GomokuUtility.indexToCoordinates(moveIndex);
                GomokuGame afterMove = game.makeHypotheticalMoveToNewGame(move[0], move[1]);
                int score = GomokuEvaluatorV1.evaluateGomokuGame(afterMove);

                if ((botTurn && score > bestPreventionScore) || (!botTurn && score < bestPreventionScore)) {
                    bestPreventionScore = score;
                    bestMoveIndex = moveIndex;
                }
            }

            if (bestMoveIndex != -1) {
                int[] bestMove = GomokuUtility.indexToCoordinates(bestMoveIndex);
                game.makeMove(bestMove[0], bestMove[1]);
                moveMadeIndex = bestMoveIndex;
                System.out.println("Move " + game.moveMade + " made by Preventing Straight Four");
                System.out.println("Move made at: " + moveMadeIndex);
                System.out.println("Evaluation: " + GomokuEvaluatorV1.evaluateGomokuGame(game) + "\n");
                return true;
            }
        }

        return false;
    }

    public boolean findTheStraightFour() {
        int straightFourIndex = -1;
        boolean botTurn = game.turn;
        int[] allIndex = GomokuUtility.getAllSquaresAroundOccupied(game.getBoard(), findWinSight);

        for (int index : allIndex) {
            int[] move = GomokuUtility.indexToCoordinates(index);
            GomokuGame afterMove = game.makeMoveToNewGame(move[0], move[1]);
            int score = GomokuEvaluatorV1.evaluateGomokuGame(afterMove);
            if (botTurn && score > GomokuEvaluatorV1.WIN_GUARANTEED) {
                straightFourIndex = index;
            } else if (!botTurn && score < -GomokuEvaluatorV1.WIN_GUARANTEED) {
                straightFourIndex = index;
            }
        }
        if (straightFourIndex != -1) {
            int[] bestMove = GomokuUtility.indexToCoordinates(straightFourIndex);
            game.makeMove(bestMove[0], bestMove[1]);
            moveMadeIndex = straightFourIndex;
            System.out.println("Move " + game.moveMade + " made by Finding Straight Four");
            System.out.println("Move made at: " + moveMadeIndex);
            System.out.println("Evaluation: " + GomokuEvaluatorV1.evaluateGomokuGame(game) + "\n");
            return true;
        } else {
            return false;
        }
    }

    public boolean findWinningMove() {
        int winningMoveIndex = -1;
        boolean botTurn = game.turn;
        int[] allIndex = GomokuUtility.getAllSquaresAroundOccupied(game.getBoard(), findWinSight);

        for (int index : allIndex) {
            int[] move = GomokuUtility.indexToCoordinates(index);
            GomokuGame afterMove = game.makeMoveToNewGame(move[0], move[1]);
            int score = GomokuEvaluatorV1.evaluateGomokuGame(afterMove);
            if (botTurn && score == GomokuEvaluatorV1.HIGHEST_EVALUATION) {
                winningMoveIndex = index;
            } else if (!botTurn && score == GomokuEvaluatorV1.LOWEST_EVALUATION) {
                winningMoveIndex = index;
            }
        }
        if (winningMoveIndex != -1) {
            int[] bestMove = GomokuUtility.indexToCoordinates(winningMoveIndex);
            game.makeMove(bestMove[0], bestMove[1]);
            moveMadeIndex = winningMoveIndex;
            System.out.println("Move " + game.moveMade + " made by Finding Winning Move");
            System.out.println("Move made at: " + moveMadeIndex);
            System.out.println("Evaluation: " + GomokuEvaluatorV1.evaluateGomokuGame(game) + "\n");
            return true;
        } else {
            return false;
        }
    }

    public void makeRandomMoveInRange() {
        if (!game.isOver()) {
            Random random = new Random();
            int[] allAvailableMove = GomokuUtility.getAllSquaresAroundOccupied(game.getBoard(), sight);
            int randomMoveIndex = random.nextInt(allAvailableMove.length);
            int[] randomMove = GomokuUtility.indexToCoordinates(allAvailableMove[randomMoveIndex]);
            game.makeMove(randomMove[0], randomMove[1]);
            moveMadeIndex = allAvailableMove[randomMoveIndex];
        }
    }

    public static int minimax(GomokuGame currentGame, int alpha, int beta, int depth, boolean turn, int sight) {
        reach++;

        Long hash = currentGame.getHash();
        if (transpositionTable.containsKey(hash)) {
            return transpositionTable.get(hash);
        }

        if (depth == 0 || currentGame.isOver()) {
            int evaluation = GomokuEvaluatorV1.evaluateGomokuGame(currentGame);
            transpositionTable.put(hash, evaluation);
            return evaluation;
        }

        if (turn) {
            int maxEval = GomokuEvaluatorV1.LOWEST_EVALUATION + 10;
            for (int index : GomokuUtility.getAllSquaresAroundOccupied(currentGame.getBoard(), sight)) {
                int[] move = GomokuUtility.indexToCoordinates(index);
                GomokuGame afterMove = currentGame.makeMoveToNewGame(move[0], move[1]);
                int eval = minimax(afterMove, alpha, beta, depth - 1, afterMove.turn, sight);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, maxEval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = GomokuEvaluatorV1.HIGHEST_EVALUATION - 10;
            for (int index : GomokuUtility.getAllSquaresAroundOccupied(currentGame.getBoard(), sight)) {
                int[] move = GomokuUtility.indexToCoordinates(index);
                GomokuGame afterMove = currentGame.makeMoveToNewGame(move[0], move[1]);
                int eval = minimax(afterMove, alpha, beta, depth - 1, afterMove.turn, sight);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, minEval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

}
