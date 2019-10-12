package emse.ismin.server;

import emse.ismin.Level;
import emse.ismin.minesweeper.Champ;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Random;

class Server extends Thread {

    private ServerSocket serverSocket;
    private IHMServer ihmServer;
    private int clientID = 0;
    private HashSet<ClientThread> clientList = new HashSet<>();
    private DateFormat dateFormat = new SimpleDateFormat("HH:mm");
    private String date;
    private boolean isPaused;

    private Champ champ;
    private Level level;
    private boolean[][] clicked;

    Server(int port, IHMServer ihmServer) {

        this.ihmServer = ihmServer;

        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!(ihmServer.isGameStarted())) {

            Socket socket;

            try {

                socket = serverSocket.accept();

                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                ClientThread clientThread = new ClientThread(socket, in, out, clientID, this);
                out.writeInt(clientID);
                clientID++;
                clientList.add(clientThread);

                ihmServer.getLog().append(getDate() + " - A new client is connected : " + clientThread.getPlayerName() + "\n");

                broadcastMessage(getDate() + " - A new client is connected : " + clientThread.getPlayerName() + "\n");
                broadcastMessage("Clients connected :\n");
                for (ClientThread cT : clientList) {
                    broadcastMessage("     " + cT.getPlayerName() + "\n");
                }

            } catch (SocketException ignored) {
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void stopGame() {
        int playerScore;
        for (ClientThread clientThread : clientList) {
            playerScore = clientThread.getScoreRound();
            broadcastMessage("     " + clientThread.getPlayerName() + ": " + playerScore + "\n");
            ihmServer.getLog().append("     " + clientThread.getPlayerName() + ": " + playerScore + "\n");
            clientList.remove(clientThread);
        }
        broadcastMessage("The server will be closed shortly. Thanks for playing!\n");
    }

    void stopSocketServer() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void broadcastMessage(String msg) {
        for (ClientThread clientThread : clientList) {
            try {
                clientThread.getOut().writeUTF(msg);
            } catch (IOException ignored) {
            }
        }
    }

    void startGame(String level) {
        for (ClientThread clientThread : clientList) {
            if (!clientThread.isStarted()) {
                clientThread.start();
            }
            clientThread.setHasLost(false);
            clientThread.setWantsToReplay(false);
        }
        broadcastMessage("start" + " " + getDate() + " " + level);
        this.champ = new Champ(Level.valueOf(level));
        this.clicked = new boolean[champ.getBoard().length][champ.getBoard().length];
        champ.placeMines();
    }

    void pauseGame() {
        ihmServer.getLog().append(getDate() + " -  Game has been paused\n");
        broadcastMessage("pause" + " " + getDate());
        isPaused = true;
    }

    void resumeGame() {
        ihmServer.getLog().append(getDate() + " -  Game has been resumed\n");
        broadcastMessage("resume" + " " + getDate());
        isPaused = false;
    }

    void forceCaseRepaint(int x, int y, ClientThread clientThread, boolean isMine) {
        if (isMine) {
            broadcastMessage("eliminated" + " " + getDate() + " " + clientThread.getPlayerName() + " " + x + " " + y + " " + clientThread.getPlayerColor().getRGB() + " " + clientThread.getScore() + " " + clientThread.getClientID());
        } else {
            String minesAround = champ.minesAround(x, y);
            if (minesAround.isEmpty()) {
                broadcastMessage("clicked" + " " + 0 + " " + x + " " + y + " " + clientThread.getPlayerColor().getRGB());
            } else {
                broadcastMessage("clicked" + " " + minesAround + " " + x + " " + y + " " + clientThread.getPlayerColor().getRGB());
            }

        }
    }

    void stopThread(int clientID) {
        for (ClientThread clientThread : clientList) {
            if (clientThread.getClientID() == clientID) {
                broadcastMessage(clientThread.getPlayerName() + " has left the game\n");
                ihmServer.getLog().append(getDate() + " - " + clientThread.getPlayerName() + " has left the game\n");
                clientThread.stopThread();
                clientList.remove(clientThread);
            }
        }
    }

    void checkPlayersStatus() {
        int totalScore = 0;
        int numberOfCases = getClicked().length * getClicked().length - getChamp().getNumberOfMines();
        int numberOfLooser = 0;

        for (ClientThread clientThread : clientList) {
            totalScore += clientThread.getScore();
            if (clientThread.hasLost()) {
                numberOfLooser = +1;
            }
        }
        if (clientList.size() == numberOfLooser || totalScore == numberOfCases) {
            broadcastMessage("end");
            for (ClientThread clientThread : clientList) {
                clientThread.resetScore();
            }

        }
    }

    void checkReplay() {
        for (ClientThread clientThread : clientList) {
            if (!clientThread.getReplay()) {
                return;
            }
        }
        startGame(ihmServer.getLevelBox());
    }

    int clientListLength() {
        return clientList.size();
    }

    Champ getChamp() {
        return champ;
    }

    String getDate() {
        date = dateFormat.format(Calendar.getInstance().getTime());
        date = "[" + date + "]";
        return date;
    }

    boolean[][] getClicked() {
        return clicked;
    }

    IHMServer getIhmServer() {
        return ihmServer;
    }

    boolean isPaused() {
        return isPaused;
    }
}

class ClientThread extends Thread {

    private Socket sock;
    private DataInputStream in;
    private DataOutputStream out;
    private int clientID;
    private String playerName;
    private Server server;
    private Color playerColor = new Color(new Random().nextInt(255), new Random().nextInt(255), new Random().nextInt(255));
    private int score = 0;
    private int scoreRound = 0;
    private boolean hasLost = false;
    private boolean wantsToReplay;
    private boolean started;

    ClientThread(Socket sock, DataInputStream in, DataOutputStream out, int clientID, Server server) throws IOException {
        this.sock = sock;
        this.out = out;
        this.in = in;
        this.clientID = clientID;
        this.playerName = in.readUTF();
        this.server = server;
        this.started = false;
    }

    @Override
    public void run() {
        started = true;
        try {
            while (this != null) {
                String instruction = in.readUTF();
                String[] arrayInstruction = instruction.split("\\s+");
                switch (arrayInstruction[0]) {
                    case "click":
                        int x = Integer.parseInt(arrayInstruction[1]);
                        int y = Integer.parseInt(arrayInstruction[2]);
                        if (!server.getClicked()[x][y] && !hasLost && !server.isPaused()) {
                            boolean isMine = server.getChamp().getBoard()[x][y];
                            if (!isMine) {
                                score += 1;
                            } else {
                                hasLost = true;
                                server.getIhmServer().getLog().append(server.getDate() + " - " + playerName + " has lost!\nHe scored " + score + " points!\n");
                            }
                            server.getClicked()[x][y] = true;
                            server.forceCaseRepaint(x, y, this, isMine);
                            server.checkPlayersStatus();
                        }
                        break;
                    case "new":
                        if (arrayInstruction[1].equals("true")) {
                            server.getIhmServer().getLog().append(server.getDate() + " - " + playerName + " wants to play again!\n");
                            wantsToReplay = true;
                            server.checkReplay();

                        } else {
                            server.stopThread(clientID);
                        }
                        break;
                }
            }
        } catch (EOFException e) {
            server.stopThread(clientID);
            server.broadcastMessage("left" + " " + server.getDate() + " " + playerName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException ignored) {
        }
    }

    void resetScore() {
        scoreRound += score;
        score = 0;
    }

    void stopThread() {
        try {
            in.close();
            out.close();
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        server = null;
    }

    int getClientID() {
        return clientID;
    }

    String getPlayerName() {
        return playerName;
    }

    Color getPlayerColor() {
        return playerColor;
    }

    DataOutputStream getOut() {
        return out;
    }

    boolean hasLost() {
        return hasLost;
    }

    void setHasLost(boolean hasLost) {
        this.hasLost = hasLost;
    }

    int getScore() {
        return score;
    }

    int getScoreRound() {
        return scoreRound;
    }

    boolean getReplay() {
        return wantsToReplay;
    }

    boolean isStarted() {
        return started;
    }

    void setWantsToReplay(boolean wantsToReplay) {
        this.wantsToReplay = wantsToReplay;
    }
}