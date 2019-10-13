package emse.ismin.minesweeper;

import emse.ismin.Level;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class Client extends Thread {

    private int playerNum;
    private String playerName;
    private Socket sock;
    private DataOutputStream out;
    private DataInputStream in;
    private Demineur demineur;
    private boolean started;
    private boolean replaying;

    Client(String ipAddress, String port, String playerName, Demineur demineur) {
        try {
            sock = new Socket(ipAddress, Integer.parseInt(port));

            out = new DataOutputStream(sock.getOutputStream());
            in = new DataInputStream(sock.getInputStream());
            if (playerName.isEmpty()) {
                this.playerName = "Huguette";
            } else {
                this.playerName = playerName;
            }
            this.demineur = demineur;

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

    @Override
    public void run() {
        try {
            demineur.getIhmDemineur().createLog();
            out.writeUTF(playerName);
            playerNum = in.readInt();
            demineur.getIhmDemineur().displayID();

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
                        demineur.getIhmDemineur().appendToPane(date + " - Game started !\nDifficulty: " + difficulty + "\n", Color.black, true);
                        demineur.getChamp().setBoard(Level.valueOf(difficulty));
                        demineur.getIhmDemineur().newGame(Level.valueOf(difficulty));
                        break;
                    case "eliminated":
                        date = arrayInstruction[1];
                        String playerName = arrayInstruction[2];
                        x = Integer.parseInt(arrayInstruction[3]);
                        y = Integer.parseInt(arrayInstruction[4]);
                        playerColor = arrayInstruction[5];
                        playerScore = arrayInstruction[6];
                        playerID = Integer.parseInt(arrayInstruction[7]);
                        demineur.getIhmDemineur().appendToPane(date + " - " + playerName + " is eliminated ! He scored " + playerScore + " points\n", new Color(Integer.parseInt(playerColor)), true);
                        demineur.getIhmDemineur().getTabCases()[x][y].clientRepaint(true, new Color(Integer.parseInt(playerColor)), 0);
                        if (playerID == demineur.getClient().getPlayerNum()) {
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
                        demineur.getIhmDemineur().getTabCases()[x][y].clientRepaint(false, new Color(Integer.parseInt(playerColor)), nbMines);
                        break;
                    case "pause":
                        date = arrayInstruction[1];
                        JOptionPane.showConfirmDialog(
                                null,
                                "Game has been paused.",
                                "Pause",
                                JOptionPane.DEFAULT_OPTION
                        );
                        demineur.getIhmDemineur().appendToPane(date + " - Game has been paused\n", Color.black, true);
                        break;
                    case "resume":
                        date = arrayInstruction[1];
                        demineur.getIhmDemineur().appendToPane(date + " - Game has been resumed! Be fast to collect points!\n", Color.black, true);
                        break;
                    case "left":
                        date = arrayInstruction[1];
                        playerName = arrayInstruction[2];
                        demineur.getIhmDemineur().appendToPane(date + " - " + playerName + " has left the game!\n", Color.black, true);
                        break;
                    case "end":
                        if (!replaying) {
                            if (JOptionPane.showConfirmDialog(
                                    null,
                                    "Game is over. Do you want to start over ?",
                                    "Game is finished !",
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
                        StringBuilder message = new StringBuilder();
                        message.append(date);
                        message.append(" - ");
                        for (int i = 3; i < arrayInstruction.length; i++) {
                            message.append(arrayInstruction[i]).append(" ");
                        }
                        message.append("\n");
                        demineur.getIhmDemineur().appendToPane(message.toString(), new Color(Integer.parseInt(playerColor)), false);
                        break;
                    default:
                        demineur.getIhmDemineur().appendToPane(instruction, Color.black, true);
                }
            }

        } catch (IOException e) {
            demineur.getIhmDemineur().appendToPane("Connexion lost ...\n", Color.red, true);
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

    int getPlayerNum() {
        return playerNum;
    }

    String getPlayerName() {
        return playerName;
    }

    DataOutputStream getOut() {
        return out;
    }

    boolean isStarted() {
        return started;
    }

    private void close() {
        try {
            in.close();
            out.close();
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        demineur.getChamp().setBoard(Level.Easy);
        demineur.getIhmDemineur().newGame(Level.Easy);
        demineur.getIhmDemineur().getChat().setEnabled(false);
        demineur.getIhmDemineur().getSendChat().setEnabled(false);
        demineur.setClient(null);
    }

    void sendMessage(String message) {
        try {
            out.writeUTF("message " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
