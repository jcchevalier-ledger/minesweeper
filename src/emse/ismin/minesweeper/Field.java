package emse.ismin.minesweeper;

import emse.ismin.Level;

import java.util.Random;

/**
 * This classes contains methods and variables that describes the champ. It contains 2 variables:
 * - boolean[][] board : a squared board. The case value is "true" if it contains a mine, "false" if it does not.
 * - int numberOfMines : specify the number of mines inside that champ.
 */

public class Field {

    private int numberOfMines;
    private boolean[][] board;
    private Level level;

    /**
     * Default constructor, only used to create an empty instance of the class
     */
    private Field() {
    }

    /**
     * @param numberOfMines the number of mines that you want
     * @param boardSize     the size of the side of the board
     */
    Field(int numberOfMines, int boardSize) {
        this.numberOfMines = numberOfMines;
        this.board = new boolean[boardSize][boardSize];
    }

    /**
     * Creates a new Champ instance in function of the difficulty
     *
     * @param level the difficulty of the game
     */
    public Field(Level level) {
        this();
        setBoard(level);
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
    public void placeMines() {
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
    public String minesAround(int x, int y) {
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

    /**
     * @param x the x-coordinate of the case
     * @param y the y-coordinate of the case
     * @return a String, which is "Mine" if the case clicked-on is a mine or the number of mines around if not.
     */
    String display(int x, int y) {
        if (board[x][y]) return "Mine";
        else return minesAround(x, y);
    }

    /**
     * @return the board of the Champ
     */
    public boolean[][] getBoard() {
        return board;
    }

    /**
     * This method is a Board setter, in function of the level.
     *
     * @param level the level of the game. It can be:
     *              - Easy
     *              - Medium
     *              - Hard
     */
    void setBoard(Level level) {
        if (level == Level.Easy) {
            this.level = Level.Easy;
            setChamp(2, 5);
        } else if (level == Level.Medium) {
            this.level = Level.Medium;
            setChamp(15, 8);
        } else {
            this.level = Level.Hard;
            setChamp(30, 10);
        }
    }

    /**
     * @return the number of mines in this champ.
     */
    public int getNumberOfMines() {
        return numberOfMines;
    }

    /**
     * @return the actual level of the game.
     */
    Level getLevel() {
        return level;
    }
}