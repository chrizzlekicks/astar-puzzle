import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Board is a class which represents a board of a puzzle game, called 15-puzzle (or 8-puzzle),
 * see <a href="https://en.wikipedia.org/wiki/15_puzzle">the wikipedia article</a>. Here, the
 * size of the board is not fixed, and can even be a non-square rectangle.
 * <p>
 * There is one particularity in the implementation. For performance reason, the positions of
 * a board are stored in a <em>static</em> variable. Since those positions are dependend on the
 * size of the board, this imposes the restriction that all objects existing simultaneously have
 * to have the same dimensions.
 * For usual use cases, this is no limitation.
 */
public class Board {
    private int[][] field;
    private int nx;
    private int ny;
    private Position blank;

    // Variable positions is declared static for performance reason (see comment in the docstring)
    private static Stack<Position> positions = null;
    private static int posnx;
    private static int posny;

    private final static int OFF = -1;   // used for positions that are outside of the actual field
    private final static int BLANK = 0;  // denotes the field that is blank

    /**
     * Constructor generates an empty board of the given diemnsions (<em>nx</em> horizontally and
     * <em>ny</em> vertically.
     *
     * @param nx horizontal dimension of the board
     * @param ny vertical dimension of the board
     */
    public Board(int nx, int ny) {
        this.nx = nx;
        this.ny = ny;
        constructorBody();
    }

    /**
     * Constructor which reads a {@link Board} from a text file. The first line has the
     * horizontal <em>nx</em> and the vertical <em>ny</em> dimensions of the field. Each of the following lines
     * defines the fields of one row. No check for consistency is performed. The tokens have
     * the numbers from 1 to {@code nx*ny - 1} and 0 is used for the blank field.
     * Files are not check for consistency!
     *
     * @param filename name of the text file
     */
    public Board(String filename) {
        Scanner in = null;
        try {
            in = new Scanner(new FileInputStream(filename));
        } catch (IOException e) {
            System.err.println("!!!!! Cannot read file '" + filename + "'.");
        }
        nx = in.nextInt();
        ny = in.nextInt();
        constructorBody();

        for (int y = 0; y < ny; y++) {
            for (int x = 0; x < nx; x++) {
                field[y][x] = in.nextInt();
                if (field[y][x] == BLANK)
                    blank = new Position(x, y);
            }
        }
        in.close();
    }

    /**
     * Main part of the constructor. This is put in a separate method to avoid that the code
     * needs to be double. The {@code Board(String)} constructor cannot call the basic constructor
     * since that is allowed as the first line only, and the size of the board needs to be read
     * from the file first.
     */
    private void constructorBody() {
        field = new int[ny][nx];

        if (positions == null || posnx != nx || posny != ny) {
            positions = new Stack<>();
            for (int y = 0; y < ny; y++)
                for (int x = 0; x < nx; x++)
                    positions.push(new Position(x, y));
            posnx = nx;
            posny = ny;
        }
    }


    /**
     * Copy constructor
     *
     * @param that {@link Board} object which is to be copied
     */
    public Board(Board that) {
        this(that.nx, that.ny);
        for (int y = 0; y < ny; y++) {
            this.field[y] = that.field[y].clone();
        }
        this.blank = that.blank;
    }

    /**
     * Return the content of a field (token number, or {@code Board.BLANK} if the field is free,
     * or {@code Board.OFF} is the specified {@link Position} does not correspond to a field on
     * the board.
     *
     * @param pos the {@link Position} of the field which is inspected
     * @return the content of the field at {@link Position} <em>pos</em>
     */
    public int getField(Position pos) {
        if (pos.x < 0 || pos.x >= nx || pos.y < 0 || pos.y >= ny)
            return OFF;
        else
            return field[pos.y][pos.x];
    }

    /**
     * Put a token on the field at the specified {@link Position} of the board.
     *
     * @param pos   The {@link Position} where the <em>token</em> should be placed
     * @param token The token (number between 1 and {@code nx*ny - 1} that is to be placed,
     *              or {@code Board.BLANK} to clear a field.
     * @throws InputMismatchException for invalid <em>pos</em> or <em>token</em> out of range
     */
    public void setField(Position pos, int token) throws InputMismatchException {
        if (getField(pos) == OFF || token < 0 || token >= nx * ny)
            throw new InputMismatchException();
        field[pos.y][pos.x] = token;
    }

