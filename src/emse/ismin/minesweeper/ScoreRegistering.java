package emse.ismin.minesweeper;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

class ScoreRegistering {

    private List score = new List();
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

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

    void write() {
        try {
            BufferedWriter file = new BufferedWriter(new FileWriter(new File("bestScores.csv")));
            ListElement head = score.getHead();
            while (head != null) {
                file.write(head.getScore().getTime() + ';' + head.getScore().getDate() + ';' + head.getScore().getLevel() + '\n');
                head = head.getNext();
            }
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void addScore(String rating, Date date, String level) {
        score.addScore(new Score(rating, dateFormat.format(date), level));
    }
}

class Score {

    private String time;
    private String date;
    private String level;

    Score(String time, String date, String level) {
        this.time = time;
        this.date = date;
        this.level = level;
    }

    String getTime() {
        return time;
    }

    String getDate() {
        return date;
    }

    String getLevel() {
        return level;
    }
}