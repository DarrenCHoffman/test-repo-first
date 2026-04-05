package spaceshooter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * Represents the player's spaceship.
 * The ship moves horizontally along the bottom of the screen and fires
 * upward-travelling bullets.
 */
public class Player {

    public static final int WIDTH = 40;
    public static final int HEIGHT = 36;

    private int x;
    private int y;
    private final int panelWidth;

    private static final int SPEED = 5;
    private static final int BULLET_SPEED = 10;

    private boolean movingLeft;
    private boolean movingRight;

    public Player(int panelWidth, int panelHeight) {
        this.panelWidth = panelWidth;
        this.x = panelWidth / 2 - WIDTH / 2;
        this.y = panelHeight - HEIGHT - 20;
    }

    /** Updates the player position based on the current movement flags. */
    public void update() {
        if (movingLeft) {
            x -= SPEED;
        }
        if (movingRight) {
            x += SPEED;
        }
        // Clamp to panel bounds
        x = Math.max(0, Math.min(panelWidth - WIDTH, x));
    }

    /** Draws the player as a small yellow bird (Kiiroitori-style). */
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color yellow     = new Color(255, 215, 0);
        Color deepYellow = new Color(230, 185, 0);
        Color orange     = new Color(255, 140, 0);

        // Wings (slightly darker yellow, behind body)
        g2d.setColor(deepYellow);
        g2d.fillOval(x,      y + 14, 11, 18);  // left wing
        g2d.fillOval(x + 29, y + 14, 11, 18);  // right wing

        // Body
        g2d.setColor(yellow);
        g2d.fillOval(x + 7, y + 14, 26, 20);

        // Head
        g2d.fillOval(x + 11, y + 2, 18, 16);

        // Tuft on top of head
        g2d.fillOval(x + 18, y, 4, 5);

        // Beak (orange triangle pointing upward)
        g2d.setColor(orange);
        int[] bx = {x + 20, x + 16, x + 24};
        int[] by = {y + 2,  y + 8,  y + 8};
        g2d.fillPolygon(bx, by, 3);

        // Eyes
        g2d.setColor(Color.BLACK);
        g2d.fillOval(x + 15, y + 8, 3, 3);
        g2d.fillOval(x + 22, y + 8, 3, 3);

        // Legs
        g2d.setColor(orange);
        g2d.fillRect(x + 14, y + 32, 3, 3);  // left leg
        g2d.fillRect(x + 23, y + 32, 3, 3);  // right leg

        // Feet (toe stubs)
        g2d.fillRect(x + 10, y + 34, 8, 2);  // left foot
        g2d.fillRect(x + 21, y + 34, 8, 2);  // right foot
    }

    /**
     * Creates a bullet fired from the tip of the ship's cannon.
     *
     * @return a new {@link Bullet} travelling upward
     */
    public Bullet shoot() {
        return new Bullet(x + WIDTH / 2, y - Bullet.HEIGHT, BULLET_SPEED, true);
    }

    /** Returns the bounding rectangle used for collision detection. */
    public Rectangle getBounds() {
        return new Rectangle(x + 6, y + 4, WIDTH - 12, HEIGHT - 4);
    }

    public void setMovingLeft(boolean b) {
        this.movingLeft = b;
    }

    public void setMovingRight(boolean b) {
        this.movingRight = b;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
