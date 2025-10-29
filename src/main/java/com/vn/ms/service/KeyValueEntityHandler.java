package com.vn.ms.service;

import com.vn.ms.entity.EntityMeta;
import com.vn.ms.entity.EntityMetaAttribute;
import com.vn.ms.interfaces.VirtualEntityHandler;
import io.jmix.core.LoadContext;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * Abstract handler để sinh KeyValueEntity đúng cấu trúc từ EntityMeta
 */
public abstract class KeyValueEntityHandler implements VirtualEntityHandler<KeyValueEntity> {

    @Autowired
    protected DynamicStoreRegistry registry;

    protected final String storeName;
    protected final String entityName;
    protected final String fullyQualifiedName;

    public KeyValueEntityHandler(String storeName, String entityName) {
        this.storeName = storeName;
        this.entityName = entityName;
        this.fullyQualifiedName = storeName + "$" + entityName;
    }

    @Override
    public List<KeyValueEntity> loadAll(LoadContext<?> ctx) {
        List<Map<String, Object>> rawData = loadRawData(ctx);
        return rawData.stream()
                .map(this::createKeyValueEntity)
                .toList();
    }

    @Override
    public KeyValueEntity loadOne(LoadContext<?> ctx) {
        Map<String, Object> rawData = loadOneRawData(ctx);
        return rawData != null ? createKeyValueEntity(rawData) : null;
    }

    /**
     * Tạo KeyValueEntity từ raw data theo cấu trúc EntityMeta
     */
    protected KeyValueEntity createKeyValueEntity(Map<String, Object> rawData) {
        // Lấy MetaClass từ registry
        MetaClass metaClass = registry.getVirtualMetaClass(fullyQualifiedName);
        if (metaClass == null) {
            throw new RuntimeException("MetaClass not found for: " + fullyQualifiedName);
        }

        // Tạo KeyValueEntity với MetaClass
        KeyValueEntity entity = new KeyValueEntity();
        entity.setInstanceMetaClass(metaClass);

        // Lấy EntityMeta để biết cấu trúc
        EntityMeta entityMeta = registry.getEntityMeta(storeName, entityName);
        if (entityMeta != null && entityMeta.getAttributes() != null) {
            // Set value cho từng attribute theo cấu trúc định nghĩa
            for (EntityMetaAttribute attribute : entityMeta.getAttributes()) {
                String attrName = attribute.getName();
                if (rawData.containsKey(attrName)) {
                    Object value = convertValue(rawData.get(attrName), attribute);
                    entity.setValue(attrName, value);
                }
            }
        }

        return entity;
    }

    /**
     * Convert value theo DataType định nghĩa trong EntityMetaAttribute
     */
    protected Object convertValue(Object rawValue, EntityMetaAttribute attribute) {
        if (rawValue == null) {
            return null;
        }

        // Implement conversion logic based on attribute.getType()
        switch (attribute.getType()) {
            case STRING:
                return rawValue.toString();
            case INTEGER:
                return rawValue instanceof Integer ? rawValue : Integer.valueOf(rawValue.toString());
            case LONG:
                return rawValue instanceof Long ? rawValue : Long.valueOf(rawValue.toString());
            case DOUBLE:
                return rawValue instanceof Double ? rawValue : Double.valueOf(rawValue.toString());
            case BOOLEAN:
                return rawValue instanceof Boolean ? rawValue : Boolean.valueOf(rawValue.toString());
            // Add more conversions as needed
            default:
                return rawValue;
        }
    }

    /**
     * Subclass phải implement để load raw data
     */
    protected abstract List<Map<String, Object>> loadRawData(LoadContext<?> ctx);

    /**
     * Subclass phải implement để load một record raw data
     */
    protected abstract Map<String, Object> loadOneRawData(LoadContext<?> ctx);

    /**
     * Helper method để tạo KeyValueEntity đơn giản
     */
    public static KeyValueEntity createSimpleEntity(MetaClass metaClass, Map<String, Object> data) {
        KeyValueEntity entity = new KeyValueEntity();
        entity.setInstanceMetaClass(metaClass);
        
        data.forEach(entity::setValue);
        
        return entity;
    }
}
