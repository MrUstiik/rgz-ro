package root.core;

import com.itextpdf.text.Paragraph;

public class MinCostBasicPlanSearch extends AbstractBasicPlanSearch {
    @Override
    protected void findBasic() throws Exception {
        init();
        document.add(new Paragraph("Minimal Cost Basic Plan Search Method."));
        int diff;
        byte counter = 1;
        do {
            document.add(new Paragraph("Iteration number " + counter));
            findMinCostCell();
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

    private void findMinCostCell() {
        cell = getFirstUndeletedCell();
        int min = table.getCostAt(cell.i, cell.j);
        for (int i = 0; i < table.getHeight(); i++) {
//            if (Arrays.asList(table.getTraffics()[i]).contains(-1)) {
            if(isDeletedRow(i)){
                continue;
            }
            for (int j = 0; j < table.getWidth(); j++) {
                if (!inDeletedColumn(j)
                        && table.getCostAt(i, j) != 0
                        && table.getCostAt(i, j) < min) {
                    min = table.getCostAt(i, j);
                    cell.i = i;
                    cell.j = j;
                }
            }
        }
    }

    private Cell getFirstUndeletedCell() {
//        for (int i = 0; i < table.getTraffics().length; i++) {
//            for (int j = 0; j < table.getTraffics()[0].length; j++) {
//                if (table.getTrafficAt(i, j) == 0) {
//                    return new Cell(i, j);
//                }
//                if(table.getTrafficAt(i,j) == -1){
//                    break;
//                }
//            }
//        }
        Cell res = new Cell(0, 0);
        while (table.getStocks()[res.i] == 0) {
            res.i++;
        }
        while (table.getNeeds()[res.j] == 0) {
            res.j++;
        }
        return res;
    }

    private boolean isDeletedRow(int i){
        return table.getStocks()[i] == 0;
    }

    private boolean inDeletedColumn(int j){
        return table.getNeeds()[j] == 0;
    }
}
