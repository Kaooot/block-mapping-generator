package com.github.kaooot.mapping.property;

import lombok.Value;
import lombok.experimental.Accessors;

/**
 * @author Kaooot
 * @version 1.0
 */
@Value
@Accessors(fluent = true)
public class BlockStateProperty {

    String name;
    BlockStatePropertyType type;
    Object value;

    public int intValue() {
        return (int) this.value;
    }

    public String stringValue() {
        return this.value.toString();
    }

    public byte byteValue() {
        return (byte) this.value;
    }
}