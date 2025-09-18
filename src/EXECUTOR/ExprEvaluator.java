package EXECUTOR;
import  java.util.*;
import CORE.*;
import PARSER.EXPR.*;
/**
 * ExprEvaluator
 *
 * <p>The execution engine. Recursively evaluates a parsed {@link PARSER.EXPR.Expr}
 * abstract syntax tree (AST) into an {@link InMemoryTable}, applying relational
 * algebra operators over in-memory tables.</p>
 *
 * <h3>Supported operators</h3>
 * <ul>
 *   <li>Selection (σ) — filters rows by a boolean condition expression.</li>
 *   <li>Projection (π) — reduces the schema to a specified attribute list.</li>
 *   <li>Rename (ρ) — renames a relation; currently treated as a no-op on schema.</li>
 *   <li>Join (⋈) — nested-loop join; supports both natural joins and θ-joins with conditions.</li>
 *   <li>Set operations — UNION (∪), INTERSECT (∩), MINUS (−), with schema compatibility checks.</li>
 *   <li>Base relations — {@link PARSER.EXPR.RelationRef} nodes resolved through the {@link EvaluationContext} catalog.</li>
 * </ul>
 *
 * <h3>Design</h3>
 * <ul>
 *   <li>Recursive: children are always evaluated before parent operators.</li>
 *   <li>Tuple-at-a-time algorithms (nested loop, row copying), adequate for small datasets.</li>
 *   <li>Uses only core Java collections and custom in-memory data structures.</li>
 *   <li>Delegates schema checks to {@link CORE.Schema} (e.g., set op compatibility).</li>
 * </ul>
 *
 * <h3>Collaborators</h3>
 * <ul>
 *   <li>{@link EXECUTOR.EvaluationContext} — maps relation names to base tables.</li>
 *   <li>{@link PARSER.EXPR.Expr} and subclasses — the AST node hierarchy being executed.</li>
 *   <li>{@link CORE.Schema}, {@link CORE.Attribute}, {@link CORE.DataType} — schema definition and type checking.</li>
 *   <li>{@link EXECUTOR.InMemoryTable}, {@link EXECUTOR.InMemoryRow} — runtime relational data model.</li>
 *   <li>{@code APP} package — orchestrates parse → execute pipeline and provides initial context.</li>
 *   <li>{@code UI} package — consumes results for display.</li>
 * </ul>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * var ctx = new EvaluationContext(Map.of("Employees", employeesTable));
 * var evaluator = new ExprEvaluator(ctx);
 * InMemoryTable result = evaluator.eval(ast);
 * }</pre>
 *
 * @author Seydi Cheikh Wade: 101323727
 * @version Assignment Bonus, 9/17/2025
 */



public class ExprEvaluator {

    private final EvaluationContext ctx;

    public ExprEvaluator(EvaluationContext ctx) {
        this.ctx = ctx;
    }

    /** Entry point: evaluate any Expr AST into an InMemoryTable */
    public InMemoryTable eval(Expr e) {
        if (e instanceof RelationRef r) {
            return ctx.table(r.name());
        }
        if (e instanceof Selection s) {
            InMemoryTable child = eval(s.child());
            return evalSelection(s.condition(), child);
        }
        if (e instanceof Projection p) {
            InMemoryTable child = eval(p.child());
            return evalProjection(p.attrs(), child);
        }
        if (e instanceof Rename r) {
            InMemoryTable child = eval(r.child());
            return evalRename(r.newName(), child);
        }
        if (e instanceof Join j) {
            InMemoryTable left = eval(j.left());
            InMemoryTable right = eval(j.right());
            return evalJoin(left, right, j.on());
        }
        if (e instanceof SetOp s) {
            InMemoryTable left = eval(s.left());
            InMemoryTable right = eval(s.right());
            return switch (s.kind()) {
                case UNION     -> evalUnion(left, right);
                case INTERSECT -> evalIntersect(left, right);
                case MINUS     -> evalMinus(left, right);
            };
        }
        throw new UnsupportedOperationException("Unknown expr: " + e);
    }

    // --- Selection (σ) ---
    private InMemoryTable evalSelection(Expr cond, InMemoryTable input) {
        InMemoryTable out = new InMemoryTable(input.schema());
        for (InMemoryRow row : input.rows()) {
            Object val = cond.eval(row.asMap());
            if (val instanceof Boolean b && b) {
                out.add(row);
            }
        }
        return out;
    }

    // --- Projection (π) ---
    private InMemoryTable evalProjection(List<String> attrs, InMemoryTable input) {
        // build new schema with selected attributes
        List<Attribute> projectedAttrs = new ArrayList<>();
        for (String a : attrs) {
            projectedAttrs.add(input.schema().attribute(input.schema().indexOf(a)));
        }
        Schema schema = new Schema(projectedAttrs);

        InMemoryTable out = new InMemoryTable(schema);
        for (InMemoryRow row : input.rows()) {
            InMemoryRow projected = InMemoryRow.empty();
            for (String a : attrs) {
                projected = projected.with(a, row.asMap().get(a));
            }
            out.add(projected);
        }
        return out;
    }

    // --- Rename (ρ) ---
    private InMemoryTable evalRename(String newName, InMemoryTable input) {
        // Your Schema doesn’t currently track relation names,
        // so rename is effectively a no-op unless you extend Schema.
        return input;
    }

    // --- Join (⋈) ---
    private InMemoryTable evalJoin(InMemoryTable left, InMemoryTable right, Expr on) {
        Schema schema = Schema.merge(left.schema(), right.schema());
        InMemoryTable out = new InMemoryTable(schema);

        for (InMemoryRow lrow : left.rows()) {
            for (InMemoryRow rrow : right.rows()) {
                // build combined row
                InMemoryRow combined = InMemoryRow.empty();
                for (var entry : lrow.asMap().entrySet()) {
                    combined = combined.with(entry.getKey(), entry.getValue());
                }
                for (var entry : rrow.asMap().entrySet()) {
                    combined = combined.with(entry.getKey(), entry.getValue());
                }

                boolean ok = true;
                if (on != null) {
                    Object val = on.eval(combined.asMap());
                    ok = (val instanceof Boolean b && b);
                }
                if (ok) out.add(combined);
            }
        }
        return out;
    }

    // --- Set operations ---
    private InMemoryTable evalUnion(InMemoryTable a, InMemoryTable b) {
        Schema.checkCompatible(a.schema(), b.schema());
        InMemoryTable out = new InMemoryTable(a.schema());

        Set<InMemoryRow> seen = new HashSet<>();
        for (InMemoryRow row : a.rows()) {
            if (seen.add(row)) {
                out.add(row);
            }
        }
        for (InMemoryRow row : b.rows()) {
            if (seen.add(row)) {
                out.add(row);
            }
        }
        return out;
    }


    private InMemoryTable evalIntersect(InMemoryTable a, InMemoryTable b) {
        Schema.checkCompatible(a.schema(), b.schema());
        InMemoryTable out = new InMemoryTable(a.schema());
        for (InMemoryRow row : a.rows()) {
            if (b.rows().contains(row)) out.add(row);
        }
        return out;
    }

    private InMemoryTable evalMinus(InMemoryTable a, InMemoryTable b) {
        Schema.checkCompatible(a.schema(), b.schema());
        InMemoryTable out = new InMemoryTable(a.schema());
        for (InMemoryRow row : a.rows()) {
            if (!b.rows().contains(row)) out.add(row);
        }
        return out;
    }
}
