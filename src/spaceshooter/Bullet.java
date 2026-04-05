package spaceshooter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Represents a bullet (projectile) fired by the player or an enemy.
 */
public class Bullet {

    public static final int WIDTH = 4;
    public static final int HEIGHT = 12;

    private int x;
    private int y;
    private final int speed;
    private final boolean fromPlayer;
    private boolean active;

    /**
     * Creates a new bullet.
     *
     * @param x          centre x of the bullet
     * @param y          top-left y of the bullet
     * @param speed      pixels moved per game tick (positive = downward)
     * @param fromPlayer {@code true} if fired by the player (moves upward)
     */
    public Bullet(int x, int y, int speed, boolean fromPlayer) {
        this.x = x - WIDTH / 2;
        this.y = y;
        this.speed = speed;
        this.fromPlayer = fromPlayer;
        this.active = true;
    }

    /** Updates the bullet position each game tick. */
    public void update() {
        if (fromPlayer) {
            y -= speed;
        } else {
            y += speed;
        }
    }

    /** Draws the bullet on the provided {@link Graphics} context. */
    public void draw(Graphics g) {
        if (!active) return;
        if (fromPlayer) {
            g.setColor(new Color(0, 220, 255));
        } else {
            g.setColor(new Color(255, 80, 80));
        }
        g.fillRect(x, y, WIDTH, HEIGHT);
    }

    /** Returns the bounding rectangle used for collision detection. */
    public Rectangle getBounds() {
        return new Rectangle(x, y, WIDTH, HEIGHT);
    }

    public boolean isFromPlayer() {
        return fromPlayer;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getY() {
        return y;
    }
}