    /**
     * Check whether a {@link Move} is valid in the current board.
     *
     * @param move The {@link Move} that is to be checked
     * @return true, if the <em>move</em> is valid.
     */
    public boolean checkMove(Move move) {
        return getField(move.targetPosition(0)) > BLANK &&
                getField(move.targetPosition(1)) == BLANK;
    }

    /**
     * Execute the specified {@link Move}.
     *
     * @param move The {@link Move} that should be performed.
     */
    public void doMove(Move move) {
        blank = move.targetPosition(0);
        int token = getField(blank);
        setField(move.targetPosition(1), token);
        setField(blank, BLANK);
    }

    /**
     * Returns all possible moves for the current board as Iterable.
     *
     * @return all possible moves as {@code Iterable<Move>}
     */
    public Iterable<Move> validMoves() {
        return validMoves(null);
    }

    /**
     * Returns all possible moves for the current board as {@code Iterable}. The move that would undo
     * the move that is provided are argument <em>lastMove</em> is excluded. If {@code lastMove}
     * is {@code null}, no move is excluded.
     *
     * @param {@link Move} lastMove Last move in the move sequence: the inverse of this move is excluded.
     * @return all possible moves as {@code Iterable<Move>}, excluding the inverse of <em>lastMove</em>
     */
    public Iterable<Move> validMoves(Move lastMove) {
        Stack<Move> moves = new Stack<>();
        for (int dir = 0; dir < 4; dir++) {
            Move invmove = new Move(blank, dir);
            Position source = invmove.targetPosition(-1);
            Move move = new Move(source, dir);
            if (checkMove(move) && !move.isInverse(lastMove))
                moves.push(move);
        }
        return moves;
    }

    /**
     * Checks whether the state of the board is the solved state, i.e. the number are
     * increasing from left to right and top to bottom. The blank field is in the
     * bottom right corner.
     *
     * @return {@code true} if board is solved
     */
    public boolean isSolved() {
        for (int counter = 0; counter < nx * ny - 1; counter++) {
            if (getField(new Position(counter % nx, counter / nx)) != counter + 1)
                return false;
        }
        return true;
    }


    /**
     * Computes the Manhattan distance (aka taxicab metric), see <a href="https://en.wikipedia.org/wiki/Taxicab_geometry">here</a>,
     * between the current puzzle configuration and the goal configuration: For each token, the distance from its
     * curent position to its position in the solution is calculated in x- and y-direction. These
     * two (absolute) values are added for each token. The sum across all tokens (not for the blank
     * field) is the value of the metric.
     *
     * @return distance of current board state to solution in Manhattan metric
     */
    public int manhattan() {
        /* TODO */
        int heuristic = 0;
        int[][] ideal = idealFields();
        for (Position pos : positions) {
            int v = getField(pos);
            if (v != 0 && v != ideal[pos.y][pos.x]) {
                Position vPos = posLookUp(v, ideal);
                int vertical = Math.abs(vPos.y - pos.y);
                int horizontal = Math.abs(vPos.x - pos.x);
                heuristic += vertical + horizontal;
            }
        }
        return heuristic;
    }

    /* generate the ideal order of values for the board */
    private int[][] idealFields() {
        /* TODO: helper */
        int[][] fields = new int[field.length][field[0].length];
        int count = 1;
        for (int j = 0; j < fields.length; j++) {
            for (int i = 0; i < fields[0].length; i++) {
                fields[j][i] = count;
                count++;
            }
        }
        fields[field.length-1][field[0].length-1] = 0;
        return fields;
    }

    /* find the position of a value */
    /* TODO: helper */
    static private Position posLookUp(int value, int[][] fields) {
        Position pos = new Position(0, 0);
        for (int j = 0; j < fields.length; j++) {
            for (int i = 0; i < fields[0].length; i++) {
                if (value == fields[j][i]) {
                    pos.y = j;
                    pos.x = i;
                }
            }
        }
        return pos;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (Position pos : positions) {
            str.append(String.format("%2d ", getField(pos)));
            if (pos.x == nx - 1)
                str.append("\n");
        }
        return str.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Board that = (Board) o;
        for (Position pos : positions) {
            if (getField(pos) != that.getField(pos))
                return false;
        }
        return true;
    }


    public static void main(String[] args) {
        String filename = "samples/board-manhattan2.txt";
        Board board = new Board(filename);
        for (Position pos : positions) {
            System.out.println(pos);
        }
        System.out.println(Arrays.deepToString(board.field));
        System.out.println("Manhattan: " + board.manhattan());
    }
}

