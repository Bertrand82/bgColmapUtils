package bg.display.together.gui;

import java.awt.*;
import javax.swing.*;



public class DisplayTogetherPanelPopup {

  public static void showPopup(Component parent, ParamsConfiguration parametres) {
    JTextField tfPoints1 = new JTextField(String.valueOf(parametres.nbPointsExtraitsMax), 6);
    JTextField tfPoints2 = new JTextField(String.valueOf(parametres.taillePaquet), 6);
    JTextField tfRecouvrement = new JTextField(String.valueOf(parametres.recouvrementPaquets), 6);

    JSpinner spSeq  = new JSpinner(new SpinnerNumberModel(parametres.nbSeq, 0, 9999, 1));
    JSpinner spProx = new JSpinner(new SpinnerNumberModel(parametres.nbProx, 0, 9999, 1));

    // Formulaire en GridLayout: 4 lignes, 2 colonnes (Label / Champ)
    JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
    form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    form.add(new JLabel("Nombre de points extrait :"));
    form.add(tfPoints1);

    form.add(new JLabel("Taille paquet :"));
    form.add(tfPoints2);

    form.add(new JLabel("Nombre d'images séquentielles :"));
    form.add(spSeq);

    form.add(new JLabel("Nombre d'images à proximité :"));
    form.add(spProx);
    
    form.add(new JLabel("Recouvrement des paquets (entre 0 et 1) :"));
    form.add(tfRecouvrement);


    JDialog dlg = new JDialog(
        SwingUtilities.getWindowAncestor(parent),
        "Paramètres",
        Dialog.ModalityType.APPLICATION_MODAL
    );

    JButton btCancel = new JButton("Cancel");
    JButton btApply  = new JButton("Apply and close");

    btCancel.addActionListener(e -> dlg.dispose());

    btApply.addActionListener(e -> {
      try {
        int points = Integer.parseInt(tfPoints1.getText().trim());
        int taillePaquet = Integer.parseInt(tfPoints2.getText().trim());
        int nbSeq  = (Integer) spSeq.getValue();
        int prox = (Integer) spProx.getValue();
        double recouvrementPaquet =Double.parseDouble(tfRecouvrement.getText().trim());

        parametres.nbPointsExtraitsMax = points;
        parametres.taillePaquet = taillePaquet; // manquait dans ton code original
        parametres.nbSeq = nbSeq;
        parametres.nbProx = prox;
        parametres.recouvrementPaquets=recouvrementPaquet;
        parametres.save();
        dlg.dispose();
      } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(
            dlg,
            "Les champs numériques doivent être des entiers.",
            "Erreur",
            JOptionPane.ERROR_MESSAGE
        );
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