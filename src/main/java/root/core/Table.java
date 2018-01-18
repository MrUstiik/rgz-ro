package root.core;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Table {
    // rows
    protected int[] stocks;
    // columns
    protected int[] needs;
    protected int[][] costs;
    // number - shipment, 0 - empty, -1 - deleted
    protected int[][] traffics;

    protected int[] startStocks, startNeeds;

    Table(String fName) throws IOException {
        readFromTxt(fName);
    }

    Table(int[] stocks, int[] needs, int[][] costs) throws Exception {
        if (costs.length != stocks.length || costs[0].length != needs.length) {
            throw new Exception("Incorrect data exception!! (stocks[" + stocks.length +
                    "], needs[" + needs.length + "], costs["
                    + costs.length + "][" + costs[0].length + "]).");
        }
        this.stocks = stocks;
        startStocks = stocks.clone();
        this.needs = needs;
        startNeeds = needs.clone();
        this.costs = costs;
        traffics = new int[costs.length][];
        for (int i = 0; i < traffics.length; i++) {
            traffics[i] = new int[costs[0].length];
            for (int j = 0; j < traffics[i].length; j++) {
                traffics[i][j] = 0;
            }
        }
    }

    public int[] getStocks() {
        return stocks.clone();
    }

    public int getStockAt(int i) {
        return stocks[i];
    }

    public void setStockAt(int i, int volume) {
        stocks[i] = volume;
    }

    public int[] getNeeds() {
        return needs.clone();
    }

    public int getNeedAt(int j) {
        return needs[j];
    }

    public void setNeedAt(int j, int volume) {
        needs[j] = volume;
    }

    public int[][] getCosts() {
        return costs.clone();
    }

    public int getCostAt(int i, int j) {
        return costs[i][j];
    }

    public void setCostAt(int i, int j, int value) {
        costs[i][j] = value;
    }

    public int[][] getTraffics() {
        return traffics.clone();
    }

    public int getTrafficAt(int i, int j) {
        return traffics[i][j];
    }

    public void setTrafficAt(int i, int j, int value) {
        traffics[i][j] = value;
    }

    public boolean isBalanced() {
        return getDisbalance() == 0;
    }

    // 0 - balanced, positive - excess, negative - deficit
    private int getDisbalance() {
        int sumStocks = 0, sumNeeds = 0;
        // rows sum
        for (int x : startStocks) {
            sumStocks += x;
        }
        // columns sum
        for (int x : startNeeds) {
            sumNeeds += x;
        }
        return sumStocks - sumNeeds;
    }

    public void toBalanced() {
        int disbalance = getDisbalance();
        if (disbalance == 0)
            return;
        if (disbalance > 0) {
            addFictitiousTarget(disbalance);
        } else {
            addFictitiousSource(-disbalance);
        }
    }

    protected void addFictitiousSource(int deficit) {
        int[] newStocks = new int[stocks.length + 1];
        System.arraycopy(stocks, 0, newStocks, 0, stocks.length);
        newStocks[newStocks.length - 1] = deficit;
        stocks = newStocks;
        startStocks = newStocks.clone();

        int[][] newCosts = new int[costs.length + 1][costs[0].length];
        int[][] newTraffics = new int[costs.length + 1][costs[0].length];
        for (int i = 0; i < costs.length; i++) {
            System.arraycopy(costs[i], 0, newCosts[i], 0, costs[0].length);
            System.arraycopy(traffics[i], 0, newTraffics[i], 0, costs[0].length);
        }
        Arrays.fill(newCosts[newCosts.length - 1], 0);
        Arrays.fill(newTraffics[newTraffics.length - 1], 0);
        costs = newCosts;
        traffics = newTraffics;
    }

    protected void addFictitiousTarget(int excess) {
        int[] newNeeds = new int[needs.length + 1];
        System.arraycopy(needs, 0, newNeeds, 0, needs.length);
        newNeeds[newNeeds.length - 1] = excess;
        needs = newNeeds;
        startNeeds = newNeeds.clone();

        int[][] newCosts = new int[costs.length][costs[0].length + 1];
        int[][] newTraffics = new int[costs.length][costs[0].length + 1];
        for (int i = 0; i < costs.length; i++) {
            System.arraycopy(costs[i], 0, newCosts[i], 0, costs[0].length);
            newCosts[i][newCosts[0].length - 1] = 0;
            System.arraycopy(traffics[i], 0, newTraffics[i], 0, traffics[0].length);
            newTraffics[i][newTraffics[0].length - 1] = 0;
        }
        costs = newCosts;
        traffics = newTraffics;
    }

    public void deleteRow(int i) {
        for (int j = 0; j < getWidth(); j++) {
            if (traffics[i][j] == 0) {
                traffics[i][j] = -1;
            }
        }
        stocks[i] = 0;
    }

    public void deleteColumn(int j) {
        for (int i = 0; i < getHeight(); i++) {
            if (traffics[i][j] == 0) {
                traffics[i][j] = -1;
            }
        }
        needs[j] = 0;
    }

    public int getWidth() {
        return startNeeds.length;
    }

    public int getHeight() {
        return startStocks.length;
    }

    public int getRang() {
        return getHeight() + getWidth() - 1;
    }

    public void reset() {
        for (int i = 0; i < getHeight(); i++) {
            stocks[i] = startStocks[i];
        }
        for (int j = 0; j < getWidth(); j++) {
            needs[j] = startNeeds[j];
        }
    }

    public void fullReset() {
        reset();
        for (int i = 0; i < traffics.length; i++) {
            traffics[i] = new int[costs[0].length];
            for (int j = 0; j < traffics[i].length; j++) {
                traffics[i][j] = 0;
            }
        }
    }

    public List<Cell> getBasis() {
        List<Cell> res = new LinkedList<>();
        basis:
        for (int i = 0; i < getHeight(); i++) {
            for (int j = 0; j < getWidth(); j++) {
                if (getTrafficAt(i, j) > 0) {
                    res.add(new Cell(i, j));
                    if (res.size() == getRang()) {
                        break basis;
                    }
                }
            }
        }
        return res;
    }

    public Cell getFirstBasicCell() {
        for (int i = 0; i < getHeight(); i++) {
            for (int j = 0; j < getWidth(); j++) {
                if (getTrafficAt(i, j) > 0) {
                    return new Cell(i, j);
                }
            }
        }
        return null;
    }

    public boolean isBasic(int i, int j) {
        return traffics[i][j] > 0;
    }

    private boolean inCycle(int i, int j, List<Cell> cycle) {
        for (Cell cell :
                cycle) {
            if (cell.i == i && cell.j == j) {
                return true;
            }
        }
        return false;
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

    public void readFromTxt(String fName) throws IOException {
        FileReader fr = new FileReader(fName);
        BufferedReader br = new BufferedReader(fr);
        String line = br.readLine();
        String[] sArr = line.split(" ");
        stocks = new int[sArr.length];
        startStocks = new int[sArr.length];
        int index = 0;
        for (String s : sArr) {
            startStocks[index] = stocks[index] = Integer.parseInt(s);
            index++;
        }
        line = br.readLine();
        sArr = line.split(" ");
        needs = new int[sArr.length];
        startNeeds = new int[sArr.length];
        index = 0;
        for (String s : sArr) {
            startNeeds[index] = needs[index] = Integer.parseInt(s);
            index++;
        }
        costs = new int[stocks.length][needs.length];
        traffics = new int[stocks.length][needs.length];
        for (int i = 0; i < costs.length; i++) {
            line = br.readLine();
            sArr = line.split(" ");
            for (int j = 0; j < costs[i].length; j++) {
                costs[i][j] = Integer.parseInt(sArr[j]);
                traffics[i][j] = 0;
            }
        }
    }

    public void saveToPdf(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(getWidth() + 2);
        table.setKeepTogether(true);

        table.addCell("");
        for (int j = 0; j < getWidth(); j++) {
            table.addCell(j + "");
        }
        table.addCell("Stocks");

        for (int i = 0; i < getHeight(); i++) {
            table.addCell(i + "");
            for (int j = 0; j < getWidth() + 1; j++) {
                if (j == getWidth()) {
                    table.addCell(startStocks[i] + "");
                    break;
                }
                table.addCell(costs[i][j] + (traffics[i][j] > 0 ? "[" + traffics[i][j] + "]" : ""));
            }
        }

        table.addCell("Needs");
        for (int j = 0; j < getWidth(); j++) {
            table.addCell(startNeeds[j] + "");
        }
        table.addCell("");

        document.add(new Paragraph(" "));
        document.add(table);
        document.add(new Paragraph(" "));
    }

    public void saveBufferToPdf(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(getWidth() + 1);
        table.setKeepTogether(true);

        for (int i = 0; i < getHeight(); i++) {
            for (int j = 0; j < getWidth() + 1; j++) {
                if (j == getWidth()) {
                    table.addCell(stocks[i] + "");
                    continue;
                }
                switch (traffics[i][j]) {
                    case -1:
                        table.addCell("-");
                        break;
                    default:
                        table.addCell(costs[i][j] + "");
                }
            }
        }
        for (int j = 0; j < getWidth(); j++) {
            table.addCell(needs[j] + "");
        }
        table.addCell("");

        document.add(new Paragraph(" "));
        document.add(table);
        document.add(new Paragraph(" "));
    }

    public void saveVogelsToPdf(Document document, int[] additionalRow, int[] additionalColumn) throws DocumentException {
        PdfPTable table = new PdfPTable(getWidth() + 3);
        table.setKeepTogether(true);

        table.addCell("");
        for (int j = 0; j < getWidth(); j++) {
            table.addCell(j + "");
        }
        table.addCell("Stocks");
        table.addCell("Fines");

        for (int i = 0; i < getHeight(); i++) {
            table.addCell(i + "");
            for (int j = 0; j <= getWidth() + 1; j++) {
                if (j == getWidth()) {
                    table.addCell(getStockAt(i) + "");
                    continue;
                }
                if (j == getWidth() + 1) {
                    table.addCell(additionalColumn[i] >= 0 ? additionalColumn[i] + "" : "-");
                    break;
                }
                switch (getTrafficAt(i, j)) {
                    case -1:
                        table.addCell("-");
                        break;
                    default:
                        table.addCell(getCostAt(i, j) + "");
                }
            }
        }

        table.addCell("Needs");
        for (int j = 0; j < getWidth(); j++) {
            table.addCell(getNeedAt(j) + "");
        }
        table.addCell("");
        table.addCell("");

        table.addCell("Fines");
        for (int j = 0; j < getWidth(); j++) {
            table.addCell(additionalRow[j] >= 0 ? additionalRow[j] + "" : "-");
        }
        table.addCell("");
        table.addCell("");

        document.add(new Paragraph(" "));
        document.add(table);
        document.add(new Paragraph(" "));
    }

    public void savePotentialsToPdf(Document document, Integer[] u, Integer[] v) throws DocumentException {
        PdfPTable table = new PdfPTable(getWidth() + 1);
        table.setKeepTogether(true);

        table.addCell("");
        for (int j = 0; j < getWidth(); j++) {
            table.addCell("v[" + j + "]=" + v[j]);
        }

        for (int i = 0; i < getHeight(); i++) {
            table.addCell("u[" + i + "]=" + u[i]);
            for (int j = 0; j < getWidth(); j++) {
                table.addCell(costs[i][j] + (traffics[i][j] > 0 ? "[" + traffics[i][j] + "]" : ""));
            }
        }

        document.add(new Paragraph(" "));
        document.add(table);
        document.add(new Paragraph(" "));
    }

    public void saveCycleToPdf(Document document, List<Cell> cycle) throws DocumentException {
        PdfPTable table = new PdfPTable(getWidth() + 1);
        table.setKeepTogether(true);

        table.addCell("");
        for (int j = 0; j < getWidth(); j++) {
            table.addCell(j + "");
        }

        for (int i = 0; i < getHeight(); i++) {
            table.addCell(i + "");
            for (int j = 0; j < getWidth(); j++) {
                if (inCycle(i, j, cycle)) {
                    table.addCell(costs[i][j]
                            + (traffics[i][j] > 0 ? "[" + traffics[i][j] + "]" : "")
                            + (isEven(i, j, cycle) ? "[+]" : "[-]"));
                } else {
                    table.addCell(costs[i][j]
                            + (traffics[i][j] > 0 ? "[" + traffics[i][j] + "]" : ""));
                }
            }
        }

        table.addCell("");

        document.add(new Paragraph(" "));
        document.add(table);
        document.add(new Paragraph(" "));
    }
}
