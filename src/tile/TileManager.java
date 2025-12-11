package tile;
import main.GamePanel;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.io.File;

public class TileManager {
    GamePanel gp;
    tile[] tile;

    public TileManager(GamePanel gp){
        this.gp=gp;
        tile=new tile[10];
        getTileImage();
    }
    public void getTileImage(){
        try {
            tile[0] = new tile();
            tile[0].image = loadTileImage("grass");

            tile[1] = new tile();
            tile[1].image = loadTileImage("water");
            tile[1].collision = true;

            tile[2] = new tile();
            tile[2].image = loadTileImage("wall");
        }
        catch (Exception e){
            e.printStackTrace();
        }

        // Ensure none of the used tiles are null (use placeholders)
        for (int i=0;i<=2;i++){
            if (tile[i] == null) tile[i] = new tile();
            if (tile[i].image == null){
                BufferedImage placeholder = new BufferedImage(gp.tileSize, gp.tileSize, BufferedImage.TYPE_INT_ARGB);
                java.awt.Graphics2D g = placeholder.createGraphics();
                g.setColor(i==1? Color.CYAN : Color.GRAY); // water cyan, others gray
                g.fillRect(0,0,gp.tileSize,gp.tileSize);
                g.setColor(Color.BLACK);
                g.drawString("T"+(i), gp.tileSize/2-4, gp.tileSize/2+4);
                g.dispose();
                tile[i].image = placeholder;
            }
        }
    }

    // Try classpath (png/jpg), then common disk locations, then parent dirs; return null if not found
    private BufferedImage loadTileImage(String baseName){
        String[] exts = new String[]{".png", ".jpg", ".jpeg"};
        // 1) classpath
        for (String ext : exts){
            String rp = "/tiles/" + baseName + ext;
            try {
                java.io.InputStream is = getClass().getResourceAsStream(rp);
                if (is != null){
                    BufferedImage img = ImageIO.read(is);
                    is.close();
                    return img;
                }
                java.net.URL url = getClass().getResource(rp);
                if (url != null){
                    return ImageIO.read(url);
                }
            } catch (Exception ignored){}
        }

        // 2) disk candidates
        String cwd = System.getProperty("user.dir");
        String[] candidates = new String[]{
                "src/res/tiles/" + baseName + ".png",
                "src/res/tiles/" + baseName + ".jpg",
                "res/tiles/" + baseName + ".png",
                "res/tiles/" + baseName + ".jpg",
                "src/tiles/" + baseName + ".png",
                "src/tiles/" + baseName + ".jpg",
                "tiles/" + baseName + ".png",
                "tiles/" + baseName + ".jpg",
                "out/production/2Djavavideogame/tiles/" + baseName + ".png",
                "out/production/2Djavavideogame/tiles/" + baseName + ".jpg"
        };

        for (String c : candidates){
            try{
                File f = new File(cwd, c.replace('/', File.separatorChar));
                if (f.exists()){
                    return ImageIO.read(f);
                }
            } catch (Exception ignored){}
        }

        // 3) search upward for src/res/tiles or res/tiles
        File dir = new File(cwd);
        while (dir != null){
            for (String rel : new String[]{"src/res/tiles/","res/tiles/","tiles/"}){
                for (String ext : new String[]{".png", ".jpg"}){
                    File f = new File(dir, rel + baseName + ext);
                    try{
                        if (f.exists()) return ImageIO.read(f);
                    } catch (Exception ignored){}
                }
            }
            dir = dir.getParentFile();
        }

        System.err.println("Tile image not found: " + baseName + " (tried classpath and disk)");
        return null;
    }
    public void draw(java.awt.Graphics2D g2){
        // Fill the whole panel with the grass tile (tile[0])
        if (tile[0] != null && tile[0].image != null) {
            int maxCol = Math.max(1, gp.getWidth() / gp.tileSize);
            int maxRow = Math.max(1, gp.getHeight() / gp.tileSize);
            for (int col = 0; col < maxCol; col++){
                for (int row = 0; row < maxRow; row++){
                    int x = col * gp.tileSize;
                    int y = row * gp.tileSize;
                    g2.drawImage(tile[0].image, x, y, gp.tileSize, gp.tileSize, null);
                }
            }
        } else {
            // fallback: fill background with a green rect if grass image missing
            g2.setColor(new java.awt.Color(50,150,50));
            g2.fillRect(0,0,gp.getWidth(), gp.getHeight());
        }

        // Optional: draw other sample tiles at the top for debugging (commented out)
        // g2.drawImage(tile[1].image,48,0,gp.tileSize,gp.tileSize,null);
        // g2.drawImage(tile[2].image,96,0,gp.tileSize,gp.tileSize,null);


            }
        }
