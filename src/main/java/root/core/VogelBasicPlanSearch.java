package root.core;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VogelBasicPlanSearch extends AbstractBasicPlanSearch {
    private int[] aRow, aColumn;

    @Override
    protected void findBasic() throws Exception {
        init();
        document.add(new Paragraph("Vogel's Basic Plan Search Method."));
        int diff;
        byte counter = 1;
        cell = new Cell();
        do {
            document.add(new Paragraph("Iteration number " + counter));
            findCell();
            table.saveVogelsToPdf(document, aRow, aColumn);
            document.add(new Paragraph("x[" + cell.i + "][" + cell.j + "]=" + table.getCostAt(cell.i, cell.j)));
            diff = table.getNeedAt(cell.j) - table.getStockAt(cell.i);
            if (diff < 0) {
                document.add(new Paragraph(table.getStockAt(cell.i) + "-" + table.getNeedAt(cell.j) + "=" + -diff
                        + ". Delete " + cell.j + " column."));
                table.setStockAt(cell.i, -diff);
                table.setTrafficAt(cell.i, cell.j, table.getNeedAt(cell.j));
                table.deleteColumn(cell.j);
            } else {
                document.add(new Paragraph(table.getNeedAt(cell.j) + "-" + table.getStockAt(cell.i) + "=" + diff
                        + ". Delete " + cell.i + " row."));
                table.setTrafficAt(cell.i, cell.j, table.getStockAt(cell.i));
                table.setNeedAt(cell.j, diff);
                table.deleteRow(cell.i);
            }
            table.saveBufferToPdf(document);
        } while (++counter <= table.getRang());
        document.add(new Paragraph("Basic plan:"));
        table.saveToPdf(document);
    }

    private void findCell() throws DocumentException {
        int[][] columns = new int[table.getWidth()][table.getHeight()];
        document.add(new Paragraph("Fines of rows:"));
        for (int i = 0; i < table.getHeight(); i++) {
            if (table.getStockAt(i) > 0) {
                List<Integer> list = toList(table.getCosts()[i]);
                removeDeletedForRow(list, i);
                int[] mins = findTwoMins(list);
                aColumn[i] = mins[1] != 0 ? mins[1] - mins[0] : mins[0];
                document.add(new Paragraph("dr[" + i + "]= " + mins[1] + " - " + mins[0] + " = " + aColumn[i]));

                for (int j = 0; j < table.getWidth(); j++) {
                    columns[j][i] = table.getCostAt(i, j);
                }
            } else {
                aColumn[i] = -1;
                for (int j = 0; j < table.getWidth(); j++) {
                    columns[j][i] = -1;
                }
            }
        }

        document.add(new Paragraph("Fines of columns:"));
        for (int j = 0; j < columns.length; j++) {
            if (table.getNeedAt(j) > 0) {
                List<Integer> list = toList(columns[j]);
                removeDeletedForColumn(list, j);
                int[] mins = findTwoMins(list);
                aRow[j] = mins[1] != 0 ? mins[1] - mins[0] : mins[0];
                ;
                document.add(new Paragraph("dc[" + j + "]= " + mins[1] + " - " + mins[0] + " = " + aRow[j]));
            } else {
                aRow[j] = -1;
            }
        }

        int[] maxes = new int[2];
        maxes[0] = max(aRow);
        maxes[1] = max(aColumn);
        if (maxes[0] > maxes[1]) {
            document.add(new Paragraph("max(" + maxes[0] + ", " + maxes[1] + ") = " + maxes[0]));
            cell.j = toList(aRow).indexOf(maxes[0]);
            cell.i = indexOfMinInColumn(cell.j);
        } else {
            document.add(new Paragraph("max(" + maxes[0] + ", " + maxes[1] + ") = " + maxes[1]));
            cell.i = toList(aColumn).indexOf(maxes[1]);
            cell.j = indexOfMinInRow(cell.i);
        }
        document.add(new Paragraph("min[" + cell.i + "][" + cell.j + "]=" + table.getCostAt(cell.i, cell.j)));
    }

    private int[] findTwoMins(int[] arr) {
        return findTwoMins(toList(arr));

    }

    private int[] findTwoMins(List<Integer> list) {
        list = new ArrayList<>(list);
        int[] res = new int[2];
        res[0] = min(list);
        res[1] = list.isEmpty() ? 0 : min(list);
        return res;
    }

    private int max(int[] arr) {
        return Collections.max(toList(arr));
    }

    private int min(int[] arr) {
        return min(toList(arr));
    }

    private int min(List<Integer> list) {
        int res;
        do {
            res = Collections.min(list);
            list.remove(list.indexOf(res));
        } while (res <= 0 && !list.isEmpty());
        return res;
    }

    private void removeDeletedForRow(List<Integer> row, int rowIndex) {
        int deleted = 0;
        for (int j = 0; j < table.getWidth(); j++) {
            if (table.getTrafficAt(rowIndex, j) != 0) {
                row.remove(j - deleted++);
            }
        }
    }

    private void removeDeletedForColumn(List<Integer> column, int columnIndex) {
        int deleted = 0;
        for (int i = 0; i < table.getHeight(); i++) {
            if (table.getTrafficAt(i, columnIndex) != 0) {
                column.remove(i - deleted++);
            }
        }
    }

    private List<Integer> toList(int[] arr) {
        return IntStream.of(arr).boxed().collect(Collectors.toList());
    }

    private int indexOfMinInRow(int rowIndex) {
        int min = 0;
        int res = -1;
        for (int j = 0; j < table.getWidth(); j++) {
            if (table.getTrafficAt(rowIndex, j) == 0) {
                min = table.getCostAt(rowIndex, j);
                res = j;
            }
        }
        for (int j = 0; j < table.getWidth(); j++) {
            int cost = table.getCostAt(rowIndex, j);
            if (table.getTrafficAt(rowIndex, j) == 0 && cost < min) {
                min = cost;
                res = j;
            }
        }
        return res;
    }

    private int indexOfMinInColumn(int columnIndex) {
        int min = 0;
        int res = -1;
        for (int i = 0; i < table.getHeight(); i++) {
            if (table.getTrafficAt(i, columnIndex) == 0) {
                min = table.getCostAt(i, columnIndex);
                res = i;
            }
        }
        for (int i = 0; i < table.getHeight(); i++) {
            int cost = table.getCostAt(i, columnIndex);
            if (table.getTrafficAt(i, columnIndex) == 0 && cost < min) {
                min = cost;
                res = i;
            }
        }
        return res;
    }

    @Override
    protected void init() throws Exception {
        super.init();
        aRow = new int[table.getWidth()];
        aColumn = new int[table.getHeight()];
    }
}
