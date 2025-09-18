package PARSER.EXPR;

import java.util.Map;

/**
 * Join (⋈) with optional ON condition
 */
public class Join implements Expr {
    private final Expr left;
    private final Expr right;
    private final Expr on; // can be null (natural join)

    public Join(Expr left, Expr right, Expr on) {
        this.left = left;
        this.right = right;
        this.on = on;
    }

    public Expr left() { return left; }
    public Expr right() { return right; }
    public Expr on() { return on; }

    @Override
    public Object eval(Map<String, Object> row) {
        throw new UnsupportedOperationException("Join eval is handled by Executor");
    }

    @Override
    public String toString() {
        return "⋈" + (on != null ? "[" + on + "]" : "") + " (" + left + ", " + right + ")";
    }
}
