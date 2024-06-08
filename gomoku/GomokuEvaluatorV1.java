package gomoku;

public class GomokuEvaluatorV1 {

    public static final int LOWEST_EVALUATION = Integer.MIN_VALUE;
    public static final int HIGHEST_EVALUATION = Integer.MAX_VALUE;
    public static final int WIN_GUARANTEED = 180000;
    public static final int STRONG_POSITIONAL_BONUS = 5000;
    public static final int EQUAL_EVALUATION = 0;

    public static final int FOUR_VALUE_CURRENT_TURN = 7000;
    public static final int FOUR_VALUE_OFF_TURN = 2000;
    public static final int STRAIGHT_FOUR_VALUE_CURRENT_TURN = HIGHEST_EVALUATION;
    public static final int STRAIGHT_FOUR_VALUE_OFF_TURN = 200000;
    public static final int STRAIGHT_THREE_VALUE_CURRENT_TURN = 2500;
    public static final int STRAIGHT_THREE_VALUE_OFF_TURN = 1500;
    public static final int OPEN_THREE_VALUE_CURRENT_TURN = 2500;
    public static final int OPEN_THREE_VALUE_OFF_TURN = 1400;
    public static final int BROKEN_THREE_VALUE_CURRENT_TURN = 2500;
    public static final int BROKEN_THREE_VALUE_OFF_TURN = 1450;

    static boolean debugNewEvaluation = false;

    public static void debugAllPattern(GomokuGame game) {
        System.out.println("Pattern for player: WHITE - BLACK");
        System.out.println("The four: " + getPatternTheFour(game.getBoard(), Gomoku.WHITE) + " - " + getPatternTheFour(game.getBoard(), Gomoku.BLACK));
        System.out.println("The straight four: " + getPatternTheStraightFour(game.getBoard(), Gomoku.WHITE) + " - " + getPatternTheStraightFour(game.getBoard(), Gomoku.BLACK));
        System.out.println("The straight three: " + getPatternTheStraightThree(game.getBoard(), Gomoku.WHITE) + " - " + getPatternTheStraightThree(game.getBoard(), Gomoku.BLACK));
        System.out.println("The open three: " + getPatternTheOpenThree(game.getBoard(), Gomoku.WHITE) + " - " + getPatternTheOpenThree(game.getBoard(), Gomoku.BLACK));
        System.out.println("The broken three: " + getPatternTheBrokenThree(game.getBoard(), Gomoku.WHITE) + " - " + getPatternTheBrokenThree(game.getBoard(), Gomoku.BLACK));
        System.out.println("Eval by broken sets: " + getEvaluationOfPlayerByBrokenSet(game.getBoard(), Gomoku.WHITE, !game.turn) + " - " + getEvaluationOfPlayerByBrokenSet(game.getBoard(), Gomoku.BLACK, game.turn));
        System.out.println("Eval: " + evaluateGomokuGame(game));
    }

