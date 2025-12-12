package model;

import view.GamePanel;
import java.awt.*;

public class Enemy extends Entity {
    GamePanel gp;
    public boolean active = true;
    private final int id;
    private int chaseRange = 5; // rango en celdas (tiles) para empezar a perseguir
    private int patrolCounter = 0;
    private int patrolDirection = 0;
    private final int PATROL_TIME = 60;
    private Color enemyColor;

    private String type;
    private int baseSpeed;

    private static final long ENEMY_SEED = 54321L;
    private static java.util.Random enemyRand = new java.util.Random(ENEMY_SEED);

    // Variables para optimización de pathfinding
    private int bfsFrameCounter = 0;
    private int bfsNextDx = 0, bfsNextDy = 0;

    public Enemy(GamePanel gp, int id) {
        this.gp = gp;
        this.id = id;
        initializeEnemy();
    }

    /**
     * Inicializa tamaño, tipo y color del enemigo.
     * La posición se asigna desde GamePanel.
     */
    private void initializeEnemy() {
        width = gp.tileSize;
        height = gp.tileSize;
        int level = gp.currentLevel;
        if (id % 2 == 0) {
            type = "fast";
            baseSpeed = (level == 2) ? 4 : 3 + level; // velocidad alta en nivel 2
            enemyColor = Color.RED;
        } else {
            type = "normal";
            baseSpeed = (level == 2) ? 3 : 2 + level;
            enemyColor = Color.PINK;
        }
        speed = baseSpeed;
        direction = "down";
        // Rango de persecución muy alto en nivel 2
        chaseRange = (level == 2) ? 16 : 7;
        patrolDirection = enemyRand.nextInt(4);
        patrolCounter = enemyRand.nextInt(PATROL_TIME);
    }


    /**
     * update se invoca cada frame de juego cuando el enemigo está activo.
     * Decide si perseguir o patrullar y actualiza posición y colisiones.
     */
    public void update(Player player) {
        if (!active) return;
        int enemyCol = gp.getTileManager().screenToMapCol(x + gp.tileSize/2);
        int enemyRow = gp.getTileManager().screenToMapRow(y + gp.tileSize/2);
        int playerCol = gp.getTileManager().screenToMapCol(player.x + gp.tileSize/2);
        int playerRow = gp.getTileManager().screenToMapRow(player.y + gp.tileSize/2);
        int distance = Math.abs(playerCol - enemyCol) + Math.abs(playerRow - enemyRow);

        if (distance <= chaseRange) {
            int bfsFrequency = (gp.currentLevel == 2) ? 10 : 30; // Nivel 1 es menos frecuente
            if (bfsFrameCounter % bfsFrequency == 0) {
                int[] step = chasePlayerBFSGetStep(player);
                bfsNextDy = step[0];
                bfsNextDx = step[1];
            }
            bfsFrameCounter++;
            int newX = x + bfsNextDx * speed;
            int newY = y + bfsNextDy * speed;
            if (!checkCollision(newX, newY)) {
                x = newX; y = newY;
                if (bfsNextDx == 1) direction = "right";
                else if (bfsNextDx == -1) direction = "left";
                else if (bfsNextDy == 1) direction = "down";
                else if (bfsNextDy == -1) direction = "up";
            } else {
                // Si el camino BFS está bloqueado, intenta un movimiento alternativo simple
                // para no quedarse pegado. Esto es útil después de un respawn.
                bfsFrameCounter = bfsFrequency; // Forzar recálculo de BFS en el siguiente frame
                tryAlternativeMove();
            }
        } else {
            patrol();
        }

        checkPlayerCollision(player);
        int mapWidth = gp.getMap()[0].length * gp.tileSize;
        int mapHeight = gp.getMap().length * gp.tileSize;
        int offsetX = gp.getTileManager().getOffsetX();
        int offsetY = gp.getTileManager().getOffsetY();
        x = Math.max(offsetX, Math.min(x, offsetX + mapWidth - gp.tileSize));
        y = Math.max(offsetY, Math.min(y, offsetY + mapHeight - gp.tileSize));
    }

