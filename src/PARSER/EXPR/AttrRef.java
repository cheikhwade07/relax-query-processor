package PARSER.EXPR;
import java.util.Map;
public class AttrRef implements Expr {
    private final String name;

    public AttrRef(String name) {
        this.name = name;
    }

    @Override
    public Object eval(Map<String, Object> row) {
        return row.get(name); // Assumes row is Map<String,Object>
    }

    @Override
    public String toString() {
        return name;
    }
}
