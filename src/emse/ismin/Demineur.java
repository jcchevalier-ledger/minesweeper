package emse.ismin;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Demineur extends JFrame{

    private Champ champ;
    private IHMDemineur ihmDemineur;

    private Demineur(){
        super("Démineur");

        this.champ = new Champ(Level.Easy);
        champ.placeMines();

        ihmDemineur = new IHMDemineur(this);
        setContentPane(ihmDemineur);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        pack();
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                if(JOptionPane.showConfirmDialog(
                        null,
                        "Are you sure?",
                        "Quit",
                        JOptionPane.YES_NO_OPTION
                ) == JOptionPane.YES_OPTION) quit();
            }
        });
    }

    public static void main(String[] args){
        new Demineur();
    }

    Champ getChamp() {
        return champ;
    }

    void quit() {
        System.out.println("Bye-Bye");
        System.exit(0);
    }

    IHMDemineur getIhmDemineur() {
        return ihmDemineur;
    }
}
