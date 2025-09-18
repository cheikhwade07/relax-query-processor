package CORE;
import java.util.Objects;
/**
 * Represents an attribute (column) in a relation schema.
 *
 * Responsibilities:
 *  - Store the attribute's name
 *  - Store the attribute's datatype
 *
 * Collaborators:
 *  - {@link CORE.DataType} : provides the type of the attribute
 *
 * Example: Attribute("Age", DataType.INT)
 *
 * @author Seydi Cheikh Wade: 101323727
 * @version Assignment Bonus, 9/17/2025
 */
public final class Attribute {
    private final String name;
    private final DataType type;

    public  Attribute(String name, DataType type){
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Attribute name required");
        if (type == null) throw new IllegalArgumentException("Attribute type required");
        this.name = name;
        this.type = type;
    }
    public String name(){
        return name;
    }
    public DataType type(){
        return type;
    }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attribute)) return false;
        Attribute that = (Attribute) o;
        return name.equals(that.name) && type == that.type;
    }
    @Override public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override public String toString() {
        return name + ":" + type;
    }
}


