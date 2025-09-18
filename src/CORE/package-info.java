/**
 * Core package
 *
 * Responsibilities:
 *  - Define the basic building blocks of the system
 *  - Represent schemas, attributes, and datatypes
 *  - Provide foundation classes used by the parser and executor
 *
 * Key Classes:
 *  - {@link CORE.Attribute} : Represents a column in a relation (name + datatype)
 *  - {@link CORE.DataType}  : Enum for supported datatypes (e.g., INT, STRING)
 *  - {@link CORE.Schema}    : Holds a collection of attributes, representing the structure of a relation
 *
 * Collaborators:
 *  - PARSER package: consumes schemas and attributes when parsing queries
 *  - EXECUTOR package: uses schemas and attributes to run relational algebra operations
 *
 * @author Seydi Cheikh Wade: 101323727
 * @version Assignment Bonus, 9/17/2025
 */
package CORE;
