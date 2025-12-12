package main;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import entity.Player;
import entity.Enemy;
import tile.TileManager;

public class GamePanel extends JPanel implements Runnable {
    final int originalTileSize = 16;
    final int scale = 3;
    public final int tileSize = originalTileSize * scale;
    final int maxScreenCol = 16;
    final int maxScreenRow = 12;
    public final int screenWidth = tileSize * maxScreenCol;
    public final int screenHeight = tileSize * maxScreenRow;
    int FPS = 60;

    public int gameState;
    public final int TITLE_STATE = 0;
    public final int PLAY_STATE = 1;
    public final int PAUSE_STATE = 2;
    public final int GAME_OVER_STATE = 3;
    public final int LEVEL_COMPLETE_STATE = 4;

    public TileManager tileM;
    KeyHandler keyH = new KeyHandler();
    Thread gameThread;
    Player player;
    java.util.List<Enemy> enemies;
    public int currentLevel = 1;
    private final int MAX_LEVELS = 3;
    private int score = 0;
    private Font gameFont;

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

    private void printDebugInfo() {
        try {
            if (tileM != null && tileM.map != null) {
                int rows = tileM.map.length;
                int cols = tileM.map[0].length;
                int open = 0;
                for (int r = 0; r < rows; r++)
                    for (int c = 0; c < cols; c++)
                        if (tileM.map[r][c] == 0) open++;
                System.out.println("GamePanel: " + rows + "x" + cols + " openCells=" + open);
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

                for (Enemy enemy : enemies) {
                    if (enemy.active) {
                        enemy.update(player);
                    }
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
            case LEVEL_COMPLETE_STATE:
                if (keyH.enterPressed) {
                    keyH.enterPressed = false;
                    if (gameState == GAME_OVER_STATE) {
                        restartGame();
                    } else {
                        nextLevel();
                    }
                }
                break;
        }
    }

    private void checkLevelComplete() {
        int playerCol = tileM.screenToMapCol(player.x + tileSize/2);
        int playerRow = tileM.screenToMapRow(player.y + tileSize/2);

        if (playerCol == tileM.exitCol && playerRow == tileM.exitRow) {
            score += 100 * currentLevel;
            gameState = LEVEL_COMPLETE_STATE;
        }
    }

    private void startGame() {
        currentLevel = 1;
        score = 0;
        player.reset();
        tileM.regenerateMaze();
        initializeEnemies();
        System.out.println("startGame: enemies=" + enemies.size());
        gameState = PLAY_STATE;
        requestFocusInWindow();
    }

    private void nextLevel() {
        if (currentLevel < MAX_LEVELS) {
            currentLevel++;
            player.reset();
            tileM.regenerateMaze();
            initializeEnemies();
            gameState = PLAY_STATE;
        } else {
            gameState = TITLE_STATE;
        }
        requestFocusInWindow();
    }

    private void restartGame() {
        player.reset();
        tileM.regenerateMaze();
        initializeEnemies();
        gameState = PLAY_STATE;
        requestFocusInWindow();
    }

    private void initializeEnemies() {
        enemies.clear();

        int enemyCount = currentLevel + 1;

        // Build list of open tiles (value == 0)
        java.util.List<int[]> openTiles = new java.util.ArrayList<>();
        if (tileM != null && tileM.map != null) {
            for (int r = 0; r < tileM.map.length; r++) {
                for (int c = 0; c < tileM.map[0].length; c++) {
                    if (tileM.map[r][c] == 0) openTiles.add(new int[]{r, c});
                }
            }
        }

        // DIAGNOSTIC: print map info and sample open tiles
        try {
            if (tileM != null && tileM.map != null) {
                int rows = tileM.map.length;
                int cols = tileM.map[0].length;
                System.out.println("initializeEnemies DIAG: map=" + rows + "x" + cols + " exit=(" + tileM.exitCol + "," + tileM.exitRow + ") openTiles=" + openTiles.size());
                int sample = Math.min(10, openTiles.size());
                StringBuilder sb = new StringBuilder();
                sb.append("open samples:");
                for (int i = 0; i < sample; i++) {
                    int[] t = openTiles.get(i);
                    sb.append(" (" + t[0] + "," + t[1] + ")");
                }
                System.out.println(sb.toString());

                // print small textual map preview (use '.' for open, '#' for wall, 'E' for exit)
                int maxRows = Math.min(rows, 15);
                int maxCols = Math.min(cols, 60);
                for (int r = 0; r < maxRows; r++) {
                    StringBuilder line = new StringBuilder();
                    for (int c = 0; c < maxCols; c++) {
                        int v = tileM.map[r][c];
                        char ch = (v == 0) ? '.' : (v == 3) ? 'E' : '#';
                        line.append(ch);
                    }
                    System.out.println("map[" + r + "]=" + line.toString());
                }
            } else {
                System.out.println("initializeEnemies DIAG: tileM or tileM.map is null");
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        int ox = (tileM != null) ? tileM.getOffsetX() : 0;
        int oy = (tileM != null) ? tileM.getOffsetY() : 0;

        // Get player tile to avoid placing enemies on top of the player
        int playerCol = -1000, playerRow = -1000;
        try {
            if (player != null && tileM != null) {
                playerCol = tileM.screenToMapCol(player.x + tileSize/2);
                playerRow = tileM.screenToMapRow(player.y + tileSize/2);
            }
        } catch (Exception ignored) {}

        java.util.Set<String> used = new java.util.HashSet<>();
        int placed = 0;

        // Deterministic selection: pick the first enemyCount valid open tiles in order
        for (int i = 0; i < openTiles.size() && placed < enemyCount; i++) {
            int[] t = openTiles.get(i);
            int r = t[0], c = t[1];
            if (tileM != null && r == tileM.exitRow && c == tileM.exitCol) continue;
            if (r == playerRow && c == playerCol) continue;
            String key = r + "_" + c;
            if (used.contains(key)) continue;

            Enemy enemy = new Enemy(this, placed);
            enemy.x = ox + c * tileSize;
            enemy.y = oy + r * tileSize;
            enemies.add(enemy);
            used.add(key);
            placed++;
        }

        // If we didn't place enough (e.g., openTiles < enemyCount or many excluded), try again deterministically
        if (placed < enemyCount) {
            if (openTiles.size() < enemyCount) {
                System.out.println("initializeEnemies: WARNING insufficient open tiles (" + openTiles.size() + ") for enemyCount=" + enemyCount);
            }
            for (int i = 0; i < openTiles.size() && placed < enemyCount; i++) {
                int[] t = openTiles.get(i);
                String key = t[0] + "_" + t[1];
                if (used.contains(key)) continue;
                Enemy enemy = new Enemy(this, placed);
                enemy.x = ox + t[1] * tileSize;
                enemy.y = oy + t[0] * tileSize;
                enemies.add(enemy);
                used.add(key);
                placed++;
            }
        }

        // Last resort: if still not enough, create new enemies (they will place themselves using placeEnemy)
        for (int i = placed; i < enemyCount; i++) {
            Enemy enemy = new Enemy(this, i);

            // determine the tile where the constructor placed it
            int eCol = -1, eRow = -1;
            try {
                eCol = tileM.screenToMapCol(enemy.x + tileSize/2);
                eRow = tileM.screenToMapRow(enemy.y + tileSize/2);
            } catch (Exception ignored) {}

            String key = eRow + "_" + eCol;
            if (eCol >= 0 && eRow >= 0 && used.contains(key)) {
                // find first free open tile and move enemy there
                boolean moved = false;
                for (int j = 0; j < openTiles.size(); j++) {
                    int[] t = openTiles.get(j);
                    String k2 = t[0] + "_" + t[1];
                    if (used.contains(k2)) continue;
                    enemy.x = ox + t[1] * tileSize;
                    enemy.y = oy + t[0] * tileSize;
                    used.add(k2);
                    moved = true;
                    break;
                }
                if (!moved) {
                    // give the enemy its original placement anyway but avoid duplicate key add
                    System.out.println("initializeEnemies: fallback enemy placed on used tile, no free tile to move");
                }
            } else {
                if (eCol >= 0 && eRow >= 0) used.add(key);
            }

            enemies.add(enemy);
            System.out.println("initializeEnemies: fallback created Enemy#" + i + " at tile=(" + eCol + "," + eRow + ")");
        }

        // DEBUG: report placed enemies and their tile positions
        try {
            System.out.println("initializeEnemies: requested=" + enemyCount + " placed=" + enemies.size());
            for (int i = 0; i < enemies.size(); i++) {
                Enemy e = enemies.get(i);
                int col = tileM.screenToMapCol(e.x + tileSize/2);
                int row = tileM.screenToMapRow(e.y + tileSize/2);
                System.out.println("initializeEnemies: Enemy#" + i + " tile=(" + col + "," + row + ") pixel=(" + e.x + "," + e.y + ")");
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public void gameOver() {
        gameState = GAME_OVER_STATE;
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

        // DEBUG overlay: draw small dot + index over each enemy to make them visible easily
        try {
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            for (int i = 0; i < enemies.size(); i++) {
                Enemy e = enemies.get(i);
                int cx = e.x + tileSize / 2;
                int cy = e.y + tileSize / 2;
                g2.setColor(new Color(255, 0, 255, 180));
                g2.fillOval(cx - 5, cy - 5, 10, 10);
                g2.setColor(Color.WHITE);
                g2.drawString("#" + i, cx + 8, cy + 4);
            }
        } catch (Exception ignored) {}
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
        g2.setColor(new Color(30, 30, 70));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(Color.YELLOW);
        Font titleFont = new Font("Arial", Font.BOLD, 48);
        g2.setFont(titleFont);
        String title = "LABERINTO MORTAL";
        FontMetrics fm = g2.getFontMetrics();
        int titleX = (screenWidth - fm.stringWidth(title)) / 2;
        int titleY = screenHeight / 3;
        g2.drawString(title, titleX, titleY);

        g2.setColor(Color.WHITE);
        Font subFont = new Font("Arial", Font.PLAIN, 24);
        g2.setFont(subFont);
        String sub = "Niveles: " + MAX_LEVELS;
        FontMetrics fm2 = g2.getFontMetrics();
        int subX = (screenWidth - fm2.stringWidth(sub)) / 2;
        int subY = titleY + 50;
        g2.drawString(sub, subX, subY);

        Font instFont = new Font("Arial", Font.PLAIN, 18);
        g2.setFont(instFont);
        String[] instructions = {
                "CONTROLES:",
                "WASD - Movimiento",
                "ENTER - Comenzar/Continuar",
                "ESC - Pausa",
                "",
                "OBJETIVO:",
                "Llega a la salida (E) evitando enemigos",
                "Tienes 3 vidas, ¡cuídalas!"
        };

        int startY = subY + 50;
        for (int i = 0; i < instructions.length; i++) {
            String line = instructions[i];
            int lineX = (screenWidth - g2.getFontMetrics().stringWidth(line)) / 2;
            g2.drawString(line, lineX, startY + (i * 25));
        }

        g2.setColor(Color.GREEN);
        String startText = "PRESIONA ENTER PARA COMENZAR";
        Font startFont = new Font("Arial", Font.BOLD, 20);
        g2.setFont(startFont);
        int startX = (screenWidth - g2.getFontMetrics().stringWidth(startText)) / 2;
        g2.drawString(startText, startX, screenHeight - 50);

        long time = System.currentTimeMillis();
        if ((time / 500) % 2 == 0) {
            g2.fillRect(startX - 10, screenHeight - 55, 10, 10);
            g2.fillRect(startX + g2.getFontMetrics().stringWidth(startText), screenHeight - 55, 10, 10);
        }
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
        g2.setColor(new Color(139, 0, 0, 150));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(Color.RED);
        Font gameOverFont = new Font("Arial", Font.BOLD, 72);
        g2.setFont(gameOverFont);
        String gameOverText = "GAME OVER";
        FontMetrics fm = g2.getFontMetrics();
        int gameOverX = (screenWidth - fm.stringWidth(gameOverText)) / 2;
        int gameOverY = screenHeight / 2 - 50;
        g2.drawString(gameOverText, gameOverX, gameOverY);

        g2.setColor(Color.WHITE);
        Font scoreFont = new Font("Arial", Font.PLAIN, 36);
        g2.setFont(scoreFont);
        String scoreText = "Puntaje: " + score;
        int scoreX = (screenWidth - g2.getFontMetrics().stringWidth(scoreText)) / 2;
        g2.drawString(scoreText, scoreX, gameOverY + 60);

        Font instFont = new Font("Arial", Font.PLAIN, 24);
        g2.setFont(instFont);
        String instText = "Presiona ENTER para reintentar";
        int instX = (screenWidth - g2.getFontMetrics().stringWidth(instText)) / 2;
        g2.drawString(instText, instX, gameOverY + 120);
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

    public int[][] getMap() {
        return tileM != null ? tileM.map : null;
    }

    public TileManager getTileManager() {
        return tileM;
    }

    public java.util.List<Enemy> getEnemies() {
        return enemies;
    }
}