    // Devuelve el siguiente paso óptimo hacia el jugador usando BFS (sin mover al enemigo)
    private int[] chasePlayerBFSGetStep(Player player) {
        int[][] map = gp.getMap();
        int rows = map.length, cols = map[0].length;
        int enemyCol = gp.getTileManager().screenToMapCol(x + gp.tileSize/2);
        int enemyRow = gp.getTileManager().screenToMapRow(y + gp.tileSize/2);
        int playerCol = gp.getTileManager().screenToMapCol(player.x + gp.tileSize/2);
        int playerRow = gp.getTileManager().screenToMapRow(player.y + gp.tileSize/2);
        boolean[][] visited = new boolean[rows][cols];
        int[][] prev = new int[rows * cols][2];
        for (int[] p : prev) { p[0] = -1; p[1] = -1; }
        java.util.Queue<int[]> queue = new java.util.LinkedList<>();
        queue.add(new int[]{enemyRow, enemyCol});
        visited[enemyRow][enemyCol] = true;
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        boolean found = false;
        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            if (curr[0] == playerRow && curr[1] == playerCol) { found = true; break; }
            for (int d = 0; d < 4; d++) {
                int nr = curr[0] + dirs[d][0];
                int nc = curr[1] + dirs[d][1];
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && !visited[nr][nc] && !gp.getTileManager().isCollision(nr, nc)) {
                    visited[nr][nc] = true;
                    prev[nr * cols + nc][0] = curr[0];
                    prev[nr * cols + nc][1] = curr[1];
                    queue.add(new int[]{nr, nc});
                }
            }
        }
        if (found) {
            int tr = playerRow, tc = playerCol;
            int pr = prev[tr * cols + tc][0], pc = prev[tr * cols + tc][1];
            while (pr != -1 && pc != -1 && !(pr == enemyRow && pc == enemyCol)) {
                tr = pr; tc = pc;
                pr = prev[tr * cols + tc][0];
                pc = prev[tr * cols + tc][1];
            }
            int dx = tc - enemyCol;
            int dy = tr - enemyRow;
            return new int[]{dy, dx};
        }
        return new int[]{0,0};
    }

    /**
     * Intenta moverse en una dirección perpendicular a la bloqueada para evitar atascos.
     */
    private void tryAlternativeMove() {
        int originalDx = bfsNextDx;
        int originalDy = bfsNextDy;

        // Intentar movimiento perpendicular
        int altDx = -originalDy;
        int altDy = originalDx;

        for (int i = 0; i < 2; i++) {
            int newX = x + altDx * speed;
            int newY = y + altDy * speed;
            if (!checkCollision(newX, newY)) {
                x = newX;
                y = newY;
                // Actualizar dirección visual
                if (altDx == 1) direction = "right";
                else if (altDx == -1) direction = "left";
                else if (altDy == 1) direction = "down";
                else if (altDy == -1) direction = "up";
                return; // Movimiento exitoso
            }
            // Probar la otra dirección perpendicular
            altDx = originalDy;
            altDy = -originalDx;
        }
    }

    /**
     * Movimiento de patrulla simple: cambia dirección cada PATROL_TIME frames.
     */
    private void patrol() {
        patrolCounter++;

        if (patrolCounter >= PATROL_TIME) {
            patrolDirection = (int)(Math.random() * 4);
            patrolCounter = 0;
        }

        int dx = 0, dy = 0;
        switch (patrolDirection) {
            case 0: dx = 1; direction = "right"; break;
            case 1: dx = -1; direction = "left"; break;
            case 2: dy = 1; direction = "down"; break;
            case 3: dy = -1; direction = "up"; break;
        }

        int newX = x + dx * speed;
        int newY = y + dy * speed;
        if (!checkCollision(newX, newY)) {
            x = newX; y = newY;
        } else {
            // si está bloqueado, cambiar dirección en el siguiente tick
            patrolCounter = PATROL_TIME;
        }
    }

    private boolean canMoveTile(int tileCol, int tileRow) {
        if (mapInvalid()) return false;
        if (tileRow < 0 || tileRow >= gp.getMap().length || tileCol < 0 || tileCol >= gp.getMap()[0].length) return false;
        return !gp.getTileManager().isCollision(tileRow, tileCol);
    }

    private boolean mapInvalid() {
        return gp.getMap() == null || gp.getMap().length == 0 || gp.getMap()[0].length == 0;
    }

    /**
     * Comprobación de colisión pixel-perfect basada en las esquinas del rectángulo del enemigo.
     * Si colisiona con el jugador, le hace daño y empuja ligeramente al enemigo.
     */
    private boolean checkCollision(int newX, int newY) {
        int[][] corners = {
                {newX, newY},
                {newX + gp.tileSize - 1, newY},
                {newX, newY + gp.tileSize - 1},
                {newX + gp.tileSize - 1, newY + gp.tileSize - 1}
        };

        for (int[] corner : corners) {
            int mapCol = gp.getTileManager().screenToMapCol(corner[0]);
            int mapRow = gp.getTileManager().screenToMapRow(corner[1]);
            if (mapRow >= 0 && mapRow < gp.getMap().length && mapCol >= 0 && mapCol < gp.getMap()[0].length) {
                if (gp.getTileManager().isCollision(mapRow, mapCol)) return true;
            } else {
                return true; // out of bounds is collision
            }
        }
        return false;
    }

    private void checkPlayerCollision(Player player) {
        // Si el jugador es invulnerable, ignorar colisiones dañinas
        if (player.isInvulnerable()) {
            return;
        }
        Rectangle enemyRect = new Rectangle(x, y, gp.tileSize, gp.tileSize);
        Rectangle playerRect = new Rectangle(player.x, player.y, gp.tileSize, gp.tileSize);

        if (enemyRect.intersects(playerRect)) {
            player.takeDamage();
            int dx = Integer.compare(player.x, x);
            int dy = Integer.compare(player.y, y);
            x -= dx * gp.tileSize / 2;
            y -= dy * gp.tileSize / 2;
        }
    }

    public void draw(Graphics2D g2) {
        if (!active) return;

        switch (type) {
            case "fast": drawFastEnemy(g2); break;
            default: drawNormalEnemy(g2); break;
        }
    }

    private void drawNormalEnemy(Graphics2D g2) {
        g2.setColor(enemyColor);
        g2.fillOval(x + 2, y + 2, gp.tileSize - 4, gp.tileSize - 4);

        g2.setColor(Color.WHITE);
        int eyeSize = gp.tileSize / 6;
        int eyeOffset = gp.tileSize / 4;

        switch (direction) {
            case "up":
                g2.fillOval(x + eyeOffset, y + eyeOffset, eyeSize, eyeSize);
                g2.fillOval(x + gp.tileSize - eyeOffset - eyeSize, y + eyeOffset, eyeSize, eyeSize);
                break;
            case "down":
                g2.fillOval(x + eyeOffset, y + gp.tileSize - eyeOffset - eyeSize, eyeSize, eyeSize);
                g2.fillOval(x + gp.tileSize - eyeOffset - eyeSize, y + gp.tileSize - eyeOffset - eyeSize, eyeSize, eyeSize);
                break;
            case "left":
                g2.fillOval(x + eyeOffset, y + eyeOffset, eyeSize, eyeSize);
                g2.fillOval(x + eyeOffset, y + gp.tileSize - eyeOffset - eyeSize, eyeSize, eyeSize);
                break;
            case "right":
                g2.fillOval(x + gp.tileSize - eyeOffset - eyeSize, y + eyeOffset, eyeSize, eyeSize);
                g2.fillOval(x + gp.tileSize - eyeOffset - eyeSize, y + gp.tileSize - eyeOffset - eyeSize, eyeSize, eyeSize);
                break;
        }

        g2.setColor(Color.BLACK);
        switch (direction) {
            case "up":
                g2.fillOval(x + eyeOffset + 1, y + eyeOffset + 1, eyeSize/2, eyeSize/2);
                g2.fillOval(x + gp.tileSize - eyeOffset - eyeSize + 1, y + eyeOffset + 1, eyeSize/2, eyeSize/2);
                break;
            case "down":
                g2.fillOval(x + eyeOffset + 1, y + gp.tileSize - eyeOffset - eyeSize + 1, eyeSize/2, eyeSize/2);
                g2.fillOval(x + gp.tileSize - eyeOffset - eyeSize + 1, y + gp.tileSize - eyeOffset - eyeSize + 1, eyeSize/2, eyeSize/2);
                break;
            case "left":
                g2.fillOval(x + eyeOffset + 1, y + eyeOffset + 1, eyeSize/2, eyeSize/2);
                g2.fillOval(x + eyeOffset + 1, y + gp.tileSize - eyeOffset - eyeSize + 1, eyeSize/2, eyeSize/2);
                break;
            case "right":
                g2.fillOval(x + gp.tileSize - eyeOffset - eyeSize + 1, y + eyeOffset + 1, eyeSize/2, eyeSize/2);
                g2.fillOval(x + gp.tileSize - eyeOffset - eyeSize + 1, y + gp.tileSize - eyeOffset - eyeSize + 1, eyeSize/2, eyeSize/2);
                break;
        }

        g2.setFont(new Font("Arial", Font.PLAIN, 8));
        g2.setColor(Color.BLACK);
        g2.drawString(type.substring(0, 1).toUpperCase(), x + gp.tileSize/2 - 3, y + gp.tileSize/2 + 3);
    }

    private void drawFastEnemy(Graphics2D g2) {
        int size = gp.tileSize - 4;
        int offset = 2;

        g2.setColor(enemyColor);
        g2.fillOval(x + offset, y + offset, size, size);

        g2.setColor(Color.YELLOW);
        for (int i = 0; i < 4; i++) {
            int angle = i * 90;
            double rad = Math.toRadians(angle);
            int x1 = x + gp.tileSize/2 + (int)(Math.cos(rad) * (size/2));
            int y1 = y + gp.tileSize/2 + (int)(Math.sin(rad) * (size/2));
            int x2 = x + gp.tileSize/2 + (int)(Math.cos(rad) * (size/2 + 4));
            int y2 = y + gp.tileSize/2 + (int)(Math.sin(rad) * (size/2 + 4));
            g2.drawLine(x1, y1, x2, y2);
        }

        g2.setColor(Color.WHITE);
        g2.fillOval(x + offset + 4, y + offset + 4, 6, 6);
        g2.fillOval(x + offset + size - 10, y + offset + 4, 6, 6);
        g2.setColor(Color.BLACK);
        g2.fillOval(x + offset + 5, y + offset + 5, 3, 3);
        g2.fillOval(x + offset + size - 9, y + offset + 5, 3, 3);

        g2.drawArc(x + offset + 6, y + offset + 10, size - 12, 6, 0, -180);
    }

    public void resetEnemy() {
        active = true;
        initializeEnemy();
    }

    public static void resetEnemySeed() {
        enemyRand = new java.util.Random(ENEMY_SEED);
    }

    /** Reset cached AI/pathfinding state to avoid lag or stuck movement after relocation. */
    public void resetAI() {
        bfsFrameCounter = 0;
        bfsNextDx = 0;
        bfsNextDy = 0;
        direction = "down";
        patrolCounter = 0; // Reinicia también el contador de patrulla
    }

    /** Immediately recompute next movement step towards the player. */
    public void replanTowards(Player player) {
        try {
            int[] step = chasePlayerBFSGetStep(player);
            bfsNextDy = step[0];
            bfsNextDx = step[1];
            // set a direction hint
            if (bfsNextDx == 1) direction = "right";
            else if (bfsNextDx == -1) direction = "left";
            else if (bfsNextDy == 1) direction = "down";
            else if (bfsNextDy == -1) direction = "up";
        } catch (Throwable ignored) { /* keep safe defaults */ }
    }
}
