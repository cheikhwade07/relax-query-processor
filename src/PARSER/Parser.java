package PARSER;
import PARSER.EXPR.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * Recursive-descent parser for relational algebra queries.
 *
 * Responsibilities:
 *  - Consume tokens from {@link PARSER.Tokenizer}
 *  - Validate query syntax according to grammar rules
 *  - Build {@link PARSER.EXPR.Expr} trees representing the abstract syntax tree (AST)
 *  - Report syntax errors with {@link PARSER.ParseException}
 *
 * Collaborators:
 *  - {@link PARSER.Tokenizer}, {@link PARSER.Token}, {@link PARSER.TokenType}
 *  - {@link PARSER.EXPR.Expr} : AST nodes
 *  - {@link PARSER.ParseException} : error reporting
 *
 * Example:
 *  Input:  "π name, email (σ age >= 30 (Student))"
 *  Output: Expr.Projection(child = Expr.Selection(child = Expr.RelationRef("Student"), ...))
 *
 * Grammar (simplified):
 *  expr    := joinExpr ( (∪|∩|−) joinExpr )*
 *  joinExpr:= unary ( JOIN cond? unary )*
 *  unary   := (σ cond '(' expr ')') | (π attrs '(' expr ')')
 *             | (ρ IDENT '(' expr ')') | primary
 *  primary := IDENT | '(' expr ')'
 *
 * @author Seydi Cheikh Wade: 101323727
 * @version Assignment Bonus, 9/17/2025
 */
public final class Parser {
    private final List<Token> toks;
    private int p = 0;

    public Parser(String input) {
        this.toks = new Tokenizer(input).tokenize();
    }

    public Parser(List<Token> tokens) {
        this.toks = tokens;
    }

    /** Entry point */
    public Expr parse() {
        Expr e = parseSet();
        expect(TokenType.EOF);
        return e;
    }

    // setExpr := joinExpr ( (∪ | ∩ | −) joinExpr )*
    private Expr parseSet() {
        Expr left = parseJoin();
        while (match(TokenType.UNION, TokenType.INTERSECT, TokenType.MINUS)) {
            Token op = prev();
            Expr right = parseJoin();
            SetOp.Kind kind = switch (op.type()) {
                case UNION     -> SetOp.Kind.UNION;
                case INTERSECT -> SetOp.Kind.INTERSECT;
                case MINUS     -> SetOp.Kind.MINUS;
                default        -> throw err("unexpected set op: " + op);
            };
            left = new SetOp(kind, left, right);
        }
        return left;
    }


    // joinExpr := unary ( (⨝ | JOIN) cond? unary )*
    // joinExpr := unary ( (⨝ | JOIN) cond? unary )*
    private Expr parseJoin() {
        Expr left = parseUnary();
        while (match(TokenType.JOIN)) {
            Expr on = null;
            // Only parse a condition if it doesn’t look like the start of a relation
            if (startsExpr(peek().type()) && !looksLikeRightRelationStart()) {
                on = parseExpr();
            }
            Expr right = parseUnary();
            left = new Join(left, right, on);
        }
        return left;
    }

    /** Heuristic: if next token is IDENT and the token after is not a comparison,
     *  then it’s probably the start of the right relation (not a condition). */
    private boolean looksLikeRightRelationStart() {
        if (peek().type() != TokenType.IDENT) return false;
        TokenType t1 = (p + 1 < toks.size()) ? toks.get(p + 1).type() : TokenType.EOF;
        return !(t1 == TokenType.EQUAL || t1 == TokenType.NOT_EQUAL ||
                t1 == TokenType.LT || t1 == TokenType.LTE ||
                t1 == TokenType.GT || t1 == TokenType.GTE ||
                t1 == TokenType.DOT);
    }



    // unary := selection | projection | rename | primary
    private Expr parseUnary() {
        if (match(TokenType.SIGMA)) {             // σ cond '(' expr ')'
            Expr cond = parseExpr();              // was Condition c = parseCond();
            expect(TokenType.LPAREN);
            Expr child = parseSet();
            expect(TokenType.RPAREN);
            return new Selection(cond, child);    // EXP.Selection
        }
        if (match(TokenType.PI)) {                // π a,b '(' expr ')'
            List<String> attrs = parseAttrList();
            expect(TokenType.LPAREN);
            Expr child = parseSet();
            expect(TokenType.RPAREN);
            return new Projection(attrs, child);  // EXP.Projection
        }
        if (match(TokenType.RHO)) {               // ρ NewName '(' expr ')'
            String newName = expect(TokenType.IDENT).lexeme();
            expect(TokenType.LPAREN);
            Expr child = parseSet();
            expect(TokenType.RPAREN);
            return new Rename(newName, child);    // EXP.Rename
        }
        return parsePrimary();
    }

