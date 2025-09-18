package PARSER.EXPR;


import java.util.Map;

/**
 * A reference to a base relation (table) by name.
 */
public class RelationRef implements Expr {
    private final String name;

    public RelationRef(String name) {
        this.name = name;
    }

    public String name() { return name; }

    @Override
    public Object eval(Map<String, Object> row) {
        // Relation evaluation happens in Executor, not here.
        throw new UnsupportedOperationException("RelationRef cannot be evaluated directly");
    }

    @Override
    public String toString() {
        return name;
    }
}

