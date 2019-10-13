package emse.ismin.minesweeper;

import emse.ismin.Level;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * This class contains the main functions that launches a minesweeper's instance.
 */
public class Minesweeper extends JFrame {

    private Field field;
    private GUIMinesweeper GUIMineSweeper;
    private int discoveredCases = 0;
    private ScoreRegistering scoreRegistering = new ScoreRegistering();
    private Client client = null;

    /**
     * Creates the frame that contains the minesweeper.
     */
    private Minesweeper() {
        super("MineSweeper");

        this.field = new Field(Level.Easy);
        field.placeMines();

        GUIMineSweeper = new GUIMinesweeper(this);
        setContentPane(GUIMineSweeper);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        pack();
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                if (JOptionPane.showConfirmDialog(
                        null,
                        "Are you sure?",
                        "Quit",
                        JOptionPane.YES_NO_OPTION
                ) == JOptionPane.YES_OPTION) quit();
            }
        });
    }

    /**
     * @param args no arguments are used here.
     */
    public static void main(String[] args) {
        new Minesweeper();
    }

    /**
     * @return the Field instance used in the minesweeper.
     */
    Field getField() {
        return field;
    }

    /**
     * @return the ScoreRegistering instance used in the minesweeper.
     */
    ScoreRegistering getScoreRegistering() {
        return scoreRegistering;
    }

    /**
     * This method stop the execution of this program.
     */
    void quit() {
        scoreRegistering.write();
        System.out.println("Bye-Bye");
        System.exit(0);
    }

    /**
     * @return return the GUI instance of this minesweeper.
     */
    GUIMinesweeper getGUIMineSweeper() {
        return GUIMineSweeper;
    }

    /**
     * @return the number of clicked cases in the field, mines excluded.
     */
    int getDiscoveredCases() {
        return discoveredCases;
    }

    /**
     * @param discoveredCases the new number of discovered cases.
     */
    void setDiscoveredCases(int discoveredCases) {
        this.discoveredCases = discoveredCases;
    }

    /**
     * @return the Client instance.
     */
    Client getClient() {
        return client;
    }

    /**
     * @param client the new client.
     */
    void setClient(Client client) {
        this.client = client;
    }
}
