package emse.ismin.minesweeper;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * This class is used to measure the elapsed time since the beginning of the game. It is a thread that is
 * displayed on a JPanel.
 */
public class Counter extends JPanel implements Runnable {

    private Thread processScores;
    private double cpt;
    private boolean started = false;
    private NumberFormat nf = new DecimalFormat("0.##");


    /**
     * @param width  width of the JPanel that displays the counter.
     * @param height height of the JPanel that displays the counter.
     */
    Counter(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        cpt = 0.00;
        processScores = new Thread(this);
    }

    /**
     * Starts the thread, and by doing so it also starts the measure of the elapsed time.
     */
    void start() {
        if (!started) {
            processScores.start();
            started = true;
        }
    }

    /**
     * Resets the counter.
     */
    void reset() {
        started = false;
        cpt = 0.00;
        processScores = new Thread(this);
    }

    /**
     * Runs the counter.
     */
    @Override
    public void run() {
        while (processScores != null) {
            try {
                Thread.sleep(10);
                cpt += 0.01;
                repaint();
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }
        processScores = new Thread(this);
    }

    /**
     * Repaints the case, with the string corresponding to the timer at the center of the JPanel.
     *
     * @param gc an instance of the Graphic class, which is used in order to draw onto components.
     */
    @Override
    public void paintComponent(Graphics gc) {
        FontMetrics fm = gc.getFontMetrics();
        int stringWidth = fm.stringWidth("Timer: " + nf.format(cpt));
        int stringAccent = fm.getAscent();
        int xCoordinate = getWidth() / 2 - stringWidth / 2;
        int yCoordinate = getHeight() / 2 + stringAccent / 2;
        gc.drawString("Timer: " + nf.format(cpt), xCoordinate, yCoordinate);
    }

    /**
     * Stop the thread by setting its reference to null.
     */
    void stop() {
        processScores = null;
    }

    /**
     * @return the elapsed time in a format defined by "nf" attribute.
     */
    String getTime() {
        return nf.format(cpt);
    }
}
