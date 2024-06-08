package gomoku;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class GomokuGame {

    public boolean turn;
    private Gomoku[][] board;
    public final int width;
    public final int height;
    public final int size;
    public final int toWinInARow;
    public int[] winningIndexes;
    public int moveMade;

    public boolean replayMode;
    public ArrayList<Gomoku[][]> replayTape;

    private static final long[][][] ZOBRIST_TABLE = generateZobristTable();
    private long positionHash;

    public GomokuGame(int size, int toWinInARow) {
        replayMode = false;
        replayTape = new ArrayList<>();
        turn = false;
        width = size;
        height = size;
        this.size = size;
        GomokuUtility.attachSize(size);
        this.toWinInARow = toWinInARow;
        winningIndexes = new int[toWinInARow];
        moveMade = 0;
        initializeBoard();
        positionHash = computePositionHash();
    }

    public GomokuGame(boolean turn, Gomoku[][] board, int toWinInARow) {
        this.turn = turn;
        width = board[0].length;
        height = board.length;
        this.size = board.length;
        GomokuUtility.attachSize(size);
        this.toWinInARow = toWinInARow;
        winningIndexes = new int[toWinInARow];
        moveMade = -1;
        this.board = GomokuUtility.copyBoard(board);
        positionHash = computePositionHash();
    }

    private void initializeBoard() {
        board = new Gomoku[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                board[i][j] = Gomoku.EMPTY;
            }
        }
    }

    public Gomoku[][] getBoard() {
        return board;
    }

    public void setBoard(Gomoku[][] board) {
        this.board = board;
    }

    private void setSquare(int posX, int posY, Gomoku player) {
        board[posX][posY] = player;
    }

    public Gomoku getSquare(int posX, int posY) {
        if (withinBoard(posX, posY)) return board[posX][posY];
        else return Gomoku.OUTSIDE;
    }

    public boolean withinBoard(int posX, int posY) {
        return posX >= 0 && posX < height && posY >= 0 && posY < width;
    }

    public boolean makeMove(int posX, int posY) {
        if (getSquare(posX, posY) == Gomoku.EMPTY) {
            setSquare(posX, posY, turn ? Gomoku.WHITE : Gomoku.BLACK);
            turn = !turn;
            moveMade++;
            positionHash = computePositionHash();
            return true;
        }
        return false;
    }

    public GomokuGame makeMoveToNewGame(int posX, int posY) {
        GomokuGame newGame = new GomokuGame(this.turn, GomokuUtility.copyBoard(this.getBoard()), this.toWinInARow);
        newGame.makeMove(posX, posY);
        return newGame;
    }

    public GomokuGame makeHypotheticalMoveToNewGame(int posX, int posY) {
        GomokuGame newGame = new GomokuGame(this.turn, GomokuUtility.copyBoard(this.getBoard()), this.toWinInARow);
        newGame.turn = !turn;
        newGame.makeMove(posX, posY);
        return newGame;
    }

    public Gomoku checkGameState() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                if (checkGameStateOmniDirection(i, j, Gomoku.BLACK)) return Gomoku.BLACK_WON;
                if (checkGameStateOmniDirection(i, j, Gomoku.WHITE)) return Gomoku.WHITE_WON;
            }
        }
        if (GomokuUtility.getAllEmptySquareIndexes(board).length == 0) return Gomoku.DRAW;
        return Gomoku.GAME_ONGOING;
    }

    private boolean checkGameStateHelper(int originX, int originY, int deltaX, int deltaY, Gomoku player) {
        int countForWin = 0;
        while (withinBoard(originX, originY)) {
            if (getSquare(originX, originY) == Gomoku.EMPTY
                    || getSquare(originX, originY) != player) return false;
            winningIndexes[countForWin] = GomokuUtility.coordinatesToIndex(originX, originY);
            originX += deltaX;
            originY += deltaY;
            countForWin++;
            if (countForWin >= toWinInARow) {
                winningIndexes = Arrays.copyOf(winningIndexes, countForWin);
                return true;
            }
        }
        return false;
    }

    private boolean checkGameStateOmniDirection(int originX, int originY, Gomoku player) {
        for (int[] dir : GomokuUtility.DIRECTIONS_DELTAS) {
            if (checkGameStateHelper(originX, originY, dir[0], dir[1], player)) {
                return true;
            }
        }
        return false;
    }

    public boolean isOver() {
        return checkGameState() != Gomoku.GAME_ONGOING;
    }

    public boolean enterReplayMode() {
        if (isOver()) {
            replayMode = true;
            return true;
        } else return false;
    }

    public void windBack() {
        if (replayMode && moveMade > 1) {
            setBoard(replayTape.get(--moveMade - 1));
        }
    }

    public void windForward() {
        if (replayMode && moveMade < replayTape.size()) {
            setBoard(replayTape.get(moveMade++));
        }
    }

    private long computePositionHash() {
        long hash = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Gomoku piece = board[i][j];
                if (piece != Gomoku.EMPTY) {
                    hash ^= ZOBRIST_TABLE[i][j][piece.ordinal()];
                }
            }
        }
        return hash;
    }

    public long getHash() {
        return positionHash;
    }

    private static long[][][] generateZobristTable() {
        Random random = new Random();
        long[][][] table = new long[19][19][3];
        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                for (int k = 0; k < 3; k++) {
                    table[i][j][k] = random.nextLong();
                }
            }
        }
        return table;
    }
    
}
