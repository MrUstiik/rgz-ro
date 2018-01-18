package root.core;

import com.itextpdf.text.Paragraph;

public class NorthWestBasicPlanSearch extends AbstractBasicPlanSearch {
    @Override
    protected void findBasic() throws Exception {
        init();
        document.add(new Paragraph("North-West Basic Plan Search Method."));
        cell = new Cell(0, 0);
        int diff;
        int counter = 0;
        do {
            document.add(new Paragraph("Iteration number " + ++counter));
            document.add(new Paragraph("x[" + cell.i + "][" + cell.j + "]=" + table.getCostAt(cell.i, cell.j)));
            diff = table.getNeedAt(cell.j) - table.getStockAt(cell.i);
            if (diff < 0) {
                document.add(new Paragraph(table.getStockAt(cell.i) + "-" + table.getNeedAt(cell.j) + "=" + -diff
                        + ". Delete " + cell.j + " column."));
                table.setStockAt(cell.i, -diff);
                table.setTrafficAt(cell.i, cell.j, table.getNeedAt(cell.j));
                table.deleteColumn(cell.j);
                cell.j++;
            } else {
                document.add(new Paragraph(table.getNeedAt(cell.j) + "-" + table.getStockAt(cell.i) + "=" + diff
                        + ". Delete " + cell.i + " row."));
                table.setTrafficAt(cell.i, cell.j, table.getStockAt(cell.i));
                table.setNeedAt(cell.j, diff);
                table.deleteRow(cell.i);
                cell.i++;
            }
            table.saveBufferToPdf(document);
        } while (cell.i < table.getHeight() && cell.j < table.getWidth());
        document.add(new Paragraph("Basic plan:"));
        table.saveToPdf(document);
    }
}
