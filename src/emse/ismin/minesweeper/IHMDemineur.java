package emse.ismin.minesweeper;

import emse.ismin.Level;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
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

    private JTextPane log = new JTextPane();
    private JTextArea chat = new JTextArea(1, 20);
    private JButton sendChat = new JButton("Send");

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
        JPanel logPanel = new JPanel();
        JPanel chatBar = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.Y_AXIS));

        log.setEditable(false);
        JScrollPane jScrollPane = new JScrollPane(log);
        jScrollPane.setPreferredSize(new Dimension(50, 300));
        EmptyBorder emptyBorder = new EmptyBorder(new Insets(10, 10, 10, 10));
        jScrollPane.setBorder(emptyBorder);
        log.setText("Welcome to the online version of the MineSweeper !\n");

        DefaultCaret caret = (DefaultCaret) log.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        chatBar.add(chat);
        chatBar.add(sendChat);
        sendChat.addActionListener(this);
        sendChat.setMnemonic(KeyEvent.VK_ENTER);

        logPanel.add(jScrollPane);
        logPanel.add(chatBar);

        add(logPanel, BorderLayout.EAST);
        demineur.pack();
        setVisible(true);
    }

    void newGame() {
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

    void newGame(Level level) {
        grid.removeAll();
        createGrid();
        add(grid, BorderLayout.CENTER);
        newGame();
        demineur.pack();
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (mQuitter.equals(source)) {
            if (JOptionPane.showConfirmDialog(
                    null,
                    "Are you sure?",
                    "Quit",
                    JOptionPane.YES_NO_OPTION
            ) == JOptionPane.YES_OPTION) demineur.quit();
        } else if (mEasy.equals(source)) {
            demineur.getChamp().setBoard(Level.Easy);
            newGame(Level.Easy);
        } else if (mMedium.equals(source)) {
            demineur.getChamp().setBoard(Level.Medium);
            newGame(Level.Medium);
        } else if (mHard.equals(source)) {
            demineur.getChamp().setBoard(Level.Hard);
            newGame(Level.Hard);
        } else if (mRestart.equals(source)) {
            newGame();
        } else if (grid.equals(source)) {
            compteur.start();
        } else if (join.equals(source)) {
            demineur.setClient(new Client(ipAddress.getText(), port.getText(), pseudo.getText(), demineur));
        } else if (sendChat.equals(source)) {
            String message = this.chat.getText();
            this.chat.setText("");
            demineur.getClient().sendMessage(message);
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

    void appendToPane(String msg, Color color, boolean isAdmin) {
        StyledDocument styledDocument = log.getStyledDocument();

        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setForeground(attributeSet, color);
        StyleConstants.setBold(attributeSet, isAdmin);
        log.setCharacterAttributes(attributeSet, false);
        log.setCaretPosition(log.getText().length());

        try {
            styledDocument.insertString(styledDocument.getLength(), msg, attributeSet);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
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

    Case[][] getTabCases() {
        return tabCases;
    }

    JTextArea getChat() {
        return chat;
    }

    JButton getSendChat() {
        return sendChat;
    }
}