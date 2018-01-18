package root.core;

public class Cell {
    public int i;
    public int j;

    Cell() {
        i = j = 0;
    }

    Cell(int i, int j) {
        this.i = i;
        this.j = j;
    }

    public Cell(Cell cell) {
        this.i = cell.i;
        this.j = cell.j;
    }

    @Override
    public boolean equals(Object obj) {
        Cell cell = (Cell) obj;
        return this.i == cell.i && this.j == cell.j;
    }

    @Override
    public String toString() {
        return "[" + i + ", " + j + "]";
    }
}
