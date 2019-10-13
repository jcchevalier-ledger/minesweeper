package emse.ismin.minesweeper;

import emse.ismin.Level;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * This class is used to handle the client-server connection client-side. It extends the Thread class.
 */
class Client extends Thread {

    private int playerNum;
    private String playerName;
    private Socket sock;
    private DataOutputStream out;
    private DataInputStream in;
    private Minesweeper minesweeper;
    private boolean started;
    private boolean replaying;

    /**
     * @param ipAddress   is the ipAddress of the server.
     * @param port        is the port on which the server accept connexions for this application.
     * @param playerName  is the player's name. "Huguette" is the default player-name.
     * @param minesweeper is the actual instance of the minesweeper.
     */
    Client(String ipAddress, String port, String playerName, Minesweeper minesweeper) {
        try {
            sock = new Socket(ipAddress, Integer.parseInt(port));

            out = new DataOutputStream(sock.getOutputStream());
            in = new DataInputStream(sock.getInputStream());
            if (playerName.isEmpty()) {
                this.playerName = "Huguette";
                minesweeper.getGUIMineSweeper().getPseudo().setText(playerName);
            } else {
                this.playerName = playerName;
            }
            this.minesweeper = minesweeper;
            this.start();
        } catch (IOException e) {
            System.out.println("The ip address " + ipAddress + ":" + port + " is unreachable");
            JOptionPane.showConfirmDialog(
                    null,
                    "The ip address " + ipAddress + ":" + port + " is unreachable",
                    "Error",
                    JOptionPane.DEFAULT_OPTION
            );
        }
    }

    /**
     * This method handles the management of the server's instructions.
     */
    @Override
    public void run() {
        try {
            minesweeper.getGUIMineSweeper().createLog();
            out.writeUTF(playerName);
            playerNum = in.readInt();
            minesweeper.getGUIMineSweeper().displayID();

            while (this != null) {
                String instruction = in.readUTF();
                String[] arrayInstruction = instruction.split("\\s+");
                String date, playerColor, playerScore;
                int x, y, nbMines, playerID;
                switch (arrayInstruction[0]) {
                    case "start":
                        replaying = false;
                        started = true;
                        date = arrayInstruction[1];
                        String difficulty = arrayInstruction[2];
                        minesweeper.getGUIMineSweeper().appendToPane(date + " - Game started !\nDifficulty: " + difficulty + "\n", Color.black, true);
                        minesweeper.getField().setBoard(Level.valueOf(difficulty));
                        minesweeper.getGUIMineSweeper().newGame(Level.valueOf(difficulty));
                        break;
                    case "eliminated":
                        date = arrayInstruction[1];
                        String playerName = arrayInstruction[2];
                        x = Integer.parseInt(arrayInstruction[3]);
                        y = Integer.parseInt(arrayInstruction[4]);
                        playerColor = arrayInstruction[5];
                        playerScore = arrayInstruction[6];
                        playerID = Integer.parseInt(arrayInstruction[7]);
                        minesweeper.getGUIMineSweeper().appendToPane(date + " - " + playerName + " is eliminated ! He scored " + playerScore + " points\n", new Color(Integer.parseInt(playerColor)), true);
                        minesweeper.getGUIMineSweeper().getTabCells()[x][y].clientRepaint(true, new Color(Integer.parseInt(playerColor)), 0);
                        if (playerID == minesweeper.getClient().getPlayerNum()) {
                            if (JOptionPane.showConfirmDialog(
                                    null,
                                    "Game is over. You scored " + playerScore + " points\nDo you want to wait the next game?",
                                    "Game over !",
                                    JOptionPane.YES_NO_OPTION
                            ) == JOptionPane.NO_OPTION) {
                                out.writeUTF("new false");
                                this.close();
                            } else {
                                out.writeUTF("new true");
                                replaying = true;
                            }
                        }
                        break;
                    case "clicked":
                        nbMines = Integer.parseInt(arrayInstruction[1]);
                        x = Integer.parseInt(arrayInstruction[2]);
                        y = Integer.parseInt(arrayInstruction[3]);
                        playerColor = arrayInstruction[4];
                        minesweeper.getGUIMineSweeper().getTabCells()[x][y].clientRepaint(false, new Color(Integer.parseInt(playerColor)), nbMines);
                        break;
                    case "pause":
                        date = arrayInstruction[1];
                        JOptionPane.showConfirmDialog(
                                null,
                                "Game has been paused by an administrator",
                                "Pause",
                                JOptionPane.DEFAULT_OPTION
                        );
                        minesweeper.getGUIMineSweeper().appendToPane(date + " - Game has been paused\n", Color.black, true);
                        break;
                    case "resume":
                        date = arrayInstruction[1];
                        minesweeper.getGUIMineSweeper().appendToPane(date + " - Game has been resumed! Be fast to collect points!\n", Color.black, true);
                        break;
                    case "left":
                        date = arrayInstruction[1];
                        playerName = arrayInstruction[2];
                        minesweeper.getGUIMineSweeper().appendToPane(date + " - " + playerName + " has left the game!\n", Color.black, true);
                        break;
                    case "end":
                        if (!replaying) {
                            if (JOptionPane.showConfirmDialog(
                                    null,
                                    "Game is over. Do you want to play again ?",
                                    "Game is over !",
                                    JOptionPane.YES_NO_OPTION
                            ) == JOptionPane.YES_OPTION) {
                                out.writeUTF("new true");
                                replaying = true;
                            }
                        }
                        break;
                    case "message":
                        date = arrayInstruction[1];
                        playerColor = arrayInstruction[2];
                        playerName = arrayInstruction[3];
                        StringBuilder message = new StringBuilder();
                        message.append(date);
                        message.append(" - ");
                        message.append(playerName).append(": ");
                        for (int i = 4; i < arrayInstruction.length; i++) {
                            message.append(arrayInstruction[i]).append(" ");
                        }
                        message.append("\n");
                        minesweeper.getGUIMineSweeper().appendToPane(message.toString(), new Color(Integer.parseInt(playerColor)), false);
                        break;
                    default:
                        minesweeper.getGUIMineSweeper().appendToPane(instruction, Color.black, true);
                }
            }

        } catch (IOException e) {
            minesweeper.getGUIMineSweeper().appendToPane("Connexion lost ...\n", Color.red, true);
            int choice = JOptionPane.showConfirmDialog(
                    null,
                    "You have lost your connection with the server! Do you want to play in solo mode ?",
                    "Connexion lost !",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                this.close();
            }
        }
    }

    /**
     * Closes the thread and starts a new game.
     */
    private void close() {
        try {
            in.close();
            out.close();
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        minesweeper.setClient(null);
        minesweeper.getField().setBoard(Level.Easy);
        minesweeper.getGUIMineSweeper().newGame(Level.Easy);
        minesweeper.getGUIMineSweeper().disableOnlineDisplay();
    }

    /**
     * @param message message to be sent to the server.
     */
    void sendMessage(String message) {
        try {
            out.writeUTF("message " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the ID of the player stocked in this class.
     */
    int getPlayerNum() {
        return playerNum;
    }

    /**
     * @return returns the name of the player.
     */
    String getPlayerName() {
        return playerName;
    }

    /**
     * @return the DataOutputStream linked to this thread.
     */
    DataOutputStream getOut() {
        return out;
    }

    /**
     * @return whether or not the game has started.
     */
    boolean isStarted() {
        return started;
    }
}