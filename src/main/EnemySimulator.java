package main;

import entity.Enemy;
import entity.Player;

public class EnemySimulator {
    public static void main(String[] args) throws Exception {
        GamePanel gp = new GamePanel();
        Player player = gp.player;

        java.util.List<Enemy> enemies = gp.getEnemies();

        for (int i = 0; i < enemies.size(); i++) {
            Enemy e = enemies.get(i);
            int col = gp.getTileManager().screenToMapCol(e.x + gp.tileSize/2);
            int row = gp.getTileManager().screenToMapRow(e.y + gp.tileSize/2);
        }

        int ticks = 30;
        for (int t = 0; t < ticks; t++) {
            if (t == 5 && enemies.size()>0) {
                Enemy target = enemies.get(0);
                int eCol = gp.getTileManager().screenToMapCol(target.x + gp.tileSize/2);
                int eRow = gp.getTileManager().screenToMapRow(target.y + gp.tileSize/2);
                int newPlayerCol = Math.max(0, eCol - 2);
                int newPlayerRow = eRow;
                int ox = gp.getTileManager().getOffsetX();
                int oy = gp.getTileManager().getOffsetY();
                player.x = ox + newPlayerCol * gp.tileSize;
                player.y = oy + newPlayerRow * gp.tileSize;
            }

            for (Enemy e : enemies) {
                e.update(player);
            }

            try { Thread.sleep(20); } catch (InterruptedException ignored) {}
        }
    }
}
