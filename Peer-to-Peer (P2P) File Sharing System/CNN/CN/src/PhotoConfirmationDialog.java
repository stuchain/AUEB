import javax.swing.*;
import java.awt.*;

public class PhotoConfirmationDialog extends JFrame {
    public PhotoConfirmationDialog(ImageIcon icon) {
        JLabel label = new JLabel(icon);
        add(label, BorderLayout.CENTER);
        setTitle("Press X to close");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack(); // Αυτόματη προσαρμογή μεγέθους παραθύρου στο μέγεθος της εικόνας
        setLocationRelativeTo(null); // Κεντράρει το παράθυρο στο κέντρο της οθόνης
        setVisible(true);
    }

}
