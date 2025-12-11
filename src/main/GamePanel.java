package main;
import javax.swing.JPanel;
import java.awt.*;
import entity.Player;
import tile.TileManager;

public class GamePanel extends JPanel implements Runnable  {

        final int originalTileSize=16; //16x16 tile
        final int scale=3;

        public final int tileSize=originalTileSize*scale; //48x48 tile
        final int maxScreenCol=16;
        final int maxScreenRow=12;
        final int screenwidth=tileSize*maxScreenCol; //768 pixels
        final int screenheight=tileSize*maxScreenRow; //576 pixels
        int FPS=60;

    TileManager tileM=new  TileManager(this);
    KeyHandler keyH=new KeyHandler();
    Thread gameThread;
    Player player=new Player(this,keyH);



    public GamePanel(){

    this.setPreferredSize(new Dimension(screenwidth, screenheight));
    this.setBackground(Color.black);
    this.setDoubleBuffered(true);
    this.addKeyListener(keyH);
    this.setFocusable(true);
    }
    public void startGameThread(){
        gameThread=new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;
        
        while (gameThread != null) {

            Update();


            repaint();
            try {
                double remainingTime = nextDrawTime - System.nanoTime();
                remainingTime = remainingTime / 1000000;

                if (remainingTime < 0) {
                    remainingTime = 0;
                }

                Thread.sleep((long) remainingTime);
                nextDrawTime += drawInterval;

            } catch (InterruptedException e) {
                e.printStackTrace();

            }

        }
    }
        public void Update(){
        player.update();
        };


        public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)g;
        tileM.draw(g2);
        player.draw(g2);
        g2.dispose();

    }
    }

