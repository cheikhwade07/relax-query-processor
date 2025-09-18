package PARSER;
/**
 * Exception thrown to indicate a syntax error during parsing.
 *
 * Responsibilities:
 *  - Provide clear error messages when token sequences
 *    do not match expected grammar rules
 *
 * Collaborators:
 *  - {@link PARSER.Parser} : throws this when encountering invalid input
 *
 * Example:
 *  throw new ParseException("Expected ')', found " + token.type())
 *
 * @see java.lang.RuntimeException
 *
 * @author Seydi Cheikh Wade: 101323727
 * @version Assignment Bonus, 9/17/2025
 */
public class ParseException extends RuntimeException{
    public ParseException(String message) {
        super(message);
    }
}
