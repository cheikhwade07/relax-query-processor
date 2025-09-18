package PARSER;
import java.util.*;

/**
 * Tokenizer for relational algebra queries.
 *
 * Responsibilities:
 *  - Read raw query input text
 *  - Skip whitespace and ignore irrelevant characters
 *  - Convert ASCII keywords ("select", "project", "join", etc.) and
 *    Greek symbols (σ, π, ρ, ⨝, ∪, ∩, −) into tokens
 *  - Recognize identifiers, numbers, and string literals
 *
 * Collaborators:
 *  - {@link PARSER.Token} : stores individual token data
 *  - {@link PARSER.TokenType} : defines categories of tokens
 *
 * Example:
 *  Input:  "π name (σ age >= 30 (Student))"
 *  Output: [PI, IDENT(name), LPAREN, SIGMA, IDENT(age), GTE,
 *           NUMBER(30), LPAREN, IDENT(Student), RPAREN, RPAREN, EOF]
 *
 * @author Seydi Cheikh Wade: 101323727
 * @version Assignment Bonus, 9/17/2025
 */
public final class Tokenizer {
    private final String s;
    private int i = 0;
    public Tokenizer(String s) {
        this.s = s;
    }
    public List<Token> tokenize() {
        List<Token> out = new ArrayList<>();
        while (true) {
            skipWs();//skip over whitespace
            if (eof()) {
                out.add(new Token(TokenType.EOF, "")); break;
            }
            char c = peek();
            switch (c) {
                case '(' -> { i++; out.add(tok(TokenType.LPAREN, "(")); }
                case ')' -> { i++; out.add(tok(TokenType.RPAREN, ")")); }
                case ',' -> { i++; out.add(tok(TokenType.COMMA, ",")); }
                case '.' -> { i++; out.add(tok(TokenType.DOT, ".")); }
                case '=' -> { i++; out.add(tok(TokenType.EQUAL, "=")); }
                case '!' -> { // !=
                    i++;
                    if (!eof() && peek()=='=') { i++; out.add(tok(TokenType.NOT_EQUAL, "!=")); }
                    else throw err("Unexpected '!'");
                }
                case '<' -> { i++; if (!eof() && peek()=='=') { i++; out.add(tok(TokenType.LTE,"<=")); } else out.add(tok(TokenType.LT,"<")); }
                case '>' -> { i++; if (!eof() && peek()=='=') { i++; out.add(tok(TokenType.GTE,">=")); } else out.add(tok(TokenType.GT,">")); }
                default -> {
                    // symbols: ∪ ∩ − ⨝ σ π ρ
                    if (c=='∪') { i++; out.add(tok(TokenType.UNION,"∪")); }
                    else if (c=='∩') { i++; out.add(tok(TokenType.INTERSECT,"∩")); }
                    else if (c=='−' || c=='-') { i++; out.add(tok(TokenType.MINUS,"-")); }
                    else if (c=='⨝') { i++; out.add(tok(TokenType.JOIN,"⨝")); }
                    else if (c=='⋈') { i++; out.add(tok(TokenType.JOIN,"⋈")); }
                    else if (c=='σ') { i++; out.add(tok(TokenType.SIGMA,"σ")); }
                    else if (c=='π') { i++; out.add(tok(TokenType.PI,"π")); }
                    else if (c=='ρ') { i++; out.add(tok(TokenType.RHO,"ρ")); }
                    else if (Character.isDigit(c)) {
                        out.add(number());
                    } else if (c=='\'' || c=='"') {
                        out.add(string());
                    } else if (isIdentStart(c)) {
                        out.add(identOrKeyword());
                    } else {
                        throw err("Unexpected char: "+c);
                    }
                }
            }
        }
        return out;
    }

    private Token identOrKeyword() {
        int start = i;
        while (!eof() && (Character.isLetterOrDigit(peek()) || peek()=='_' )) i++;
        String raw = s.substring(start, i);
        String k = raw.toLowerCase();
        return switch (k) {
            case "select", "sigma" -> tok(TokenType.SIGMA, raw);
            case "project", "pi" -> tok(TokenType.PI, raw);
            case "rename", "rho" -> tok(TokenType.RHO, raw);
            case "and" -> tok(TokenType.AND, raw);
            case "or"  -> tok(TokenType.OR, raw);
            case "not" -> tok(TokenType.NOT, raw);
            case "join"-> tok(TokenType.JOIN, raw);
            case "union" -> tok(TokenType.UNION, raw);
            case "intersect" -> tok(TokenType.INTERSECT, raw);
            default -> tok(TokenType.IDENT, raw);
        };
    }

    private Token number() {
        int start = i;
        while (!eof() && Character.isDigit(peek())) i++;
        return tok(TokenType.NUMBER, s.substring(start, i));
    }

    private Token string() {
        char quote = peek(); i++;
        int start = i;
        while (!eof() && peek()!=quote) i++;
        if (eof()) throw err("Unterminated string");
        String val = s.substring(start, i);
        i++; // closing quote
        return tok(TokenType.STRING, val);
    }
    private boolean isIdentStart(char c){
        return Character.isLetter(c) || c=='_';
    }
    private boolean eof(){
        return i>=s.length();
    }
    private char peek(){
        return s.charAt(i);
    }
    private void skipWs() {
        while(!eof() && Character.isWhitespace(peek())) i++;
    }
    private Token tok(TokenType t, String v){
        return new Token(t, v);
    }
    private RuntimeException err(String m){
        return new RuntimeException("Lex error at "+i+": "+m);
    }
}

