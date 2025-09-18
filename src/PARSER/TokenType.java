package PARSER;
/**
 * Enumerates all token types used in relational algebra queries.
 *
 * Responsibilities:
 *  - Provide symbolic names for query operators, punctuation,
 *    keywords, identifiers, and literals
 *
 * Collaborators:
 *  - {@link PARSER.Token} : pairs a token type with the actual lexeme
 *  - {@link PARSER.Tokenizer} : assigns token types during scanning
 *
 * Example types:
 *  - SIGMA (σ or "select")
 *  - PI (π or "project")
 *  - RHO (ρ or "rename")
 *  - JOIN (⨝ or "join")
 *  - UNION (∪), INTERSECT (∩), MINUS (−)
 *  - IDENT, NUMBER, STRING
 *
 * @author Seydi Cheikh Wade: 101323727
 * @version Assignment Bonus, 9/17/2025
 */
public enum TokenType {
    // symbols
    LPAREN, RPAREN, COMMA, DOT, EQUAL, NOT_EQUAL, LT, LTE, GT, GTE,
    UNION, INTERSECT, MINUS, JOIN, // ⨝, ∪, ∩, −
    // keywords / operators
    SIGMA, PI, RHO, SELECT, PROJECT, RENAME, AND, OR, NOT,
    IDENT, NUMBER, STRING,
    EOF
}