    public static int evaluateGomokuGame(GomokuGame game) {
        if (game.checkGameState() == Gomoku.WHITE_WON) return HIGHEST_EVALUATION;
        else if (game.checkGameState() == Gomoku.BLACK_WON) return LOWEST_EVALUATION;
        else if (game.checkGameState() == Gomoku.DRAW) return EQUAL_EVALUATION;

        boolean currentTurn = game.turn;

        int eval = EQUAL_EVALUATION;

        int whiteFour = getPatternTheFour(game.getBoard(), Gomoku.WHITE);
        int blackFour = getPatternTheFour(game.getBoard(), Gomoku.BLACK);
        int whiteStraightFour = getPatternTheStraightFour(game.getBoard(), Gomoku.WHITE);
        int blackStraightFour = getPatternTheStraightFour(game.getBoard(), Gomoku.BLACK);
        int whiteStraightThree = getPatternTheStraightThree(game.getBoard(), Gomoku.WHITE);
        int blackStraightThree = getPatternTheStraightThree(game.getBoard(), Gomoku.BLACK);
        int whiteOpenThree = getPatternTheOpenThree(game.getBoard(), Gomoku.WHITE);
        int blackOpenThree = getPatternTheOpenThree(game.getBoard(), Gomoku.BLACK);
        int whiteBrokenThree = getPatternTheBrokenThree(game.getBoard(), Gomoku.WHITE);
        int blackBrokenThree = getPatternTheBrokenThree(game.getBoard(), Gomoku.BLACK);

        if ((whiteStraightThree >= 2) || (whiteFour >= 2) || (whiteOpenThree >= 2) ||
                (whiteBrokenThree >= 2) || (whiteOpenThree + whiteBrokenThree >= 2) ||
                (whiteOpenThree + whiteStraightThree >= 2) || (whiteBrokenThree + whiteStraightThree >= 2) ||
                (whiteFour + whiteOpenThree >= 2) || (whiteFour + whiteStraightThree >= 2) ||
                (whiteFour + whiteBrokenThree >= 2)) {
            eval += currentTurn ? STRONG_POSITIONAL_BONUS : STRONG_POSITIONAL_BONUS / 2;
        }
        if ((blackStraightThree >= 2) || (blackFour >= 2) || (blackOpenThree >= 2) ||
                (blackBrokenThree >= 2) || (blackOpenThree + blackBrokenThree >= 2) ||
                (blackOpenThree + blackStraightThree >= 2) || (blackBrokenThree + blackStraightThree >= 2) ||
                (blackFour + blackOpenThree >= 2) || (blackFour + blackStraightThree >= 2) ||
                (blackFour + blackBrokenThree >= 2)) {
            eval -= !currentTurn ? STRONG_POSITIONAL_BONUS : STRONG_POSITIONAL_BONUS / 2;
        }

        eval += whiteFour * (currentTurn ? FOUR_VALUE_CURRENT_TURN : FOUR_VALUE_OFF_TURN) - blackFour * (!currentTurn ? FOUR_VALUE_CURRENT_TURN : FOUR_VALUE_OFF_TURN);
        eval += whiteStraightFour * (currentTurn ? STRAIGHT_FOUR_VALUE_CURRENT_TURN : STRAIGHT_FOUR_VALUE_OFF_TURN) - blackStraightFour * (!currentTurn ? STRAIGHT_FOUR_VALUE_CURRENT_TURN : STRAIGHT_FOUR_VALUE_OFF_TURN);
        eval += whiteStraightThree * (currentTurn ? STRAIGHT_THREE_VALUE_CURRENT_TURN : STRAIGHT_THREE_VALUE_OFF_TURN) - blackStraightThree * (!currentTurn ? STRAIGHT_THREE_VALUE_CURRENT_TURN : STRAIGHT_THREE_VALUE_OFF_TURN);
        eval += whiteOpenThree * (currentTurn ? OPEN_THREE_VALUE_CURRENT_TURN : OPEN_THREE_VALUE_OFF_TURN) - blackOpenThree * (!currentTurn ? OPEN_THREE_VALUE_CURRENT_TURN : OPEN_THREE_VALUE_OFF_TURN);
        eval += whiteBrokenThree * (currentTurn ? BROKEN_THREE_VALUE_CURRENT_TURN : BROKEN_THREE_VALUE_OFF_TURN) - blackBrokenThree * (!currentTurn ? BROKEN_THREE_VALUE_CURRENT_TURN : BROKEN_THREE_VALUE_OFF_TURN);

        if (eval == EQUAL_EVALUATION || debugNewEvaluation) {
            eval += getEvaluationOfPLayerBySet(game.getBoard(), Gomoku.WHITE, currentTurn) / 2
                    - getEvaluationOfPLayerBySet(game.getBoard(), Gomoku.BLACK, !currentTurn) / 2;
            eval += getEvaluationOfPlayerByBrokenSet(game.getBoard(), Gomoku.WHITE, currentTurn) / 2
                    - getEvaluationOfPlayerByBrokenSet(game.getBoard(), Gomoku.BLACK, !currentTurn) / 2;
        }

        return eval;
    }

    public static int getEvaluationByDisruption(Gomoku[][] board, Gomoku player) {
        int[] allPlayerIndex = GomokuUtility.getAllSpecifiedSquaresAroundPlayer(board, player, GomokuUtility.getOpponent(player), 1);
        return allPlayerIndex.length;
    }

    public static int getEvaluationByTacticalPotential(Gomoku[][] board, Gomoku player) {
        int[] allPlayerIndex = GomokuUtility.getAllSpecifiedSquaresAroundPlayer(board, player, player, 1);
        return allPlayerIndex.length * 2;
    }

    public static int getEvaluationByMobility(Gomoku[][] board, Gomoku player) {
        int[] allPlayerIndex = GomokuUtility.getAllSpecifiedSquaresAroundPlayer(board, player, Gomoku.EMPTY, 1);
        return allPlayerIndex.length;
    }

