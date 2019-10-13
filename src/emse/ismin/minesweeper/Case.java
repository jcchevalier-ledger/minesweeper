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

public class Case extends JPanel implements MouseListener {

    private final static int DIM = 100;
    private int x;
    private int y;
    private String txt = "";
    private Demineur demineur;
    private boolean clicked = false;
    private Compteur compteur;
    private boolean enabledClick;
    private boolean counted;
    private Color color = Color.lightGray;

    Case(int x, int y, Demineur demineur, Compteur compteur) {
        setPreferredSize(new Dimension(DIM, DIM));
        addMouseListener(this);
        setBackground(Color.lightGray);
        this.demineur = demineur;
        this.compteur = compteur;
        this.enabledClick = true;
        this.x = x;
        this.y = y;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabledClick = enabled;
    }

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

    private void drawCenterString(Graphics gc) {
        FontMetrics fm = gc.getFontMetrics();
        int stringWidth = fm.stringWidth(txt);
        int stringAccent = fm.getAscent();
        int xCoordinate = getWidth() / 2 - stringWidth / 2;
        int yCoordinate = getHeight() / 2 + stringAccent / 2;
        gc.drawString(txt, xCoordinate, yCoordinate);
    }

    private void countCases() {
        if (!counted && (demineur.getClient() == null)) {
            demineur.setDiscoveredCases(demineur.getDiscoveredCases() + 1);
            repaint();
            if (demineur.getChamp().getNumberOfMines() == Math.pow(demineur.getChamp().getBoard().length, 2) - demineur.getDiscoveredCases()){

                demineur.getIhmDemineur().blockGame();
                demineur.getScoreRegistering().addScore(demineur.getIhmDemineur().getCompteur().getTime(), Calendar.getInstance().getTime(), demineur.getChamp().getLevel().name());
                demineur.getScoreRegistering().write();

                if (JOptionPane.showConfirmDialog(
                        null,
                        "Hey ! You won this round, well played !\nWanna try again ?",
                        "Handsome fellow !!!",
                        JOptionPane.YES_NO_OPTION
                ) == JOptionPane.YES_OPTION) {
                    demineur.getIhmDemineur().newGame();
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
        if (demineur.getClient() == null) {
            if (enabledClick) {
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
                        demineur.getIhmDemineur().newGame();
                    }
                }
            }
        } else {
            try {
                if (demineur.getClient().isStarted()) {
                    demineur.getClient().getOut().writeUTF("click " + x + " " + y);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
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
        enabledClick = true;
        counted = false;
        repaint();
    }

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
