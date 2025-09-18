package TEST;

import CORE.*;
import EXECUTOR.*;
import PARSER.*;
import PARSER.EXPR.*;

import java.util.List;
import java.util.Map;

public class ExecutorSmoke {
    public static void main(String[] args) {
        // ------------------ Build base tables ------------------
        Schema empSchema = new Schema(List.of(
                new Attribute("EID", DataType.STRING),
                new Attribute("Name", DataType.STRING),
                new Attribute("Age", DataType.INT)
        ));
        InMemoryTable employees = new InMemoryTable(empSchema);
        employees.add(InMemoryRow.empty().with("EID", "E1").with("Name", "John").with("Age", 32));
        employees.add(InMemoryRow.empty().with("EID", "E2").with("Name", "Alice").with("Age", 28));
        employees.add(InMemoryRow.empty().with("EID", "E3").with("Name", "Bob").with("Age", 29));

        Schema takesSchema = new Schema(List.of(
                new Attribute("SID", DataType.STRING),
                new Attribute("Course", DataType.STRING)
        ));
        InMemoryTable takes = new InMemoryTable(takesSchema);
        takes.add(InMemoryRow.empty().with("SID", "E1").with("Course", "COMP3005"));
        takes.add(InMemoryRow.empty().with("SID", "E2").with("Course", "COMP3005"));
        takes.add(InMemoryRow.empty().with("SID", "E4").with("Course", "COMP3006"));

        // ------------------ Evaluation context ------------------
        var ctx = new EvaluationContext(Map.of(
                "Employees", employees,
                "Takes", takes
        ));
        var evaluator = new ExprEvaluator(ctx);

        // ------------------ 1) Selection ------------------
        String q1 = "σ Age > 30 (Employees)";
        Expr ast1 = new Parser(q1).parse();
        InMemoryTable res1 = evaluator.eval(ast1);
        System.out.println("Selection: " + q1);
        res1.rows().forEach(r -> System.out.println(r.asMap()));
        System.out.println();

        // ------------------ 2) Projection ------------------
        String q2 = "π Name (Employees)";
        Expr ast2 = new Parser(q2).parse();
        InMemoryTable res2 = evaluator.eval(ast2);
        System.out.println("Projection: " + q2);
        res2.rows().forEach(r -> System.out.println(r.asMap()));
        System.out.println();

        // ------------------ 3) Join ------------------
        String q3 = "Employees ⨝ EID=SID (Takes)";
        Expr ast3 = new Parser(q3).parse();
        InMemoryTable res3 = evaluator.eval(ast3);
        System.out.println("Join: " + q3);
        res3.rows().forEach(r -> System.out.println(r.asMap()));
        System.out.println();

        // ------------------ 4) Set operation ------------------
        Schema aSchema = new Schema(List.of(new Attribute("X", DataType.INT)));
        InMemoryTable A = new InMemoryTable(aSchema);
        A.add(InMemoryRow.empty().with("X", 1));
        A.add(InMemoryRow.empty().with("X", 2));

        InMemoryTable B = new InMemoryTable(aSchema);
        B.add(InMemoryRow.empty().with("X", 2));
        B.add(InMemoryRow.empty().with("X", 3));

        var ctx2 = new EvaluationContext(Map.of("A", A, "B", B));
        var evaluator2 = new ExprEvaluator(ctx2);

        String q4 = "A ∪ B";
        Expr ast4 = new Parser(q4).parse();
        InMemoryTable res4 = evaluator2.eval(ast4);
        System.out.println("Union: " + q4);
        res4.rows().forEach(r -> System.out.println(r.asMap()));
        System.out.println();

        String q5 = "A ∩ B";
        Expr ast5 = new Parser(q5).parse();
        InMemoryTable res5 = evaluator2.eval(ast5);
        System.out.println("Intersect: " + q5);
        res5.rows().forEach(r -> System.out.println(r.asMap()));
        System.out.println();

        String q6 = "A − B";
        Expr ast6 = new Parser(q6).parse();
        InMemoryTable res6 = evaluator2.eval(ast6);
        System.out.println("Minus: " + q6);
        res6.rows().forEach(r -> System.out.println(r.asMap()));
    }
}
