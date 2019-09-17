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

    Case(int x, int y, Demineur demineur) {
        setPreferredSize(new Dimension(DIM, DIM));
        addMouseListener(this);
        setBackground(Color.lightGray);
        this.demineur = demineur;
        this.x = x;
        this.y = y;
    }

    @Override
    public void paintComponent(Graphics gc) {
        super.paintComponent(gc);
        if(!clicked) {
            gc.setColor(new Color(138, 158, 158));
            gc.fillRect(1, 1, getWidth(), getHeight());
        }
        else {
            if(demineur.getChamp().display(x, y).equals("Mine")) {
                try {
                    BufferedImage image= ImageIO.read(new File("img/bomb.png"));
                    gc.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else gc.drawString(txt, this.getHeight()/2, this.getWidth()/2);
        }
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param e the event to be processed
     */
    @Override
    public void mousePressed(MouseEvent e) {
        txt = demineur.getChamp().display(x, y);
        clicked = true;
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    void newPartie() {
        clicked = false;
        repaint();
    }
}
