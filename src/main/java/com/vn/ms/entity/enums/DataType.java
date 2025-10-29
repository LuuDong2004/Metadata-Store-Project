package com.vn.ms.entity.enums;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum DataType implements EnumClass<String> {

    STRING("String"),
    INTEGER("Integer"),
    LONG("Long"),
    DOUBLE("Double"),
    BOOLEAN("Boolean"),
    BIG_DECIMAL("BigDecimal"),
    BIG_INTEGER("BigInteger"),
    BYTE_ARRAY("byte[]"),
    CHARACTER("Character"),
    DATE("Date"),
    DATETIME("DateTime"),
    UUID("UUID");

    private final String id;

    DataType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static DataType fromId(String id) {
        for (DataType at : DataType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}