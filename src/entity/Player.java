package entity;

import main.GamePanel;
import main.KeyHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Player extends Entity {

    GamePanel gp;
    KeyHandler keyH;



    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;
        setDefaultValues();
        getPlayerImage();
    }

    public void setDefaultValues() {
        x = 100;
        y = 100;
        speed = 4;
        direction="down";

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
    // Helper: try classpath resource, then try multiple disk locations and searching parent dirs
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

        // 2) try several likely disk locations relative to working directory
        String cwd = System.getProperty("user.dir");
        String[] candidates = new String[] {
                diskRelativePath,
                // common project layouts
                "src/res/player/" + resourcePath.substring(resourcePath.lastIndexOf('/')+1),
                "res/player/" + resourcePath.substring(resourcePath.lastIndexOf('/')+1),
                "src/" + resourcePath.substring(1),
                "res/" + resourcePath.substring(1),
                "player/" + resourcePath.substring(resourcePath.lastIndexOf('/')+1),
                // IntelliJ output location
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

        // 3) search upwards from cwd for the relative path (covers running from subfolders)
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

    public void update() {

        if (keyH.upPressed)
        {
            direction="up";
            y -= speed;
        }
        else if (keyH.downPressed)
        {
            direction="down";
            y += speed;
        }
        else if (keyH.leftPressed)
        {
            direction="left";
            x -= speed;
        }
        else if (keyH.rightPressed)
        {
            direction="right";
            x += speed;
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
        g2.drawImage(image, x, y, gp.tileSize, gp.tileSize, null);
    }
}
