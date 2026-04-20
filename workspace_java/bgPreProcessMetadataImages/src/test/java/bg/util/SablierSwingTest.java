package bg.util;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingWorker;

public class SablierSwingTest {

    JFrame f = new JFrame("Demo");

    public SablierSwingTest() {
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton b = new JButton("Init data (long)");
        b.addActionListener(e -> startJobLong());

        f.add(b);
        f.setSize(320, 120);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    public static void main(String[] args) throws Exception {
        new SablierSwingTest();
    }

    void startJobLong() {
        SablierSwing ss = new SablierSwing(f,"title1","title2");

        // LIGNE 1 (remplace le sleep): lancer le "temps long" hors EDT
        SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
            	ss.start("Chargement ....","init");
                Thread.sleep(12000);
                return null;
            }

            // LIGNE 2 (remplace ss.stop() en fin): fermer quand c'est fini
            @Override protected void done() {
                ss.stop();
            }
        };
        sw.execute();
    }
}