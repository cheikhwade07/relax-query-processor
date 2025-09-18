package CORE;
/**
 * Enum of supported data types for attributes in the system.
 *
 * Responsibilities:
 *  - Define the primitive data types available to schema attributes.
 *
 * Collaborators:
 *  -  - (none) — independent enum
 *
 * Example:
 *  Attribute age = new Attribute("Age", DataType.INT);
 *
 * @author Seydi Cheikh Wade: 101323727
 * @version Assignment Bonus, 9/17/2025
 */
public enum DataType {
    INT,
    DOUBLE,
    STRING,
    BOOL;
}
