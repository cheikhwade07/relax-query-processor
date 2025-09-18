package EXECUTOR;
import PARSER.EXPR.*;
import java.util.Map;

/**
 * EvaluationContext
 *
 * <p>Holds the execution catalog — a mapping from relation names to
 * in-memory base tables — used by the executor when resolving
 * {@link PARSER.EXPR.RelationRef} nodes.</p>
 *
 * <h3>Responsibilities</h3>
 * <ul>
 *   <li>Provide a stable lookup from relation name → base table.</li>
 *   <li>Fail fast with a clear error when a relation is unknown.</li>
 * </ul>
 *
 * <h3>Collaborators</h3>
 * <ul>
 *   <li>{@link EXECUTOR.ExprEvaluator}: calls {@link #table(String)} during evaluation.</li>
 *   <li>{@link EXECUTOR.InMemoryTable}: concrete table stored in the catalog.</li>
 * </ul>
 *
 * <h3>Usage (by APP)</h3>
 * <pre>{@code
 * var ctx = new EvaluationContext(Map.of(
 *     "Employees", employeesTable,
 *     "Department", departmentTable
 * ));
 * var result = new ExprEvaluator(ctx).eval(ast);
 * }</pre>
 *
 * @author Seydi Cheikh Wade: 101323727
 * @version Assignment Bonus, 9/17/2025
 */

public final class EvaluationContext {
    /** Immutable catalog: relation name → base table. */
    private final Map<String, InMemoryTable> catalog;

    /**
     * Create a new context with a catalog of base relations.
     * @param catalog map of relation names to tables; will be defensively copied
     */
    public EvaluationContext(Map<String, InMemoryTable> catalog) {
        this.catalog = Map.copyOf(catalog);
    }

    /**
     * Resolve a relation by name.
     * @param name logical name used in queries (e.g., "Employees")
     * @return the base {@link InMemoryTable}
     * @throws IllegalArgumentException if no such relation exists
     */
    public InMemoryTable table(String name) {
        InMemoryTable t = catalog.get(name);
        if (t == null) {
            throw new IllegalArgumentException("Unknown relation: " + name);
        }
        return t;
    }
}
