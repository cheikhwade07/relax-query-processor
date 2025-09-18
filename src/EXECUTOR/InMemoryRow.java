package EXECUTOR;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * InMemoryRow
 * <p>A minimal, map-backed tuple used during execution. The row is
 * treated as <em>persistent/immutable-style</em>: calls to
 * {@link #with(String, Object)} return a new row instance with the
 * requested change applied.</p>
 * <h3>Responsibilities</h3>
 * <ul>
 *   <li>Store attribute → value pairs in insertion order.</li>
 *   <li>Provide safe read access by attribute name.</li>
 *   <li>Support functional update via {@code with(...)} to ease row construction.</li>
 * </ul>
 *
 * <h3>Collaborators</h3>
 * <ul>
 *   <li>{@link EXECUTOR.InMemoryTable}: owns collections of rows.</li>
 *   <li>{@link EXECUTOR.ExprEvaluator}: constructs and combines rows during σ/π/⋈/∪/∩/−.</li>
 * </ul>
 *
 * <h3>Notes</h3>
 * <ul>
 *   <li>Equality in set operations uses the underlying {@link #asMap()} content.</li>
 *   <li>Values are stored as raw {@link Object}; APP/PARSER/CORE enforce types.</li>
 * </ul>
 *
 * @author Seydi Cheikh Wade: 101323727
 * @version Assignment Bonus, 9/17/2025
 */
public class InMemoryRow {
    private final Map<String, Object> data;

    /** Create an empty row. */
    public InMemoryRow() {
        this.data = new LinkedHashMap<>();
    }

    /** Internal constructor with a copy of existing data. */
    public InMemoryRow(Map<String, Object> data) {
        this.data = new LinkedHashMap<>(data);
    }

    /**
     * Get a value by attribute name.
     * @param attr attribute (column) name
     * @return value or {@code null} if not present
     */
    Object get(String attr) {
        return data.get(attr);
    }

    /**
     * Return a new row with {@code attr=value} applied.
     * @param attr attribute (column) name
     * @param value value to set
     * @return a new {@link InMemoryRow} containing the change
     */
    public InMemoryRow with(String attr, Object value) {
        var copy = new LinkedHashMap<>(data);
        copy.put(attr, value);
        return new InMemoryRow(copy);
    }

    /**
     * Immutable view of the row content. Useful for equality checks in set ops.
     * @return unmodifiable map snapshot
     */
    public Map<String, Object> asMap() {
        return Collections.unmodifiableMap(data);
    }

    /** Convenience factory for a brand-new empty row. */
    public static InMemoryRow empty() {
        return new InMemoryRow();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InMemoryRow)) return false;
        InMemoryRow other = (InMemoryRow) o;
        return data.equals(other.data);
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }

}
