import java.util.ArrayList;
import java.util.stream.IntStream;

public class Solver
{
    private int[][] numbers;
    private ArrayList<ArrayList<ArrayList<Integer>>> candidates;

    public Solver(int[][] numbers)
    {
        this.numbers = numbers;
    }

    private boolean existInSquare(int y, int x, int k)
    {
        int a = 0, b = 0;
        if (x < 3)
            b = 0;
        else if (x >= 3 && x < 6)
            b = 3;
        else if (x >= 6)
            b = 6;

        if (y < 3)
            a = 0;
        else if (y >= 3 && y < 6)
            a = 3;
        else if (y >= 6)
            a = 6;

        for (int i = a;i < a + 3;i++)
            for (int j = b;j < b + 3;j++)
                if (numbers[i][j] == k)
                    return true;
        return false;
    }

    public boolean solve(int[][] board)
    {
        for (int i = 0; i < 9; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                if (board[i][j] == 0)
                {
                    for (int k = 1; k <= 9; k++)
                    {
                        board[i][j] = k;
                        if (isValid(board, i, j) && solve(board))
                        {
                            return true;
                        }
                        board[i][j] = 0;
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isValid(int[][] board, int row, int column)
    {
        return (rowConstraint(board, row)
                && columnConstraint(board, column)
                && subsectionConstraint(board, row, column));
    }

    private boolean rowConstraint(int[][] board, int row)
    {
        boolean[] constraint = new boolean[9];
        return IntStream.range(0, 9)
                .allMatch(column -> checkConstraint(board, row, constraint, column));
    }

    private boolean columnConstraint(int[][] board, int column)
    {
        boolean[] constraint = new boolean[9];
        return IntStream.range(0, 9)
                .allMatch(row -> checkConstraint(board, row, constraint, column));
    }

    private boolean subsectionConstraint(int[][] board, int row, int column)
    {
        boolean[] constraint = new boolean[9];
        int subsectionRowStart = (row / 3) * 3;
        int subsectionRowEnd = subsectionRowStart + 3;

        int subsectionColumnStart = (column / 3) * 3;
        int subsectionColumnEnd = subsectionColumnStart + 3;

        for (int r = subsectionRowStart; r < subsectionRowEnd; r++) {
            for (int c = subsectionColumnStart; c < subsectionColumnEnd; c++) {
                if (!checkConstraint(board, r, constraint, c)) return false;
            }
        }
        return true;
    }

    boolean checkConstraint(int[][] board, int row, boolean[] constraint, int column)
    {
        if (board[row][column] != 0)
        {
            if (!constraint[board[row][column] - 1])
            {
                constraint[board[row][column] - 1] = true;
            }
            else
            {
                return false;
            }
        }
        return true;
    }

    public int[][] returnSolution()
    {
        solve(numbers);
        return numbers;
    }

    private void findCandidate()
    {
        ArrayList<ArrayList<ArrayList<Integer>>> tab = new ArrayList<>(9);

        for(int i = 0;i<9;i++)
        {
            tab.add(new ArrayList<ArrayList<Integer>>(9));
            for (int j = 0; j < 9; j++) {
                tab.get(i).add(new ArrayList<Integer>());
            }
        }

        for (int i = 0;i < 9;i++)
        {
            for (int j = 0;j < 9;j++)
            {
                if (numbers[i][j] != 0)
                    tab.get(i).get(j).add(10);
                else
                {
                    for (int k = 1;k <= 9;k++)
                    {
                        boolean exist = false;
                        for (int x = 0;x < 9;x++)
                        {
                            if (k == numbers[i][x])
                            {
                                exist = true;
                                break;
                            }
                        }
                        for (int x = 0;x < 9;x++)
                        {
                            if (k == numbers[x][j])
                            {
                                exist = true;
                                break;
                            }
                        }
                        if (existInSquare(i, j, k))
                            exist = true;

                        if (!exist)
                            tab.get(i).get(j).add(k);
                    }
                }
            }
        }
        candidates = tab;
    }

    public ArrayList<ArrayList<ArrayList<Integer>>> returnCandidates()
    {
        findCandidate();
        return candidates;
    }
}
