package spaceshooter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Core game panel that owns the game loop, state, rendering, and input.
 *
 * <p>Game states:
 * <ul>
 *   <li>MENU – title / high-score screen</li>
 *   <li>PLAYING – active gameplay</li>
 *   <li>PAUSED – game paused by the player</li>
 *   <li>GAME_OVER – all lives lost</li>
 *   <li>WIN – all enemies defeated (level cleared)</li>
 * </ul>
 */
public class GamePanel extends JPanel implements ActionListener {

    // ── Panel / Window dimensions ─────────────────────────────────────────────
    public static final int WIDTH  = 800;
    public static final int HEIGHT = 600;

    // ── Enemy grid configuration ──────────────────────────────────────────────
    private static final int ENEMY_COLS        = 11;
    private static final int ENEMY_ROWS        = 5;
    private static final int ENEMY_H_GAP       = 16;   // horizontal gap between enemies
    private static final int ENEMY_V_GAP       = 12;   // vertical gap between enemies
    private static final int ENEMY_TOP_MARGIN  = 60;   // distance from top of panel
    private static final int ENEMY_DROP        = 20;   // pixels enemies drop when reversing

    // ── Game-play constants ───────────────────────────────────────────────────
    private static final int PLAYER_BULLET_COOLDOWN = 15; // ticks between player shots
    private static final int ENEMY_SHOOT_INTERVAL   = 60; // average ticks between enemy shots
    private static final int ENEMY_BASE_SPEED       = 1;  // base horizontal pixel move per tick
    private static final int INITIAL_LIVES          = 3;
    private static final int TICK_RATE_MS           = 16; // ~60 fps

    // ── Star-field ────────────────────────────────────────────────────────────
    private static final int STAR_COUNT = 120;
    private final int[] starX = new int[STAR_COUNT];
    private final int[] starY = new int[STAR_COUNT];
    private final int[] starSpeed = new int[STAR_COUNT];

    // ── Game state ────────────────────────────────────────────────────────────
    private enum State { MENU, PLAYING, PAUSED, GAME_OVER, WIN }
    private State state = State.MENU;

    private Player player;
    private List<Enemy> enemies;
    private List<Bullet> bullets;

    private int score;
    private int highScore;
    private int lives;
    private int level;
    private boolean newHighScore; // true when a new all-time high was set this session

    private int playerBulletCooldown;
    private int enemyMoveDir;      // +1 = right, -1 = left
    private int enemySpeedBonus;   // extra pixels per tick (increases each level)
    private int enemyShootTimer;
    private int invincibleTimer;   // brief invincibility after being hit
    private int levelStartDelay;   // ticks to wait before enemies start moving on a new level

    private final Random random = new Random();
    private final Timer gameTimer;

