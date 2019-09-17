package emse.ismin;
import javax.swing.JPanel;
import java.awt.*;

public class Compteur extends JPanel implements Runnable {

    private Thread processScores;
    private int cpt;
    private boolean started = false;

    Compteur(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        cpt = 0;
        processScores = new Thread(this);
    }

    void start() {
        if (!started) {
            processScores.start();
            started = true;
            System.out.println("Yesss");
        }
    }

    @Override
    public void run() {
        while (processScores != null){
            try {
                Thread.sleep(1000);
                repaint();
                cpt += 1;
            }
            catch (InterruptedException exception){
                exception.printStackTrace();
            }
        }
    }

    @Override
    public void paintComponent(Graphics gc) {
        super.paintComponent(gc);
        gc.drawString("Timer: " + cpt, this.getWidth()/2 , this.getHeight()/2);
    }
}
