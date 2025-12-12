import view.GamePanel;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame();
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setResizable(false);
            window.setTitle("");

            GamePanel gamePanel = new GamePanel();
            window.add(gamePanel);

            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);

            gamePanel.requestFocusInWindow();
            gamePanel.startGameThread();
        });
    }
}
