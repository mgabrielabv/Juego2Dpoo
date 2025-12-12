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

                for (Enemy enemy : new java.util.ArrayList<>(enemies)) {
                    if (enemy.active) {
                        enemy.update(player);
                    }
                }
                enemies.removeIf(e -> !e.active);

                checkLevelComplete();
                break;

            case PAUSE_STATE:
                if (keyH.escapePressed) {
                    keyH.escapePressed = false;
                    gameState = PLAY_STATE;
                }
                break;

            case GAME_OVER_STATE:
                if (keyH.enterPressed) {
                    keyH.enterPressed = false;
                    restartGame();
                }
                break;
            case LEVEL_COMPLETE_STATE:
                if (keyH.enterPressed) {
                    keyH.enterPressed = false;
                    nextLevel();
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
        if (currentLevel == 2) {
            currentLevel = 1;
            tileM.regenerateMaze();
            player.reset();
            hasKey = false;
            initializeEnemies();
            gameState = PLAY_STATE;
        } else {
            gameState = GAME_OVER_STATE;
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

    private void drawGameWonScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 150, 0, 200));
        g2.fillRect(0, 0, screenWidth, screenHeight);
        g2.setColor(Color.WHITE);
        Font winFont = new Font("Arial", Font.BOLD, 48);
        g2.setFont(winFont);
        String text = "¡SALISTE! GANASTE";
        int x = (screenWidth - g2.getFontMetrics().stringWidth(text)) / 2;
        int y = screenHeight / 2;
        g2.drawString(text, x, y);

        Font sc = new Font("Arial", Font.PLAIN, 24);
        g2.setFont(sc);
        String scoreText = "Puntaje final: " + score;
        int sx = (screenWidth - g2.getFontMetrics().stringWidth(scoreText)) / 2;
        g2.drawString(scoreText, sx, y + 50);
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
