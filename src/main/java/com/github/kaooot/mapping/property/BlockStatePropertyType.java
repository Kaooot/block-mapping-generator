package com.github.kaooot.mapping.property;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Kaooot
 * @version 1.0
 */
@Getter
@RequiredArgsConstructor
@Accessors(fluent = true)
public enum BlockStatePropertyType {

    INT("int"),
    STRING("string"),
    BYTE("byte");

    private final String jsonName;

    private static final BlockStatePropertyType[] VALUES = BlockStatePropertyType.values();

    public static BlockStatePropertyType from(JsonObject object) {
        for (final BlockStatePropertyType value : BlockStatePropertyType.VALUES) {
            if (object.has(value.jsonName)) {
                return value;
            }
        }

        throw new IllegalArgumentException("Could not collect block property from " + object);
    }
}