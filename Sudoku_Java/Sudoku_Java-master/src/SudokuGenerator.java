import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Prosta klasa do wygenerowania planszy Sudoku:
 * 1) Najpierw tworzy pełne, poprawne rozwiązanie (backtracking z losową kolejnością prób).
 * 2) Potem usuwa określoną liczbę pól, żeby zostało "puzzle" do rozwiązania.
 */
public class SudokuGenerator {

    private final int[][] board;
    private final Random rand;

    public SudokuGenerator() {
        board = new int[9][9];
        rand = new Random();
    }

    /**
     * Zwraca wygenerowaną planszę 9×9 z wartościami 0..9, gdzie 0 oznacza puste pole.
     */
    public int[][] generatePuzzle(int removals) {
        // 1) Najpierw wygeneruj pełne rozwiązanie
        fillBoard();
        // 2) Teraz usuń dokładnie `removals` pól (ustawiając je na 0)
        removeCells(removals);
        return board;
    }

    // Krok 1: Fill całej planszy
    private boolean fillBoard() {
        // Znajdź pierwsze puste pole (0)
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == 0) {
                    // Losowa kolejność prób od 1 do 9
                    ArrayList<Integer> nums = new ArrayList<>();
                    for (int i = 1; i <= 9; i++) nums.add(i);
                    Collections.shuffle(nums, rand);

                    for (int candidate : nums) {
                        if (isValidPlacement(row, col, candidate)) {
                            board[row][col] = candidate;
                            if (fillBoard()) {
                                return true;
                            }
                            // backtrack
                            board[row][col] = 0;
                        }
                    }
                    // jeśli żaden candidate nie działa → backtrack
                    return false;
                }
            }
        }
        // Cała plansza wypełniona poprawnie
        return true;
    }

    // Sprawdza, czy można wstawić k w board[row][col]
    private boolean isValidPlacement(int row, int col, int k) {
        // 1) w wierszu
        for (int c = 0; c < 9; c++) {
            if (board[row][c] == k) return false;
        }
        // 2) w kolumnie
        for (int r = 0; r < 9; r++) {
            if (board[r][col] == k) return false;
        }
        // 3) w kwadracie 3×3
        int boxRowStart = (row / 3) * 3;
        int boxColStart = (col / 3) * 3;
        for (int r = boxRowStart; r < boxRowStart + 3; r++) {
            for (int c = boxColStart; c < boxColStart + 3; c++) {
                if (board[r][c] == k) return false;
            }
        }
        return true;
    }

    // Krok 2: usuń `removals` losowych pól
    private void removeCells(int removals) {
        int count = 0;
        while (count < removals) {
            int r = rand.nextInt(9);
            int c = rand.nextInt(9);
            if (board[r][c] != 0) {
                board[r][c] = 0;
                count++;
            }
        }
    }
}
