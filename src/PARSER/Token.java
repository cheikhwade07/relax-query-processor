package PARSER;
/**
 * Immutable record representing a lexical token.
 *
 * Responsibilities:
 *  - Store the token type (kind)
 *  - Store the lexeme string (exact text matched)
 *
 * Collaborators:
 *  - {@link PARSER.TokenType} : identifies the kind of token
 *  - {@link PARSER.Tokenizer} : produces tokens
 *
 * Example:
 *  new Token(TokenType.IDENT, "Student")
 *
 * @param type   kind of token (e.g., IDENT, SIGMA, NUMBER)
 * @param lexeme exact string matched in the input
 *
 * @author Seydi Cheikh Wade: 101323727
 * @version Assignment Bonus, 9/17/2025
 */
public record Token(TokenType type, String lexeme) {

}
