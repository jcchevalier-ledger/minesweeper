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

/**
 * This class handles the graphic interface of the minesweeper.
 */
public class GUIMinesweeper extends JPanel implements ActionListener {

    private Cell[][] tabCells;
    private Minesweeper minesweeper;

    private JPanel grid = new JPanel();

    private JMenuItem mLeave = new JMenuItem("Leave", KeyEvent.VK_Q);
    private JMenuItem mEasy = new JMenuItem("Easy", KeyEvent.VK_E);
    private JMenuItem mMedium = new JMenuItem("Medium", KeyEvent.VK_M);
    private JMenuItem mHard = new JMenuItem("Hard", KeyEvent.VK_H);
    private JMenuItem mRestart = new JMenuItem("Restart");
    private Counter counter = new Counter(150, 30);

    private JTextPane log = new JTextPane();
    private JTextArea chat = new JTextArea(1, 20);
    private JButton sendChat = new JButton("Send");

    private JButton join;
    private JTextField ipAddress;
    private JTextField port;
    private JTextField pseudo;

    /**
     * @param minesweeper is the minesweeper's instance which will run on this instance of the graphical interface.
     */
    GUIMinesweeper(Minesweeper minesweeper) {
        setLayout(new BorderLayout());

        this.minesweeper = minesweeper;

        add(createHeader(), BorderLayout.NORTH);

        createGrid();
        add(grid, BorderLayout.CENTER);

        add(createFooter(), BorderLayout.SOUTH);

        createGameMenu();
    }

