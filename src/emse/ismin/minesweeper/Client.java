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
            demineur.getIhmDemineur().createLog();
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
                        demineur.getIhmDemineur().getLog().append(date + " - Game started !\nDifficulty: " + difficulty + "\n");
                        demineur.getChamp().setBoard(Level.valueOf(difficulty));
                        demineur.getIhmDemineur().newPartie(Level.valueOf(difficulty));
                        break;
                    case "eliminated":
                        date = arrayInstruction[1];
                        String playerName = arrayInstruction[2];
                        x = Integer.parseInt(arrayInstruction[3]);
                        y = Integer.parseInt(arrayInstruction[4]);
                        playerColor = arrayInstruction[5];
                        playerScore = arrayInstruction[6];
                        playerID = Integer.parseInt(arrayInstruction[7]);
                        demineur.getIhmDemineur().getLog().append(date + " - " + playerName + " is eliminated ! He scored " + playerScore + " points\n");
                        demineur.getIhmDemineur().getTabCases()[x][y].clientRepaint(true, new Color(Integer.parseInt(playerColor)), 0);
                        if (playerID == demineur.getClient().getPlayerNum()) {
                            if (JOptionPane.showConfirmDialog(
                                    null,
                                    "Game is over. You scored " + playerScore + " points\nDo you want to wait the next game?",
                                    "Game over !",
                                    JOptionPane.YES_NO_OPTION
                            ) == JOptionPane.NO_OPTION) {
                                out.writeUTF("new false");
                                demineur.setClient(null);
                                demineur.getChamp().setBoard(Level.Easy);
                                demineur.getIhmDemineur().newPartie(Level.Easy);
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
                        demineur.getIhmDemineur().getLog().append(date + " - Game has been paused\n");
                        break;
                    case "resume":
                        date = arrayInstruction[1];
                        demineur.getIhmDemineur().getLog().append(date + " - Game has been resumed! Be fast to collect points!\n");
                        break;
                    case "left":
                        date = arrayInstruction[1];
                        playerName = arrayInstruction[2];
                        demineur.getIhmDemineur().getLog().append(date + " - " + playerName + " has left the game!\n");
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
                    default:
                        demineur.getIhmDemineur().getLog().append(instruction);
                }
            }

        } catch (IOException e) {
            System.out.println("Cannot reach server");
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

    private void close() throws IOException {
        in.close();
        out.close();
        sock.close();
    }
}
