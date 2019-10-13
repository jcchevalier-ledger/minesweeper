package emse.ismin.minesweeper;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class is used to read the bestScores.csv file and write inside it.
 */
class ScoreRegistering {

    private List score = new List();
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

    /**
     * Create the linked list that contains all the data inside the file bestScores.csv. Create a new file if not
     * founded.
     */
    ScoreRegistering() {
        try {
            BufferedReader file = new BufferedReader(new FileReader("bestScores.csv"));
            String row;
            while ((row = file.readLine()) != null) {
                String[] data = row.split(";");
                this.score.addScore(new Score(data[0], data[1], data[2]));
            }
        } catch (FileNotFoundException e) {
            try {
                new BufferedWriter(new FileWriter(new File("bestScores.csv")));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write inside the bestScores.csv file all the data contained inside the linked list.
     */
    void write() {
        try {
            BufferedWriter file = new BufferedWriter(new FileWriter(new File("bestScores.csv")));
            ListElement head = score.getHead();
            while (head != null) {
                file.write(head.getScore().getRating() + ';' + head.getScore().getDate() + ';' + head.getScore().getLevel() + '\n');
                head = head.getNext();
            }
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a new score to the linked list.
     *
     * @param rating the score of the player. It is the value of the counter at then end of the game when he clicked on
     *               all possible cases.
     * @param date   the date and hour at the end of the game.
     * @param level  the difficulty of the game.
     */
    void addScore(String rating, Date date, String level) {
        score.addScore(new Score(rating, dateFormat.format(date), level));
    }
}

/**
 * This class is used to stock each score of the player.
 */
class Score {

    private String rating;
    private String date;
    private String level;

    /**
     * @param rating the score of the player. It is the value of the counter at then end of the game when he clicked on
     *               all possible cases.
     * @param date   the date and hour at the end of the game.
     * @param level  the difficulty of the game.
     */
    Score(String rating, String date, String level) {
        this.rating = rating;
        this.date = date;
        this.level = level;
    }

    /**
     * @return the rating of this score instance.
     */
    String getRating() {
        return rating;
    }

    /**
     * @return the date of this score instance.
     */
    String getDate() {
        return date;
    }

    /**
     * @return the level of this score instance.
     */
    String getLevel() {
        return level;
    }
}