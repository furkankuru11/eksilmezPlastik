package eksilmezPlastik;

import javax.swing.*;
import java.awt.*;

public class KullanicilarPanel extends JPanel {

    public KullanicilarPanel() {
        setLayout(new BorderLayout());

        // Basit bir başlık
        JLabel titleLabel = new JLabel("Kullanıcılar", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        // Örnek bir içerik
        JTextArea contentArea = new JTextArea();
        contentArea.setText("Kullanıcılar hakkında bilgiler burada görüntülenecek.");
        add(new JScrollPane(contentArea), BorderLayout.CENTER);
    }
}
