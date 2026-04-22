package bg.util;

import javax.swing.*;
import java.awt.*;

public class SablierSwing {
    private final JFrame frame;
    private final JDialog dlg;
    private final JLabel labelTitle = new JLabel();
    public SablierSwing(JFrame frame, String labelTxt,String labelDialog) {
        this.frame = frame;
        labelTitle.setText(labelTxt);
        dlg = new JDialog(frame, labelDialog, Dialog.ModalityType.MODELESS);
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);

        dlg.setLayout(new BorderLayout(10, 10));
        dlg.add(labelTitle, BorderLayout.NORTH);
        dlg.add(bar, BorderLayout.CENTER);
        dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dlg.pack();
        dlg.setLocationRelativeTo(frame);
    }
    
    public void  setLabel(String label) {
    	this.labelTitle.setText(label);
    }

    public void start(String labelTitle, String labelDialog) {
    	this.labelTitle.setText(labelTitle);
    	this.dlg.setTitle(labelDialog);
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        dlg.setVisible(true);
    }

    public void stop() {
        dlg.setVisible(false);
        dlg.dispose();
        frame.setCursor(Cursor.getDefaultCursor());
    }
}