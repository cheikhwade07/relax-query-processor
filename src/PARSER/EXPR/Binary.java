package PARSER.EXPR;
import java.util.Map;
public class Binary implements Expr{
    private final Expr left;
    private final Expr right;
    private final Op op;

    public Binary(Expr left, Op op, Expr right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public Object eval(Map<String, Object> row) {
        switch (op) {
            case AND:
                return (Boolean) left.eval(row) && (Boolean) right.eval(row);
            case OR:
                return (Boolean) left.eval(row) || (Boolean) right.eval(row);
            default:
                Object lv = left.eval(row);
                Object rv = right.eval(row);
                return compare(lv, rv);
        }
    }

    private boolean compare(Object lv, Object rv) {
        return switch (op) {
            case EQ  -> lv.equals(rv);
            case NEQ -> !lv.equals(rv);
            case LT  -> ((Comparable) lv).compareTo(rv) < 0;
            case LTE -> ((Comparable) lv).compareTo(rv) <= 0;
            case GT  -> ((Comparable) lv).compareTo(rv) > 0;
            case GTE -> ((Comparable) lv).compareTo(rv) >= 0;
            default  -> throw new IllegalStateException("Unexpected operator: " + op);
        };
    }

    @Override
    public String toString() {
        return "(" + left + " " + op + " " + right + ")";
    }
}
