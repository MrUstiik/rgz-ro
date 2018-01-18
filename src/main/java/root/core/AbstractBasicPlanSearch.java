package root.core;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;

public abstract class AbstractBasicPlanSearch {
    protected Table table;
    protected Cell cell;

    protected Document document;

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) throws Exception {
        this.table = table;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public void findBasicPlan() throws Exception{
        findBasic();
        table.reset();
    }

    protected abstract void findBasic() throws Exception;

    protected void init() throws Exception {
        if(table == null){
            throw new Exception("No data!!");
        }
        document.add(new Paragraph("Input table:"));
        table.saveToPdf(document);
        if (!table.isBalanced()) {
            document.add(new Paragraph("Table is not balanced.\nBalanced table:"));
            table.toBalanced();
            table.saveToPdf(document);
        }
    }
}
