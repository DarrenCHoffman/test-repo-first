package spaceshooter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Represents a single enemy (alien invader).
 * Enemies are arranged in a grid and move in formation; they also fire
 * downward-travelling bullets at random intervals.
 */
public class Enemy {

    public static final int WIDTH = 32;
    public static final int HEIGHT = 24;

    /** Enemy visual type (0 = top rows, 1 = middle rows, 2 = bottom rows). */
    private final int type;

    private int x;
    private int y;
    private boolean alive;

    // Points awarded per type
    private static final int[] POINT_VALUES = {30, 20, 10};

    private static final int BULLET_SPEED = 5;

    public Enemy(int x, int y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.alive = true;
    }

    /** Moves the enemy by the given delta values. */
    public void move(int dx, int dy) {
        x += dx;
        y += dy;
    }

    /** Draws the enemy on the provided {@link Graphics} context. */
    public void draw(Graphics g) {
        if (!alive) return;

        switch (type) {
            case 0:
                drawTopEnemy(g);
                break;
            case 1:
                drawMidEnemy(g);
                break;
            default:
                drawBottomEnemy(g);
                break;
        }
    }

    /** Top-row enemy: small, crab-like shape. */
    private void drawTopEnemy(Graphics g) {
        g.setColor(new Color(220, 80, 255));
        // Body
        g.fillRect(x + 8, y + 4, 16, 12);
        // Head antennae
        g.fillRect(x + 6, y, 4, 6);
        g.fillRect(x + 22, y, 4, 6);
        // Legs
        g.fillRect(x + 4, y + 12, 6, 4);
        g.fillRect(x + 22, y + 12, 6, 4);
        // Eyes
        g.setColor(Color.WHITE);
        g.fillOval(x + 10, y + 6, 5, 5);
        g.fillOval(x + 17, y + 6, 5, 5);
        g.setColor(Color.BLACK);
        g.fillOval(x + 12, y + 8, 2, 2);
        g.fillOval(x + 19, y + 8, 2, 2);
    }

    /** Mid-row enemy: medium squid-like shape. */
    private void drawMidEnemy(Graphics g) {
        g.setColor(new Color(80, 180, 255));
        // Body
        g.fillOval(x + 4, y + 2, 24, 16);
        // Tentacles
        g.fillRect(x + 4, y + 14, 4, 8);
        g.fillRect(x + 10, y + 16, 4, 6);
        g.fillRect(x + 18, y + 16, 4, 6);
        g.fillRect(x + 24, y + 14, 4, 8);
        // Eyes
        g.setColor(Color.WHITE);
        g.fillOval(x + 9, y + 5, 6, 6);
        g.fillOval(x + 17, y + 5, 6, 6);
        g.setColor(Color.BLACK);
        g.fillOval(x + 11, y + 7, 2, 2);
        g.fillOval(x + 19, y + 7, 2, 2);
    }

    /** Bottom-row enemy: large beetle-like shape. */
    private void drawBottomEnemy(Graphics g) {
        g.setColor(new Color(255, 160, 30));
        // Main body
        g.fillRect(x + 6, y + 4, 20, 14);
        // Shell wings
        g.fillOval(x, y + 6, 14, 10);
        g.fillOval(x + 18, y + 6, 14, 10);
        // Head
        g.fillOval(x + 9, y, 14, 12);
        // Eyes
        g.setColor(Color.WHITE);
        g.fillOval(x + 10, y + 2, 5, 5);
        g.fillOval(x + 17, y + 2, 5, 5);
        g.setColor(Color.BLACK);
        g.fillOval(x + 12, y + 4, 2, 2);
        g.fillOval(x + 19, y + 4, 2, 2);
    }

    /**
     * Creates a bullet fired downward from this enemy's position.
     *
     * @return a new {@link Bullet} travelling downward
     */
    public Bullet shoot() {
        return new Bullet(x + WIDTH / 2, y + HEIGHT, BULLET_SPEED, false);
    }

    /** Returns the bounding rectangle used for collision detection. */
    public Rectangle getBounds() {
        return new Rectangle(x + 4, y + 2, WIDTH - 8, HEIGHT - 4);
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public int getPointValue() {
        return POINT_VALUES[Math.min(type, POINT_VALUES.length - 1)];
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
