package UI;

import CORE.*;
import EXECUTOR.*;
import PARSER.*;
import PARSER.EXPR.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Relax-like console (with ';' as statement terminator):
 *
 * - You can end any command or query with ';'
 * - Multiple statements on one line are supported: ":tables; :show Employees; π Name (Employees);"
 * - Relation blocks still end at '}', optional ';' after '}' is tolerated.
 */
public class RelaxConsole {
    private final Map<String, InMemoryTable> catalog = new LinkedHashMap<>();

    public static void main(String[] args) throws Exception {
        new RelaxConsole().run();
    }

    public RelaxConsole() { }

    public RelaxConsole(EvaluationContext ctx) {
        if (ctx != null) {
            try {
                var field = EvaluationContext.class.getDeclaredField("catalog");
                field.setAccessible(true);
                @SuppressWarnings("unchecked")
                Map<String, InMemoryTable> m = (Map<String, InMemoryTable>) field.get(ctx);
                catalog.putAll(m);
            } catch (Exception ignored) {}
        }
    }

    public void run() throws Exception {
        System.out.println("RELAX-style console. Type :help ;for commands. Each statement should end with ; or } ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("relax> ");
            String line = br.readLine();
            if (line == null) break;

            // ---- accumulate until we have a complete statement:
            //      relation block => ends with '}', otherwise require a ';'
            while (line != null) {
                String trimmed = line.trim();
                boolean done =
                        trimmed.endsWith("}") || trimmed.endsWith(";");

                if (done) break;

                System.out.print(" "); // continuation prompt
                String next = br.readLine();
                if (next == null) break;
                line += "\n" + next;
            }
            if (line == null) break;
            if (line.trim().isEmpty()) continue;

            // ---- split into statements by ';' (outside quotes/braces) ----
            List<String> statements = splitStatements(line);

            for (String rawStmt : statements) {
                String stmt = rawStmt.trim();
                if (stmt.isEmpty()) continue;

                // strip trailing ';' if present
                if (stmt.endsWith(";")) {
                    stmt = stmt.substring(0, stmt.length() - 1).trim();
                    if (stmt.isEmpty()) continue;
                }

                // ---- commands first (allow with or without ':') ----
                String low = stmt.toLowerCase(Locale.ROOT);
                if (low.equals("exit") || low.equals(":exit") || low.equals("quit") || low.equals(":quit")) {
                    System.out.println("bye");
                    return;
                }
                if (low.equals("help") || low.equals(":help") || low.equals("?")) {
                    printHelp();
                    continue;
                }
                if (low.equals("tables") || low.equals(":tables")) {
                    listTables();
                    continue;
                }
                if (low.startsWith(":show") || low.startsWith("show")) {
                    // handle ":show R" or "show R"
                    int sp = stmt.indexOf(' ');
                    if (sp < 0 || sp == stmt.length() - 1) {
                        System.out.println("Usage: :show <RelationName>");
                        continue;
                    }
                    String name = stmt.substring(sp + 1).trim();
                    InMemoryTable t = catalog.get(name);
                    if (t == null) {
                        System.out.println("Relation \"" + name + "\" does not exist.");
                    } else {
                        TablePrinter.print(t);
                    }
                    continue;
                }

                // ---- relation block? (Relax-style) ----
                if (looksLikeRelationHeader(stmt)) {
                    try {
                        readAndInstallRelation(br, stmt);
                    } catch (IllegalArgumentException e) {
                        System.out.println("! " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("! Failed to load relation: " + e.getMessage());
                    }
                    continue;
                }

                // ---- otherwise: treat as a query ----
                try {
                    EvaluationContext ctx = new EvaluationContext(catalog);
                    Expr ast = new Parser(stmt).parse();
                    InMemoryTable out = new ExprEvaluator(ctx).eval(ast);
                    TablePrinter.print(out);
                } catch (Exception e) {
                    System.out.println("! " + e.getMessage());
                }
            }
        }
    }

    /* -------------------- relation block parsing -------------------- */

    private boolean looksLikeRelationHeader(String line) {
        // e.g. Employees (EID, Name, Age) = {   OR same line with rows (and possibly ending with ';')
        return line.contains("(") && line.contains(")") && line.contains("=") && line.contains("{");
    }

    private void readAndInstallRelation(BufferedReader br, String headerLine) throws Exception {
        // Parse: Name (a, b, c) = { ... }  (optional trailing ';' is tolerated)
        Header h = parseHeader(headerLine);
        List<String> rowLines = new ArrayList<>();

        // If header includes a '}' on same line (inline rows), grab inside
        if (headerLine.contains("}")) {
            String inside = headerLine.substring(headerLine.indexOf('{') + 1, headerLine.lastIndexOf('}'));
            if (!inside.isBlank()) {
                for (String r : inside.split("\\n|;")) {
                    String t = r.trim();
                    if (!t.isEmpty()) rowLines.add(t);
                }
            }
        } else {
            // keep reading lines until a line with '}' is seen
            String row;
            while ((row = br.readLine()) != null) {
                row = row.trim();
                if (row.contains("}")) {
                    String before = row.substring(0, row.indexOf('}')).trim();
                    if (!before.isEmpty()) rowLines.add(before);
                    break;
                }
                if (!row.isEmpty()) rowLines.add(row);
            }
        }

        if (rowLines.isEmpty()) {
            List<Attribute> attrs = new ArrayList<>();
            for (String an : h.attrs) attrs.add(new Attribute(an, DataType.STRING));
            InMemoryTable t = new InMemoryTable(new Schema(attrs));
            catalog.put(h.name, t);
            System.out.println("created empty " + h.name + " :: " + t.schema());
            return;
        }

        Object[] firstVals = parseRow(rowLines.get(0), h.attrs.size());
        List<Attribute> attrs = new ArrayList<>();
        for (int i = 0; i < h.attrs.size(); i++) {
            attrs.add(new Attribute(h.attrs.get(i), inferType(firstVals[i])));
        }
        InMemoryTable t = new InMemoryTable(new Schema(attrs));

        t.add(buildRow(t.schema(), firstVals));

        for (int i = 1; i < rowLines.size(); i++) {
            Object[] vals = parseRow(rowLines.get(i), h.attrs.size());
            InMemoryRow r = buildRow(t.schema(), vals);
            t.add(r);
        }

        catalog.put(h.name, t);
        System.out.println("loaded relation: " + h.name + " :: " + t.schema());
        TablePrinter.print(t);
    }

    private static class Header {
        final String name; final List<String> attrs;
        Header(String n, List<String> a) { name=n; attrs=a; }
    }

    private Header parseHeader(String s) {
        // Name (a, b, c) = {
        int p1 = s.indexOf('('), p2 = s.indexOf(')');
        int eq = s.indexOf('='), lb = s.indexOf('{');
        if (p1 < 0 || p2 < 0 || eq < 0 || lb < 0 || p1 > p2 || p2 > eq || eq > lb)
            throw new IllegalArgumentException("bad relation header");
        String name = s.substring(0, p1).trim();
        String inside = s.substring(p1 + 1, p2).trim();
        List<String> attrs = new ArrayList<>();
        for (String tok : inside.split(",")) {
            String a = tok.trim();
            if (!a.isEmpty()) attrs.add(a);
        }
        if (attrs.isEmpty()) throw new IllegalArgumentException("no attributes in header");
        return new Header(name, attrs);
    }

    private Object[] parseRow(String row, int arity) {
        List<String> toks = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean q = false;
        for (int i = 0; i < row.length(); i++) {
            char c = row.charAt(i);
            if (c == '"') { q = !q; continue; }
            if (!q && c == ',') { toks.add(cur.toString().trim()); cur.setLength(0); }
            else cur.append(c);
        }
        if (cur.length() > 0) toks.add(cur.toString().trim());
        if (toks.size() != arity) {
            throw new IllegalArgumentException("row arity mismatch: expected " + arity + " values");
        }
        Object[] vals = new Object[arity];
        for (int i = 0; i < arity; i++) {
            vals[i] = toks.get(i);
        }
        return vals;
    }

    private InMemoryRow buildRow(Schema schema, Object[] rawVals) {
        InMemoryRow r = InMemoryRow.empty();
        for (int i = 0; i < schema.size(); i++) {
            Attribute a = schema.attribute(i);
            r = r.with(a.name(), coerce(String.valueOf(rawVals[i]), a.type()));
        }
        return r;
    }

    private static DataType inferType(Object v) {
        String s = String.valueOf(v);
        if (looksInt(s)) return DataType.INT;
        if (looksDouble(s)) return DataType.DOUBLE;
        if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false")) return DataType.BOOL;
        return DataType.STRING;
    }

    private static boolean looksInt(String s) {
        try { Integer.parseInt(s); return true; } catch (Exception e) { return false; }
    }
    private static boolean looksDouble(String s) {
        try {
            if (s.contains(".")) { Double.parseDouble(s); return true; }
            return false;
        } catch (Exception e) { return false; }
    }

    private static Object coerce(String raw, DataType t) {
        return switch (t) {
            case INT -> Integer.valueOf(raw);
            case DOUBLE -> Double.valueOf(raw);
            case BOOL -> {
                if (raw.equalsIgnoreCase("true") || raw.equalsIgnoreCase("false"))
                    yield Boolean.valueOf(raw);
                throw new IllegalArgumentException("expected boolean value (true/false): " + raw);
            }
            case STRING -> raw.startsWith("\"") && raw.endsWith("\"")
                    ? raw.substring(1, raw.length()-1)
                    : raw;
        };
    }

    /* -------------------- utilities -------------------- */

    private void listTables() {
        if (catalog.isEmpty()) { System.out.println("(no tables)"); return; }
        for (var e : catalog.entrySet()) {
            System.out.println("- " + e.getKey() + " :: " + e.getValue().schema());
        }
    }

    private static void printHelp() {
        System.out.println("""
        =================== RELAX HELP ===================

        Define a relation (Relax-style):
          R (A, B, C) = {
            a1, b1, c1
            a2, b2, c2
          };

        Relational Algebra Operators (symbols and keywords):

          • Selection (σ / select / sigma)
              σ Age > 30 (Employees);
              select Age > 30 (Employees);

          • Projection (π / project / pi)
              π Name, Email (Employees);
              project Name, Email (Employees);

          • Join (⋈ / join)
              Employees ⋈ EID=SID Takes;
              Employees join EID=SID Takes;

          • Union (⋃ / union)
              A ⋃ B;
              A union B;

          • Intersection (∩ / intersect)
              A ∩ B;
              A intersect B;

          • Difference (− / minus / difference)
              A − B;
              A - B;
              A difference B;

        Example Combined Query:
          π Name (σ Age > 30 (Employees));
          => Select employees older than 30, then project only Name.

        Commands (with ;)
          :tables   List loaded relations
          :show R   Print relation R
          :help     This help menu
          :exit     Quit console

        ==================================================
        """);
    }


    /* ===================== NEW HELPERS ===================== */

    /**
     * Split input into ';'-terminated statements, ignoring semicolons inside quotes or inside a '{...}' block.
     * Keeps the trailing ';' on each returned statement (caller may strip).
     */
    private static List<String> splitStatements(String s) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();

        boolean inQuotes = false;
        int braceDepth = 0; // for { ... }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
                cur.append(c);
                continue;
            }

            if (!inQuotes) {
                if (c == '{') braceDepth++;
                else if (c == '}') braceDepth = Math.max(0, braceDepth - 1);

                if (c == ';' && braceDepth == 0) {
                    // end of a statement
                    cur.append(c);
                    out.add(cur.toString());
                    cur.setLength(0);
                    continue;
                }
            }

            cur.append(c);
        }

        // final chunk (could be a relation block ending with '}' or a naked query without ';')
        String tail = cur.toString().trim();
        if (!tail.isEmpty()) out.add(tail);
        return out;
    }
}
