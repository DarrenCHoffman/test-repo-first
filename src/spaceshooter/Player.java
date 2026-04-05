package spaceshooter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Represents the player's spaceship.
 * The ship moves horizontally along the bottom of the screen and fires
 * upward-travelling bullets.
 */
public class Player {

    public static final int WIDTH = 40;
    public static final int HEIGHT = 24;

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

    /** Draws the player ship on the provided {@link Graphics} context. */
    public void draw(Graphics g) {
        // Ship body
        g.setColor(new Color(0, 200, 100));
        int[] xPoints = {x + WIDTH / 2, x + WIDTH, x};
        int[] yPoints = {y, y + HEIGHT, y + HEIGHT};
        g.fillPolygon(xPoints, yPoints, 3);

        // Cockpit highlight
        g.setColor(new Color(150, 255, 200));
        g.fillOval(x + WIDTH / 2 - 5, y + 6, 10, 8);

        // Engine glow
        g.setColor(new Color(255, 180, 0));
        g.fillRect(x + WIDTH / 4, y + HEIGHT - 4, WIDTH / 2, 4);
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