    public static int getEvaluationOfPlayerByBrokenSet(Gomoku[][] board, Gomoku player, boolean currentTurn) {
        int eval = 0;
        Gomoku opponent = GomokuUtility.getOpponent(player);
        int[] allPlayerIndex = GomokuUtility.getAllPlayerIndexes(board, player);
        for (int playerIndex : allPlayerIndex) {
            int x = GomokuUtility.indexToCoordinates(playerIndex)[0];
            int y = GomokuUtility.indexToCoordinates(playerIndex)[1];
            for (int[] dir : GomokuUtility.AXIS_DELTAS) {
                int inARow = 1;
                int blocking = 0;
                int between = 0;
                int shiftingX = x;
                int shiftingY = y;
                boolean nestedCheck = false;
                while (!nestedCheck) {
                    shiftingX += dir[0];
                    shiftingY += dir[1];
                    if (!GomokuUtility.isValidCoordinates(shiftingX, shiftingY)) break;
                    else if (board[shiftingX][shiftingY] == player) {
                        inARow++;
                    } else if (board[shiftingX][shiftingY] == opponent) {
                        blocking++;
                        int shiftingXRev = x;
                        int shiftingYRev = y;
                        nestedCheck = true;
                        while (true) {
                            shiftingXRev -= dir[0];
                            shiftingYRev -= dir[1];
                            if (!GomokuUtility.isValidCoordinates(shiftingXRev, shiftingYRev)) break;
                            if (board[shiftingXRev][shiftingYRev] == opponent) {
                                blocking++;
                                break;
                            } else if (board[shiftingXRev][shiftingYRev] == player) {
                                inARow++;
                            } else if (board[shiftingXRev][shiftingYRev] == Gomoku.EMPTY) {
                                between++;
                            }
                        }
                    } else if (board[shiftingX][shiftingY] == Gomoku.EMPTY) {
                        between++;
                    }
                }
                if (inARow > 0 && between > 0) {
                    eval += getEvaluationOfBrokenSet(inARow, blocking, between, currentTurn);
                }
            }
        }
        return eval;
    }

    public static int getEvaluationOfBrokenSet(int setOf, int blocking, int between, boolean currentTurn) {
        if (blocking > 1 || between > 2) return EQUAL_EVALUATION;
        int penalty;
        int eval = EQUAL_EVALUATION;
        switch (setOf) {
            case 1:
                return eval;
            case 2:
                if (blocking == 1 || between > 1) return EQUAL_EVALUATION;
                penalty = 4;
                eval = currentTurn ? 20 : 4;
                break;
            case 3:
                if (blocking == 1) return 20;
                penalty = currentTurn ? 80 : 100;
                eval = currentTurn ? 500 : 100;
                break;
            case 4:
                penalty = currentTurn ? 3500 : 5000;
                eval = currentTurn ? 10000 : 5800;
                break;
            default:
                if (setOf > 4) return GomokuEvaluatorV1.HIGHEST_EVALUATION;
                return GomokuEvaluatorV1.EQUAL_EVALUATION;
        }
        return eval - penalty;
    }

    public static int getEvaluationOfPLayerBySet(Gomoku[][] board, Gomoku player, boolean currentTurn) {
        int eval = 0;
        Gomoku opponent = GomokuUtility.getOpponent(player);
        int[] allPlayerIndex = GomokuUtility.getAllPlayerIndexes(board, player);
        for (int playerIndex : allPlayerIndex) {
            int x = GomokuUtility.indexToCoordinates(playerIndex)[0];
            int y = GomokuUtility.indexToCoordinates(playerIndex)[1];
            for (int[] dir : GomokuUtility.AXIS_DELTAS) {
                int inARow = 1;
                int blocking = 0;
                int shiftingX = x;
                int shiftingY = y;
                boolean nestedCheck = false;
                while (!nestedCheck) {
                    shiftingX += dir[0];
                    shiftingY += dir[1];
                    if (!GomokuUtility.isValidCoordinates(shiftingX, shiftingY)) break;
                    else if (board[shiftingX][shiftingY] == player) {
                        inARow++;
                    } else if (board[shiftingX][shiftingY] == opponent) {
                        blocking++;
                        int shiftingXRev = x;
                        int shiftingYRev = y;
                        nestedCheck = true;
                        while (true) {
                            shiftingXRev -= dir[0];
                            shiftingYRev -= dir[1];
                            if (!GomokuUtility.isValidCoordinates(shiftingXRev, shiftingYRev)) break;
                            if (board[shiftingXRev][shiftingYRev] == opponent) {
                                blocking++;
                                break;
                            } else if (board[shiftingXRev][shiftingYRev] == player) {
                                inARow++;
                            } else if (board[shiftingXRev][shiftingYRev] == Gomoku.EMPTY) break;
                        }
                    } else if (board[shiftingX][shiftingY] == Gomoku.EMPTY) break;
                }
                if (GomokuUtility.isValidCoordinates(x - dir[0], y - dir[1])
                        && board[x - dir[0]][y - dir[1]] == opponent) blocking++;
                if (inARow > 0) {
                    eval += getEvaluationOfSet(inARow, blocking, currentTurn);
                }
            }
        }
        return eval;
    }

