package tile;

import main.GamePanel;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;

public class TileManager {
    GamePanel gp;
    private Tile[] tiles;
    public int[][] map;
    public int exitRow, exitCol;

    public TileManager(GamePanel gp) {
        this.gp = gp;
        tiles = new Tile[10];
        getTileImage();
        generateMaze();
    }

    public int getOffsetX() {
        if (map == null || map[0].length == 0) return 0;
        int mapWidth = map[0].length * gp.tileSize;
        return (gp.screenWidth - mapWidth) / 2;
    }

    public int getOffsetY() {
        if (map == null || map.length == 0) return 0;
        int mapHeight = map.length * gp.tileSize;
        return (gp.screenHeight - mapHeight) / 2;
    }

    public int screenToMapCol(int screenX) {
        int ox = getOffsetX();
        return (screenX - ox) / gp.tileSize;
    }

    public int screenToMapRow(int screenY) {
        int oy = getOffsetY();
        return (screenY - oy) / gp.tileSize;
    }

    private void getTileImage() {
        try {
            tiles[0] = new Tile();
            tiles[0].image = loadTileImage("grass");
            tiles[0].collision = false;
            tiles[0].type = "grass";

            tiles[1] = new Tile();
            tiles[1].image = loadTileImage("water");
            tiles[1].collision = true;
            tiles[1].type = "water";

            tiles[2] = new Tile();
            tiles[2].image = loadTileImage("wall");
            tiles[2].collision = true;
            tiles[2].type = "wall";

            tiles[3] = new Tile();
            tiles[3].image = loadTileImage("exit");
            tiles[3].collision = false;
            tiles[3].type = "exit";

            for (int i = 0; i < 4; i++) {
                if (tiles[i] == null) tiles[i] = new Tile();
                if (tiles[i].image == null) {
                    createPlaceholderTile(i);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createPlaceholderTile(int index) {
        BufferedImage placeholder = new BufferedImage(
                gp.tileSize, gp.tileSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = placeholder.createGraphics();

        switch(index) {
            case 0:
                g.setColor(new Color(100, 200, 100));
                g.fillRect(0, 0, gp.tileSize, gp.tileSize);
                g.setColor(new Color(80, 180, 80));
                g.drawRect(0, 0, gp.tileSize-1, gp.tileSize-1);
                break;
            case 1:
                g.setColor(new Color(50, 150, 255));
                g.fillRect(0, 0, gp.tileSize, gp.tileSize);
                g.setColor(new Color(30, 130, 235));
                for (int i = 0; i < gp.tileSize; i += 4) {
                    g.drawLine(i, 0, i, gp.tileSize);
                }
                break;
            case 2:
                g.setColor(new Color(100, 100, 100));
                g.fillRect(0, 0, gp.tileSize, gp.tileSize);
                g.setColor(Color.DARK_GRAY);
                g.fillRect(2, 2, gp.tileSize-4, gp.tileSize-4);
                g.setColor(Color.GRAY);
                g.drawRect(0, 0, gp.tileSize-1, gp.tileSize-1);
                break;
            case 3:
                g.setColor(new Color(255, 255, 100));
                g.fillRect(0, 0, gp.tileSize, gp.tileSize);
                g.setColor(Color.YELLOW);
                g.setFont(new Font("Arial", Font.BOLD, 12));
                g.drawString("EXIT", 5, gp.tileSize/2 + 4);
                break;
        }

        g.dispose();
        tiles[index].image = placeholder;
    }

    public void generateMaze() {
        int baseSize = 15;
        int sizeIncrease = (gp.currentLevel - 1) * 5;
        int mazeSize = baseSize + sizeIncrease;

        map = new int[mazeSize][mazeSize];

        for (int r = 0; r < mazeSize; r++) {
            for (int c = 0; c < mazeSize; c++) {
                map[r][c] = 2;
            }
        }

        java.util.Random rand = new java.util.Random();
        java.util.Stack<int[]> stack = new java.util.Stack<>();

        int startRow = 1;
        int startCol = 1;
        map[startRow][startCol] = 0;

        stack.push(new int[]{startRow, startCol});
        boolean[][] visited = new boolean[mazeSize][mazeSize];
        visited[startRow][startCol] = true;

        int[][] directions = {{-2, 0}, {2, 0}, {0, -2}, {0, 2}};

        while (!stack.isEmpty()) {
            int[] current = stack.peek();
            int r = current[0];
            int c = current[1];

            java.util.ArrayList<int[]> neighbors = new java.util.ArrayList<>();
            for (int[] dir : directions) {
                int nr = r + dir[0];
                int nc = c + dir[1];

                if (nr > 0 && nr < mazeSize - 1 && nc > 0 && nc < mazeSize - 1 && !visited[nr][nc]) {
                    neighbors.add(new int[]{nr, nc, dir[0], dir[1]});
                }
            }

            if (!neighbors.isEmpty()) {
                int[] chosen = neighbors.get(rand.nextInt(neighbors.size()));
                int nr = chosen[0];
                int nc = chosen[1];
                int dr = chosen[2];
                int dc = chosen[3];

                map[r + dr/2][c + dc/2] = 0;
                map[nr][nc] = 0;

                visited[nr][nc] = true;
                stack.push(new int[]{nr, nc});
            } else {
                stack.pop();
            }
        }

        int extraPaths = gp.currentLevel * 10;
        for (int i = 0; i < extraPaths; i++) {
            int r = rand.nextInt(mazeSize - 2) + 1;
            int c = rand.nextInt(mazeSize - 2) + 1;
            if (map[r][c] == 2) {
                int pathNeighbors = 0;
                if (r > 0 && map[r-1][c] == 0) pathNeighbors++;
                if (r < mazeSize-1 && map[r+1][c] == 0) pathNeighbors++;
                if (c > 0 && map[r][c-1] == 0) pathNeighbors++;
                if (c < mazeSize-1 && map[r][c+1] == 0) pathNeighbors++;

                if (pathNeighbors >= 2) {
                    map[r][c] = 0;
                }
            }
        }

        exitRow = mazeSize - 2;
        exitCol = mazeSize - 2;
        map[exitRow][exitCol] = 3;

        ensurePathToExit(startRow, startCol, exitRow, exitCol);

        int openCells = 0;
        for (int r = 0; r < mazeSize; r++) {
            for (int c = 0; c < mazeSize; c++) {
                if (map[r][c] == 0 || map[r][c] == 3) openCells++;
            }
        }
        System.out.println("Laberinto generado: " + mazeSize + "x" + mazeSize +
                ", Celdas abiertas: " + openCells +
                ", Nivel: " + gp.currentLevel);
    }

    private void ensurePathToExit(int startRow, int startCol, int exitRow, int exitCol) {
        boolean[][] visited = new boolean[map.length][map[0].length];
        java.util.Queue<int[]> queue = new java.util.LinkedList<>();
        queue.add(new int[]{startRow, startCol});
        visited[startRow][startCol] = true;

        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        boolean pathExists = false;

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int r = current[0];
            int c = current[1];

            if (r == exitRow && c == exitCol) {
                pathExists = true;
                break;
            }

            for (int[] dir : dirs) {
                int nr = r + dir[0];
                int nc = c + dir[1];

                if (nr >= 0 && nr < map.length && nc >= 0 && nc < map[0].length) {
                    if (!visited[nr][nc] && (map[nr][nc] == 0 || map[nr][nc] == 3)) {
                        visited[nr][nc] = true;
                        queue.add(new int[]{nr, nc});
                    }
                }
            }
        }

        if (!pathExists) {
            System.out.println("Creando camino forzado a la salida...");
            int r = startRow, c = startCol;
            while (r != exitRow || c != exitCol) {
                if (r < exitRow) r++;
                else if (r > exitRow) r--;
                else if (c < exitCol) c++;
                else if (c > exitCol) c--;

                if (r >= 0 && r < map.length && c >= 0 && c < map[0].length) {
                    map[r][c] = 0;
                }
            }
        }
    }

    public void regenerateMaze() {
        generateMaze();
    }

    private BufferedImage loadTileImage(String baseName) {
        String[] extensions = {".png", ".jpg", ".jpeg", ".gif"};

        for (String ext : extensions) {
            String path = "/tiles/" + baseName + ext;
            try {
                java.io.InputStream is = getClass().getResourceAsStream(path);
                if (is != null) {
                    BufferedImage img = ImageIO.read(is);
                    is.close();
                    return img;
                }
            } catch (Exception ignored) {}
        }

        String[] searchPaths = {
                "src/res/tiles/",
                "res/tiles/",
                "tiles/",
                "src/tiles/",
                "resources/tiles/"
        };

        String cwd = System.getProperty("user.dir");
        for (String path : searchPaths) {
            for (String ext : extensions) {
                File file = new File(cwd, path + baseName + ext);
                if (file.exists()) {
                    try {
                        return ImageIO.read(file);
                    } catch (Exception ignored) {}
                }
            }
        }

        return null;
    }

    public void draw(Graphics2D g2) {
        int screenCols = gp.screenWidth / gp.tileSize;
        int screenRows = gp.screenHeight / gp.tileSize;

        int mapWidth = map[0].length * gp.tileSize;
        int mapHeight = map.length * gp.tileSize;
        int offsetX = (gp.screenWidth - mapWidth) / 2;
        int offsetY = (gp.screenHeight - mapHeight) / 2;

        g2.setColor(new Color(50, 50, 50));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[0].length; col++) {
                int tileNum = map[row][col];
                if (tileNum >= 0 && tileNum < tiles.length && tiles[tileNum] != null) {
                    int x = offsetX + col * gp.tileSize;
                    int y = offsetY + row * gp.tileSize;

                    if (tiles[tileNum].image != null) {
                        g2.drawImage(tiles[tileNum].image, x, y, gp.tileSize, gp.tileSize, null);
                    }
                }
            }
        }
    }

    public boolean isCollision(int row, int col) {
        if (row < 0 || row >= map.length || col < 0 || col >= map[0].length) {
            return true;
        }
        int tileNum = map[row][col];
        return tileNum >= 0 && tileNum < tiles.length && tiles[tileNum] != null && tiles[tileNum].collision;
    }

    public int getTileAt(int row, int col) {
        if (row < 0 || row >= map.length || col < 0 || col >= map[0].length) {
            return -1;
        }
        return map[row][col];
    }
}