package view;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import model.Player;
import model.Enemy;
import model.TileManager;
import controller.KeyHandler;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class GamePanel extends JPanel implements Runnable {
    final int originalTileSize = 16;
    final int scale = 3;
    public final int tileSize = originalTileSize * scale;
    public final int maxScreenCol = 16;
    public final int maxScreenRow = 12;
    public final int screenWidth = tileSize * maxScreenCol;
    public final int screenHeight = tileSize * maxScreenRow;
    int FPS = 60;

    public int gameState;
    public final int TITLE_STATE = 0;
    public final int PLAY_STATE = 1;
    public final int PAUSE_STATE = 2;
    public final int GAME_OVER_STATE = 3;
    public final int LEVEL_COMPLETE_STATE = 4;
    public final int GAME_WON_STATE = 5;

    public TileManager tileM;
    KeyHandler keyH = new KeyHandler();
    Thread gameThread;
    Player player;
    java.util.List<Enemy> enemies;
    public int currentLevel = 1;
    private final int MAX_LEVELS = 2;
    public boolean hasKey = false;
    private int score = 0;
    private Font gameFont;

    private BufferedImage[] deadAnimation;
    private int deadAnimationCounter = 0;
    private int deadAnimationFrame = 0;

    // Celebration animation for game won (Jump 1-20)
    private BufferedImage[] jumpWinAnimation;
    private int jumpWinCounter = 0;
    private int jumpWinFrame = 0;

    private int freezeEnemiesFrames = 0;

    // Title screen Idle animation (Idle 1-16)
    private BufferedImage[] idleTitleAnimation;
    private int idleTitleCounter = 0;
    private int idleTitleFrame = 0;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);

        gameState = TITLE_STATE;
        enemies = new java.util.ArrayList<>();

        try {
            gameFont = new Font("Arial", Font.PLAIN, 12);
        } catch (Exception e) {
            gameFont = new Font("Serif", Font.PLAIN, 12);
        }

        tileM = new TileManager(this);
        player = new Player(this, keyH);
        initializeEnemies();
        loadGameOverAnimation();
        loadJumpWinAnimation();
        loadIdleTitleAnimation();

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameState == TITLE_STATE) {
                    startGame();
                    requestFocusInWindow();
                } else if (gameState == GAME_OVER_STATE || gameState == LEVEL_COMPLETE_STATE) {
                    if (gameState == GAME_OVER_STATE) {
                        restartGame();
                    } else {
                        nextLevel();
                    }
                    requestFocusInWindow();
                }
            }
        });

        printDebugInfo();
    }

    private void loadGameOverAnimation() {
        deadAnimation = new BufferedImage[30];
        try {
            for (int i = 0; i < 30; i++) {
                String path = "/res/inicio/Dead (" + (i + 1) + ").png";
                deadAnimation[i] = ImageIO.read(getClass().getResourceAsStream(path));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadJumpWinAnimation() {
        jumpWinAnimation = new BufferedImage[20];
        try {
            for (int i = 0; i < 20; i++) {
                String path = "/res/inicio/Jump (" + (i + 1) + ")" + ".png";
                jumpWinAnimation[i] = ImageIO.read(getClass().getResourceAsStream(path));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadIdleTitleAnimation() {
        idleTitleAnimation = new BufferedImage[16];
        try {
            for (int i = 0; i < 16; i++) {
                String path = "/res/inicio/Idle (" + (i + 1) + ")" + ".png";
                idleTitleAnimation[i] = ImageIO.read(getClass().getResourceAsStream(path));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printDebugInfo() {
        try {
            if (tileM != null && tileM.map != null) {
                int rows = tileM.map.length;
                int cols = tileM.map[0].length;
                int open = 0;
                for (int r = 0; r < rows; r++)
                    for (int c = 0; c < cols; c++)
                        if (tileM.map[r][c] == 0) open++;
            }
        } catch (Exception ignored) {}
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;
        long lastTime = System.currentTimeMillis();
        int frameCount = 0;

        while (gameThread != null) {
            update();
            repaint();
            frameCount++;

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTime >= 1000) {
                frameCount = 0;
                lastTime = currentTime;
            }

            try {
                double remainingTime = nextDrawTime - System.nanoTime();
                remainingTime /= 1000000;

                if (remainingTime < 0) remainingTime = 0;

                Thread.sleep((long) remainingTime);
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        switch (gameState) {
            case TITLE_STATE:
                // advance idle animation (looping)
                idleTitleCounter++;
                if (idleTitleCounter > 6) { // slow a bit for title
                    idleTitleFrame = (idleTitleFrame + 1) % 16;
                    idleTitleCounter = 0;
                }
                if (keyH.enterPressed) {
                    keyH.enterPressed = false;
                    startGame();
                }
                break;

            case PLAY_STATE:
                if (keyH.escapePressed) {
                    keyH.escapePressed = false;
                    gameState = PAUSE_STATE;
                    break;
                }
                player.update();

                boolean freeze = false;
                if (freezeEnemiesFrames > 0) {
                    freezeEnemiesFrames--;
                    freeze = true;
                }

                if (!freeze) {
                    for (Enemy enemy : new java.util.ArrayList<>(enemies)) {
                        if (enemy.active) {
                            enemy.update(player);
                        }
                    }
                    enemies.removeIf(e -> !e.active);
                }

                checkLevelComplete();
                break;

            case PAUSE_STATE:
                if (keyH.escapePressed) {
                    keyH.escapePressed = false;
                    gameState = PLAY_STATE;
                }
                break;

            case GAME_OVER_STATE:
                deadAnimationCounter++;
                if (deadAnimationCounter > 5) {
                    deadAnimationFrame++;
                    if (deadAnimationFrame >= 30) {
                        deadAnimationFrame = 29;
                    }
                    deadAnimationCounter = 0;
                }

                if (keyH.enterPressed) {
                    keyH.enterPressed = false;
                    startGame();
                }
                break;
            case LEVEL_COMPLETE_STATE:
                if (keyH.enterPressed) {
                    keyH.enterPressed = false;
                    nextLevel();
                }
                break;
            case GAME_WON_STATE:
                // advance jump celebration frames (looping)
                jumpWinCounter++;
                if (jumpWinCounter > 5) {
                    jumpWinFrame = (jumpWinFrame + 1) % 20; // loop 0..19
                    jumpWinCounter = 0;
                }
                // allow exiting the win screen
                if (keyH.enterPressed) {
                    keyH.enterPressed = false;
                    gameState = TITLE_STATE;
                }
                break;
        }
    }

    private void checkLevelComplete() {
        int playerCol = tileM.screenToMapCol(player.x + tileSize/2);
        int playerRow = tileM.screenToMapRow(player.y + tileSize/2);

        if (playerCol == tileM.exitCol && playerRow == tileM.exitRow) {
            int tileAtExit = tileM.getTileAt(playerRow, playerCol);
            if (currentLevel == 1 && tileAtExit == 4) {
                hasKey = true;
                score += 100 * currentLevel;
                gameState = LEVEL_COMPLETE_STATE;
            } else if (currentLevel == 2 && tileAtExit == 5) {
                score += 200 * currentLevel;
                // reset celebration counters when entering won state
                jumpWinFrame = 0;
                jumpWinCounter = 0;
                gameState = GAME_WON_STATE;
            } else {
                score += 100 * currentLevel;
                gameState = LEVEL_COMPLETE_STATE;
            }
        }
    }

    private void startGame() {
        currentLevel = 1;
        score = 0;
        tileM.regenerateMaze();
        // Reset player lives and state
        player.reset();
        player.setToStart();
        hasKey = false;
        initializeEnemies();
        gameState = PLAY_STATE;
        requestFocusInWindow();
    }

    private void nextLevel() {
        if (currentLevel < MAX_LEVELS) {
            currentLevel++;
            tileM.regenerateMaze();
            player.setToStart();
            hasKey = false;
            initializeEnemies();
            gameState = PLAY_STATE;
        } else {
            gameState = TITLE_STATE;
        }
        requestFocusInWindow();
    }

    private void restartGame() {
        tileM.regenerateMaze();
        // Reset player lives and state
        player.reset();
        player.setToStart();
        hasKey = false;
        initializeEnemies();
        gameState = PLAY_STATE;
        requestFocusInWindow();
    }

    private void initializeEnemies() {
        enemies.clear();
        Enemy.resetEnemySeed();
        int enemyCount = (currentLevel == 2) ? 2 : 1;

        java.util.List<int[]> openTiles = new java.util.ArrayList<>();
        if (tileM != null && tileM.map != null) {
            for (int r = 0; r < tileM.map.length; r++) {
                for (int c = 0; c < tileM.map[0].length; c++) {
                    if (tileM.map[r][c] == 0) openTiles.add(new int[]{r, c});
                }
            }
        }

        int ox = (tileM != null) ? tileM.getOffsetX() : 0;
        int oy = (tileM != null) ? tileM.getOffsetY() : 0;

        int playerStartRow = 1, playerStartCol = 1;
        if (tileM != null && tileM.map != null) {
            outer:
            for (int r = 0; r < tileM.map.length; r++) {
                for (int c = 0; c < tileM.map[0].length; c++) {
                    if (tileM.map[r][c] == 0) {
                        playerStartRow = r;
                        playerStartCol = c;
                        break outer;
                    }
                }
            }
        }
        int exitRow = (tileM != null) ? tileM.exitRow : -1;
        int exitCol = (tileM != null) ? tileM.exitCol : -1;

        java.util.List<int[]> validTiles = new java.util.ArrayList<>();
        for (int[] t : openTiles) {
            int r = t[0], c = t[1];
            boolean nearPlayer = Math.abs(r - playerStartRow) <= 2 && Math.abs(c - playerStartCol) <= 2;
            boolean isExit = (r == exitRow && c == exitCol);
            if (!nearPlayer && !isExit) {
                validTiles.add(t);
            }
        }

        java.util.Collections.shuffle(validTiles, new java.util.Random(54321L));
        int placed = 0;
        if (currentLevel == 2 && enemyCount > 0) {
            int[][] fixedPositions = { {5, 7}, {8, 12} };
            for (int i = 0; i < fixedPositions.length && placed < enemyCount; i++) {
                int r = fixedPositions[i][0], c = fixedPositions[i][1];
                if (tileM.map[r][c] == 0) {
                    Enemy enemy = new Enemy(this, placed);
                    enemy.x = ox + c * tileSize;
                    enemy.y = oy + r * tileSize;
                    enemies.add(enemy);
                    placed++;
                }
            }
        }
        for (int i = 0; i < validTiles.size() && placed < enemyCount; i++) {
            int[] t = validTiles.get(i);
            int r = t[0], c = t[1];
            boolean isFixed = (currentLevel == 2 && ((r == 5 && c == 7) || (r == 8 && c == 12)));
            if (!isFixed) {
                Enemy enemy = new Enemy(this, placed);
                enemy.x = ox + c * tileSize;
                enemy.y = oy + r * tileSize;
                enemies.add(enemy);
                placed++;
            }
        }
    }

    public void gameOver() {
        deadAnimationFrame = 0;
        deadAnimationCounter = 0;
        gameState = GAME_OVER_STATE;
    }

    public void freezeEnemies(int frames) {
        freezeEnemiesFrames = Math.max(freezeEnemiesFrames, frames);
    }

    /**
     * Called when the player loses a life and is respawned at the start.
     * Relocates any enemy camping the spawn to an open tile far enough from
     * the player's start and resets their AI to avoid stuck movement.
     */
    public void onPlayerRespawnAfterHit() {
        // Do not freeze enemies to avoid visible lag; just reposition campers
        if (tileM == null || tileM.map == null) return;

        int playerStartCol = tileM.screenToMapCol(player.x + tileSize / 2);
        int playerStartRow = tileM.screenToMapRow(player.y + tileSize / 2);

        java.util.List<int[]> safeTiles = new java.util.ArrayList<>();
        for (int r = 0; r < tileM.map.length; r++) {
            for (int c = 0; c < tileM.map[0].length; c++) {
                if (tileM.map[r][c] == 0) {
                    int manhattan = Math.abs(r - playerStartRow) + Math.abs(c - playerStartCol);
                    if (manhattan >= 8 && !(r == tileM.exitRow && c == tileM.exitCol)) {
                        safeTiles.add(new int[]{r, c});
                    }
                }
            }
        }

        if (safeTiles.isEmpty()) return;

        java.util.Collections.shuffle(safeTiles, new java.util.Random(54321L));

        int ox = tileM.getOffsetX();
        int oy = tileM.getOffsetY();

        int safeIdx = 0;
        for (model.Enemy enemy : enemies) {
            if (enemy == null || !enemy.active) continue;
            int eCol = tileM.screenToMapCol(enemy.x + tileSize / 2);
            int eRow = tileM.screenToMapRow(enemy.y + tileSize / 2);
            int manhattan = Math.abs(eRow - playerStartRow) + Math.abs(eCol - playerStartCol);
            if (manhattan <= 3) {
                int[] t = safeTiles.get(safeIdx % safeTiles.size());
                safeIdx++;
                enemy.x = ox + t[1] * tileSize;
                enemy.y = oy + t[0] * tileSize;
                // Reset enemy AI to resume normal movement
                try { enemy.resetAI(); } catch (Throwable ignored) {}
                // Force immediate replanning towards player to avoid waiting for BFS tick
                try { enemy.replanTowards(player); } catch (Throwable ignored) {}
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(gameFont);

        switch (gameState) {
            case TITLE_STATE:
                drawTitleScreen(g2);
                break;

            case PLAY_STATE:
            case PAUSE_STATE:
                drawGameScreen(g2);
                if (gameState == PAUSE_STATE) {
                    drawPauseScreen(g2);
                }
                break;

            case GAME_OVER_STATE:
                drawGameScreen(g2);
                drawGameOverScreen(g2);
                break;

            case LEVEL_COMPLETE_STATE:
                drawGameScreen(g2);
                drawLevelCompleteScreen(g2);
                break;

            case GAME_WON_STATE:
                drawGameScreen(g2);
                drawGameWonScreen(g2);
                break;
        }

        g2.dispose();
    }

    private void drawGameScreen(Graphics2D g2) {
        tileM.draw(g2);

        for (Enemy enemy : enemies) {
            if (enemy.active) {
                enemy.draw(g2);
            }
        }

        player.draw(g2);

        drawHUD(g2);
    }

    private void drawHUD(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(5, 5, 200, 60);

        g2.setColor(Color.WHITE);
        g2.drawString("Nivel: " + currentLevel + "/" + MAX_LEVELS, 10, 20);
        g2.drawString("Vidas: " + player.lives, 10, 35);
        g2.drawString("Puntaje: " + score, 10, 50);
        g2.drawString("Salida: (" + tileM.exitCol + "," + tileM.exitRow + ")", 10, 65);

        int playerCol = tileM.screenToMapCol(player.x + tileSize/2);
        int playerRow = tileM.screenToMapRow(player.y + tileSize/2);
        g2.drawString("Posición: (" + playerCol + "," + playerRow + ")", screenWidth - 150, 20);
    }

    private void drawTitleScreen(Graphics2D g2) {
        // Pink-themed background
        g2.setColor(new Color(255, 180, 210));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        // Draw Idle animation at top-left
        if (idleTitleAnimation != null && idleTitleAnimation[idleTitleFrame] != null) {
            BufferedImage img = idleTitleAnimation[idleTitleFrame];
            int imgX = 20;
            int imgY = 20;
            g2.drawImage(img, imgX, imgY, null);
        }

        // Right-side block for title and instructions
        int rightMargin = 30;
        int blockWidth = screenWidth / 2; // use right half for text
        int blockX = screenWidth - blockWidth - rightMargin;

        // Title: Maze Girl aligned in the right block
        g2.setColor(Color.WHITE);
        Font titleFont = new Font("Arial", Font.BOLD, 42);
        g2.setFont(titleFont);
        String title = "MAZE GIRL";
        int titleX = blockX + (blockWidth - g2.getFontMetrics().stringWidth(title));
        int titleY = 100;
        g2.drawString(title, titleX, titleY);

        // Controls and instructions aligned to the right block
        g2.setColor(Color.WHITE);
        Font instFont = new Font("Arial", Font.BOLD, 24);
        g2.setFont(instFont);
        String[] lines = new String[] {
                "CONTROLES",
                "WASD o FLECHAS - Mover",
                "ENTER - Iniciar"
        };
        int startY = titleY + 50;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineX = blockX + (blockWidth - g2.getFontMetrics().stringWidth(line));
            g2.drawString(line, lineX, startY + i * 32);
        }

        // Enter hint at bottom aligned to right block
        Font startFont = new Font("Arial", Font.PLAIN, 20);
        g2.setFont(startFont);
        String startText = "Presiona ENTER para comenzar";
        int startTextX = blockX + (blockWidth - g2.getFontMetrics().stringWidth(startText));
        g2.drawString(startText, startTextX, screenHeight - 40);
    }

    private void drawPauseScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(Color.YELLOW);
        Font pauseFont = new Font("Arial", Font.BOLD, 72);
        g2.setFont(pauseFont);
        String pauseText = "PAUSA";
        FontMetrics fm = g2.getFontMetrics();
        int pauseX = (screenWidth - fm.stringWidth(pauseText)) / 2;
        int pauseY = screenHeight / 2;
        g2.drawString(pauseText, pauseX, pauseY);

        g2.setColor(Color.WHITE);
        Font instFont = new Font("Arial", Font.PLAIN, 24);
        g2.setFont(instFont);
        String instText = "Presiona ESC para continuar";
        int instX = (screenWidth - g2.getFontMetrics().stringWidth(instText)) / 2;
        g2.drawString(instText, instX, pauseY + 50);
    }

    private void drawGameOverScreen(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(Color.RED);
        Font gameOverFont = new Font("Arial", Font.BOLD, 72);
        g2.setFont(gameOverFont);
        String gameOverText = "GAME OVER";
        FontMetrics fm = g2.getFontMetrics();
        int gameOverX = (screenWidth - fm.stringWidth(gameOverText)) / 2;
        int gameOverY = 100;
        g2.drawString(gameOverText, gameOverX, gameOverY);

        if (deadAnimation != null && deadAnimation[deadAnimationFrame] != null) {
            int x = (screenWidth - deadAnimation[deadAnimationFrame].getWidth()) / 2;
            int y = (screenHeight - deadAnimation[deadAnimationFrame].getHeight()) / 2;
            g2.drawImage(deadAnimation[deadAnimationFrame], x, y, null);
        }

        g2.setColor(Color.WHITE);
        Font instFont = new Font("Arial", Font.PLAIN, 24);
        g2.setFont(instFont);
        String instText = "Presiona ENTER para volver a intentar";
        int instX = (screenWidth - g2.getFontMetrics().stringWidth(instText)) / 2;
        g2.drawString(instText, instX, screenHeight - 50);
    }

    private void drawLevelCompleteScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 100, 0, 150));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(Color.GREEN);
        Font completeFont = new Font("Arial", Font.BOLD, 48);
        g2.setFont(completeFont);
        String completeText = "¡NIVEL " + currentLevel + " COMPLETADO!";
        FontMetrics fm = g2.getFontMetrics();
        int completeX = (screenWidth - fm.stringWidth(completeText)) / 2;
        int completeY = screenHeight / 2 - 50;
        g2.drawString(completeText, completeX, completeY);

        g2.setColor(Color.YELLOW);
        Font scoreFont = new Font("Arial", Font.PLAIN, 36);
        g2.setFont(scoreFont);
        String scoreText = "Puntaje: " + score;
        int scoreX = (screenWidth - g2.getFontMetrics().stringWidth(scoreText)) / 2;
        g2.drawString(scoreText, scoreX, completeY + 60);

        Font nextFont = new Font("Arial", Font.PLAIN, 24);
        g2.setFont(nextFont);
        String nextText;
        if (currentLevel < MAX_LEVELS) {
            nextText = "Presiona ENTER para el nivel " + (currentLevel + 1);
        } else {
            nextText = "¡JUEGO COMPLETADO! Presiona ENTER para volver al inicio";
        }
        int nextX = (screenWidth - g2.getFontMetrics().stringWidth(nextText)) / 2;
        g2.drawString(nextText, nextX, completeY + 120);
    }

    private void drawGameWonScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 150, 0, 200));
        g2.fillRect(0, 0, screenWidth, screenHeight);
        g2.setColor(Color.WHITE);
        Font winFont = new Font("Arial", Font.BOLD, 48);
        g2.setFont(winFont);
        String text = "¡SALISTE! GANASTE";
        int xText = (screenWidth - g2.getFontMetrics().stringWidth(text)) / 2;
        int yText = screenHeight / 6;
        g2.drawString(text, xText, yText);

        // Instruction to exit (draw below title with shadow for visibility)
        String inst = "Presiona ENTER para salir al inicio";
        Font instFont = new Font("Arial", Font.BOLD, 22);
        g2.setFont(instFont);
        int instX = (screenWidth - g2.getFontMetrics().stringWidth(inst)) / 2;
        int instY = yText + 30;
        g2.setColor(new Color(0, 0, 0, 160));
        g2.drawString(inst, instX + 2, instY + 2);
        g2.setColor(Color.WHITE);
        g2.drawString(inst, instX, instY);

        // Draw jump celebration centered horizontally, below the instruction
        if (jumpWinAnimation != null && jumpWinAnimation[jumpWinFrame] != null) {
            BufferedImage img = jumpWinAnimation[jumpWinFrame];
            int imgX = (screenWidth - img.getWidth()) / 2;
            int imgY = instY + 20;
            g2.drawImage(img, imgX, imgY, null);
        }

        // Score below the image
        Font sc = new Font("Arial", Font.PLAIN, 24);
        g2.setFont(sc);
        String scoreText = "Puntaje final: " + score;
        int sx = (screenWidth - g2.getFontMetrics().stringWidth(scoreText)) / 2;
        g2.drawString(scoreText, sx, instY + 80);
    }

    public int[][] getMap() {
        return tileM != null ? tileM.map : null;
    }

    public TileManager getTileManager() {
        return tileM;
    }

    public java.util.List<Enemy> getEnemies() {
        return enemies;
    }

    public Player getPlayer() {
        return player;
    }

    /** Restart the current level instantly: reset player to start and reinitialize enemies without maze regeneration or freezes. */
    public void restartLevel() {
        if (tileM == null) return;
        // Keep the same maze; just reset positions
        player.setToStart();
        initializeEnemies();
        // Ensure we stay in play state
        gameState = PLAY_STATE;
        requestFocusInWindow();
    }
}