    // ── Input flags ───────────────────────────────────────────────────────────
    private boolean keyLeft;
    private boolean keyRight;
    private boolean keySpace;
    private boolean keySpacePrev;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);

        initStars();

        gameTimer = new Timer(TICK_RATE_MS, this);
        gameTimer.start();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPressed(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyReleased(e.getKeyCode());
            }
        });
    }

    // ── Initialisation ────────────────────────────────────────────────────────

    private void initStars() {
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = random.nextInt(WIDTH);
            starY[i] = random.nextInt(HEIGHT);
            starSpeed[i] = 1 + random.nextInt(2);
        }
    }

    private void startGame() {
        score = 0;
        lives = INITIAL_LIVES;
        level = 1;
        enemySpeedBonus = 0;
        newHighScore = false;
        initRound();
        state = State.PLAYING;
    }

    private void initRound() {
        player = new Player(WIDTH, HEIGHT);
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        enemyMoveDir = 1;
        playerBulletCooldown = 0;
        enemyShootTimer = ENEMY_SHOOT_INTERVAL;
        invincibleTimer = 0;
        levelStartDelay = 60;

        // Build enemy grid
        int gridWidth = ENEMY_COLS * (Enemy.WIDTH + ENEMY_H_GAP) - ENEMY_H_GAP;
        int startX = (WIDTH - gridWidth) / 2;

        for (int row = 0; row < ENEMY_ROWS; row++) {
            // Row 0 = top = type 0 (highest points)
            // Rows 1-2 = type 1; Rows 3-4 = type 2 (lowest points)
            int type = (row == 0) ? 0 : (row < 3 ? 1 : 2);
            for (int col = 0; col < ENEMY_COLS; col++) {
                int ex = startX + col * (Enemy.WIDTH + ENEMY_H_GAP);
                int ey = ENEMY_TOP_MARGIN + row * (Enemy.HEIGHT + ENEMY_V_GAP);
                enemies.add(new Enemy(ex, ey, type));
            }
        }
    }

    // ── Game loop ─────────────────────────────────────────────────────────────

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }

    private void update() {
        updateStars();

        switch (state) {
            case PLAYING:
                updatePlaying();
                break;
            default:
                break;
        }
    }

    private void updateStars() {
        for (int i = 0; i < STAR_COUNT; i++) {
            starY[i] += starSpeed[i];
            if (starY[i] > HEIGHT) {
                starY[i] = 0;
                starX[i] = random.nextInt(WIDTH);
            }
        }
    }

    private void updatePlaying() {
        if (levelStartDelay > 0) {
            levelStartDelay--;
            player.update();   // allow player to move during countdown
            return;
        }

        // Player movement
        player.setMovingLeft(keyLeft);
        player.setMovingRight(keyRight);
        player.update();

        // Player shoot (fire once per press)
        if (keySpace && !keySpacePrev) {
            if (playerBulletCooldown <= 0) {
                bullets.add(player.shoot());
                playerBulletCooldown = PLAYER_BULLET_COOLDOWN;
            }
        }
        keySpacePrev = keySpace;

        if (playerBulletCooldown > 0) {
            playerBulletCooldown--;
        }

        // Update bullets
        Iterator<Bullet> it = bullets.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();
            b.update();
            // Remove off-screen bullets
            if (b.getY() < -Bullet.HEIGHT || b.getY() > HEIGHT) {
                it.remove();
            }
        }

        // Enemy movement
        int currentSpeed = ENEMY_BASE_SPEED + enemySpeedBonus;
        // Speed increases as fewer enemies remain (classic Space Invaders feel)
        int aliveCount = countAliveEnemies();
        if (aliveCount <= 10 && aliveCount > 0) {
            currentSpeed += 2;
        } else if (aliveCount <= 20) {
            currentSpeed += 1;
        }

        boolean hitEdge = false;
        for (Enemy en : enemies) {
            if (!en.isAlive()) continue;
            en.move(currentSpeed * enemyMoveDir, 0);
            int ex = en.getX();
            if (ex <= 0 || ex + Enemy.WIDTH >= WIDTH) {
                hitEdge = true;
            }
        }

        if (hitEdge) {
            enemyMoveDir = -enemyMoveDir;
            for (Enemy en : enemies) {
                if (en.isAlive()) {
                    en.move(0, ENEMY_DROP);
                }
            }
        }

        // Enemy shooting
        enemyShootTimer--;
        if (enemyShootTimer <= 0) {
            enemyShootTimer = ENEMY_SHOOT_INTERVAL + random.nextInt(ENEMY_SHOOT_INTERVAL / 2);
            fireEnemyBullet();
        }

        // Collision: player bullets vs enemies
        Iterator<Bullet> bulletIt = bullets.iterator();
        while (bulletIt.hasNext()) {
            Bullet b = bulletIt.next();
            if (!b.isFromPlayer()) continue;
            for (Enemy en : enemies) {
                if (en.isAlive() && b.getBounds().intersects(en.getBounds())) {
                    en.setAlive(false);
                    score += en.getPointValue();
                    if (score > highScore) {
                        highScore = score;
                        newHighScore = true;
                    }
                    bulletIt.remove();
                    break;
                }
            }
        }

        // Collision: enemy bullets vs player
        if (invincibleTimer <= 0) {
            for (Bullet b : bullets) {
                if (b.isFromPlayer()) continue;
                if (b.isActive() && b.getBounds().intersects(player.getBounds())) {
                    b.setActive(false);
                    lives--;
                    invincibleTimer = 120; // ~2 seconds of invincibility
                    break;
                }
            }
        } else {
            invincibleTimer--;
        }

        // Remove inactive enemy bullets
        bullets.removeIf(b -> !b.isActive());

        // Check win condition
        if (countAliveEnemies() == 0) {
            state = State.WIN;
            return;
        }

        // Check game-over conditions
        if (lives <= 0) {
            state = State.GAME_OVER;
            return;
        }

        // Enemies reached the bottom
        for (Enemy en : enemies) {
            if (en.isAlive() && en.getY() + Enemy.HEIGHT >= player.getY()) {
                state = State.GAME_OVER;
                return;
            }
        }
    }

    private int countAliveEnemies() {
        int count = 0;
        for (Enemy en : enemies) {
            if (en.isAlive()) count++;
        }
        return count;
    }

    /** Picks a random alive enemy (preferring front rows) and fires a bullet. */
    private void fireEnemyBullet() {
        // Group alive enemies by approximate column (each column is spaced by WIDTH + H_GAP)
        // Find the bottom-most alive enemy in each column
        List<Enemy> shooters = new ArrayList<>();
        for (int col = 0; col < ENEMY_COLS; col++) {
            Enemy bottomEnemy = null;
            for (Enemy en : enemies) {
                if (!en.isAlive()) continue;
                // Check if this enemy belongs to the current column by x-position alignment
                int gridWidth = ENEMY_COLS * (Enemy.WIDTH + ENEMY_H_GAP) - ENEMY_H_GAP;
                int startX = (WIDTH - gridWidth) / 2;
                int expectedX = startX + col * (Enemy.WIDTH + ENEMY_H_GAP);
                if (Math.abs(en.getX() - expectedX) > Enemy.WIDTH / 2) continue;
                // Keep the one with the greatest Y (closest to the bottom)
                if (bottomEnemy == null || en.getY() > bottomEnemy.getY()) {
                    bottomEnemy = en;
                }
            }
            if (bottomEnemy != null) {
                shooters.add(bottomEnemy);
            }
        }

        if (shooters.isEmpty()) return;
        // Pick a random column shooter
        Enemy shooter = shooters.get(random.nextInt(shooters.size()));
        bullets.add(shooter.shoot());
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2);

        switch (state) {
            case MENU:
                drawMenu(g2);
                break;
            case PLAYING:
                drawGame(g2);
                break;
            case PAUSED:
                drawGame(g2);
                drawPaused(g2);
                break;
            case WIN:
                drawWin(g2);
                break;
            case GAME_OVER:
                drawGameOver(g2);
                break;
            default:
                break;
        }
    }

    private void drawBackground(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Star field
        for (int i = 0; i < STAR_COUNT; i++) {
            int brightness = 120 + starSpeed[i] * 50;
            g.setColor(new Color(brightness, brightness, brightness));
            int size = starSpeed[i];
            g.fillRect(starX[i], starY[i], size, size);
        }
    }

    private void drawHUD(Graphics2D g) {
        g.setColor(new Color(0, 220, 120));
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.drawString("SCORE: " + score, 10, 25);

        String hiStr = "HI: " + highScore;
        FontMetrics fm = g.getFontMetrics();
        g.drawString(hiStr, (WIDTH - fm.stringWidth(hiStr)) / 2, 25);

        g.drawString("LEVEL: " + level, WIDTH - 130, 25);

        // Draw lives as small ship icons
        g.setColor(new Color(0, 200, 100));
        for (int i = 0; i < lives; i++) {
            int lx = 10 + i * 28;
            int ly = HEIGHT - 30;
            int[] xp = {lx + 10, lx + 20, lx};
            int[] yp = {ly, ly + 14, ly + 14};
            g.fillPolygon(xp, yp, 3);
        }

        // Separator line
        g.setColor(new Color(0, 100, 60));
        g.setStroke(new BasicStroke(1));
        g.drawLine(0, HEIGHT - 40, WIDTH, HEIGHT - 40);
    }

    private void drawGame(Graphics2D g) {
        // Enemies
        for (Enemy en : enemies) {
            en.draw(g);
        }

        // Bullets
        for (Bullet b : bullets) {
            b.draw(g);
        }

        // Player (flash when invincible)
        if (invincibleTimer <= 0 || (invincibleTimer / 6) % 2 == 0) {
            player.draw(g);
        }

        drawHUD(g);
    }

    private void drawMenu(Graphics2D g) {
        // Title
        g.setFont(new Font("Monospaced", Font.BOLD, 52));
        String title = "SPACE SHOOTER";
        FontMetrics fm = g.getFontMetrics();
        int tx = (WIDTH - fm.stringWidth(title)) / 2;
        // Glow effect
        g.setColor(new Color(0, 80, 200, 80));
        g.drawString(title, tx - 2, 148);
        g.drawString(title, tx + 2, 152);
        g.setColor(new Color(80, 160, 255));
        g.drawString(title, tx, 150);

        // High score
        g.setFont(new Font("Monospaced", Font.PLAIN, 20));
        String hi = "HIGH SCORE: " + highScore;
        fm = g.getFontMetrics();
        g.setColor(new Color(255, 220, 0));
        g.drawString(hi, (WIDTH - fm.stringWidth(hi)) / 2, 210);

        // Controls
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g.setColor(new Color(180, 180, 180));
        String[] lines = {
            "ARROW KEYS  -  Move",
            "SPACE       -  Fire",
            "P           -  Pause",
            "",
            "Press ENTER or SPACE to Start"
        };
        int startY = 310;
        for (String line : lines) {
            fm = g.getFontMetrics();
            g.drawString(line, (WIDTH - fm.stringWidth(line)) / 2, startY);
            startY += 30;
        }

        // Enemy legend
        drawEnemyLegend(g);
    }

    private void drawEnemyLegend(Graphics2D g) {
        int legendY = 490;
        g.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g.setColor(new Color(180, 180, 180));
        g.drawString("= 30 pts", 290, legendY + 12);
        g.drawString("= 20 pts", 290, legendY + 46);
        g.drawString("= 10 pts", 290, legendY + 80);

        // Draw sample enemies
        Enemy e0 = new Enemy(240, legendY, 0);
        Enemy e1 = new Enemy(240, legendY + 34, 1);
        Enemy e2 = new Enemy(240, legendY + 68, 2);
        e0.draw(g);
        e1.draw(g);
        e2.draw(g);
    }

    private void drawPaused(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setFont(new Font("Monospaced", Font.BOLD, 48));
        g.setColor(new Color(255, 220, 0));
        String msg = "PAUSED";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (WIDTH - fm.stringWidth(msg)) / 2, HEIGHT / 2);
        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g.setColor(Color.WHITE);
        String sub = "Press P to resume";
        fm = g.getFontMetrics();
        g.drawString(sub, (WIDTH - fm.stringWidth(sub)) / 2, HEIGHT / 2 + 50);
    }

    private void drawGameOver(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setFont(new Font("Monospaced", Font.BOLD, 56));
        g.setColor(new Color(255, 60, 60));
        String msg = "GAME OVER";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (WIDTH - fm.stringWidth(msg)) / 2, HEIGHT / 2 - 30);

        g.setFont(new Font("Monospaced", Font.PLAIN, 22));
        g.setColor(Color.WHITE);
        String scoreMsg = "SCORE: " + score + "   LEVEL: " + level;
        fm = g.getFontMetrics();
        g.drawString(scoreMsg, (WIDTH - fm.stringWidth(scoreMsg)) / 2, HEIGHT / 2 + 30);

        if (newHighScore) {
            g.setColor(new Color(255, 220, 0));
            String newHi = "NEW HIGH SCORE!";
            fm = g.getFontMetrics();
            g.drawString(newHi, (WIDTH - fm.stringWidth(newHi)) / 2, HEIGHT / 2 + 70);
        }

        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g.setColor(new Color(180, 180, 180));
        String restart = "Press ENTER to play again  |  ESC for menu";
        fm = g.getFontMetrics();
        g.drawString(restart, (WIDTH - fm.stringWidth(restart)) / 2, HEIGHT / 2 + 120);
    }

    private void drawWin(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setFont(new Font("Monospaced", Font.BOLD, 48));
        g.setColor(new Color(0, 255, 160));
        String msg = "LEVEL " + level + " CLEARED!";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (WIDTH - fm.stringWidth(msg)) / 2, HEIGHT / 2 - 20);

        g.setFont(new Font("Monospaced", Font.PLAIN, 22));
        g.setColor(Color.WHITE);
        String next = "Get ready for Level " + (level + 1) + "...";
        fm = g.getFontMetrics();
        g.drawString(next, (WIDTH - fm.stringWidth(next)) / 2, HEIGHT / 2 + 40);

        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g.setColor(new Color(180, 180, 180));
        String cont = "Press ENTER to continue";
        fm = g.getFontMetrics();
        g.drawString(cont, (WIDTH - fm.stringWidth(cont)) / 2, HEIGHT / 2 + 90);
    }

    // ── Input handling ────────────────────────────────────────────────────────

    private void handleKeyPressed(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                keyLeft = true;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                keyRight = true;
                break;
            case KeyEvent.VK_SPACE:
                keySpace = true;
                if (state == State.MENU) startGame();
                break;
            case KeyEvent.VK_ENTER:
                if (state == State.MENU || state == State.GAME_OVER) {
                    startGame();
                } else if (state == State.WIN) {
                    level++;
                    enemySpeedBonus++;
                    initRound();
                    state = State.PLAYING;
                }
                break;
            case KeyEvent.VK_P:
                if (state == State.PLAYING) {
                    state = State.PAUSED;
                } else if (state == State.PAUSED) {
                    state = State.PLAYING;
                }
                break;
            case KeyEvent.VK_ESCAPE:
                if (state == State.PLAYING || state == State.PAUSED
                        || state == State.GAME_OVER || state == State.WIN) {
                    state = State.MENU;
                }
                break;
            default:
                break;
        }
    }

    private void handleKeyReleased(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                keyLeft = false;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                keyRight = false;
                break;
            case KeyEvent.VK_SPACE:
                keySpace = false;
                break;
            default:
                break;
        }
    }
}
