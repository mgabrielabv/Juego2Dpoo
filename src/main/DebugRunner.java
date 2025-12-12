package main;

import java.io.FileWriter;

public class DebugRunner {
    public static void main(String[] args) {
        GamePanel gp = new GamePanel();
        String out;
        if (gp.tileM != null && gp.tileM.map != null) {
            int rows = gp.tileM.map.length;
            int cols = gp.tileM.map[0].length;
            int open = 0;
            for (int r=0;r<rows;r++) for (int c=0;c<cols;c++) if (gp.tileM.map[r][c]==0) open++;
            out = "DebugRunner: map="+rows+"x"+cols+" openCells="+open;
        } else {
            out = "DebugRunner: tileM or map is null";
        }
        try (FileWriter fw = new FileWriter("src/debug_map.txt")){
            fw.write(out + System.lineSeparator());
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
