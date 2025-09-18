package TEST;
import CORE.*;
import java.util.List;
public class CoreSmokeTest {
    // Minimal assertion helper (always on)
    static void require(boolean cond, String msg) {
        if (!cond) throw new IllegalStateException(msg);
    }

    public static void main(String[] args) {
        // 1) Attribute basics
        Attribute a1 = new Attribute("Age", DataType.INT);
        Attribute a2 = new Attribute("Age", DataType.INT);
        Attribute a3 = new Attribute("Name", DataType.STRING);

        require(a1.equals(a2), "Attributes with same name+type should be equal");
        require(!a1.equals(a3), "Different attributes should not be equal");
        System.out.println("Attribute equality OK");

        // 2) Build schema & basic lookups
        Schema s = new Schema(List.of(
                new Attribute("EID", DataType.STRING),
                new Attribute("Name", DataType.STRING),
                new Attribute("Age", DataType.INT)
        ));

        require(s.size() == 3, "Schema size should be 3");
        require(s.has("Name"), "Schema should have column 'Name'");
        require(s.indexOf("Name") == 1, "Name should be at index 1");
        require(s.typeOf("Age") == DataType.INT, "Age type is INT");
        System.out.println("Schema lookups OK");

        // 3) Projection
        int[] projIdx = {0,1, 2}; // Name, Age
        Schema projected = s.project(projIdx);
        require(projected.size() == 3, "Projected schema should have 3 attributes");
        require(projected.attribute(0).name().equals("EID"), "First projected attr should be EID");
        require(projected.attribute(1).name().equals("Name"), "Second projected attr should be Name");
        System.out.println("Schema projection OK");

        // 4) Compatibility
        Schema s2 = new Schema(List.of(
                new Attribute("X", DataType.STRING),
                new Attribute("Y", DataType.STRING),
                new Attribute("Z", DataType.INT)
        ));
        require(s.isCompatible(s2), "Schemas with same arity & types (order) should be compatible");

        Schema bad = new Schema(List.of(
                new Attribute("EID", DataType.STRING),
                new Attribute("Name", DataType.STRING),
                new Attribute("Age", DataType.STRING) // wrong type
        ));
        require(!s.isCompatible(bad), "Mismatched types should be incompatible");
        System.out.println("Schema compatibility OK");

        // 5) Duplicates should be rejected
        boolean threw = false;
        try {
            new Schema(List.of(
                    new Attribute("X", DataType.INT),
                    new Attribute("X", DataType.INT) // duplicate name
            ));
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        require(threw, "Schema should reject duplicate attribute names");
        System.out.println("Duplicate-name guard OK");

        System.out.println("✅ CORE smoke tests passed.");
    }
}