    public static int getEvaluationOfSet(int setOf, int blocking, boolean currentTurn) {
        if (blocking >= 2) return EQUAL_EVALUATION;
        int eval = EQUAL_EVALUATION;
        switch (setOf) {
            case 1:
                return eval;
            case 2:
                eval = currentTurn ? 20 : 4;
                break;
            case 3:
                eval = currentTurn ? 500 : 100;
                break;
            case 4:
                eval = currentTurn ? WIN_GUARANTEED : 5000;
                break;
            default:
                if (setOf > 4) return GomokuEvaluatorV1.HIGHEST_EVALUATION;
                return GomokuEvaluatorV1.EQUAL_EVALUATION;
        }
        if (blocking == 1) eval /= 2;
        return eval;
    }

    public static int getPatternTheFour(Gomoku[][] board, Gomoku player) {
        int numPattern = 0;
        Gomoku opponent = GomokuUtility.getOpponent(player);
        int[] allOppIndex = GomokuUtility.getAllPlayerIndexes(board, opponent);
        for (int oppIndex : allOppIndex) {
            int oppX = GomokuUtility.indexToCoordinates(oppIndex)[0];
            int oppY = GomokuUtility.indexToCoordinates(oppIndex)[1];
            for (int[] dir : GomokuUtility.DIRECTIONS_DELTAS) {
                int inARow = 0;
                int shiftingOppX = oppX;
                int shiftingOppY = oppY;
                while (true) {
                    shiftingOppX += dir[0];
                    shiftingOppY += dir[1];
                    if (!GomokuUtility.isValidCoordinates(shiftingOppX, shiftingOppY)) break;
                    if (board[shiftingOppX][shiftingOppY] == opponent) break;
                    else if (board[shiftingOppX][shiftingOppY] == player) {
                        inARow++;
                    } else if (board[shiftingOppX][shiftingOppY] == Gomoku.EMPTY) {
                        if (inARow >= 4) numPattern++;
                        break;
                    }
                }
            }
        }
        return numPattern;
    }

    public static int getPatternTheStraightFour(Gomoku[][] board, Gomoku player) {
        int numPattern = 0;
        Gomoku opponent = GomokuUtility.getOpponent(player);
        int[] allEmptyIndexes = GomokuUtility.getAllEmptySquareIndexes(board);
        for (int emptyIndex : allEmptyIndexes) {
            int empX = GomokuUtility.indexToCoordinates(emptyIndex)[0];
            int empY = GomokuUtility.indexToCoordinates(emptyIndex)[1];
            for (int[] dir : GomokuUtility.DIRECTIONS_DELTAS) {
                int inARow = 0;
                int shiftingEmpX = empX;
                int shiftingEmpY = empY;
                while (true) {
                    shiftingEmpX += dir[0];
                    shiftingEmpY += dir[1];
                    if (!GomokuUtility.isValidCoordinates(shiftingEmpX, shiftingEmpY)) break;
                    if (board[shiftingEmpX][shiftingEmpY] == opponent) break;
                    else if (board[shiftingEmpX][shiftingEmpY] == player) {
                        inARow++;
                    } else if (board[shiftingEmpX][shiftingEmpY] == Gomoku.EMPTY) {
                        if (inARow == 4) numPattern++;
                        break;
                    }
                }
            }
        }
        return numPattern / 2;
    }

