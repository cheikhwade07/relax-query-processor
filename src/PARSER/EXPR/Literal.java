package PARSER.EXPR;
import java.util.Map;
public  class Literal implements Expr{
    private final Object value;

    public Literal(Object value) {
        this.value = value;
    }

    @Override
    public Object eval(Map<String, Object> row) {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