    // primary := IDENT | '(' expr ')'
    private Expr parsePrimary() {
        if (match(TokenType.IDENT)) {
            return new RelationRef(prev().lexeme());   // EXP.RelationRef
        }
        if (match(TokenType.LPAREN)) {
            Expr inner = parseSet();
            expect(TokenType.RPAREN);
            return inner;
        }
        throw err("Expected relation name or '('");
    }


    // ---------- conditions (precedence: OR < AND < comparison) ----------

    private Expr parseOr() {
        Expr left = parseAnd();
        while (match(TokenType.OR)) {
            Expr right = parseAnd();
            left = new Binary(left, Op.OR, right);
        }
        return left;
    }

    private Expr parseAnd() {
        Expr left = parseNeg();
        while (match(TokenType.AND)) {
            Expr right = parseNeg();
            left = new Binary(left, Op.AND, right);
        }
        return left;
    }

    private Expr parseNeg() {
        if (match(TokenType.NOT)) return new Not(parseNeg());
        if (match(TokenType.LPAREN)) {
            Expr inner = parseExpr();
            expect(TokenType.RPAREN);
            return inner;
        }
        return parseCmp();
    }

    private Expr parseCmp() {
        Expr left = parseOperand();
        if (match(TokenType.EQUAL, TokenType.NOT_EQUAL, TokenType.LT,
                TokenType.LTE, TokenType.GT, TokenType.GTE)) {
            Token op = prev();
            Expr right = parseOperand();
            Op o = switch (op.type()) {
                case EQUAL     -> Op.EQ;
                case NOT_EQUAL -> Op.NEQ;
                case LT        -> Op.LT;
                case LTE       -> Op.LTE;
                case GT        -> Op.GT;
                case GTE       -> Op.GTE;
                default        -> throw err("bad comparison op");
            };
            return new Binary(left, o, right);
        }
        return left;
    }

    private Expr parseOperand() {
        if (match(TokenType.STRING)) {
            return new Literal(prev().lexeme());
        }

        if (match(TokenType.NUMBER)) {
            String lit = prev().lexeme();
            // Decide based on presence of decimal point
            if (lit.contains(".")) {
                return new Literal(Double.parseDouble(lit)); // matches DataType.DOUBLE
            } else {
                return new Literal(Integer.parseInt(lit));   // matches DataType.INT
            }
        }

        Token id = expect(TokenType.IDENT);
        if (match(TokenType.DOT)) {
            String attr = expect(TokenType.IDENT).lexeme();
            return new AttrRef(id.lexeme() + "." + attr);
        }

        return new AttrRef(id.lexeme());
    }





    // attrList := IDENT (',' IDENT)*
    private List<String> parseAttrList() {
        List<String> names = new ArrayList<>();
        names.add(expect(TokenType.IDENT).lexeme());
        while (match(TokenType.COMMA)) names.add(expect(TokenType.IDENT).lexeme());
        return names;
    }



    // ---------- token helpers ----------

    private boolean match(TokenType... types) {
        for (TokenType t : types) {
            if (check(t)) { advance(); return true; }
        }
        return false;
    }

    private boolean check(TokenType t) {
        return peek().type() == t;
    }
    private Token advance() {
        if (!isAtEnd()) p++; return prev();
    }
    private boolean isAtEnd() {
        return peek().type() == TokenType.EOF;
    }
    private Token peek() {
        return toks.get(p);
    }
    private Token prev() {
        return toks.get(p - 1);
    }

    private Token expect(TokenType t) {
        if (check(t)) return advance();
        throw err("Expected " + t + " but found " + peek().type() + " (" + peek().lexeme() + ")");
    }

    private Token expectOneOf(TokenType... ts) {
        for (TokenType t : ts) if (check(t)) return advance();
        throw err("Expected one of " + Arrays.toString(ts) + " but found " + peek().type());
    }

    private ParseException err(String msg) {
        return new ParseException("Parse error at token " + p + ": " + msg);
    }
    private boolean startsExpr(TokenType tt) {
        return tt == TokenType.IDENT
                || tt == TokenType.NUMBER
                || tt == TokenType.STRING
                || tt == TokenType.LPAREN
                || tt == TokenType.NOT;
    }
    private Expr parseExpr() {
        return parseOr();
    }
}
