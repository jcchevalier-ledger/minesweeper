package emse.ismin.server;

import emse.ismin.Level;
import emse.ismin.minesweeper.Field;

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

/**
 * This class extends a thread in order to support a basic client-server architecture.
 */
class Server extends Thread {

    private ServerSocket serverSocket;
    private GUIServer GUIServer;
    private int clientID = 0;
    private HashSet<ClientThread> clientList = new HashSet<>();
    private DateFormat dateFormat = new SimpleDateFormat("HH:mm");

    private Field field;
    private boolean[][] clicked;

    /**
     * @param port      the port on which the server will accept connexions.
     * @param GUIServer the ihmServer on which the server is displayed.
     */
    Server(int port, GUIServer GUIServer) {

        this.GUIServer = GUIServer;

        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the server's thread. This thread is only used to accept client's entering connexions and create a
     * dedicated thread for each client. When the game is started, the socket server is closed and does not accept
     * entering connexions anymore.
     */
    @Override
    public void run() {
        while (!(GUIServer.isGameStarted())) {
            Socket socket;
            try {
                socket = serverSocket.accept();

                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                ClientThread clientThread = new ClientThread(socket, in, out, clientID, this);
                out.writeInt(clientID);
                clientID++;
                clientList.add(clientThread);

                GUIServer.getLog().append(getDate() + " - A new client is connected : " + clientThread.getPlayerName() + "\n");

                broadcastMessage(getDate() + " - A new client is connected : " + clientThread.getPlayerName() + "\n");
                broadcastMessage("Clients connected :\n");
                for (ClientThread cT : clientList) {
                    broadcastMessage("     " + cT.getPlayerName() + "\n");
                }
                clientThread.start();

            } catch (SocketException ignored) {
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method stops the game and broadcast all overall scores to all connected clients.
     */
    void stopGame() {
        int playerScore;
        broadcastMessage(getDate() + " -  Game is finished ! Here are the overall scores:\n");
        for (ClientThread clientThread : clientList) {
            playerScore = clientThread.getOverallScore();
            broadcastMessage("     " + clientThread.getPlayerName() + ": " + playerScore + "\n");
            GUIServer.getLog().append("     " + clientThread.getPlayerName() + ": " + playerScore + "\n");
        }
        broadcastMessage("The server will be closed shortly. Thanks for playing!\n");
        closeServer();
    }

    /**
     * This method closes the client's list and stops the program.
     */
    void closeServer() {
        clientList.clear();
        System.exit(0);
    }

    /**
     * Closes the socket server.
     */
    void stopSocketServer() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method broadcasts a string entered as a parameter to each client.
     *
     * @param msg message to be sent
     */
    void broadcastMessage(String msg) {
        for (ClientThread clientThread : clientList) {
            try {
                clientThread.getOut().writeUTF(msg);
            } catch (IOException | NullPointerException ignored) {
            }
        }
    }

    /**
     * Starts a new game by creating a new field and
     *
     * @param level the level of the game.
     */
    void startGame(String level) {
        for (ClientThread clientThread : clientList) {
            clientThread.startGame();
        }
        broadcastMessage("start" + " " + getDate() + " " + level);
        this.field = new Field(Level.valueOf(level));
        this.clicked = new boolean[field.getBoard().length][field.getBoard().length];
        field.placeMines();
    }

    /**
     * Pauses the game.
     */
    void pauseGame() {
        GUIServer.getLog().append(getDate() + " -  Game has been paused\n");
        broadcastMessage("pause" + " " + getDate());
        for (ClientThread clientThread : clientList) {
            clientThread.setDisabled(true);
        }
    }

    /**
     * Resumes the game.
     */
    void resumeGame() {
        GUIServer.getLog().append(getDate() + " -  Game has been resumed\n");
        broadcastMessage("resume" + " " + getDate());
        for (ClientThread clientThread : clientList) {
            clientThread.setDisabled(false);
        }
    }

    /**
     * Send a string to every client to specify that one of them clicked on a cell, and to inform them if this case
     * is a mine or not. It also sends the color of the player who clicked on the case.
     *
     * @param x            x-coordinates of the case to repaint
     * @param y            y-coordinates of the case to repaint
     * @param clientThread thread linked to the client who clicked on this specific cell
     * @param isMine       specifies if the player clicked on a case or not
     */
    void forceCellRepaint(int x, int y, ClientThread clientThread, boolean isMine) {
        if (isMine) {
            broadcastMessage("eliminated" + " " + getDate() + " " + clientThread.getPlayerName() + " " + x + " " + y + " " + clientThread.getPlayerColor().getRGB() + " " + clientThread.getScore() + " " + clientThread.getClientID());
        } else {
            String minesAround = field.minesAround(x, y);
            if (minesAround.isEmpty()) {
                broadcastMessage("clicked" + " " + 0 + " " + x + " " + y + " " + clientThread.getPlayerColor().getRGB());
            } else {
                broadcastMessage("clicked" + " " + minesAround + " " + x + " " + y + " " + clientThread.getPlayerColor().getRGB());
            }

        }
    }

    /**
     * Check if the number of cases clicked by all players is equals the the overall number of cases in which there is
     * no mine. If its true, ends the round.
     */
    void checkPlayersStatus() {
        int totalScore = 0;
        int numberOfCases = getClicked().length * getClicked().length - getField().getNumberOfMines();

        for (ClientThread clientThread : clientList) {
            totalScore += clientThread.getScore();
        }

        if (totalScore == numberOfCases) {
            broadcastMessage("end");
            resetAllScores();
        }
    }

    /**
     * Resets all scores for each client.
     */
    private void resetAllScores() {
        for (ClientThread clientThread : clientList) {
            clientThread.resetScore();
        }
    }

    /**
     * Check if each clients does not want to replay yet and has not left the game. If each client has whether left the
     * game or wants to replay, resets all scores and starts a new game.
     */
    void checkReplay() {
        for (ClientThread clientThread : clientList) {
            if ((!clientThread.wantsToReplay()) && (!clientThread.hasLeft())) {
                return;
            }
        }
        resetAllScores();
        startGame(GUIServer.getLevelBox());
    }

    /**
     * @return returns the number of clients.
     */
    int clientListLength() {
        return clientList.size();
    }

    /**
     * @return the Field class linked to this server's game.
     */
    Field getField() {
        return field;
    }

    /**
     * @return the actual date calculated server-side.
     */
    String getDate() {
        String date = dateFormat.format(Calendar.getInstance().getTime());
        date = "[" + date + "]";
        return date;
    }

    /**
     * @return the array specifying which boxes have been clicked.
     */
    boolean[][] getClicked() {
        return clicked;
    }

    /**
     * @return the GUI linked to this server.
     */
    GUIServer getGUIServer() {
        return GUIServer;
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
    private int overallScore = 0;
    private boolean disabled = true;
    private boolean wantsToReplay;
    private boolean hasLeft = false;

    /**
     * @param sock     the socket linking this server and the client
     * @param in       the DataInputStream
     * @param out      the DataOutputStream
     * @param clientID the ID of the client
     * @param server   the server that accepted the connexions
     * @throws IOException unused
     */
    ClientThread(Socket sock, DataInputStream in, DataOutputStream out, int clientID, Server server) throws IOException {
        this.sock = sock;
        this.out = out;
        this.in = in;
        this.clientID = clientID;
        this.playerName = in.readUTF();
        this.server = server;
    }

    /**
     * Starts the Client's thread. It listens on the DataStreamInput linked to the socket dedicated to this client, and
     * does some tasks in function of what the client has send.
     */
    @Override
    public void run() {
        try {
            while (this != null) {
                String instruction = in.readUTF();
                String[] arrayInstruction = instruction.split("\\s+");
                switch (arrayInstruction[0]) {
                    case "click":
                        int x = Integer.parseInt(arrayInstruction[1]);
                        int y = Integer.parseInt(arrayInstruction[2]);
                        if (!server.getClicked()[x][y] && !disabled) {
                            boolean isMine = server.getField().getBoard()[x][y];
                            if (!isMine) {
                                score += 1;
                            } else {
                                disabled = true;
                                server.getGUIServer().getLog().append(server.getDate() + " - " + playerName + " has lost!\nHe scored " + score + " points!\n");
                            }
                            server.getClicked()[x][y] = true;
                            server.forceCellRepaint(x, y, this, isMine);
                            server.checkPlayersStatus();
                        }
                        break;
                    case "new":
                        if (arrayInstruction[1].equals("true")) {
                            server.getGUIServer().getLog().append(server.getDate() + " - " + playerName + " wants to play again!\n");
                            wantsToReplay = true;
                        } else {
                            hasLeft = true;
                            stopThread();
                        }
                        server.checkReplay();
                        break;
                    case "message":
                        if (arrayInstruction.length > 1) {
                            StringBuilder message = new StringBuilder();
                            message.append("message ");
                            message.append(server.getDate()).append(" ");
                            message.append(playerColor.getRGB()).append(" ");
                            message.append(playerName).append(" ");
                            for (int i = 1; i < arrayInstruction.length; i++) {
                                message.append(arrayInstruction[i]).append(" ");
                            }
                            server.broadcastMessage(message.toString());
                        }
                        break;
                }
            }
        } catch (EOFException e) {
            hasLeft = true;
            stopThread();
        } catch (NullPointerException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Resets the score and add it to the overall score.
     */
    void resetScore() {
        overallScore += score;
        score = 0;
    }

    /**
     * Stops this thread.
     */
    private void stopThread() {
        server.broadcastMessage("left" + " " + server.getDate() + " " + playerName);
        server.getGUIServer().getLog().append(server.getDate() + " - " + playerName + " has left the game\n");
        try {
            in.close();
            out.close();
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the ID of this thread.
     */
    int getClientID() {
        return clientID;
    }

    /**
     * @return the pseudo of the player.
     */
    String getPlayerName() {
        return playerName;
    }

    /**
     * @return the color of the player.
     */
    Color getPlayerColor() {
        return playerColor;
    }

    /**
     * @return the output stream of this class.
     */
    DataOutputStream getOut() {
        return out;
    }

    /**
     * Allow the player to play or not.
     */
    void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * @return the score of a player for this round only.
     */
    int getScore() {
        return score;
    }

    /**
     * @return the score of a player for the whole game.
     */
    int getOverallScore() {
        return overallScore;
    }

    /**
     * @return if the player has lost and wants to play again.
     */
    boolean wantsToReplay() {
        return wantsToReplay;
    }

    /**
     * @return if the player has left the game. This allows to keep player's data even after he left, for score display
     * for instance.
     */
    boolean hasLeft() {
        return hasLeft;
    }

    /**
     * Starts the game. Players can now clock on cases.
     */
    void startGame() {
        disabled = false;
        wantsToReplay = false;
    }
}