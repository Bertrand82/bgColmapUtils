package bg.util;

import javax.swing.*;
import java.awt.*;

public class SablierSwing {
    private final JFrame frame;
    private final JDialog dlg;

    public SablierSwing(JFrame frame) {
        this.frame = frame;

        dlg = new JDialog(frame, "Chargement", Dialog.ModalityType.MODELESS);
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);

        dlg.setLayout(new BorderLayout(10, 10));
        dlg.add(new JLabel("Initialisation en cours, merci de patienter..."), BorderLayout.NORTH);
        dlg.add(bar, BorderLayout.CENTER);
        dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dlg.pack();
        dlg.setLocationRelativeTo(frame);
    }

    public void start() {
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        dlg.setVisible(true);
    }

    public void stop() {
        dlg.setVisible(false);
        dlg.dispose();
        frame.setCursor(Cursor.getDefaultCursor());
    }
}