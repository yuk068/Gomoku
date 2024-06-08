package gomoku;

import java.util.*;

public class GomokuUtility {

    public static final int[][] DIRECTIONS_DELTAS = {{0, 1}, {1, 0}, {1, 1}, {-1, -1}, {0, -1}, {-1, 0}, {1, -1}, {-1, 1}};
    public static final int[][] AXIS_DELTAS = {{0, 1}, {1, 1}, {1, 0}};
    public static int currentSize;
    
    public static void attachSize(int size) {
        currentSize = size;
    }

    public static Gomoku[][] copyBoard(Gomoku[][] board) {
        int rows = board.length;
        int cols = board[0].length;
        Gomoku[][] copy = new Gomoku[rows][cols];

        for (int i = 0; i < rows; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, cols);
        }
        return copy;
    }

    public static int[] getAllSquaresAroundOccupied(Gomoku[][] board, int range) {
        Set<Integer> squaresSet = new HashSet<>();
        int rows = board.length;
        int cols = board[0].length;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (board[row][col] != Gomoku.EMPTY) {
                    for (int r = Math.max(0, row - range); r <= Math.min(rows - 1, row + range); r++) {
                        for (int c = Math.max(0, col - range); c <= Math.min(cols - 1, col + range); c++) {
                            if (board[r][c] == Gomoku.EMPTY && (r != row || c != col)) {
                                squaresSet.add(r * currentSize + c);
                            }
                        }
                    }
                }
            }
        }
        int[] squaresAroundOccupied = new int[squaresSet.size()];
        int index = 0;
        for (int square : squaresSet) {
            squaresAroundOccupied[index++] = square;
        }

        squaresAroundOccupied = removeAll(squaresAroundOccupied, getAllOccupiedIndexes(board));
        return squaresAroundOccupied;
    }

    public static int[] getAllSpecifiedSquaresAroundPlayer(Gomoku[][] board, Gomoku player, Gomoku specification, int range) {
        Set<Integer> squaresSet = new HashSet<>();
        int rows = board.length;
        int cols = board[0].length;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (board[row][col] == player) {
                    for (int r = Math.max(0, row - range); r <= Math.min(rows - 1, row + range); r++) {
                        for (int c = Math.max(0, col - range); c <= Math.min(cols - 1, col + range); c++) {
                            if (board[r][c] == specification && (r != row || c != col)) {
                                squaresSet.add(r * currentSize + c);
                            }
                        }
                    }
                }
            }
        }
        int[] squaresAroundOccupied = new int[squaresSet.size()];
        int index = 0;
        for (int square : squaresSet) {
            squaresAroundOccupied[index++] = square;
        }

        squaresAroundOccupied = removeAll(squaresAroundOccupied, getAllOccupiedIndexes(board));
        return squaresAroundOccupied;
    }

    public static int[] getAllPlayerIndexes(Gomoku[][] board, Gomoku player) {
        int rows = board.length;
        int cols = board[0].length;
        int[] indexesOfPlayer = new int[currentSize * currentSize];
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j] == player) {
                    indexesOfPlayer[count++] = i * currentSize + j;
                }
            }
        }
        return Arrays.copyOf(indexesOfPlayer, count);
    }

    public static int[] getAllOccupiedIndexes(Gomoku[][] board) {
        int rows = board.length;
        int cols = board[0].length;
        int[] indexesOfPlayer = new int[currentSize * currentSize];
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j] == Gomoku.WHITE || board[i][j] == Gomoku.BLACK) {
                    indexesOfPlayer[count++] = i * currentSize + j;
                }
            }
        }
        return Arrays.copyOf(indexesOfPlayer, count);
    }

    public static int[] getAllEmptySquareIndexes(Gomoku[][] board) {
        int rows = board.length;
        int cols = board[0].length;
        int[] emptySquare = new int[currentSize * currentSize];
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j] == Gomoku.EMPTY) {
                    emptySquare[count++] = i * currentSize + j;
                }
            }
        }
        return Arrays.copyOf(emptySquare, count);
    }

    public static int[] removeAll(int[] original, int[] remove) {
        Set<Integer> removeSet = new HashSet<>();
        for (int value : remove) {
            removeSet.add(value);
        }

        List<Integer> resultList = new ArrayList<>();
        for (int value : original) {
            if (!removeSet.contains(value)) {
                resultList.add(value);
            }
        }

        int[] resultArray = new int[resultList.size()];
        for (int i = 0; i < resultList.size(); i++) {
            resultArray[i] = resultList.get(i);
        }

        return resultArray;
    }

    public static boolean contains(int[] array, int value) {
        if (array == null) {
            return false;
        }
        for (int element : array) {
            if (element == value) {
                return true;
            }
        }
        return false;
    }
    
    public static Gomoku getOpponent(Gomoku player) {
        return player == Gomoku.WHITE ? Gomoku.BLACK : Gomoku.WHITE;
    }
    
    public static boolean isValidIndex(int index) {
        return index >= 0 && index < currentSize * currentSize;
    }

    public static boolean isValidCoordinates(int posX, int posY) {
        return posX >= 0 && posX < currentSize && posY >= 0 && posY < currentSize;
    }

    public static int coordinatesToIndex(int posX, int posY) {
        return posX * currentSize + posY;
    }

    public static int[] indexToCoordinates(int index) {
        return new int[]{index / currentSize, index % currentSize};
    }

}
