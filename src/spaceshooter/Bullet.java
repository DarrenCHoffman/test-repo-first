package spaceshooter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents a bullet (projectile) fired by the player or an enemy.
 * Player bullets are drawn as one of five randomly-selected sushi pieces.
 */
public class Bullet {

    public static final int WIDTH = 4;
    public static final int HEIGHT = 12;

    private static final int SUSHI_TYPE_COUNT = 5;

    private int x;
    private int y;
    private final int speed;
    private final boolean fromPlayer;
    private boolean active;
    /** Sushi type index (0-4) for player bullets; -1 for enemy bullets. */
    private final int sushiType;

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
        this.sushiType = fromPlayer ? ThreadLocalRandom.current().nextInt(SUSHI_TYPE_COUNT) : -1;
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
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cx = x + WIDTH / 2;
            int cy = y + HEIGHT / 2;
            drawSushi(g2, cx, cy);
            g2.dispose();
        } else {
            g.setColor(new Color(255, 80, 80));
            g.fillRect(x, y, WIDTH, HEIGHT);
        }
    }

    private void drawSushi(Graphics2D g, int cx, int cy) {
        switch (sushiType) {
            case 0: drawNigiri(g, cx, cy);  break;
            case 1: drawMaki(g, cx, cy);    break;
            case 2: drawTemaki(g, cx, cy);  break;
            case 3: drawGunkan(g, cx, cy);  break;
            case 4: drawOnigiri(g, cx, cy); break;
            default: break;
        }
    }

    /**
     * Nigiri: oval cream rice mound with a pink salmon topping.
     */
    private void drawNigiri(Graphics2D g, int cx, int cy) {
        // Rice base (cream oval)
        g.setColor(new Color(255, 252, 225));
        g.fillOval(cx - 7, cy - 2, 14, 7);
        // Salmon topping (pink-orange rounded rect)
        g.setColor(new Color(255, 135, 80));
        g.fillRoundRect(cx - 6, cy - 8, 12, 7, 4, 4);
        // Salmon highlight stripe
        g.setColor(new Color(255, 175, 130));
        g.fillRoundRect(cx - 4, cy - 7, 5, 4, 2, 2);
        // Outlines
        g.setColor(new Color(160, 90, 40));
        g.setStroke(new BasicStroke(0.8f));
        g.drawRoundRect(cx - 6, cy - 8, 12, 7, 4, 4);
        g.setColor(new Color(200, 190, 155));
        g.drawOval(cx - 7, cy - 2, 14, 7);
    }

    /**
     * Maki roll: circular cross-section with dark nori outside, cream rice
     * ring, and a red tuna centre.
     */
    private void drawMaki(Graphics2D g, int cx, int cy) {
        // Outer nori (very dark)
        g.setColor(new Color(25, 25, 25));
        g.fillOval(cx - 7, cy - 7, 14, 14);
        // Rice ring (cream)
        g.setColor(new Color(255, 250, 220));
        g.fillOval(cx - 5, cy - 5, 10, 10);
        // Tuna filling (red centre)
        g.setColor(new Color(195, 45, 45));
        g.fillOval(cx - 3, cy - 3, 6, 6);
        // Tuna shine
        g.setColor(new Color(230, 100, 100));
        g.fillOval(cx - 2, cy - 2, 2, 2);
    }

    /**
     * Temaki: triangular hand-roll cone (point down) with rice and toppings
     * peeking out from the open top.
     */
    private void drawTemaki(Graphics2D g, int cx, int cy) {
        // Nori cone (dark green triangle, point at bottom)
        int[] xPts = {cx - 7, cx + 7, cx};
        int[] yPts = {cy - 3, cy - 3, cy + 8};
        g.setColor(new Color(20, 50, 20));
        g.fillPolygon(xPts, yPts, 3);
        // Rice visible at open top (cream oval)
        g.setColor(new Color(255, 250, 220));
        g.fillOval(cx - 6, cy - 9, 12, 8);
        // Salmon filling dot
        g.setColor(new Color(255, 130, 70));
        g.fillOval(cx - 3, cy - 8, 5, 4);
        // Cucumber dot
        g.setColor(new Color(100, 180, 80));
        g.fillOval(cx + 2, cy - 7, 3, 3);
        // Nori outline
        g.setColor(new Color(0, 30, 0));
        g.setStroke(new BasicStroke(0.8f));
        g.drawPolygon(xPts, yPts, 3);
    }

    /**
     * Gunkan (battleship) sushi: nori belt wrapped around a rice base with
     * three salmon-roe pearls piled on top.
     */
    private void drawGunkan(Graphics2D g, int cx, int cy) {
        // Nori band (black rectangle)
        g.setColor(new Color(20, 20, 20));
        g.fillRect(cx - 7, cy - 3, 14, 8);
        // Rice inside nori (cream)
        g.setColor(new Color(255, 250, 220));
        g.fillRect(cx - 6, cy - 2, 12, 6);
        // Salmon roe pearls (orange ovals above nori)
        g.setColor(new Color(255, 100, 15));
        g.fillOval(cx - 5, cy - 8, 4, 5);
        g.fillOval(cx - 1, cy - 9, 4, 5);
        g.fillOval(cx + 3, cy - 8, 4, 5);
        // Roe shine highlights
        g.setColor(new Color(255, 185, 110));
        g.fillOval(cx - 4, cy - 7, 1, 1);
        g.fillOval(cx,    cy - 8, 1, 1);
        g.fillOval(cx + 4, cy - 7, 1, 1);
        // Nori outline
        g.setColor(new Color(0, 0, 0));
        g.setStroke(new BasicStroke(0.7f));
        g.drawRect(cx - 7, cy - 3, 14, 8);
    }

    /**
     * Onigiri: white triangular rice ball with a dark nori strip across the
     * middle.
     */
    private void drawOnigiri(Graphics2D g, int cx, int cy) {
        // Rice triangle (cream-white, point at top)
        int[] xPts = {cx - 7, cx + 7, cx};
        int[] yPts = {cy + 7, cy + 7, cy - 8};
        g.setColor(new Color(255, 252, 225));
        g.fillPolygon(xPts, yPts, 3);
        // Nori strip (dark band across middle)
        g.setColor(new Color(20, 20, 20));
        g.fillRect(cx - 5, cy + 1, 10, 4);
        // Rice outline
        g.setColor(new Color(200, 195, 170));
        g.setStroke(new BasicStroke(0.8f));
        g.drawPolygon(xPts, yPts, 3);
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
