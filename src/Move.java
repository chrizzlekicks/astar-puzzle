import java.util.InputMismatchException;
import java.util.Objects;

public class Move {
    protected Position pos;
    protected int dir;

    static private int[] xd = {0, 1, 0, -1};
    static private int[] yd = {-1, 0, 1, 0};

    public Move(Position pos, int dir) throws InputMismatchException {
        if (dir < 0 || dir >= 4)
            throw new InputMismatchException();
        this.pos = pos;
        this.dir = dir;
    }

    public Position targetPosition() {
        return targetPosition(1);
    }

    public Position targetPosition(int step) {
        int x = pos.x + step * xd[dir];
        int y = pos.y + step * yd[dir];
        return new Position(x, y);
    }

    public boolean isInverse(Move that) {
        if (that == null) return false;

        return this.pos.equals(that.targetPosition()) &&
                that.pos.equals(this.targetPosition());
    }

    @Override
    public String toString() {
        return pos + "->" + targetPosition();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return dir == move.dir &&
                Objects.equals(pos, move.pos);
    }
}

