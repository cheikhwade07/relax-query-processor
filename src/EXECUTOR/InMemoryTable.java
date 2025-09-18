package EXECUTOR;
import CORE.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

/**
 * InMemoryTable
 *
 * <p>A list-backed relation that carries a {@link CORE.Schema} and a set of
 * {@link InMemoryRow} tuples. Enforces that inserted rows match the schema
 * (no missing/extra attributes; shallow Java-type checks).</p>
 *
 * <h3>Responsibilities</h3>
 * <ul>
 *   <li>Store rows produced/consumed by execution operators.</li>
 *   <li>Carry the schema describing attribute names and types.</li>
 *   <li>Validate inserted rows against the schema.</li>
 *   <li>Provide {@code newEmpty(Schema)} for operators that change headings.</li>
 * </ul>
 *
 * <h3>Collaborators</h3>
 * <ul>
 *   <li>{@link EXECUTOR.ExprEvaluator} — reads/writes rows during σ/π/⋈/∪/∩/−.</li>
 *   <li>{@link CORE.Schema}/{@link CORE.Attribute}/{@link CORE.DataType} — define/verify heading and types.</li>
 *   <li>{@link APP} (package) — seeds base tables in the catalog.</li>
 *   <li>{@link UI}  (package) — iterates rows to display results.</li>
 * </ul>
 *
 * <h3>Notes</h3>
 * <ul>
 *   <li>{@link #rows()} should be treated as read-only by callers; use {@link #add(InMemoryRow)} to insert.</li>
 *   <li>Row order is not semantically meaningful in relational algebra.</li>
 * </ul>
 *
 * @author Seydi Cheikh Wade: 101323727
 * @version Assignment Bonus, 9/17/2025
 */
public class InMemoryTable {
    private final Schema schema;
    private final List<InMemoryRow> rows = new ArrayList<>();

    /**
     * Create an empty table with a schema.
     * @param schema heading of the relation (attribute names/types)
     */
    public InMemoryTable(Schema schema) {
        this.schema = Objects.requireNonNull(schema, "schema");
    }

    /** @return the relation schema */
    public Schema schema() { return schema; }

    /**
     * @return an unmodifiable view of the rows to prevent bypassing schema checks.
     *         Use {@link #add(InMemoryRow)} / {@link #addAll(Collection)} to insert.
     */
    public List<InMemoryRow> rows() { return Collections.unmodifiableList(rows); }

    /**
     * Create a new empty table (same concrete class) with a given schema.
     * Used by operators that change the heading (e.g., projection, some joins).
     */
    public InMemoryTable newEmpty(Schema s) { return new InMemoryTable(s); }

    /** @return number of rows in the table */
    public int size() { return rows.size(); }

    /**
     * Insert a row after validating it matches this table's schema.
     * @throws IllegalArgumentException on missing/extra attributes or type mismatch
     */
    public void add(InMemoryRow r) {
        assertRowMatchesSchema(r);
        rows.add(r);
    }

    /**
     * Insert multiple rows with validation.
     * @throws IllegalArgumentException on the first offending row
     */
    public void addAll(Collection<InMemoryRow> toAdd) {
        for (InMemoryRow r : toAdd) add(r);
    }

    // -------------------------- Validation helpers --------------------------

    private void assertRowMatchesSchema(InMemoryRow r) {
        Map<String,Object> map = r.asMap();

        // 1) Check for missing attributes and type mismatches
        for (Attribute a : schema.attributes()) {
            String name = a.name();
            if (!map.containsKey(name)) {
                throw new IllegalArgumentException("Missing attribute: " + name);
            }
            Object val = map.get(name);
            if (val != null && !typeMatches(a.type(), val)) {
                throw new IllegalArgumentException(
                        "Type mismatch for attribute '" + name + "': expected " + a.type()
                                + " but got " + val.getClass().getSimpleName()
                );
            }
        }

        // 2) Reject extra attributes not declared in the schema
        for (String key : map.keySet()) {
            if (!schemaHasAttribute(key)) {
                throw new IllegalArgumentException("Unknown attribute in row: " + key);
            }
        }
    }

    private boolean schemaHasAttribute(String name) {
        for (Attribute a : schema.attributes()) {
            if (a.name().equals(name)) return true;
        }
        return false;
    }

    /**
     * Shallow Java-type check for the given {@link DataType}.
     * Adjust the mapping if your enum includes more variants.
     */
    private boolean typeMatches(DataType t, Object val) {
        if (val == null) return true;
        return switch (t) {
            case INT    -> val instanceof Integer;
            case STRING -> val instanceof String;
            case BOOL   -> val instanceof Boolean;
            default     -> throw new IllegalArgumentException(
                    "Unsupported DataType: " + t);
        };
    }
}
