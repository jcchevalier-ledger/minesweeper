package emse.ismin.minesweeper;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Compteur extends JPanel implements Runnable {

    private Thread processScores;
    private double cpt;
    private boolean started = false;
    private NumberFormat nf = new DecimalFormat("0.##");


    Compteur(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        cpt = 0.00;
        processScores = new Thread(this);
    }

    void start() {
        if (!started) {
            processScores.start();
            started = true;
        }
    }

    void reset() {
        started = false;
        cpt = 0.00;
        processScores = new Thread(this);
    }

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

    @Override
    public void paintComponent(Graphics gc) {
        super.paintComponent(gc);
        gc.drawString("Timer: " + nf.format(cpt), this.getWidth() / 2, this.getHeight() / 2);
    }

    void stop() {
        processScores = null;
    }

    String getTime() {
        return nf.format(cpt);
    }
}
