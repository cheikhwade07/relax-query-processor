/**
 * PARSER package
 *
 * <p>Provides a recursive-descent parser for relational algebra queries.
 * Consumes user input strings and produces abstract syntax trees (ASTs)
 * made of {@link PARSER.EXPR.Expr} nodes.</p>
 *
 * <h3>Responsibilities</h3>
 * <ul>
 *   <li>Tokenize input strings using {@link PARSER.Tokenizer} and {@link PARSER.Token}.</li>
 *   <li>Parse tokens according to relational algebra grammar rules.</li>
 *   <li>Build an AST of {@link PARSER.EXPR.Expr} nodes representing the query structure.</li>
 *   <li>Report syntax errors via {@link PARSER.ParseException}.</li>
 * </ul>
 *
 * <h3>Supported grammar</h3>
 * <ul>
 *   <li><b>Selection (σ)</b> — <code>σ condition (expr)</code></li>
 *   <li><b>Projection (π)</b> — <code>π attr1, attr2 (expr)</code></li>
 *   <li><b>Rename (ρ)</b> — <code>ρ NewName (expr)</code></li>
 *   <li><b>Join (⋈)</b> — <code>expr ⨝ condition expr</code></li>
 *   <li><b>Set operations</b> — <code>expr ∪ expr</code>, <code>expr ∩ expr</code>, <code>expr − expr</code></li>
 *   <li><b>Parentheses</b> — <code>(expr)</code></li>
 *   <li><b>Base relations</b> — identifiers resolve to {@link PARSER.EXPR.RelationRef} nodes.</li>
 * </ul>
 *
 * <h3>Key Classes</h3>
 * <ul>
 *   <li>{@link PARSER.Parser} — recursive-descent parser that builds the AST.</li>
 *   <li>{@link PARSER.Tokenizer} — splits input into {@link PARSER.Token} instances.</li>
 *   <li>{@link PARSER.TokenType} — enumeration of token categories.</li>
 *   <li>{@link PARSER.ParseException} — error reporting utility.</li>
 *   <li>{@link PARSER.EXPR.Expr} and subclasses — AST nodes for all operators.</li>
 * </ul>
 *
 * <h3>Collaborators</h3>
 * <ul>
 *   <li>{@link PARSER.EXPR} package — defines AST node hierarchy consumed by Executor.</li>
 *   <li>{@link EXECUTOR.ExprEvaluator} — consumes parsed ASTs for evaluation.</li>
 *   <li>APP package — orchestrates user input → parse → execute pipeline.</li>
 *   <li>UI package — provides query strings to the parser and displays results from Executor.</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>{@code
 * String input = "π Name (σ Age > 30 (Employees))";
 * Expr ast = new Parser(input).parse();
 * // AST: Projection[Name] (Selection[Age > 30] (RelationRef(Employees)))
 * }</pre>
 *
 * <h3>Notes</h3>
 * <ul>
 *   <li>Implements operator precedence: OR < AND < comparison.</li>
 *   <li>Supports nested queries and parentheses for grouping.</li>
 *   <li>Extensible: new operators can be added by extending {@link PARSER.EXPR.Expr}
 *       and updating {@link PARSER.Parser} grammar rules.</li>
 * </ul>
 *
 * @author Seydi Cheikh Wade: 101323727
 * @version Assignment Bonus, 9/17/2025
 */
package PARSER;
