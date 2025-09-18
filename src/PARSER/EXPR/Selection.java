package PARSER.EXPR;

import java.util.Map;

/**
 * Selection (σ condition (child))
 */
public class Selection implements Expr {
    private final Expr condition;
    private final Expr child;

    public Selection(Expr condition, Expr child) {
        this.condition = condition;
        this.child = child;
    }

    public Expr condition() { return condition; }
    public Expr child() { return child; }

    @Override
    public Object eval(Map<String, Object> row) {
        // Actual row filtering happens in Executor
        throw new UnsupportedOperationException("Selection eval is handled by Executor");
    }

    @Override
    public String toString() {
        return "σ " + condition + " (" + child + ")";
    }
}

