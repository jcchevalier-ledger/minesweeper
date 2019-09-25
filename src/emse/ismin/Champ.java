package emse.ismin;

import java.util.Random;

/**
 * @author jean-christophe
 * @version 1.0
 * <p>
 * This classes contains methods and variables that describes the champ. It contains 2 variables:
 * - boolean[][] board : a squared board. The case value is "true" if it contains a mine, "false" if it does not.
 * - int numberOfMines : specify the number of mines inside that champ.
 */

public class Champ {

    private int numberOfMines;
    private boolean[][] board;
    private Level level;

    /**
     * Default constructor, only used to create an empty instance of the class
     */
    private Champ() {
    }

    /**
     * @param numberOfMines the number of mines that you want
     * @param boardSize     the size of the side of the board
     */
    Champ(int numberOfMines, int boardSize) {
        this.numberOfMines = numberOfMines;
        this.board = new boolean[boardSize][boardSize];
    }

    /**
     * Creates a new emse.ismin.Champ instance in function of the difficulty
     *
     * @param level the difficulty of the game
     */
    Champ(Level level) {
        this();
        if (level == Level.Easy) {
            setChamp(2, 5);
            this.level = level;
        }
        if (level == Level.Medium) {
            setChamp(30, 8);
            this.level = level;
        }
        if (level == Level.Hard) {
            setChamp(60, 10);
            this.level = level;
        }
    }

    /**
     * Set the number of mines and the size of the board
     *
     * @param numberOfMines the number of mines that you want
     * @param boardSize     the size of the side of the board
     */
    private void setChamp(int numberOfMines, int boardSize) {
        this.numberOfMines = numberOfMines;
        this.board = new boolean[boardSize][boardSize];
    }

    /**
     * Generates the specified number of mines inside the champ and inserts their positions inside the minesPositions array.
     */
    void placeMines() {
        board = new boolean[board.length][board.length];
        int i = 0;
        while (i < Math.min(numberOfMines, board.length * board.length)) {
            int x = new Random().nextInt(board.length);
            int y = new Random().nextInt(board.length);
            if (!board[x][y]) {
                board[x][y] = true;
                i++;
            }
        }
    }

    /**
     * @param x abscissa of the point
     * @param y ordinate of the point
     * @return a string that is equal to the number of mines around the computed coordinates, or a point if there are none
     */
    private String minesAround(int x, int y) {
        String result;
        int minesCounter = 0;
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                try {
                    if (board[i][j]) minesCounter++;
                } catch (ArrayIndexOutOfBoundsException ignored) {
                }
            }
        }
        if (minesCounter > 0) result = String.valueOf(minesCounter);
        else result = "";
        return result;
    }

    /**
     * @return A "graphic" representation of the board
     */
    @Override
    public String toString() {
        for (int i = 0; i < board.length; i++) {
            StringBuilder display = new StringBuilder();
            for (int j = 0; j < board.length; j++) {
                if (board[i][j]) display.append("X ");
                else {
                    display.append(minesAround(i, j));
                    display.append(" ");
                }
            }
            System.out.println(display);
        }
        return "";
    }

    String display(int x, int y) {
        if (board[x][y]) return "Mine";
        else return minesAround(x, y);
    }

    boolean[][] getBoard() {
        return board;
    }

    void setBoard(Level level) {
        if (level == Level.Easy) {
            setChamp(2, 5);
        } else if (level == Level.Medium) {
            setChamp(30, 8);
        } else {
            setChamp(60, 10);
        }
        placeMines();
    }

    int getNumberOfMines() {
        return numberOfMines;
    }

    Level getLevel() {
        return level;
    }
}