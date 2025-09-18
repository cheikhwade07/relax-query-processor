package CORE;
import java.util.*;
/**
 * Represents a schema for a relation (table) in the relational model.
 *
 * Responsibilities:
 *  - Define the structure of a relation (list of attributes and their types).
 *  - Provide methods to access and validate attributes.
 *  - Act as a blueprint for tuples/records that belong to the relation.
 *
 * Collaborators:
 *  - {@link CORE.Attribute} : schema is composed of attributes.
 *  - {@link CORE.DataType}  : defines the type of each attribute.
 *
 * Example:
 *  Schema employees = new Schema(
 *      List.of(new Attribute("EID", DataType.STRING),
 *              new Attribute("Name", DataType.STRING),
 *              new Attribute("Age", DataType.INT)));
 *
 * @author Seydi Cheikh Wade: 101323727
 * @version Assignment Bonus, 9/17/2025
 */
public class Schema {
    private final List<Attribute> attributes;        // preserved order
    private final Map<String, Integer> indexByName;  // column -> position

    public Schema(List<Attribute> attributes) {
        if (attributes == null || attributes.isEmpty())
            throw new IllegalArgumentException("Schema must have at least one attribute");

        this.attributes = List.copyOf(attributes);

        Map<String, Integer> idx = new HashMap<>();
        for (int i = 0; i < this.attributes.size(); i++) {
            String name = this.attributes.get(i).name();
            if (idx.containsKey(name))
                throw new IllegalArgumentException("Duplicate attribute name: " + name);
            idx.put(name, i);
        }
        this.indexByName = Collections.unmodifiableMap(idx);
    }

    public int size() {
        return attributes.size();
    }
    public List<Attribute> attributes() {
        return attributes;
    }
    public boolean has(String column) {
        return indexByName.containsKey(column);
    }

    public int indexOf(String column) {
        Integer i = indexByName.get(column);
        if (i == null) throw new IllegalArgumentException("Unknown column: " + column);
        return i;
    }

    public DataType typeOf(String column) {
        return attributes.get(indexOf(column)).type();
    }
    public Attribute attribute(int idx) {
        return attributes.get(idx);
    }

    /** Two schemas are “compatible” for set ops if arity and types (in order) match. */
    public boolean isCompatible(Schema other) {
        if (other == null || other.size() != size()) return false;
        for (int i = 0; i < size(); i++) {
            if (this.attributes.get(i).type() != other.attributes.get(i).type()) return false;
        }
        return true;
    }

    /** Build a projected schema given indexes in order. */
    public Schema project(int[] indices) {
        List<Attribute> attrs = new ArrayList<>(indices.length);
        for (int i : indices) attrs.add(attributes.get(i));
        return new Schema(attrs);
    }

    /** Merge schemas for join: left attrs + right attrs; caller handles name collisions/renames. */
    public static Schema merge(Schema left, Schema right) {
        List<Attribute> attrs = new ArrayList<>(left.size() + right.size());
        attrs.addAll(left.attributes);
        attrs.addAll(right.attributes);
        return new Schema(attrs);
    }

    public static void checkCompatible(Schema a, Schema b) {
        if (!a.isCompatible(b)) {
            throw new IllegalArgumentException(
                    "Incompatible schemas for set operation:\n  left=" + a + "\n  right=" + b
            );
        }
    }

    /** Optional: attach a logical relation name (for rename ρ). */
    public Schema withName(String relationName) {
        // If you only need to track the schema itself (not table names),
        // you can just return this. Otherwise, extend Schema to hold a relationName.
        return this;
    }
    @Override public String toString() { return attributes.toString(); }
}
