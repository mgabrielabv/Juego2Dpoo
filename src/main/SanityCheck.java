package main;
import java.io.FileWriter;
public class SanityCheck {
    public static void main(String[] args) throws Exception {
        System.out.println("SANITY: stdout visible");
        System.err.println("SANITY: stderr visible");
        try (FileWriter fw = new FileWriter("sanity_out.txt")) {
            fw.write("sanity file\n");
        }
    }
}
