package TEST;
import PARSER.*;
import PARSER.EXPR.Expr;

public class ParserSmoke {
    public static void main(String[] args) {
        String q = "π name, email (σ id >= 2 (Student))";
        Expr ast = new Parser(q).parse();
        System.out.println(ast);

        String j = "(Student) ⨝ id=sid (Takes)";
        System.out.println(new Parser(j).parse());

        String s = "A ∪ B ∩ C − D";
        System.out.println(new Parser(s).parse());
    }
}
