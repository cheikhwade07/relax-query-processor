package PARSER.EXPR;

import java.util.Map;

public class Not implements Expr{
    private final Expr inner;

    public Not(Expr inner) {
        this.inner = inner;
    }

    @Override
    public Object eval(Map<String, Object> row) {
        return !(Boolean) inner.eval(row);
    }

    @Override
    public String toString() {
        return "NOT(" + inner + ")";
    }
}
