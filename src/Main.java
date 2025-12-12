import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        System.out.println("[Main] starting launcher");
        SwingUtilities.invokeLater(() -> {
            System.out.println("[Main] running UI on EDT");
            JFrame window = new JFrame();
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setResizable(false);
            window.setTitle("Game for POO");

            main.GamePanel gamePanel = new main.GamePanel();
            window.add(gamePanel);

            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);

            gamePanel.requestFocusInWindow();
            gamePanel.startGameThread();
            System.out.println("[Main] window visible and game thread started");
        });
    }
}
