import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        Sudoku sudoku = new Sudoku();
        while (sudoku.running())
        {
            try {
                sudoku.update();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sudoku.render();
        }
    }
}
