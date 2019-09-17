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
    private boolean counted;

    Case(int x, int y, Demineur demineur, Compteur compteur) {
        setPreferredSize(new Dimension(DIM, DIM));
        addMouseListener(this);
        setBackground(Color.lightGray);
        this.demineur = demineur;
        this.compteur = compteur;
        this.enabled = true;
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
            } else {
                gc.drawString(txt, this.getHeight() / 2, this.getWidth() / 2);
                countCases();
                counted = true;
            }
        }
    }

    private void countCases() {
        if(!counted) {
            demineur.setDiscoveredCases(demineur.getDiscoveredCases() + 1);
            System.out.println(demineur.getDiscoveredCases());
            repaint();
            if (demineur.getChamp().getNumberOfMines() == Math.pow(demineur.getChamp().getBoard().length, 2) - demineur.getDiscoveredCases()){
                demineur.getIhmDemineur().blockGame();
                if (JOptionPane.showConfirmDialog(
                        null,
                        "Turlututu chapeau pointu, tu as gagné ! Veux-tu recommencer ?",
                        "BEAU GOSSE",
                        JOptionPane.YES_NO_OPTION
                ) == JOptionPane.YES_OPTION) {
                    demineur.getIhmDemineur().newPartie();
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
        if (enabled) {
            compteur.start();
            repaint();
            txt = demineur.getChamp().display(x, y);
            clicked = true;
            if (txt.equals("Mine")) {
                demineur.getIhmDemineur().blockGame();
                if (JOptionPane.showConfirmDialog(
                        null,
                        "You lost ! You clicked on " + demineur.getDiscoveredCases() + " cases. Would you like to start over ?",
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
        counted = false;
        repaint();
    }
}
