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
    private BufferedImage loadImage(String resourcePath, String diskRelativePath) {
        try {
            java.io.InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                is.close();
                return img;
            }
        } catch (Exception ignored) {
        }

        try {
            java.io.File fileFromCwd = new java.io.File(System.getProperty("user.dir"), diskRelativePath.replace('/', java.io.File.separatorChar));
            if (fileFromCwd.exists()) {
                return ImageIO.read(fileFromCwd);
            }
            java.io.File fileDirect = new java.io.File(diskRelativePath);
            if (fileDirect.exists()) {
                return ImageIO.read(fileDirect);
            }
        } catch (Exception e) {
        }

        System.err.println("Resource not found (classpath & disk): " + resourcePath + "  (tried disk: " + diskRelativePath + ")");
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
