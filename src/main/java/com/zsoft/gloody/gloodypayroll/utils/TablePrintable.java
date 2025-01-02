package com.zsoft.gloody.gloodypayroll.utils;

import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipElementDto;

import java.math.BigDecimal;
import java.util.LinkedList;

public class TablePrintable {
    private TablePrintable() {
        throw new IllegalStateException("Utility class");
    }

    public static String formatBigDecimal(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    public static String[][] toObjects(LinkedList<PayslipElementDto> elements) {
        String[][] data = new String[elements.size()][];
        for (int i = 0; i < elements.size(); i++) {
            PayslipElementDto el = elements.get(i);
            data[i] = new String[]{el.getCode(), el.getLabel(), formatBigDecimal(el.getBase()), formatBigDecimal(el.getRate()), formatBigDecimal(el.getGain()), formatBigDecimal(el.getDeduction())};
        }
        return data;
    }

    public static void print(LinkedList<PayslipElementDto> elements) {
        String[] columnNames = new String[]{"Code", "Libelle", "N / Base", "Taux", "Gain", "Retenue"};
        int[] maxLengths = new int[columnNames.length];
        for (int i = 0; i < columnNames.length; i++) {
            String cell = columnNames[i];
            maxLengths[i] = Math.max(maxLengths[i], cell.length() + 1);
        }
        String[][] rows = toObjects(elements);
        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                if (row[i] != null) {
                    String cell = row[i];
                    maxLengths[i] = Math.max(maxLengths[i], cell.length() + 1);
                }
            }
        }
        StringBuilder formatBuilder = new StringBuilder();
        int lineLength = 0;
        for (int i = 0; i < maxLengths.length; i++) {
            int maxLength = maxLengths[i];
            formatBuilder.append("| %-").append(maxLength + 2).append("s");
            lineLength += maxLength + 4;
            if (i == maxLengths.length - 1) {
                formatBuilder.append(" |");
                lineLength += 2;
            }
        }
        String format = formatBuilder.toString();
        String line = String.format("%-" + lineLength + "s", "");
        String tappedLine = line.replace(' ', '-') + "\n";
        String dashedLine = line.replace(' ', '=') + "\n";

        StringBuilder result = new StringBuilder();
        result.append(tappedLine);
        result.append(String.format(format, columnNames)).append("\n");
        result.append(dashedLine);
        for (Object[] row : rows) {
            result.append(String.format(format, row)).append("\n");
        }
        result.append(tappedLine);
        System.out.println(result);
    }

}
