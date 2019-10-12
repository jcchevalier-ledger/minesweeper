package emse.ismin.minesweeper;

import emse.ismin.Level;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class IHMDemineur extends JPanel implements ActionListener {

    private Case[][] tabCases;
    private Demineur demineur;

    private JPanel grid = new JPanel();

    private JMenuItem mQuitter = new JMenuItem("Quitter", KeyEvent.VK_Q);
    private JMenuItem mEasy = new JMenuItem("Easy", KeyEvent.VK_E);
    private JMenuItem mMedium = new JMenuItem("Medium", KeyEvent.VK_M);
    private JMenuItem mHard = new JMenuItem("Hard", KeyEvent.VK_H);
    private JMenuItem mCustom = new JMenuItem("Custom", KeyEvent.VK_H);
    private JMenuItem mRestart = new JMenuItem("Restart");
    private Compteur compteur = new Compteur(150, 30);

    private JTextArea log = new JTextArea("Welcome to the Minesweeper\n", 10, 20);

    private JButton join;
    private JTextField ipAddress;
    private JTextField port;
    private JTextField pseudo;

    IHMDemineur(Demineur demineur) {
        setLayout(new BorderLayout());

        this.demineur = demineur;

        add(createHeader(), BorderLayout.NORTH);

        createGrid();
        add(grid, BorderLayout.CENTER);

        add(createFooter(), BorderLayout.SOUTH);

        createMenuPartie();
    }

    private void createMenuPartie() {
        JMenuBar jMenuBar = new JMenuBar();

        //Menu partie
        JMenu menuPartie = new JMenu("Partie");
        mQuitter.addActionListener(this);
        mQuitter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.META_DOWN_MASK));
        mQuitter.setToolTipText("The End");

        jMenuBar.add(menuPartie);
        menuPartie.add(mQuitter);

        //Menu level
        JMenu menuLevel = new JMenu("Niveau");

        jMenuBar.add(menuLevel);
        menuLevel.add(mEasy);
        mEasy.addActionListener(this);
        menuLevel.add(mMedium);
        mMedium.addActionListener(this);
        menuLevel.add(mHard);
        mHard.addActionListener(this);
        menuLevel.add(mCustom);
        mCustom.addActionListener(this);

        menuLevel.add(mRestart);
        mRestart.addActionListener(this);
        mRestart.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.META_DOWN_MASK));

        //Make some space
        jMenuBar.add(Box.createGlue());

        //Menu help
        JMenu menuHelp = new JMenu("Aide");
        JMenuItem mAbout = new JMenuItem("A propos");

        jMenuBar.add(menuHelp);
        menuHelp.add(mAbout);

        demineur.setJMenuBar(jMenuBar);
    }

    private void createGrid() {
        int n = demineur.getChamp().getBoard().length;
        tabCases = new Case[n][n];
        grid.setLayout(new GridLayout(n, n));

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                tabCases[i][j] = new Case(i, j, demineur, compteur);
                grid.add(tabCases[i][j]);
            }
        }
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel();
        footer.setLayout(new FlowLayout());

        JLabel ipAddressLabel = new JLabel("IP: ");
        ipAddress = new JTextField("127.0.0.1");

        JLabel portLabel = new JLabel("Port: ");
        port = new JTextField("10000");

        JLabel pseudoLabel = new JLabel("Pseudo: ");
        pseudo = new JTextField("", 5);

        join = new JButton("Join !");
        join.addActionListener(this);

        footer.add(ipAddressLabel);
        footer.add(ipAddress);
        footer.add(portLabel);
        footer.add(port);
        footer.add(pseudoLabel);
        footer.add(pseudo);
        footer.add(join);

        return footer;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new FlowLayout());

        JLabel title = new JLabel("Welcome on board");
        header.add(title);
        header.add(compteur);

        return header;
    }

    void createLog() {
        log.setEditable(false);
        JScrollPane jScrollPane = new JScrollPane(log);
        jScrollPane.getVerticalScrollBar().addAdjustmentListener(e -> e.getAdjustable().setValue(e.getAdjustable().getMaximum()));

        add(jScrollPane, BorderLayout.EAST);
    }

    void newPartie() {
        if (demineur.getClient() == null) {
            demineur.getChamp().placeMines();
        }
        for (int i = 0; i < demineur.getChamp().getBoard().length; i++) {
            for (int j = 0; j < demineur.getChamp().getBoard().length; j++) {
                tabCases[i][j].newPartie();
            }
        }
        compteur.reset();
        demineur.setDiscoveredCases(0);
    }

    void newPartie(Level level) {
        grid.removeAll();
        createGrid();
        add(grid, BorderLayout.CENTER);
        newPartie();
        demineur.pack();
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == mQuitter) {
            if (JOptionPane.showConfirmDialog(
                    null,
                    "Are you sure?",
                    "Quit",
                    JOptionPane.YES_NO_OPTION
            ) == JOptionPane.YES_OPTION) demineur.quit();
        }
        if (event.getSource() == mEasy) {
            demineur.getChamp().setBoard(Level.Easy);
            newPartie(Level.Easy);
        }
        if (event.getSource() == mMedium) {
            demineur.getChamp().setBoard(Level.Medium);
            newPartie(Level.Medium);
        }
        if (event.getSource() == mHard) {
            demineur.getChamp().setBoard(Level.Hard);
            newPartie(Level.Hard);
        }
        if (event.getSource() == mRestart) {
            newPartie();
        }
        if (event.getSource() == grid) {
            compteur.start();
        }
        if (event.getSource() == join) {
            demineur.setClient(new Client(ipAddress.getText(), port.getText(), pseudo.getText(), demineur));
        }
    }

    void displayID() {
        JOptionPane.showConfirmDialog(
                null,
                "You are now connected. Your pseudo is " + demineur.getClient().getPlayerName() + " and your ID is " + demineur.getClient().getPlayerNum() + ".",
                "Connected !",
                JOptionPane.DEFAULT_OPTION
        );
        join.setEnabled(false);
        ipAddress.setEnabled(false);
        port.setEnabled(false);
        pseudo.setEnabled(false);
    }

    void blockGame() {
        for (Case[] tabCase : tabCases) {
            for (int j = 0; j < tabCases.length; j++) {
                tabCase[j].setEnabled(false);
                compteur.stop();
            }
        }
    }

    Compteur getCompteur() {
        return compteur;
    }

    JTextArea getLog() {
        return log;
    }

    Case[][] getTabCases() {
        return tabCases;
    }
}