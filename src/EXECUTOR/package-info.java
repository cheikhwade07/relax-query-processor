/**
 * EXECUTOR package
 *
 * <p>Provides the execution engine for the relational algebra query processor.
 * Consumes parsed {@link PARSER.EXPR.Expr} abstract syntax trees (ASTs) and
 * evaluates them over in-memory relations, producing results as
 * {@link EXECUTOR.InMemoryTable} objects backed by {@link CORE.Schema}.</p>
 *
 * <h3>Responsibilities</h3>
 * <ul>
 *   <li>Consume {@link PARSER.EXPR.Expr} ASTs produced by the parser.</li>
 *   <li>Evaluate expressions over base tables held in an {@link EXECUTOR.EvaluationContext}.</li>
 *   <li>Implement core relational algebra operators:
 *       <ul>
 *         <li>Selection (σ) — tuple filtering by condition.</li>
 *         <li>Projection (π) — attribute subset (schema reduction).</li>
 *         <li>Rename (ρ) — renaming relations (currently a no-op on schema).</li>
 *         <li>Join (⋈) — nested-loop join with optional θ-condition.</li>
 *         <li>Set operations (∪, ∩, −) — schema-compatible set semantics.</li>
 *       </ul>
 *   </li>
 *   <li>Support nested and sequential compositions of operators.</li>
 * </ul>
 *
 * <h3>Key Classes</h3>
 * <ul>
 *   <li>{@link EXECUTOR.ExprEvaluator} — core engine that walks the AST and executes operators.</li>
 *   <li>{@link EXECUTOR.EvaluationContext} — catalog mapping relation names to base tables.</li>
 *   <li>{@link EXECUTOR.InMemoryTable} — in-memory relation (rows + schema).</li>
 *   <li>{@link EXECUTOR.InMemoryRow} — immutable-style in-memory tuple.</li>
 * </ul>
 *
 * <h3>Collaborators</h3>
 * <ul>
 *   <li>{@link CORE.Schema}, {@link CORE.Attribute}, {@link CORE.DataType} — relation structure and types.</li>
 *   <li>{@link PARSER} — builds the AST consumed by the Executor.</li>
 *   <li>APP package — orchestrates the pipeline (input → parse → execute → output).</li>
 *   <li>UI package — accepts user queries and displays results.</li>
 * </ul>
 *
 * <h3>Example Workflow</h3>
 * <pre>{@code
 * // User input:
 * π Name (σ Age > 30 (Employees))
 *
 * // APP:
 * Expr ast = new Parser(input).parse();
 * InMemoryTable result = new ExprEvaluator(ctx).eval(ast);
 * UI.render(result);
 * }</pre>
 *
 * <h3>Notes</h3>
 * <ul>
 *   <li>Implements operators manually with Java collections (no external libs).</li>
 *   <li>Row equality and set operations rely on {@link EXECUTOR.InMemoryRow#equals(Object)}.</li>
 *   <li>Schema compatibility is enforced for set operations via {@link CORE.Schema#isCompatible(CORE.Schema)}.</li>
 * </ul>
 *
 * @author Seydi Cheikh Wade: 101323727
 * @version Assignment Bonus, 9/17/2025
 */
package EXECUTOR;