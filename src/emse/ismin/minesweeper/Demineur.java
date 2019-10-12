package emse.ismin.minesweeper;

import emse.ismin.Level;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Demineur extends JFrame {

    private Champ champ;
    private IHMDemineur ihmDemineur;
    private int discoveredCases = 0;
    private ScoreRegistering scoreRegistering = new ScoreRegistering();
    private Client client = null;

    private Demineur() {
        super("MineSweeper");

        this.champ = new Champ(Level.Easy);
        champ.placeMines();

        ihmDemineur = new IHMDemineur(this);
        setContentPane(ihmDemineur);

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

    public static void main(String[] args) {
        new Demineur();
    }

    Champ getChamp() {
        return champ;
    }

    ScoreRegistering getScoreRegistering() {
        return scoreRegistering;
    }

    void quit() {
        scoreRegistering.write();
        System.out.println("Bye-Bye");
        System.exit(0);
    }

    IHMDemineur getIhmDemineur() {
        return ihmDemineur;
    }

    int getDiscoveredCases() {
        return discoveredCases;
    }

    void setDiscoveredCases(int discoveredCases) {
        this.discoveredCases = discoveredCases;
    }

    Client getClient() {
        return client;
    }

    void setClient(Client client) {
        this.client = client;
    }
}
