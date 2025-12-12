package tile;

import main.GamePanel;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.util.Random;
import java.util.Stack;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class TileManager {
    GamePanel gp;
    private final Tile[] tiles;
    public int[][] map;
    public int exitRow, exitCol;

    private static final long MAZE_SEED = 12345L;

    public TileManager(GamePanel gp) {
        this.gp = gp;
        tiles = new Tile[10];
        getTileImage();
        generateMaze();
    }

    public int getOffsetX() {
        return 0;
    }

    public int getOffsetY() {
        return 0;
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

            tiles[4] = new Tile();
            tiles[4].image = loadTileImage("key");
            tiles[4].collision = false;
            tiles[4].type = "key";

            tiles[5] = new Tile();
            tiles[5].image = loadTileImage("door");
            tiles[5].collision = false;
            tiles[5].type = "door";

            for (int i = 0; i < 6; i++) {
                if (tiles[i] == null) tiles[i] = new Tile();
                if (tiles[i].image == null) {
                    createPlaceholderTile(i);
                }
            }

        } catch (Exception e) {
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
            case 4:
                g.setColor(new Color(200, 180, 120));
                g.fillRect(0, 0, gp.tileSize, gp.tileSize);
                g.setColor(new Color(180, 150, 60));
                g.fillOval(gp.tileSize/4, gp.tileSize/4, gp.tileSize/2, gp.tileSize/2);
                g.setColor(Color.BLACK);
                g.drawLine(gp.tileSize/2, gp.tileSize/2, gp.tileSize - 4, gp.tileSize/2);
                g.drawString("KEY", 4, gp.tileSize - 4);
                break;
            case 5:
                g.setColor(new Color(120, 80, 40));
                g.fillRect(0, 0, gp.tileSize, gp.tileSize);
                g.setColor(new Color(90, 60, 30));
                g.fillRect(2, 2, gp.tileSize-4, gp.tileSize-4);
                g.setColor(Color.YELLOW);
                g.fillOval(gp.tileSize - 8, gp.tileSize/2 - 4, 6, 6);
                g.setColor(Color.WHITE);
                g.drawString("DOOR", 2, gp.tileSize - 4);
                break;
        }
        g.dispose();
        tiles[index].image = placeholder;
    }

    public void generateMaze() {
        int mazeRows = gp.maxScreenRow;
        int mazeCols = gp.maxScreenCol;

        if (gp.currentLevel == 2) {
            map = new int[][] {
                {2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2},
                {2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2},
                {2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2},
                {2,0,2,2,0,2,2,0,2,2,0,2,2,0,0,2},
                {2,0,2,2,0,2,2,0,2,2,0,2,2,0,0,2},
                {2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2},
                {2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2},
                {2,0,2,2,0,2,2,0,2,2,0,2,2,0,0,2},
                {2,0,2,2,0,2,2,0,2,2,0,2,2,0,0,2},
                {2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2},
                {2,0,0,0,0,0,0,0,0,0,0,0,0,5,0,2},
                {2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2}
            };
            exitRow = 10;
            exitCol = 13;
            map[exitRow][exitCol] = 5;
            map[1][1] = 0;
            map[1][2] = 0;
            map[2][1] = 0;
            map[exitRow][exitCol-1] = 0;
            map[exitRow-1][exitCol] = 0;
            return;
        }

        map = new int[mazeRows][mazeCols];

        for (int r = 0; r < mazeRows; r++) {
            for (int c = 0; c < mazeCols; c++) {
                map[r][c] = 2;
            }
        }

        Random rand = new Random(MAZE_SEED + gp.currentLevel);
        Stack<int[]> stack = new Stack<>();

        int startRow = 1;
        int startCol = 1;
        map[startRow][startCol] = 0;

        stack.push(new int[]{startRow, startCol});
        boolean[][] visited = new boolean[mazeRows][mazeCols];
        visited[startRow][startCol] = true;

        int[][] directions = {{-2, 0}, {2, 0}, {0, -2}, {0, 2}};

        while (!stack.isEmpty()) {
            int[] current = stack.peek();
            int r = current[0];
            int c = current[1];

            ArrayList<int[]> neighbors = new ArrayList<>();
            for (int[] dir : directions) {
                int nr = r + dir[0];
                int nc = c + dir[1];

                if (nr > 0 && nr < mazeRows - 1 && nc > 0 && nc < mazeCols - 1 && !visited[nr][nc]) {
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

        map[2][1] = 0;
        map[1][2] = 0;

        exitRow = mazeRows - 2;
        exitCol = mazeCols - 2;
        map[exitRow][exitCol] = (gp.currentLevel == 1) ? 4 : 5;

        ensurePathToExit(startRow, startCol, exitRow, exitCol);

        map[1][1] = 0;
        map[exitRow][exitCol] = (gp.currentLevel == 1) ? 4 : 5;
    }

    private void ensurePathToExit(int startRow, int startCol, int exitRow, int exitCol) {
        boolean[][] visited = new boolean[map.length][map[0].length];
        Queue<int[]> queue = new LinkedList<>();
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
                    if (!visited[nr][nc] && (map[nr][nc] == 0 || map[nr][nc] == 3 || map[nr][nc] == 4 || map[nr][nc] == 5)) {
                        visited[nr][nc] = true;
                        queue.add(new int[]{nr, nc});
                    }
                }
            }
        }

        if (!pathExists) {
            int r = startRow, c = startCol;
            while (r != exitRow || c != exitCol) {
                if (r < exitRow) r++;
                else if (r > exitRow) r--;
                else if (c < exitCol) c++;
                else if (c > exitCol) c--;

                if ((r != exitRow || c != exitCol) && (r != startRow || c != startCol)) {
                    map[r][c] = 0;
                }
            }
        }
    }

    public void regenerateMaze() {
        generateMaze();
    }

    private java.awt.image.BufferedImage loadTileImage(String name) {
        try {
            String[] exts = {".png", ".jpg"};
            java.awt.image.BufferedImage img = null;
            for (String ext : exts) {
                String path = "/res/tiles/" + name + ext;
                try {
                    img = javax.imageio.ImageIO.read(getClass().getResourceAsStream(path));
                    if (img != null) {
                        break;
                    }
                } catch (Exception e) {
                }
            }
            if (img == null) {
            }
            return img;
        } catch (Exception e) {
            return null;
        }
    }

    public void draw(Graphics2D g2) {
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

    public int getTileAt(int row, int col) {
        if (map != null && row >= 0 && row < map.length && col >= 0 && col < map[0].length) {
            return map[row][col];
        }
        return -1;
    }

    public boolean isCollision(int row, int col) {
        int tileNum = getTileAt(row, col);
        if (tileNum >= 0 && tileNum < tiles.length && tiles[tileNum] != null) {
            return tiles[tileNum].collision;
        }
        return true;
    }
}
