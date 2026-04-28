package bg.display.together.gui;

import java.awt.*;
import javax.swing.*;

import bg.display.together.gui.DisplayTogetherPanel.ParamsConfiguration;

public class DisplayTogetherPanelPopup {

    // Petit conteneur de paramètres (modifiable)

    public static void showPopup(Component parent, ParamsConfiguration parametres) {
        JTextField tfPoints = new JTextField(String.valueOf(parametres.nbPointsExtraitsMax), 4); // 4 colonnes (≈ 4 chars)
        JSpinner spSeq  = new JSpinner(new SpinnerNumberModel(parametres.nbSeq, 0, 9999, 1));
        JSpinner spProx = new JSpinner(new SpinnerNumberModel(parametres.nbProx, 0, 9999, 1));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0;
        form.add(new JLabel("Nombre de points extrait :"), c);
        c.gridx = 1;
        form.add(tfPoints, c);

        c.gridx = 0; c.gridy = 1;
        form.add(new JLabel("Nombre d'images séquentielles :"), c);
        c.gridx = 1;
        form.add(spSeq, c);

        c.gridx = 0; c.gridy = 2;
        form.add(new JLabel("Nombre d'images à proximité :"), c);
        c.gridx = 1;
        form.add(spProx, c);

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(parent), "Paramètres", Dialog.ModalityType.APPLICATION_MODAL);

        JButton btCancel = new JButton("Cancel");
        JButton btApply  = new JButton("Apply and close");

        btCancel.addActionListener(e -> dlg.dispose());

        btApply.addActionListener(e -> {
            // parsing minimal + robustesse simple
            try {
                int points = Integer.parseInt(tfPoints.getText().trim());
                int seq    = (Integer) spSeq.getValue();
                int prox   = (Integer) spProx.getValue();

                parametres.nbPointsExtraitsMax = points;
                parametres.nbSeq    = seq;
                parametres.nbProx   = prox;

                dlg.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Le nombre de points doit être un entier.", "Erreur", JOptionPane.ERROR_MESSAGE);
                tfPoints.requestFocusInWindow();
                tfPoints.selectAll();
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btCancel);
        buttons.add(btApply);

        dlg.getContentPane().setLayout(new BorderLayout());
        dlg.getContentPane().add(form, BorderLayout.CENTER);
        dlg.getContentPane().add(buttons, BorderLayout.SOUTH);

        dlg.pack();
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
    }


}