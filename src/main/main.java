package main;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("[main] Iniciando juego en EDT");

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

            System.out.println("[main] Ventana visible y juego iniciado");
        });
    }
}