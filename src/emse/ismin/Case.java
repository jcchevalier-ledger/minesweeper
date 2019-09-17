package emse.ismin;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Case extends JPanel implements MouseListener {

    private final static int DIM = 50;
    private int x;
    private int y;
    private String txt = "";
    private Demineur demineur;
    private boolean clicked = false;
    private Compteur compteur;
    private boolean enabled;
    private Case[][] tabCases;

    Case(int x, int y, Demineur demineur, Compteur compteur, Case[][] tabCases) {
        setPreferredSize(new Dimension(DIM, DIM));
        addMouseListener(this);
        setBackground(Color.lightGray);
        this.demineur = demineur;
        this.compteur = compteur;
        this.enabled = true;
        this.tabCases = tabCases;
        this.x = x;
        this.y = y;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void paintComponent(Graphics gc) {
        super.paintComponent(gc);
        if (!clicked) {
            gc.setColor(new Color(138, 158, 158));
            gc.fillRect(1, 1, getWidth(), getHeight());
        } else {
            if (demineur.getChamp().display(x, y).equals("Mine")) {
                try {
                    BufferedImage image = ImageIO.read(new File("img/bomb.png"));
                    gc.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else gc.drawString(txt, this.getHeight() / 2, this.getWidth() / 2);
        }
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param e the event to be processed
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if(enabled) {
            compteur.start();
            txt = demineur.getChamp().display(x, y);
            clicked = true;
            repaint();
            if (txt.equals("Mine")) {
                demineur.getIhmDemineur().setLose();
                if (JOptionPane.showConfirmDialog(
                        null,
                        "You lost ! Would you like to start over ?",
                        "Defeat",
                        JOptionPane.YES_NO_OPTION
                ) == JOptionPane.YES_OPTION) {
                    demineur.getIhmDemineur().newPartie();
                }
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    void newPartie() {
        clicked = false;
        enabled = true;
        repaint();
    }
}
