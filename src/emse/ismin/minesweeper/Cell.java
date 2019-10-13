package emse.ismin.minesweeper;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

/**
 * Each case in the MineSweeper is represented by this class. It is a JPanel which uses the MouseListener interface.
 *
 * @author jean-christophe
 * @version 1.0
 */

public class Cell extends JPanel implements MouseListener {

    private final static int DIM = 100;
    private int x;
    private int y;
    private String txt = "";
    private Minesweeper minesweeper;
    private boolean clicked = false;
    private Counter counter;
    private boolean enabledClick;
    private boolean counted;
    private Color color = Color.lightGray;

    /**
     * @param x           x-position of the case.
     * @param y           y-position of the case.
     * @param minesweeper is an instance of the Minesweeper class related to this specific case.
     * @param counter     is the counter that computes the elapsed time. When the first case is clicked, it starts this counter.
     */
    Cell(int x, int y, Minesweeper minesweeper, Counter counter) {
        setPreferredSize(new Dimension(DIM, DIM));
        addMouseListener(this);
        setBackground(Color.lightGray);
        this.minesweeper = minesweeper;
        this.counter = counter;
        this.enabledClick = true;
        this.x = x;
        this.y = y;
    }

    /**
     * @param enabled a boolean that specifies if whether or not the click function must be activated for this case.
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.enabledClick = enabled;
    }

    /**
     * This method is used to repaint the case in function of many condition:
     * - if the case is not clicked, it is colored in a blue-green
     * - if the case is clicked and corresponds to a mine, the case displays a wonderful face.
     * - if not, the case displays the number of mines around this case.
     *
     * @param gc an instance of the Graphic class, which is used in order to draw onto components.
     */
    @Override
    public void paintComponent(Graphics gc) {
        super.paintComponent(gc);
        if (!clicked) {
            gc.setColor(new Color(125, 158, 158));
            gc.fillRect(1, 1, getWidth(), getHeight());
        } else {
            if (txt.equals("Mine")) {
                try {
                    gc.setColor(color);
                    gc.fillRect(1, 1, getWidth(), getHeight());
                    BufferedImage image = ImageIO.read(new File("img/bomb.png"));
                    gc.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                gc.setColor(color);
                gc.fillRect(1, 1, getWidth(), getHeight());
                gc.setColor(Color.BLACK);
                drawCenterString(gc);
                countCases();
                counted = true;
            }
        }
    }

    /**
     * This method is used to draw a string at the center of the case, in a responsive way.
     *
     * @param gc an instance of the Graphic class, which is used in order to draw onto components.
     */
    private void drawCenterString(Graphics gc) {
        FontMetrics fm = gc.getFontMetrics();
        int stringWidth = fm.stringWidth(txt);
        int stringAccent = fm.getAscent();
        int xCoordinate = getWidth() / 2 - stringWidth / 2;
        int yCoordinate = getHeight() / 2 + stringAccent / 2;
        gc.drawString(txt, xCoordinate, yCoordinate);
    }

    /**
     * Computes the number of clicked cases during a game in the single-player mode. If the number of cases equals
     * the size of the champ minus the number of mines, then the game is won and it offers to start a new one.
     */
    private void countCases() {
        if (!counted && (minesweeper.getClient() == null)) {
            minesweeper.setDiscoveredCases(minesweeper.getDiscoveredCases() + 1);
            repaint();
            if (minesweeper.getField().getNumberOfMines() == Math.pow(minesweeper.getField().getBoard().length, 2) - minesweeper.getDiscoveredCases()) {

                minesweeper.getGUIMineSweeper().blockGame();
                minesweeper.getScoreRegistering().addScore(minesweeper.getGUIMineSweeper().getCounter().getTime(), Calendar.getInstance().getTime(), minesweeper.getField().getLevel().name());
                minesweeper.getScoreRegistering().write();

                if (JOptionPane.showConfirmDialog(
                        null,
                        "Hey ! You won this round, well played !\nWanna try again ?",
                        "Handsome fellow !!!",
                        JOptionPane.YES_NO_OPTION
                ) == JOptionPane.YES_OPTION) {
                    minesweeper.getGUIMineSweeper().newGame();
                }
            }
        }
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param e the event to be processed
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if (minesweeper.getClient() == null) {
            if (enabledClick) {
                counter.start();
                repaint();
                txt = minesweeper.getField().display(x, y);
                clicked = true;
                if (txt.equals("Mine")) {
                    minesweeper.getGUIMineSweeper().blockGame();
                    if (JOptionPane.showConfirmDialog(
                            null,
                            "You lost ! You clicked on " + minesweeper.getDiscoveredCases() + " cases. Would you like to start over ?",
                            "Defeat",
                            JOptionPane.YES_NO_OPTION
                    ) == JOptionPane.YES_OPTION) {
                        minesweeper.getGUIMineSweeper().newGame();
                    }
                }
            }
        } else {
            try {
                if (minesweeper.getClient().isStarted()) {
                    minesweeper.getClient().getOut().writeUTF("click " + x + " " + y);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * This method is not used.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    /**
     * This method is not used.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
    }

    /**
     * This method is not used.
     */
    @Override
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * This method is not used.
     */
    @Override
    public void mouseExited(MouseEvent e) {
    }

    /**
     * Reinitialize the case to it initial state.
     */
    void newGame() {
        clicked = false;
        enabledClick = true;
        counted = false;
        repaint();
    }

    /**
     * This method is only used in a multi-player game.
     * This function replaces the "txt" string by its value computed in the server and send back to all the
     * clients. It also forces the repaint of the case.
     *
     * @param isMine specifies if whether or not the case clicked on is a mine.
     * @param playerColor is the color of the player who clicked on this specific case.
     * @param nbMines is the number of mines around the clicked case.
     */
    void clientRepaint(boolean isMine, Color playerColor, int nbMines) {
        clicked = true;
        if (isMine) {
            txt = "Mine";
        } else if (!String.valueOf(nbMines).equals("0")) {
            txt = String.valueOf(nbMines);
        }
        color = playerColor;
        repaint();
    }
}
