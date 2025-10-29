package com.vn.ms.service;

import com.vn.ms.entity.EntityMeta;
import com.vn.ms.entity.EntityMetaAttribute;
import com.vn.ms.entity.enums.DataType;
import io.jmix.core.Metadata;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.UUID;

@Component
public class KeyValueMetaClassBuilder {

    @Autowired
    private Metadata metadata;

    /**
     * Tạo KeyValueEntity MetaClass từ EntityMeta với fully-qualified name: storeName$entityName
     * Đơn giản hóa: chỉ trả về MetaClass của KeyValueEntity
     */
    public MetaClass buildKeyValueMetaClass(String storeName, EntityMeta entityMeta) {
        // Đơn giản hóa: sử dụng MetaClass của KeyValueEntity có sẵn
        // Trong thực tế, có thể cần tạo MetaClass động phức tạp hơn
        return metadata.getClass(KeyValueEntity.class);
    }

    /**
     * Lấy Java type từ DataType
     */
    public Class<?> getJavaTypeFromDataType(DataType dataType) {
        if (dataType == null) {
            return String.class;
        }

        switch (dataType) {
            case STRING:
                return String.class;
            case INTEGER:
                return Integer.class;
            case LONG:
                return Long.class;
            case DOUBLE:
                return Double.class;
            case BOOLEAN:
                return Boolean.class;
            case BIG_DECIMAL:
                return BigDecimal.class;
            case BIG_INTEGER:
                return BigInteger.class;
            case BYTE_ARRAY:
                return byte[].class;
            case CHARACTER:
                return Character.class;
            case DATE:
            case DATETIME:
                return Date.class;
            case UUID:
                return UUID.class;
            default:
                return String.class;
        }
    }
}
