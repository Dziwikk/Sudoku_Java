import org.jsfml.graphics.*;
import org.jsfml.system.Vector2f;
import org.jsfml.window.Keyboard;
import org.jsfml.window.Mouse;
import org.jsfml.window.VideoMode;
import org.jsfml.window.event.Event;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Sudoku
{
    private RenderWindow window;
    private Vector2f mousePosition;
    private boolean pressed = false;

    // Shapes
    private RectangleShape board;
    private RectangleShape resetButton;
    private RectangleShape solveButton;
    private RectangleShape hintButton;       // ** ZMIANA: wcześniej saveButton
    private RectangleShape loadButton;
    private RectangleShape checkBox;
    private RectangleShape[][] box;
    private RectangleShape logo;

    // Textures
    private Texture boardTexture;
    private Texture boxTextures[];
    private Color greyColor;
    private Texture resetTexture[];
    private Texture solveTexture[];
    private Texture hintTexture[];          // ** ZMIANA: zamiast saveTexture
    private Texture loadTexture[];
    private Texture checkBoxTexture[];
    private Texture logoTexture;

    // Logic
    private int[][] numbers;                // aktualny stan gry (z lukami i wpisanymi cyframi)
    private int[][] solutionCache;          // pełne rozwiązanie wygenerowanej planszy
    private int nextHintRow = -1;           // wskaźnik na następne puste pole do hinta
    private int nextHintCol = -1;

    private int checkedX = -1, checkedY = -1;
    private boolean showCand = false;
    private ArrayList<ArrayList<ArrayList<RectangleShape>>> candBox;
    private ArrayList<ArrayList<ArrayList<Integer>>> candidates;

    private boolean checkedCand[][][];

    private Texture candTexture[];
    private Texture checkedCandTexture[];

    private Font font;
    private Text text;

    // ** NOWE ** – pola dla informacji o wygranej
    private boolean hasWon = false;
    private Text winText;

    /**
     * Konstruktor – inicjalizuje GUI i od razu generuje pierwszy poziom (40 usuniętych pól).
     */
    public Sudoku()
    {
        initWindow();
        initNumbers();
        try
        {
            initBoard();
            initCandidates();
            initBoxes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // ** NOWE ** – po załadowaniu fontu i inicjalizacji GUI tworzymy tekst wyświetlany po ukończeniu gry
        initWinText();

        // Generujemy pierwszą losową łamigłówkę
        generateNewPuzzle(40);
    }

    private void initWindow()
    {
        window = new RenderWindow(new VideoMode(1000,700), "Sudoku solver");
        window.setFramerateLimit(60);
    }

    private void initNumbers()
    {
        numbers = new int[9][9];
        solutionCache = new int[9][9];
        for (int i = 0;i<9;i++)
            for (int j = 0;j<9;j++)
                numbers[i][j] = 0;
    }

    private void updateNumbers()
    {
        for (int i = 0;i < 9;i++)
        {
            for (int j = 0;j < 9;j++)
            {
                box[i][j].setTexture(boxTextures[numbers[i][j]]);
            }
        }
    }

    private void initBoard() throws IOException
    {
        // --- Board ---
        board = new RectangleShape();
        boardTexture = new Texture();

        resetButton = new RectangleShape();
        resetTexture = new Texture[2];
        resetTexture[0] = new Texture();
        resetTexture[1] = new Texture();

        solveButton = new RectangleShape();
        solveTexture = new Texture[2];
        solveTexture[0] = new Texture();
        solveTexture[1] = new Texture();

        hintButton = new RectangleShape();
        hintTexture = new Texture[2];
        hintTexture[0] = new Texture();
        hintTexture[1] = new Texture();

        loadButton = new RectangleShape();
        loadTexture = new Texture[2];
        loadTexture[0] = new Texture();
        loadTexture[1] = new Texture();

        checkBox = new RectangleShape();
        checkBoxTexture = new Texture[2];
        checkBoxTexture[0] = new Texture();
        checkBoxTexture[1] = new Texture();

        board.setPosition(0.f,0.f);
        board.setSize(new Vector2f(700.f, 700.f));
        boardTexture.loadFromFile(Paths.get("Sudoku_Java-master/Textures/board.png"));
        board.setTexture(boardTexture);

        // --- Reset Button ---
        resetButton.setPosition(750.f, 300.f);
        resetButton.setSize(new Vector2f(200.f, 100.f));
        resetTexture[0].loadFromFile(Paths.get("Sudoku_Java-master/Textures/reset.png"));
        resetTexture[1].loadFromFile(Paths.get("Sudoku_Java-master/Textures/reset-hover.png"));
        resetButton.setTexture(resetTexture[0]);

        // --- Solve Button ---
        solveButton.setPosition(750.f, 200.f);
        solveButton.setSize(new Vector2f(200.f, 100.f));
        solveTexture[0].loadFromFile(Paths.get("Sudoku_Java-master/Textures/solve.png"));
        solveTexture[1].loadFromFile(Paths.get("Sudoku_Java-master/Textures/solve-hover.png"));
        solveButton.setTexture(solveTexture[0]);

        // --- Hint Button  ---
        hintButton.setPosition(750.f, 400.f);
        hintButton.setSize(new Vector2f(200.f, 100.f));
        hintTexture[0].loadFromFile(Paths.get("Sudoku_Java-master/Textures/hint.png"));
        hintTexture[1].loadFromFile(Paths.get("Sudoku_Java-master/Textures/hint-hover.png"));
        hintButton.setTexture(hintTexture[0]);

        // --- Load Button ---
        loadButton.setPosition(750.f, 500.f);
        loadButton.setSize(new Vector2f(200.f, 100.f));
        loadTexture[0].loadFromFile(Paths.get("Sudoku_Java-master/Textures/load.png"));
        loadTexture[1].loadFromFile(Paths.get("Sudoku_Java-master/Textures/load-hover.png"));
        loadButton.setTexture(loadTexture[0]);

        // --- Checkbox (kandydaci) ---
        checkBox.setPosition(750.f, 625.f);
        checkBox.setSize(new Vector2f(50.f, 50.f));
        checkBoxTexture[0].loadFromFile(Paths.get("Sudoku_Java-master/Textures/check2.png"));
        checkBoxTexture[1].loadFromFile(Paths.get("Sudoku_Java-master/Textures/check.png"));
        checkBox.setTexture(checkBoxTexture[0]);

        font = new Font();
        font.loadFromFile(Paths.get("Sudoku_Java-master/Fonts/arial.ttf"));
        text = new Text();
        text.setFont(font);
        text.setString("Candidates");
        text.setPosition(new Vector2f(810.f, 626.f));
        text.setCharacterSize(35);
        text.setColor(Color.WHITE);

        logo = new RectangleShape();
        logoTexture = new Texture();
        logo.setPosition(693.f, 30.f);
        logo.setSize(new Vector2f(312.f, 175.5f));
        logoTexture.loadFromFile(Paths.get("Sudoku_Java-master/Textures/logo.png"));
        logo.setTexture(logoTexture);
    }

    private void initWinText()  // ** NOWE **
    {
        winText = new Text();
        winText.setFont(font);
        winText.setString("GRATULACJE!");
        winText.setCharacterSize(72);
        winText.setColor(Color.GREEN);
        // Ustawiamy w przybliżeniu na środek okna 1000×700
        // (można dostosować współrzędne według potrzeb)
        Float xCenter = (1000f - winText.getLocalBounds().width) / 2f;
        Float yCenter = (700f - winText.getLocalBounds().height) / 2f;
        winText.setPosition(xCenter, yCenter);
    }

    private void initCandidates() throws IOException
    {
        ArrayList<ArrayList<ArrayList<Integer>>> tab1 = new ArrayList<>(9);

        for(int i = 0;i<9;i++)
        {
            tab1.add(new ArrayList<ArrayList<Integer>>(9));
            for (int j = 0; j < 9; j++)
            {
                tab1.get(i).add(new ArrayList<Integer>());
            }
        }

        candidates = tab1;

        checkedCand = new boolean[9][9][10];

        candTexture = new Texture[10];
        for(int i = 0;i<10;i++)
            candTexture[i] = new Texture();

        checkedCandTexture = new Texture[10];
        for(int i = 0;i<10;i++)
            checkedCandTexture[i] = new Texture();

        for (int i = 1;i < 10;i++)
        {
            String t = "Sudoku_Java-master/Textures/CandText/" + i + ".png";
            candTexture[i].loadFromFile(Paths.get(t));
            t = "Sudoku_Java-master/Textures/CheckedCand/" + i + ".png";
            checkedCandTexture[i].loadFromFile(Paths.get(t));
        }
    }

    private void drawCandidates()
    {
        for (int i = 0;i < 9;i++)
        {
            for (int j = 0;j < 9;j++)
            {
                for (int k = 0;k < candBox.get(i).get(j).size();k++)
                {
                    window.draw(candBox.get(i).get(j).get(k));
                }
            }
        }
    }

    private void drawBoard()
    {
        window.draw(board);
        window.draw(resetButton);
        window.draw(solveButton);
        window.draw(hintButton);
        window.draw(loadButton);
        window.draw(checkBox);
        window.draw(text);
        window.draw(logo);
    }

    private void initBoxes() throws IOException
    {
        boxTextures = new Texture[10];
        box = new RectangleShape[9][9];

        greyColor = new Color(186, 181, 181);
        for (int i = 0;i < 10;i++)
        {
            String t = "Sudoku_Java-master/Textures/" + i + ".png";
            boxTextures[i] = new Texture();
            boxTextures[i].loadFromFile(Paths.get(t));
        }

        int y = 14;
        for (int i = 0;i < 9;i++)
        {
            int x = 14;
            for (int j = 0;j < 9;j++)
            {
                box[i][j] = new RectangleShape();
                box[i][j].setFillColor(Color.WHITE);
                box[i][j].setSize(new Vector2f(70.f, 70.f));
                box[i][j].setPosition(x, y);
                x += 74;
                if (j == 2 || j == 5) x += 5;
            }
            y += 74;
            if (i == 2 || i == 5) y += 5;
        }
    }

    private void drawBoxes()
    {
        for(int i = 0;i < 9;i++)
            for (int j = 0;j < 9;j++)
                window.draw(box[i][j]);
    }

    public Boolean running()
    {
        return window.isOpen();
    }

    private void poolEvent()
    {
        for(Event ev : window.pollEvents())
        {
            switch (ev.type)
            {
                case CLOSED:
                    window.close();
                    break;
                case KEY_PRESSED:
                    if (ev.asKeyEvent().key == Keyboard.Key.ESCAPE)
                        window.close();
                    break;
                case MOUSE_BUTTON_RELEASED:
                    if(!Mouse.isButtonPressed(Mouse.Button.LEFT))
                        pressed = false;
                    break;
                case KEY_RELEASED:
                    if (!Keyboard.isKeyPressed(Keyboard.Key.TAB) || !Keyboard.isKeyPressed(Keyboard.Key.UP) ||
                            !Keyboard.isKeyPressed(Keyboard.Key.DOWN) || !Keyboard.isKeyPressed(Keyboard.Key.LEFT) ||
                            !Keyboard.isKeyPressed(Keyboard.Key.RIGHT))
                        pressed = false;
                    break;
            }
        }
    }

    private void menu() throws IOException
    {
        // Solve Button – wypełnia całą tablicę rozwiązaniem
        if (solveButton.getGlobalBounds().contains(mousePosition) && Mouse.isButtonPressed(Mouse.Button.LEFT) && !pressed)
        {
            pressed = true;
            Solver solver = new Solver(numbers);
            int[][] solution = solver.returnSolution();
            for (int i = 0; i < 9; i++)
                System.arraycopy(solution[i], 0, numbers[i], 0, 9);
            updateNumbers();
        }
        // Reset Button – wyczyść wszystkie pola
        else if (resetButton.getGlobalBounds().contains(mousePosition) && Mouse.isButtonPressed(Mouse.Button.LEFT))
        {
            for (int i = 0;i < 9;i++)
            {
                for (int j = 0;j < 9;j++)
                {
                    box[i][j].setTexture(boxTextures[0]);
                    numbers[i][j] = 0;
                    for (int k = 1;k < 10;k++)
                        checkedCand[i][j][k] = false;
                }
            }
            // Reset cache
            solutionCache = new int[9][9];
            nextHintRow = nextHintCol = -1;
            hasWon = false;
        }
        // Load Button – generuje nową planszę
        else if (loadButton.getGlobalBounds().contains(mousePosition) && Mouse.isButtonPressed(Mouse.Button.LEFT) && !pressed)
        {
            pressed = true;
            hasWon = false;    // resetujemy flagę wygranej
            generateNewPuzzle(40);
        }
        // Hint Button – wstawia jedno poprawne pole (kolejny krok)
        else if (hintButton.getGlobalBounds().contains(mousePosition) && Mouse.isButtonPressed(Mouse.Button.LEFT) && !pressed)
        {
            pressed = true;
            giveNextHint();
        }
        // Checkbox – pokazuj/ukryj kandydatów
        else if (checkBox.getGlobalBounds().contains(mousePosition) && Mouse.isButtonPressed(Mouse.Button.LEFT) && !pressed)
        {
            pressed = true;
            if (!showCand)
            {
                showCand = true;
                checkBox.setTexture(checkBoxTexture[1]);
            }
            else
            {
                showCand = false;
                checkBox.setTexture(checkBoxTexture[0]);
            }
        }
    }

    private void updateButtons()
    {
        if (resetButton.getGlobalBounds().contains(mousePosition))
            resetButton.setTexture(resetTexture[1]);
        else
            resetButton.setTexture(resetTexture[0]);

        if (solveButton.getGlobalBounds().contains(mousePosition))
            solveButton.setTexture(solveTexture[1]);
        else
            solveButton.setTexture(solveTexture[0]);

        if (hintButton.getGlobalBounds().contains(mousePosition))         // zmiana
            hintButton.setTexture(hintTexture[1]);                        // zmiana
        else
            hintButton.setTexture(hintTexture[0]);                        // zmiana

        if (loadButton.getGlobalBounds().contains(mousePosition))
            loadButton.setTexture(loadTexture[1]);
        else
            loadButton.setTexture(loadTexture[0]);
    }

    private void updateBoxes()
    {
        for (int i = 0;i < 9;i++)
        {
            for (int j = 0;j < 9;j++)
            {
                if (box[i][j].getGlobalBounds().contains(mousePosition) && Mouse.isButtonPressed(Mouse.Button.LEFT) && !pressed)
                {
                    // sprawdza czy kliknięto na kandydacie
                    boolean candClick = false;
                    for (int k = 0; k < candBox.get(i).get(j).size(); k++)
                        if (candBox.get(i).get(j).get(k).getGlobalBounds().contains(mousePosition))
                        {
                            candClick = true;
                            break;
                        }
                    if (!candClick || !showCand)
                    {
                        pressed = true;
                        if (checkedX == j && checkedY == i)
                        {
                            box[i][j].setFillColor(Color.WHITE);
                            checkedX = -1;
                            checkedY = -1;
                        }
                        else if (checkedX != -1 && checkedY != -1)
                        {
                            box[checkedY][checkedX].setFillColor(Color.WHITE);
                            box[i][j].setFillColor(greyColor);
                            checkedX = j;
                            checkedY = i;
                        }
                        else
                        {
                            box[i][j].setFillColor(greyColor);
                            checkedX = j;
                            checkedY = i;
                        }
                    }
                }
            }
        }

        // sterowanie klawiaturą (TAB/strzałki)
        if ((Keyboard.isKeyPressed(Keyboard.Key.TAB)) && !pressed)
        {
            pressed = true;
            if (checkedX == -1 && checkedY == -1)
            {
                box[0][0].setFillColor(greyColor);
                checkedX = 0;
                checkedY = 0;
            }
            else if (checkedX == 8 && checkedY != 8)
            {
                box[checkedY][checkedX].setFillColor(Color.WHITE);
                checkedX = 0;
                checkedY++;
                box[checkedY][checkedX].setFillColor(greyColor);
            }
            else if (checkedY == 8 && checkedX == 8)
            {
                box[checkedY][checkedX].setFillColor(Color.WHITE);
                checkedX = 0;
                checkedY = 0;
                box[checkedY][checkedX].setFillColor(greyColor);
            }
            else
            {
                box[checkedY][checkedX].setFillColor(Color.WHITE);
                checkedX++;
                box[checkedY][checkedX].setFillColor(greyColor);
            }
        }
        else if ((Keyboard.isKeyPressed(Keyboard.Key.UP)) && !pressed)
        {
            pressed = true;
            if (checkedX == -1 && checkedY == -1)
            {
                box[0][0].setFillColor(greyColor);
                checkedX = 0;
                checkedY = 0;
            }
            else
            {
                box[checkedY][checkedX].setFillColor(Color.WHITE);
                checkedY--;
                if (checkedY < 0)
                    checkedY = 8;
                box[checkedY][checkedX].setFillColor(greyColor);
            }
        }
        else if ((Keyboard.isKeyPressed(Keyboard.Key.DOWN)) && !pressed)
        {
            if (checkedX == -1 && checkedY == -1)
            {
                box[0][0].setFillColor(greyColor);
                checkedX = 0;
                checkedY = 0;
            }
            else
            {
                pressed = true;
                box[checkedY][checkedX].setFillColor(Color.WHITE);
                checkedY++;
                if (checkedY > 8)
                    checkedY = 0;
                box[checkedY][checkedX].setFillColor(greyColor);
            }
        }
        else if ((Keyboard.isKeyPressed(Keyboard.Key.LEFT)) && !pressed)
        {
            if (checkedX == -1 && checkedY == -1)
            {
                box[0][0].setFillColor(greyColor);
                checkedX = 0;
                checkedY = 0;
            }
            else
            {
                pressed = true;
                box[checkedY][checkedX].setFillColor(Color.WHITE);
                checkedX--;
                if (checkedX < 0)
                    checkedX = 8;
                box[checkedY][checkedX].setFillColor(greyColor);
            }
        }
        else if ((Keyboard.isKeyPressed(Keyboard.Key.RIGHT)) && !pressed)
        {
            if (checkedX == -1 && checkedY == -1)
            {
                box[0][0].setFillColor(greyColor);
                checkedX = 0;
                checkedY = 0;
            }
            else
            {
                pressed = true;
                box[checkedY][checkedX].setFillColor(Color.WHITE);
                checkedX++;
                if (checkedX > 8)
                    checkedX = 0;
                box[checkedY][checkedX].setFillColor(greyColor);
            }
        }

        // wpisywanie cyfr z klawiatury
        if (checkedX != -1 && checkedY != -1)
        {
            if (Keyboard.isKeyPressed(Keyboard.Key.NUM0))
            {
                box[checkedY][checkedX].setTexture(boxTextures[0]);
                numbers[checkedY][checkedX] = 0;
            }
            else if (Keyboard.isKeyPressed(Keyboard.Key.NUM1))
            {
                box[checkedY][checkedX].setTexture(boxTextures[1]);
                numbers[checkedY][checkedX] = 1;
            }
            else if (Keyboard.isKeyPressed(Keyboard.Key.NUM2))
            {
                box[checkedY][checkedX].setTexture(boxTextures[2]);
                numbers[checkedY][checkedX] = 2;
            }
            else if (Keyboard.isKeyPressed(Keyboard.Key.NUM3))
            {
                box[checkedY][checkedX].setTexture(boxTextures[3]);
                numbers[checkedY][checkedX] = 3;
            }
            else if (Keyboard.isKeyPressed(Keyboard.Key.NUM4))
            {
                box[checkedY][checkedX].setTexture(boxTextures[4]);
                numbers[checkedY][checkedX] = 4;
            }
            else if (Keyboard.isKeyPressed(Keyboard.Key.NUM5))
            {
                box[checkedY][checkedX].setTexture(boxTextures[5]);
                numbers[checkedY][checkedX] = 5;
            }
            else if (Keyboard.isKeyPressed(Keyboard.Key.NUM6))
            {
                box[checkedY][checkedX].setTexture(boxTextures[6]);
                numbers[checkedY][checkedX] = 6;
            }
            else if (Keyboard.isKeyPressed(Keyboard.Key.NUM7))
            {
                box[checkedY][checkedX].setTexture(boxTextures[7]);
                numbers[checkedY][checkedX] = 7;
            }
            else if (Keyboard.isKeyPressed(Keyboard.Key.NUM8))
            {
                box[checkedY][checkedX].setTexture(boxTextures[8]);
                numbers[checkedY][checkedX] = 8;
            }
            else if (Keyboard.isKeyPressed(Keyboard.Key.NUM9))
            {
                box[checkedY][checkedX].setTexture(boxTextures[9]);
                numbers[checkedY][checkedX] = 9;
            }
        }
    }

    private void updateMousePosition()
    {
        mousePosition = window.mapPixelToCoords(Mouse.getPosition(window));
    }

    void update() throws IOException
    {
        poolEvent();
        updateMousePosition();
        updateBoxes();
        updateButtons();
        updateNumbers();
        updateCandidates();
        updateCandColor();
        menu();

        // ** NOWE ** – sprawdzamy, czy plansza została wypełniona poprawnie
        if (!hasWon && isBoardSolved()) {
            hasWon = true;
        }
    }

    public void render()
    {
        window.clear();
        drawBoard();
        drawBoxes();
        if(showCand)
            drawCandidates();

        // ** NOWE ** – jeśli gracz wygrał, rysujemy półprzezroczysty overlay i napis
        if (hasWon) {
            // stwórz półprzezroczystą czarną nakładkę
            RectangleShape overlay = new RectangleShape(new Vector2f(1000.f, 700.f));
            overlay.setFillColor(new Color(0, 0, 0, 150)); // czarny z alfa=150
            window.draw(overlay);

            // narysuj napis „Gratulacje!”
            window.draw(winText);
        }

        window.display();
    }

    /**
     * Generuje nową planszę: używa SudokuGenerator, wczytuje
     * do `numbers` wyjściowe pole, a do `solutionCache` zapamiętuje całe rozwiązanie.
     */
    private void generateNewPuzzle(int removals) {
        SudokuGenerator gen = new SudokuGenerator();
        int[][] puzzle = gen.generatePuzzle(removals);

        // Zapisz w numbers (plansza z lukami)
        for (int i = 0; i < 9; i++) {
            System.arraycopy(puzzle[i], 0, numbers[i], 0, 9);
        }

        // Skopiuj, licząc rozwiązanie do solutionCache:
        Solver solver = new Solver(copyGrid(numbers));
        int[][] fullSolution = solver.returnSolution();
        for (int i = 0; i < 9; i++) {
            System.arraycopy(fullSolution[i], 0, solutionCache[i], 0, 9);
        }

        // Przygotuj wskaźniki do kolejnego “hinta”
        nextHintRow = 0;
        nextHintCol = 0;
        advanceHintPointer(); // ustaw na pierwsze puste
        updateNumbers();
    }

    /**
     * Zwraca kopię tablicy 9×9 (żeby nie nadpisać oryginału podczas rozwiązywania).
     */
    private int[][] copyGrid(int[][] src) {
        int[][] dst = new int[9][9];
        for (int i = 0; i < 9; i++) {
            System.arraycopy(src[i], 0, dst[i], 0, 9);
        }
        return dst;
    }

    /**
     * Znajduje w solutionCache kolejne puste pole w numbers (różne od 0).
     * Ustawia nextHintRow/Col na pozycję następnego “hinta”.
     * Jeśli nie ma więcej pustych – zostawia wskaźniki niezmienione.
     */
    private void advanceHintPointer() {
        while (nextHintRow < 9) {
            if (numbers[nextHintRow][nextHintCol] == 0) {
                return; // mamy puste pole do podpowiedzi
            }
            nextHintCol++;
            if (nextHintCol >= 9) {
                nextHintCol = 0;
                nextHintRow++;
            }
        }
        // jeśli wyszliśmy poza 8, to nie ma więcej pustych
    }

    /**
     * Gdy gracz kliknie “Hint”, wstawiamy dokładnie jedno
     * następne puste pole według solutionCache.
     */
    private void giveNextHint() {
        if (nextHintRow < 0 || nextHintRow >= 9) return;
        if (nextHintCol < 0 || nextHintCol >= 9) return;

        if (numbers[nextHintRow][nextHintCol] != 0) {
            advanceHintPointer();
            giveNextHint();
            return;
        }

        // Wstawiamy rozwiązanie
        numbers[nextHintRow][nextHintCol] = solutionCache[nextHintRow][nextHintCol];
        box[nextHintRow][nextHintCol].setTexture(boxTextures[solutionCache[nextHintRow][nextHintCol]]);

        // Po wstawieniu jednego pola – przesuń wskaźniki dalej
        advanceHintPointer();
    }

    // ======== METODA SPRAWDZAJĄCA POPRAWNOŚĆ I KOMPLETNOŚĆ PLANSZY ========

    /**
     * Sprawdza, czy na planszy:
     *  1) nie ma żadnej zera (czyli wszystkie pola wypełnione 1..9),
     *  2) każdy wiersz, kolumna i kwadrat 3×3 zawiera unikalne wartości od 1 do 9.
     */
    private boolean isBoardSolved() {
        // 1) Sprawdź wiersze
        for (int i = 0; i < 9; i++) {
            boolean[] seen = new boolean[10];
            for (int j = 0; j < 9; j++) {
                int v = numbers[i][j];
                if (v < 1 || v > 9 || seen[v]) {
                    return false;
                }
                seen[v] = true;
            }
        }
        // 2) Sprawdź kolumny
        for (int j = 0; j < 9; j++) {
            boolean[] seen = new boolean[10];
            for (int i = 0; i < 9; i++) {
                int v = numbers[i][j];
                if (v < 1 || v > 9 || seen[v]) {
                    return false;
                }
                seen[v] = true;
            }
        }
        // 3) Sprawdź każdy kwadrat 3×3
        for (int boxRow = 0; boxRow < 9; boxRow += 3) {
            for (int boxCol = 0; boxCol < 9; boxCol += 3) {
                boolean[] seen = new boolean[10];
                for (int di = 0; di < 3; di++) {
                    for (int dj = 0; dj < 3; dj++) {
                        int v = numbers[boxRow + di][boxCol + dj];
                        if (v < 1 || v > 9 || seen[v]) {
                            return false;
                        }
                        seen[v] = true;
                    }
                }
            }
        }
        return true;
    }

    // ======== KONIEC NOWYCH METOD ========

    private void updateCandidates()
    {
        var solver = new Solver(numbers);
        candidates = solver.returnCandidates();

        ArrayList<ArrayList<ArrayList<RectangleShape>>> tab = new ArrayList<>(9);

        for(int i = 0;i<9;i++)
        {
            tab.add(new ArrayList<ArrayList<RectangleShape>>(9));
            for (int j = 0; j < 9; j++)
            {
                tab.get(i).add(new ArrayList<RectangleShape>());
            }
        }

        for (int i = 0;i < 9;i++)
        {
            for (int j = 0;j < 9;j++)
            {
                if (numbers[i][j] == 0)
                {
                    float x = 0;
                    float y = 0;
                    for (int k = 0;k < candidates.get(i).get(j).size();k++)
                    {
                        var a = new RectangleShape();
                        a.setPosition(box[i][j].getPosition().x + x, box[i][j].getPosition().y + y);
                        a.setSize(new Vector2f(17.5f, 17.5f));
                        a.setTexture(candTexture[candidates.get(i).get(j).get(k)]);
                        tab.get(i).get(j).add(a);

                        if (x == 52.5)
                        {
                            x = 0;
                            y += 17.5;
                        }
                        else
                            x += 17.5;

                    }
                }
            }
        }
        candBox = tab;

        // aktualizowanie zaznaczenia kandydatów
        for (int i = 0;i < 9;i++)
        {
            for (int j = 0;j < 9;j++)
            {
                for (int k = 0;k < candBox.get(i).get(j).size();k++)
                {
                    if (candBox.get(i).get(j).get(k).getGlobalBounds().contains(mousePosition) && Mouse.isButtonPressed(Mouse.Button.LEFT) && !pressed)
                    {
                        pressed = true;
                        int candidateValue = candidates.get(i).get(j).get(k);
                        if(checkedCand[i][j][candidateValue])
                            checkedCand[i][j][candidateValue] = false;
                        else
                            checkedCand[i][j][candidateValue] = true;
                    }
                }
            }
        }
    }

    private void updateCandColor()
    {
        for (int i = 0;i < 9;i++)
        {
            for (int j = 0;j < 9;j++)
            {
                for (int k = 0;k < candBox.get(i).get(j).size();k++)
                {
                    int candidateValue = candidates.get(i).get(j).get(k);
                    if (checkedCand[i][j][candidateValue])
                        candBox.get(i).get(j).get(k).setTexture(checkedCandTexture[candidateValue]);
                    else
                        candBox.get(i).get(j).get(k).setTexture(candTexture[candidateValue]);

                    if (i == checkedY && j == checkedX)
                        candBox.get(i).get(j).get(k).setFillColor(greyColor);
                    else
                        candBox.get(i).get(j).get(k).setFillColor(Color.WHITE);
                }
            }
        }
    }
}
