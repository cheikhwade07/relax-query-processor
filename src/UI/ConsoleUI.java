package UI;

import PARSER.Parser;
import PARSER.EXPR.Expr;
import EXECUTOR.*;

import java.util.*;

public class ConsoleUI {
    private final EvaluationContext ctx;

    public ConsoleUI(EvaluationContext ctx) {
        this.ctx = ctx;
    }

    public void start() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Relational Algebra Console (type 'exit' to quit)");
        while (true) {
            System.out.print(">> ");
            String line = sc.nextLine().trim();
            if (line.equalsIgnoreCase("exit")) break;
            if (line.isBlank()) continue;

            try {
                Expr ast = new Parser(line).parse();
                InMemoryTable result = new ExprEvaluator(ctx).eval(ast);
                TablePrinter.print(result);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }



}
