package emse.ismin;
import javax.swing.JPanel;
import java.awt.*;

public class Compteur extends JPanel implements Runnable {

    private Thread processScores;
    private int cpt;

    private Compteur() {
        setPreferredSize(new Dimension(50, 50));
        setBackground(Color.CYAN);
        cpt = 0;
        processScores = new Thread(this);
        processScores.start();
    }

    @Override
    public void run() {
        while (processScores != null){
            try {
                Thread.sleep(1000);
                repaint();
            }
            catch (InterruptedException exception){
                exception.printStackTrace();
            }
        }
    }

    @Override
    public void paintComponents(Graphics g) {
        super.paintComponents(g);
        g.drawString(String.valueOf(cpt), this.getHeight()/2, this.getWidth()/2);
        cpt =+ 1;
    }
}
