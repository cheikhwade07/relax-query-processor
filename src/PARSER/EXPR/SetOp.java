package PARSER.EXPR;


import java.util.Map;

/**
 * Set operations: UNION, INTERSECT, MINUS
 */
public class SetOp implements Expr {
    public enum Kind { UNION, INTERSECT, MINUS }

    private final Kind kind;
    private final Expr left;
    private final Expr right;

    public SetOp(Kind kind, Expr left, Expr right) {
        this.kind = kind;
        this.left = left;
        this.right = right;
    }

    public Kind kind() { return kind; }
    public Expr left() { return left; }
    public Expr right() { return right; }

    @Override
    public Object eval(Map<String, Object> row) {
        throw new UnsupportedOperationException("SetOp eval is handled by Executor");
    }

    @Override
    public String toString() {
        return kind + "(" + left + ", " + right + ")";
    }
}
