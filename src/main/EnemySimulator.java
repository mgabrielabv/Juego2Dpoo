package main;

import entity.Enemy;
import entity.Player;

public class EnemySimulator {
    public static void main(String[] args) throws Exception {
        GamePanel gp = new GamePanel();
        Player player = gp.player;

        System.out.println("Simulator: screen=" + gp.screenWidth + "x" + gp.screenHeight + " tileSize=" + gp.tileSize);

        java.util.List<Enemy> enemies = gp.getEnemies();
        System.out.println("Simulator: enemies count=" + enemies.size());

        // Mostrar posiciones iniciales (tiles)
        for (int i = 0; i < enemies.size(); i++) {
            Enemy e = enemies.get(i);
            int col = gp.getTileManager().screenToMapCol(e.x + gp.tileSize/2);
            int row = gp.getTileManager().screenToMapRow(e.y + gp.tileSize/2);
            System.out.println("Enemy#" + i + " initial tile=(" + col + "," + row + ") pixel=(" + e.x + "," + e.y + ")");
        }

        // Simulación: después de algunos ticks, colocamos el jugador cerca del primer enemigo
        int ticks = 30;
        for (int t = 0; t < ticks; t++) {
            if (t == 5 && enemies.size()>0) {
                Enemy target = enemies.get(0);
                int eCol = gp.getTileManager().screenToMapCol(target.x + gp.tileSize/2);
                int eRow = gp.getTileManager().screenToMapRow(target.y + gp.tileSize/2);
                // colocar jugador a 2 celdas del enemigo
                int newPlayerCol = Math.max(0, eCol - 2);
                int newPlayerRow = eRow;
                int ox = gp.getTileManager().getOffsetX();
                int oy = gp.getTileManager().getOffsetY();
                player.x = ox + newPlayerCol * gp.tileSize;
                player.y = oy + newPlayerRow * gp.tileSize;
                System.out.println("Simulator: moved player to tile=("+newPlayerCol+","+newPlayerRow+") pixel=("+player.x+","+player.y+") on tick="+t);
            }

            // actualizar todos los enemigos (simular frames)
            for (Enemy e : enemies) {
                e.update(player);
            }

            // breve pausa para que los mensajes no se amontonen (no necesaria)
            try { Thread.sleep(20); } catch (InterruptedException ignored) {}
        }

        System.out.println("Simulator: done");
    }
}