    /**
     * Ths method creates the menu bar located above the minesweeper's header.
     */
    private void createGameMenu() {
        JMenuBar jMenuBar = new JMenuBar();

        //Creation of the game menu
        JMenu gameMenu = new JMenu("Game");
        mLeave.addActionListener(this);
        mLeave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.META_DOWN_MASK));
        mLeave.setToolTipText("The End");

        jMenuBar.add(gameMenu);
        gameMenu.add(mLeave);

        //Menu level
        JMenu menuLevel = new JMenu("Level");

        jMenuBar.add(menuLevel);
        menuLevel.add(mEasy);
        mEasy.addActionListener(this);
        menuLevel.add(mMedium);
        mMedium.addActionListener(this);
        menuLevel.add(mHard);
        mHard.addActionListener(this);

        menuLevel.add(mRestart);
        mRestart.addActionListener(this);
        mRestart.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.META_DOWN_MASK));

        //Make some space
        jMenuBar.add(Box.createGlue());

        //Menu help
        JMenu menuHelp = new JMenu("Help");
        JMenuItem mAbout = new JMenuItem("About this");

        jMenuBar.add(menuHelp);
        menuHelp.add(mAbout);

        minesweeper.setJMenuBar(jMenuBar);
    }

    /**
     * Creates the GUI of the grid, on which players will click.
     */
    private void createGrid() {
        int n = minesweeper.getField().getBoard().length;
        tabCells = new Cell[n][n];
        grid.setLayout(new GridLayout(n, n));

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                tabCells[i][j] = new Cell(i, j, minesweeper, counter);
                grid.add(tabCells[i][j]);
            }
        }
    }

    /**
     * @return the footer, which allows the player to start an online game.
     */
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

    /**
     * @return the header, which contains a little text and one instance of the Counter class.
     */
    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new FlowLayout());

        JLabel title = new JLabel("Welcome on board");
        header.add(title);
        header.add(counter);

        return header;
    }

    /**
     * Create the multi-player interface, which receives and displays all the messages sent by the server.
     */
    void createLog() {
        JPanel logPanel = new JPanel();
        JPanel chatBar = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.Y_AXIS));

        log.setEditable(false);
        JScrollPane jScrollPane = new JScrollPane(log);
        jScrollPane.setPreferredSize(new Dimension(50, 300));
        EmptyBorder emptyBorder = new EmptyBorder(new Insets(10, 10, 10, 10));
        jScrollPane.setBorder(emptyBorder);
        appendToPane("Welcome to the online version of the MineSweeper !\n", Color.red, true);

        DefaultCaret caret = (DefaultCaret) log.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        chatBar.add(chat);
        chatBar.add(sendChat);
        sendChat.addActionListener(this);
        sendChat.setMnemonic(KeyEvent.VK_ENTER);

        logPanel.add(jScrollPane);
        logPanel.add(chatBar);

        add(logPanel, BorderLayout.EAST);
        minesweeper.pack();
        setVisible(true);
    }

    /**
     * Starts a new game by resetting essential attributes of the class.
     */
    void newGame() {
        if (minesweeper.getClient() == null) {
            minesweeper.getField().placeMines();
        }
        for (int i = 0; i < minesweeper.getField().getBoard().length; i++) {
            for (int j = 0; j < minesweeper.getField().getBoard().length; j++) {
                tabCells[i][j].newGame();
            }
        }
        counter.reset();
        minesweeper.setDiscoveredCases(0);
    }

    /**
     * This method modifies the sizes of both the champ and the grid according to the level indicated by the player,
     * then it starts a new game.
     *
     * @param level the level wanted by the player for his new game.
     */
    void newGame(Level level) {
        minesweeper.getField().setBoard(level);
        grid.removeAll();
        createGrid();
        add(grid, BorderLayout.CENTER);
        newGame();
        minesweeper.pack();
    }

    /**
     * This method handles all client's actions.
     *
     * @param event the triggered event.
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (mLeave.equals(source)) {
            if (JOptionPane.showConfirmDialog(
                    null,
                    "Are you sure?",
                    "Quit",
                    JOptionPane.YES_NO_OPTION
            ) == JOptionPane.YES_OPTION) minesweeper.quit();
        } else if (mEasy.equals(source)) {
            newGame(Level.Easy);
        } else if (mMedium.equals(source)) {
            newGame(Level.Medium);
        } else if (mHard.equals(source)) {
            newGame(Level.Hard);
        } else if (mRestart.equals(source)) {
            newGame();
        } else if (grid.equals(source)) {
            counter.start();
        } else if (join.equals(source)) {
            minesweeper.setClient(new Client(ipAddress.getText(), port.getText(), pseudo.getText(), minesweeper));
        } else if (sendChat.equals(source)) {
            String message = this.chat.getText();
            this.chat.setText("");
            minesweeper.getClient().sendMessage(message);
        }
    }

    /**
     * Displays the pseudo and the ID of the player when he successfully connects to a server.
     */
    void displayID() {
        JOptionPane.showConfirmDialog(
                null,
                "You are now connected. Your pseudo is " + minesweeper.getClient().getPlayerName() + " and your ID is " + minesweeper.getClient().getPlayerNum() + ".",
                "Connected !",
                JOptionPane.DEFAULT_OPTION
        );
        join.setEnabled(false);
        ipAddress.setEnabled(false);
        port.setEnabled(false);
        pseudo.setEnabled(false);
    }

    /**
     * Adds a text to the pane "log".
     *
     * @param text    the text that needs to be added to the pane.
     * @param color   the color with which the text must be displayed.
     * @param isAdmin boolean that states if whether or not this message is send by the server, or if it is a
     *                message from another client.
     */
    void appendToPane(String text, Color color, boolean isAdmin) {
        StyledDocument styledDocument = log.getStyledDocument();

        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setForeground(attributeSet, color);
        StyleConstants.setBold(attributeSet, isAdmin);
        log.setCharacterAttributes(attributeSet, false);
        log.setCaretPosition(log.getText().length());

        try {
            styledDocument.insertString(styledDocument.getLength(), text, attributeSet);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Blocks the game when a mine is hit by the player.
     */
    void blockGame() {
        for (Cell[] tabCell : tabCells) {
            for (int j = 0; j < tabCells.length; j++) {
                tabCell[j].setEnabled(false);
                counter.stop();
            }
        }
    }

    /**
     * Allow people to join another game after their previous one finishes.
     */
    void disableOnlineDisplay() {
        chat.setEnabled(false);
        sendChat.setEnabled(false);
        pseudo.setEnabled(true);
        port.setEnabled(true);
        ipAddress.setEnabled(true);
        join.setEnabled(true);
    }

    /**
     * @return the counter displayed in this GUI.
     */
    Counter getCounter() {
        return counter;
    }

    /**
     * @return the array that references all the cases in this game.
     */
    Cell[][] getTabCells() {
        return tabCells;
    }

    /**
     * @return return the chat box.
     */
    JTextArea getChat() {
        return chat;
    }

    /**
     * @return return the button that needs to be hit to send a message
     */
    JButton getSendChat() {
        return sendChat;
    }

    /**
     * @return returns the text area in which the players writes his name.
     */
    JTextField getPseudo() {
        return pseudo;
    }
}