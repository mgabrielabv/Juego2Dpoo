package entity;

import main.GamePanel;
import main.KeyHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Player extends Entity {

    GamePanel gp;
    KeyHandler keyH;
    public int lives = 3;

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;
        setDefaultValues();
        getPlayerImage();
    }

    public void setDefaultValues() {
        x = 100;
        y = 100;
        try {
            int[][] map = (gp != null) ? gp.getMap() : null;
            if (map != null) {
                outer:
                for (int r = 0; r < map.length; r++) {
                    for (int c = 0; c < map[0].length; c++) {
                        if (map[r][c] == 0) {
                            int ox = gp.getTileManager().getOffsetX();
                            int oy = gp.getTileManager().getOffsetY();
                            x = ox + c * gp.tileSize;
                            y = oy + r * gp.tileSize;
                            break outer;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        speed = 4;
        direction="down";

    }

    public void setToStart() {
        try {
            int[][] map = (gp != null) ? gp.getMap() : null;
            if (map != null) {
                for (int r = 0; r < map.length; r++) {
                    for (int c = 0; c < map[0].length; c++) {
                        if (map[r][c] == 0) {
                            int ox = gp.getTileManager().getOffsetX();
                            int oy = gp.getTileManager().getOffsetY();
                            x = ox + c * gp.tileSize;
                            y = oy + r * gp.tileSize;
                            direction = "down";
                            return;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    public void getPlayerImage(){
    try {
        up1 = loadImage("/player/up1.png", "src/res/player/up1.png");
        up2 = loadImage("/player/up2.png", "src/res/player/up2.png");
        down1 = loadImage("/player/down1.png", "src/res/player/down1.png");
        down2 = loadImage("/player/down2.png", "src/res/player/down2.png");
        left1 = loadImage("/player/left1.png", "src/res/player/left1.png");
        left2 = loadImage("/player/left2.png", "src/res/player/left2.png");
        right1 = loadImage("/player/right1.png", "src/res/player/right1.png");
        right2 = loadImage("/player/right2.png", "src/res/player/right2.png");

    } catch (Exception e) {
        e.printStackTrace();
    }
    }

    private BufferedImage loadImage(String resourcePath, String diskRelativePath) {
        try {
            java.io.InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                is.close();
                return img;
            }
            java.net.URL url = getClass().getResource(resourcePath);
            if (url != null) {
                return ImageIO.read(url);
            }
        } catch (Exception ignored) {
        }

        String cwd = System.getProperty("user.dir");
        String[] candidates = new String[] {
                diskRelativePath,
                "src/res/player/" + resourcePath.substring(resourcePath.lastIndexOf('/')+1),
                "res/player/" + resourcePath.substring(resourcePath.lastIndexOf('/')+1),
                "src/" + resourcePath.substring(1),
                "res/" + resourcePath.substring(1),
                "player/" + resourcePath.substring(resourcePath.lastIndexOf('/')+1),
                "out/production/2Djavavideogame/player/" + resourcePath.substring(resourcePath.lastIndexOf('/')+1),
                "out/production/2Djavavideogame/res/player/" + resourcePath.substring(resourcePath.lastIndexOf('/')+1)
        };

        for (String candidate : candidates) {
            try {
                java.io.File f = new java.io.File(cwd, candidate.replace('/', java.io.File.separatorChar));
                if (f.exists()) {
                    return ImageIO.read(f);
                }
            } catch (Exception ignored) {}
        }

        String[] searchRelatives = new String[] {"src/res/player/","res/player/","player/"};
        java.io.File dir = new java.io.File(cwd);
        while (dir != null) {
            for (String relBase : searchRelatives) {
                java.io.File tryFile = new java.io.File(dir, relBase + resourcePath.substring(resourcePath.lastIndexOf('/')+1));
                try {
                    if (tryFile.exists()) {
                        return ImageIO.read(tryFile);
                    }
                } catch (Exception ignored) {}
            }
            dir = dir.getParentFile();
        }

        System.err.println("Resource not found (classpath & disk): " + resourcePath + "  (tried cwd='" + cwd + "')");
        return null;
    }

    private boolean canMoveTo(int newX, int newY) {
        int[][] corners = {
                {newX, newY},
                {newX + gp.tileSize - 1, newY},
                {newX, newY + gp.tileSize - 1},
                {newX + gp.tileSize - 1, newY + gp.tileSize - 1}
        };

        for (int[] corner : corners) {
            int mapCol = gp.getTileManager().screenToMapCol(corner[0]);
            int mapRow = gp.getTileManager().screenToMapRow(corner[1]);
            if (mapRow < 0 || mapRow >= gp.getMap().length || mapCol < 0 || mapCol >= gp.getMap()[0].length) return false;
            if (gp.getTileManager().isCollision(mapRow, mapCol)) return false;
        }
        return true;
    }

    public void update() {

        int newX = x;
        int newY = y;

        if (keyH.upPressed)
        {
            direction="up";
            newY -= speed;
        }
        else if (keyH.downPressed)
        {
            direction="down";
            newY += speed;
        }
        else if (keyH.leftPressed)
        {
            direction="left";
            newX -= speed;
        }
        else if (keyH.rightPressed)
        {
            direction="right";
            newX += speed;
        }

        boolean canMove = canMoveTo(newX, newY);

        if (canMove) {
            x = newX;
            y = newY;
        }

        spriteCounter++;
        if (spriteCounter>10){
            if (spriteNum==1){
                spriteNum=2;
            }
            else if (spriteNum==2){
                spriteNum=1;
            }
            spriteCounter=0;
        }
    }
    public void draw(Graphics2D g2){
        BufferedImage image=null;
        switch (direction){
            case"up":
                if (spriteNum==1){
                    image=up1;
                }
                if (spriteNum==2){
                    image=up2;
                }
                break;
            case"down":
                if (spriteNum==1){
                    image=down1;
                }
                if (spriteNum==2){
                    image=down2;
                }
                break;
            case"left":
                if (spriteNum==1){
                    image=left1;
                }
                if (spriteNum==2){
                    image=left2;
                }
                break;
            case"right":
                if (spriteNum==1){
                    image=right1;
                }
                if (spriteNum==2){
                    image=right2;
                }
                break;
        }
        if (image != null) {
            g2.drawImage(image, x, y, gp.tileSize, gp.tileSize, null);
        } else {
            g2.setColor(Color.MAGENTA);
            g2.fillRect(x, y, gp.tileSize, gp.tileSize);
            g2.setColor(Color.BLACK);
            g2.drawString("P", x + gp.tileSize/2 - 4, y + gp.tileSize/2 + 4);
        }
    }

    public void reset() {
        lives = 3;
        setToStart();
        direction = "down";
        spriteNum = 1;
        spriteCounter = 0;
    }

    public void takeDamage() {
        lives--;
        if (lives <= 0) {
            gp.gameOver();
        } else {
            setToStart();
        }
    }
}
