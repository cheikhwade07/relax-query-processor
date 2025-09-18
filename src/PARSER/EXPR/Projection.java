package PARSER.EXPR;

import java.util.List;
import java.util.Map;

/**
 * Projection (π attrs (child))
 */
public class Projection implements Expr {
    private final List<String> attrs;
    private final Expr child;

    public Projection(List<String> attrs, Expr child) {
        this.attrs = attrs;
        this.child = child;
    }

    public List<String> attrs() { return attrs; }
    public Expr child() { return child; }

    @Override
    public Object eval(Map<String, Object> row) {
        throw new UnsupportedOperationException("Projection eval is handled by Executor");
    }

    @Override
    public String toString() {
        return "π " + attrs + " (" + child + ")";
    }
}