    public static int getPatternTheStraightThree(Gomoku[][] board, Gomoku player) {
        int numPattern = 0;
        Gomoku opponent = GomokuUtility.getOpponent(player);
        int[] allEmptyIndexes = GomokuUtility.getAllEmptySquareIndexes(board);
        for (int emptyIndex : allEmptyIndexes) {
            int empX = GomokuUtility.indexToCoordinates(emptyIndex)[0];
            int empY = GomokuUtility.indexToCoordinates(emptyIndex)[1];
            for (int[] dir : GomokuUtility.DIRECTIONS_DELTAS) {
                int inARow = 0;
                int shiftingEmpX = empX;
                int shiftingEmpY = empY;
                while (true) {
                    shiftingEmpX += dir[0];
                    shiftingEmpY += dir[1];
                    if (!GomokuUtility.isValidCoordinates(shiftingEmpX, shiftingEmpY)) break;
                    if (board[shiftingEmpX][shiftingEmpY] == opponent) break;
                    else if (board[shiftingEmpX][shiftingEmpY] == player) {
                        inARow++;
                    } else if (board[shiftingEmpX][shiftingEmpY] == Gomoku.EMPTY) {
                        if (inARow == 3) numPattern++;
                        break;
                    }
                }
            }
        }
        return numPattern / 2 - getPatternTheOpenThree(board, player);
    }

    public static int getPatternTheOpenThree(Gomoku[][] board, Gomoku player) {
        int numPattern = 0;
        Gomoku opponent = GomokuUtility.getOpponent(player);
        int[] allOppIndex = GomokuUtility.getAllPlayerIndexes(board, opponent);
        for (int oppIndex : allOppIndex) {
            int oppX = GomokuUtility.indexToCoordinates(oppIndex)[0];
            int oppY = GomokuUtility.indexToCoordinates(oppIndex)[1];
            for (int[] dir : GomokuUtility.DIRECTIONS_DELTAS) {
                int inARow = 0;
                int shiftingOppX = oppX;
                int shiftingOppY = oppY;
                boolean nestedCheck = false;
                while (!nestedCheck) {
                    shiftingOppX += dir[0];
                    shiftingOppY += dir[1];
                    if (!GomokuUtility.isValidCoordinates(shiftingOppX, shiftingOppY)) break;
                    if (board[shiftingOppX][shiftingOppY] == opponent) break;
                    else if (board[shiftingOppX][shiftingOppY] == Gomoku.EMPTY) {
                        while (true) {
                            nestedCheck = true;
                            shiftingOppX += dir[0];
                            shiftingOppY += dir[1];
                            if (!GomokuUtility.isValidCoordinates(shiftingOppX, shiftingOppY)) break;
                            if (board[shiftingOppX][shiftingOppY] == opponent) break;
                            if (board[shiftingOppX][shiftingOppY] == Gomoku.EMPTY) {
                                if (inARow == 3) numPattern++;
                                break;
                            } else {
                                inARow++;
                            }
                        }
                    } else break;
                }
            }
        }
        return numPattern;
    }

    public static int getPatternTheBrokenThree(Gomoku[][] board, Gomoku player) {
        int numPattern = 0;
        Gomoku opponent = GomokuUtility.getOpponent(player);
        int[] allEmptyIndexes = GomokuUtility.getAllEmptySquareIndexes(board);
        for (int emptyIndex : allEmptyIndexes) {
            int empX = GomokuUtility.indexToCoordinates(emptyIndex)[0];
            int empY = GomokuUtility.indexToCoordinates(emptyIndex)[1];
            for (int[] dir : GomokuUtility.DIRECTIONS_DELTAS) {
                int inARow = 0;
                int shiftingEmpX = empX;
                int shiftingEmpY = empY;
                while (true) {
                    shiftingEmpX += dir[0];
                    shiftingEmpY += dir[1];
                    if (!GomokuUtility.isValidCoordinates(shiftingEmpX, shiftingEmpY)) break;
                    if (board[shiftingEmpX][shiftingEmpY] == opponent) break;
                    else if (board[shiftingEmpX][shiftingEmpY] == player) {
                        inARow++;
                    } else if (board[shiftingEmpX][shiftingEmpY] == Gomoku.EMPTY) {
                        if (inARow == 2) {
                            if (GomokuUtility.isValidCoordinates(empX - dir[0], empY - dir[1])
                                    && board[empX - dir[0]][empY - dir[1]] == player) {
                                if (GomokuUtility.isValidCoordinates(empX - 2 * dir[0], empY - 2 * dir[1])
                                        && board[empX - 2 * dir[0]][empY - 2 * dir[1]] == Gomoku.EMPTY) {
                                    numPattern++;
                                    break;
                                }
                            } else break;
                        }
                        break;
                    }
                }
            }
        }
        return numPattern;
    }

}
