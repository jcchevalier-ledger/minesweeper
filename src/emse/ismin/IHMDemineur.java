package emse.ismin;

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
    private JMenuItem mRestart = new JMenuItem("Recommencer");

    IHMDemineur(Demineur demineur) {
        setLayout(new BorderLayout());

        this.demineur = demineur;

        add(createHeader(), BorderLayout.NORTH);

        createGrid();
        add(grid, BorderLayout.CENTER);

        createMenuPartie(demineur);
    }

    private void createMenuPartie(Demineur demineur) {
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

        for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
                tabCases[i][j] = new Case(i, j, demineur);
                grid.add(tabCases[i][j]);
            }
        }
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new FlowLayout());
        header.add(new JLabel("Démineur"));
        header.add(new JLabel("Score: 100"));

        return header;
    }

    private void newPartie() {
        for (int i = 0; i < demineur.getChamp().getBoard().length; i++) {
            for (int j = 0; j < demineur.getChamp().getBoard().length; j++) {
                tabCases[i][j].newPartie();
            }
        }
    }

    private void newPartie(Level level) {
        grid.removeAll();
        createGrid();
        add(grid, BorderLayout.CENTER);
        newPartie();
        demineur.pack();
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == mQuitter) {
            if(JOptionPane.showConfirmDialog(
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
        /*if (event.getSource() == mCustom) {
            demineur.getChamp().setBoard(Level.Hard);
            newPartie();
        }*/
        if (event.getSource() == mRestart) {
            demineur.getChamp().placeMines();
            newPartie();
        }
    }
}