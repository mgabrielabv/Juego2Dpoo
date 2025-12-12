package main;
import java.io.FileWriter;
public class SanityCheck {
    public static void main(String[] args) throws Exception {
        try (FileWriter fw = new FileWriter("sanity_out.txt")) {
            fw.write("sanity file\n");
        }
    }
}
