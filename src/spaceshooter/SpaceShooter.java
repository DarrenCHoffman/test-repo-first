package spaceshooter;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * Entry point for the Space Shooter game.
 *
 * <p>Creates the main {@link JFrame} window, attaches the {@link GamePanel},
 * and makes everything visible.
 */
public class SpaceShooter {

    private SpaceShooter() {
        // Utility class – not instantiated
    }

    /**
     * Application entry point.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Space Shooter");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setResizable(false);

            GamePanel gamePanel = new GamePanel();
            frame.add(gamePanel);
            frame.pack();
            frame.setLocationRelativeTo(null); // centre on screen
            frame.setVisible(true);

            gamePanel.requestFocusInWindow();
        });
    }
}
