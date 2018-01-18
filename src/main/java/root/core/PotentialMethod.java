package root.core;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class PotentialMethod {
    private Table table;
    private AbstractBasicPlanSearch basicPlanSearch;

    private Document document;

    private Integer[] u;
    private Integer[] v;

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) throws Exception {
        this.table = table;
    }

    public AbstractBasicPlanSearch getBasicPlanSearch() {
        return basicPlanSearch;
    }

    public void setBasicPlanSearch(AbstractBasicPlanSearch basicPlanSearch) throws Exception {
        this.basicPlanSearch = basicPlanSearch;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public void findOptimalBasicPlan() throws Exception {
        init();
        document.add(new Paragraph("Potential method"));
        findInitialBasicPlan();
        u = new Integer[table.getHeight()];
        v = new Integer[table.getWidth()];
        Arrays.fill(u, null);
        Arrays.fill(v, null);
        int counter = 0;
        Cell cellMax, cellMin;
        List<Cell> cycle;
        while (true) {
            document.add(new Paragraph("Iteration number " + ++counter));
            findPotentials();
            table.savePotentialsToPdf(document, u, v);
            cellMax = checkOptimal();
            if (cellMax.i == -1) {
                document.add(new Paragraph("This basic plan is optimal.\nEnd calculations"));
                table.saveToPdf(document);
                break;
            } else {
                document.add(new Paragraph("This basic plan is not optimal."));
                document.add(new Paragraph("max = x[" + cellMax.i + "][" + cellMax.j + "] = "
                        + delta(u[cellMax.i], v[cellMax.j], table.getCostAt(cellMax.i, cellMax.j))));
                cycle = buildCycle(cellMax);
                StringBuilder sb = new StringBuilder("Cycle: ");
                for (Cell c : cycle) {
                    sb.append("x[" + c.i + "][" + c.j + "] -> ");
                }
                sb.replace(sb.length() - 4, sb.length(), ";");
                document.add(new Paragraph(sb.toString()));
                table.saveCycleToPdf(document, cycle);
                cellMin = minCycle(cycle);
                document.add(new Paragraph("min = x[" + cellMin.i + "][" + cellMin.j + "] = "
                        + table.getTrafficAt(cellMin.i, cellMin.j)));
                changeBasis(cycle, cellMin, cellMax);
                table.saveToPdf(document);
            }
        }
        table.fullReset();
    }

    private void init() throws Exception {
        if (basicPlanSearch == null) {
            throw new Exception("No basic plan search!!");
        }
        if (document == null) {
            if (basicPlanSearch.getDocument() != null) {
                this.document = basicPlanSearch.getDocument();
            } else {
                throw new Exception("No document!!");
            }
        } else {
            basicPlanSearch.setDocument(this.document);
        }
        if (!document.isOpen()) {
            document.open();
        }
        if (table == null) {
            if (basicPlanSearch.getTable() != null) {
                this.setTable(basicPlanSearch.getTable());
            } else {
                throw new Exception("No table!!");
            }
        } else {
            basicPlanSearch.setTable(table);
        }
    }

    private void findInitialBasicPlan() throws Exception {
        basicPlanSearch.findBasicPlan();
    }

    private void findPotentials() throws Exception {
        clearPotentials();
        int firstBasicRowIndex = table.getFirstBasicCell().i;
        u[firstBasicRowIndex] = 0;
        document.add(new Paragraph("u[" + firstBasicRowIndex + "] = 0;"));
        findPotentialsHorizontaly(firstBasicRowIndex);

        //check
        for (int i = 0; i < u.length; i++) {
            if (u[i] == null && isBasicRow(i)) {
                    throw new Exception("Potential u[" + i + "] is not found!!");

            }
        }

        for (int j = 0; j < v.length; j++) {
            if (v[j] == null && isBasicColumn(j)) {
                    throw new Exception("Potential v[" + j + "] is not found!!");

            }
        }
    }

    private void findPotentialsHorizontaly(int i) throws Exception {
        if (u[i] == null) {
            throw new Exception("Undefined u[" + i + "]!!");
        }
        for (int j = 0; j < table.getWidth(); j++) {
            if (v[j] == null && table.getTrafficAt(i, j) > 0) {
                int cost = table.getCostAt(i, j);
                v[j] = cost - u[i];
                document.add(new Paragraph("u[" + i + "] + v[" + j + "] = c[" + i + "][" + j + "]; "
                        + u[i] + " + v[" + j + "] +  = " + cost + "; "
                        + "v[" + j + "] = " + cost + " - " + u[i] + " = " + v[j] + ";"));
                findPotentialsVerticaly(j);
            }
        }
    }

    private void findPotentialsVerticaly(int j) throws Exception {
        if (v[j] == null) {
            throw new Exception("Undefined v[" + j + "]!!");
        }
        for (int i = 0; i < table.getHeight(); i++) {
            if (u[i] == null && table.getTrafficAt(i, j) > 0) {
                int cost = table.getCostAt(i, j);
                u[i] = cost - v[j];
                document.add(new Paragraph("u[" + i + "] + v[" + j + "] = c[" + i + "][" + j + "]; "
                        + "u[" + i + "] + " + v[j] + " = " + cost + "; "
                        + "u[" + i + "] = " + cost + " - " + v[j] + " = " + u[i] + ";"));
                findPotentialsHorizontaly(i);
            }
        }
    }

    private boolean isBasicRow(int rowIndex) {
        for (int j = 0; j < table.getWidth(); j++) {
            if (table.getTrafficAt(rowIndex, j) >= 0) {
                return true;
            }
        }
        return false;
    }

    private boolean isBasicColumn(int columnIndex) {
        for (int i = 0; i < table.getHeight(); i++) {
            if (table.getTrafficAt(i, columnIndex) >= 0) {
                return true;
            }
        }
        return false;
    }

    private void clearPotentials() {
        Arrays.fill(u, null);
        Arrays.fill(v, null);
    }

    private Cell checkOptimal() throws DocumentException {
        document.add(new Paragraph("Check plan for optimality"));
        int max = 0, delta;
        boolean optimal = true;
        Cell res = new Cell(-1, -1);
        firstMax:
        for (int i = 0; i < table.getHeight(); i++) {
            for (int j = 0; j < table.getWidth(); j++) {
                if (table.getTrafficAt(i, j) <= 0 && !table.isBasic(i, j)) {
                    max = delta(u[i], v[j], table.getCostAt(i, j));
                    if (max > 0) {
                        res.i = i;
                        res.j = j;
                        document.add(new Paragraph("max = delta[" + i + "][" + j + "] = " + max));
                        break firstMax;
                    }
                }
            }
        }

        for (int i = 0; i < table.getHeight(); i++) {
            for (int j = 0; j < table.getWidth(); j++) {
                if (table.getTrafficAt(i, j) <= 0 && !table.isBasic(i, j)) {
                    delta = delta(u[i], v[j], table.getCostAt(i, j));
                    document.add(new Paragraph("delta[" + i + "][" + j + "] = " + delta));
                    if (delta > max) {
                        max = delta;
                        res.i = i;
                        res.j = j;
                        document.add(new Paragraph("max = delta[" + i + "][" + j + "] = " + max));
                    }
                    if (optimal && delta > 0) {
                        optimal = false;
                    }
                }
            }
        }
        if (optimal) {
            res.i = res.j = -1;
        }
        return res;
    }

    private int delta(int u, int v, int cost) {
        return u + v - cost;
    }

    private List<Cell> buildCycle(Cell startCell) throws Exception {
        LinkedList<Cell> res = new LinkedList<>();
        lookHorizontal(res, startCell.i, startCell.j, startCell);
        res.addFirst(startCell);
        if (res.size() < 4) {
            throw new Exception("Cycle is not built!!");
        }
        return res;
    }

    private boolean lookHorizontal(LinkedList<Cell> cycle, int iCell, int jCell, Cell startCell) {
        for (int j = 0; j < table.getWidth(); j++) {
//            if (iCell == startCell.i && j == startCell.j) {
//                return true;
//            }
            if (j == jCell || table.getTrafficAt(iCell, j) <= 0) {
                continue;
            }
            if (lookVertical(cycle, iCell, j, startCell)) {
                cycle.addFirst(new Cell(iCell, j));
                return true;
            }
        }
        return false;
    }

    private boolean lookVertical(LinkedList<Cell> cycle, int iCell, int jCell, Cell startCell) {
        for (int i = 0; i < table.getHeight(); i++) {
            if (i == startCell.i && jCell == startCell.j) {
                return true;
            }
            if (i == iCell || table.getTrafficAt(i, jCell) <= 0) {
                continue;
            }
            if (lookHorizontal(cycle, i, jCell, startCell)) {
                cycle.addFirst(new Cell(i, jCell));
                return true;
            }
        }
        return false;
    }

    private Cell minCycle(List<Cell> cycle) {
        Cell minCell = new Cell(cycle.get(1)); // first is even number
        int min = table.getTrafficAt(minCell.i, minCell.j);
        int cargo;
        for (Cell c : cycle) {
            cargo = table.getTrafficAt(c.i, c.j);
            if (!isEven(c.i, c.j, cycle) && cargo < min) {
                min = cargo;
                minCell.i = c.i;
                minCell.j = c.j;
            }
        }
        return minCell;
    }

    private boolean isEven(int i, int j, List<Cell> cycle) {
        int index = 0;
        for (Cell cell : cycle) {
            if (cell.i == i && cell.j == j) {
                break;
            }
            index++;
        }
        return index % 2 == 0;
    }

    private void changeBasis(List<Cell> cycle, Cell oldCell, Cell newCell) throws DocumentException {
        int teta = table.getTrafficAt(oldCell.i, oldCell.j);
        document.add(new Paragraph("teta = x[" + oldCell.i + "][" + oldCell.j + "] = " + teta));
        int oldValue;
        for (Cell c : cycle) {
            if (c.equals(newCell)) {
                document.add(new Paragraph("x[" + newCell.i + "][" + newCell.j + "] = " + teta));
                table.setTrafficAt(newCell.i, newCell.j, teta);
                continue;
            }
            oldValue = table.getTrafficAt(c.i, c.j);
            if (isEven(c.i, c.j, cycle)) {
                table.setTrafficAt(c.i, c.j, oldValue + teta);
                document.add(new Paragraph("x[" + c.i + "][" + c.j + "] = "
                        + oldValue + " + " + teta + " = " + table.getTrafficAt(c.i, c.j)));
            } else {
                table.setTrafficAt(c.i, c.j, oldValue - teta);
                document.add(new Paragraph("x[" + c.i + "][" + c.j + "] = "
                        + oldValue + " - " + teta + " = " + table.getTrafficAt(c.i, c.j)));
            }
        }
    }
}
