package PARSER.EXPR;
import java.util.Map;
/**
 * Expr is the base interface for all expression nodes.
 */
public interface Expr {
    Object eval(Map<String, Object> row);
}
