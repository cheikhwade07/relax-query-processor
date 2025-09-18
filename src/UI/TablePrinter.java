package UI;

import CORE.Schema;
import EXECUTOR.InMemoryRow;
import EXECUTOR.InMemoryTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class TablePrinter {
    private TablePrinter() {}

    public static void print(InMemoryTable table) {
        Schema schema = table.schema();
        if (schema == null) { System.out.println("(no schema)"); return; }

        // headers
        List<String> headers = new ArrayList<>();
        schema.attributes().forEach(a -> headers.add(a.name()));

        // rows
        List<List<String>> rows = new ArrayList<>();
        for (InMemoryRow r : table.rows()) {
            Map<String,Object> m = r.asMap();
            List<String> line = new ArrayList<>();
            for (String h : headers) {
                Object v = m.get(h);
                line.add(v == null ? "" : String.valueOf(v));
            }
            rows.add(line);
        }

        // widths
        int[] w = new int[headers.size()];
        for (int i = 0; i < headers.size(); i++) w[i] = headers.get(i).length();
        for (var row : rows) for (int i = 0; i < row.size(); i++) w[i] = Math.max(w[i], row.get(i).length());

        // draw
        String sep = sep(w);
        System.out.println(sep);
        System.out.print("|");
        for (int i = 0; i < headers.size(); i++) System.out.print(" " + pad(headers.get(i), w[i]) + " |");
        System.out.println();
        System.out.println(sep);
        for (var row : rows) {
            System.out.print("|");
            for (int i = 0; i < row.size(); i++) System.out.print(" " + pad(row.get(i), w[i]) + " |");
            System.out.println();
        }
        System.out.println(sep);
        System.out.println("(rows: " + rows.size() + ")");
    }

    private static String pad(String s, int w) { return s + " ".repeat(Math.max(0, w - s.length())); }
    private static String sep(int[] w) {
        StringBuilder sb = new StringBuilder("+");
        for (int width : w) sb.append("-".repeat(width + 2)).append("+");
        return sb.toString();
    }
}


