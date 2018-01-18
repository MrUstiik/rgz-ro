package root.core;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Document document = new Document();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter filename of task with full path: ");
        String fNameIn = scanner.nextLine();
//        String fNameIn = "E:\\Projects\\IntelliJ IDEA\\RGZ-RO\\src\\main\\java\\root\\core\\source data\\task1.txt";
        System.out.println("Enter output filename with path (without file type): ");
        String fNameOut = scanner.nextLine();
//        String fNameOut = "E:\\Projects\\IntelliJ IDEA\\RGZ-RO\\task1";
        try {
            Table table = new Table(fNameIn);
            PdfWriter.getInstance(document,
                    new FileOutputStream(fNameOut + " ("
                            + DateFormat.getDateInstance().format(new Date())
                            + ").pdf"));
            PotentialMethod potentialMethod = new PotentialMethod();
            potentialMethod.setTable(table);
            potentialMethod.setDocument(document);
            document.open();
            AbstractBasicPlanSearch[] arr = new AbstractBasicPlanSearch[]{
                            new NorthWestBasicPlanSearch(),
                            new MinCostBasicPlanSearch(),
                            new VogelBasicPlanSearch()};
            for (AbstractBasicPlanSearch search : arr) {
                potentialMethod.setBasicPlanSearch(search);
                potentialMethod.findOptimalBasicPlan();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }
}