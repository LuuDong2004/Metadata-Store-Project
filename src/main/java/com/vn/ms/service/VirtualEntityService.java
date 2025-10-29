package com.vn.ms.service;

import com.vn.ms.entity.DynamicStoreMeta;
import com.vn.ms.entity.EntityMeta;
import com.vn.ms.entity.EntityMetaAttribute;
import com.vn.ms.entity.enums.DataType;
import io.jmix.core.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Service để tạo virtual entities từ JSON metadata
 */
@Service
public class VirtualEntityService {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private DynamicStoreRegistry registry;

    /**
     * Tạo virtual entity từ JSON metadata
     */
    @Transactional
    public DynamicStoreMeta createVirtualEntity(String storeName, String entityName, Map<String, String> attributes) {
        // Tạo DynamicStoreMeta
        DynamicStoreMeta storeMeta = dataManager.create(DynamicStoreMeta.class);
        storeMeta.setName(storeName);
        storeMeta.setDescription("Virtual store for " + storeName);
        storeMeta = dataManager.save(storeMeta);

        // Tạo EntityMeta
        EntityMeta entityMeta = dataManager.create(EntityMeta.class);
        entityMeta.setName(entityName);
        entityMeta.setDescription("Virtual entity " + entityName);
        entityMeta.setStoreMeta(storeMeta);
        entityMeta = dataManager.save(entityMeta);

        // Tạo EntityMetaAttributes
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            EntityMetaAttribute attribute = dataManager.create(EntityMetaAttribute.class);
            attribute.setName(entry.getKey());
            attribute.setType(DataType.fromId(entry.getValue()));
            attribute.setEntityMeta(entityMeta);
            dataManager.save(attribute);
        }

        // Đăng ký vào registry
        registry.addEntity(storeName, entityMeta);

        return storeMeta;
    }

    /**
     * Tạo virtual entity đơn giản với các attributes cơ bản
     */
    @Transactional
    public DynamicStoreMeta createSimpleEntity(String storeName, String entityName) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("id", "UUID");
        attributes.put("name", "String");
        attributes.put("description", "String");
        attributes.put("createdDate", "DateTime");
        attributes.put("isActive", "Boolean");

        return createVirtualEntity(storeName, entityName, attributes);
    }
}
