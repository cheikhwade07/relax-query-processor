package PARSER.EXPR;

import java.util.Map;

/**
 * Rename (ρ newName (child))
 */
public class Rename implements Expr {
    private final String newName;
    private final Expr child;

    public Rename(String newName, Expr child) {
        this.newName = newName;
        this.child = child;
    }

    public String newName() { return newName; }
    public Expr child() { return child; }

    @Override
    public Object eval(Map<String, Object> row) {
        throw new UnsupportedOperationException("Rename eval is handled by Executor");
    }

    @Override
    public String toString() {
        return "ρ " + newName + " (" + child + ")";
    }
}

