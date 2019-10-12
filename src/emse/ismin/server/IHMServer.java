package emse.ismin.server;

import emse.ismin.Level;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IHMServer extends JFrame implements ActionListener {

    private JLabel portLabel = new JLabel("Port:");
    private JTextField port = new JTextField("10000");
    private JLabel levelLabel = new JLabel("Level:");
    private JComboBox<String> levelBox = new JComboBox<>(new String[]{Level.Easy.name(), Level.Medium.name(), Level.Hard.name()});
    private JButton launch = new JButton("Launch");
    private JButton stop = new JButton("Stop");


    private JTextArea log = new JTextArea("Press 'Launch' to accept connections\n", 20, 40);

    private boolean gameStarted = false;
    private Server server;

    private IHMServer() {
        super("Minesweeper server");
        setLayout(new BorderLayout());

        log.setEditable(false);
        log.setEditable(false);
        JScrollPane jScrollPane = new JScrollPane(log);

        add(jScrollPane, BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        setResizable(false);

        pack();
    }

    public static void main(String[] args) {
        IHMServer ihmServer = new IHMServer();
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel();
        footer.setLayout(new FlowLayout());

        footer.add(portLabel);
        footer.add(port);
        footer.add(levelLabel);
        footer.add(levelBox);
        footer.add(launch);
        launch.addActionListener(this);
        footer.add(stop);
        stop.addActionListener(this);
        stop.setEnabled(false);
        return footer;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == launch && launch.getText().equals("Launch")) {
            int portNumber = Integer.parseInt(port.getText());
            String ipAddress = null;
            try {
                ipAddress = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException ex) {
                ex.printStackTrace();
            }
            launch.setText("Start game");
            log.append("Server started.\nIp address: " + ipAddress + ":" + portNumber + "\n");
            pack();
            server = new Server(portNumber, this);
            server.start();
        } else if (e.getSource() == launch && launch.getText().equals("Start game")) {
            gameStarted = true;
            port.setEnabled(false);
            levelBox.setEnabled(false);
            stop.setEnabled(true);
            launch.setText("Pause game");

            server.stopSocketServer();
            log.append("Entering connexions are now blocked.\nGame will start shortly with " +
                    server.clientListLength() + " players, in " + levelBox.getSelectedItem() + " mode\n");

            server.startGame((String) levelBox.getSelectedItem());
        } else if (e.getSource() == launch && launch.getText().equals("Pause game")) {
            server.pauseGame();
            launch.setText("Resume game");
        } else if (e.getSource() == launch && launch.getText().equals("Resume game")) {
            server.resumeGame();
            launch.setText("Pause game");
        } else if (e.getSource() == stop) {
            gameStarted = false;
            log.append(server.getDate() + " -  Game is finished ! Here are the overall scores:\n");
            server.stopGame();
        }

    }

    String getLevelBox() {
        return (String) levelBox.getSelectedItem();
    }

    boolean isGameStarted() {
        return gameStarted;
    }

    JTextArea getLog() {
        return log;
    }
}