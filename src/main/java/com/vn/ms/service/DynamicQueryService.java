package com.vn.ms.service;

import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service để thực hiện query trên virtual entities
 */
@Service
public class DynamicQueryService {

    @Autowired
    private DynamicStoreRegistry registry;

    /**
     * Load tất cả entities từ virtual store
     * Đơn giản hóa: chỉ kiểm tra entity có tồn tại không
     */
    public List<KeyValueEntity> loadAll(String storeName, String entityName) {
        String fullyQualifiedName = storeName + "$" + entityName;
        MetaClass metaClass = registry.getVirtualMetaClass(fullyQualifiedName);
        
        if (metaClass == null) {
            throw new RuntimeException("Virtual entity not found: " + fullyQualifiedName);
        }

        // TODO: Implement actual data loading
        // Hiện tại chỉ trả về empty list
        return List.of();
    }

    /**
     * Load một entity theo ID
     * Đơn giản hóa: chỉ kiểm tra entity có tồn tại không
     */
    public KeyValueEntity loadOne(String storeName, String entityName, Object id) {
        String fullyQualifiedName = storeName + "$" + entityName;
        MetaClass metaClass = registry.getVirtualMetaClass(fullyQualifiedName);
        
        if (metaClass == null) {
            throw new RuntimeException("Virtual entity not found: " + fullyQualifiedName);
        }

        // TODO: Implement actual data loading
        // Hiện tại chỉ trả về null
        return null;
    }

    /**
     * Thực hiện query với điều kiện
     */
    public List<KeyValueEntity> query(String storeName, String entityName, String condition) {
        // TODO: Implement query with conditions
        // Có thể tích hợp Apache Calcite ở đây để parse SQL
        return loadAll(storeName, entityName);
    }

    /**
     * Kiểm tra xem virtual entity có tồn tại không
     */
    public boolean entityExists(String storeName, String entityName) {
        String fullyQualifiedName = storeName + "$" + entityName;
        return registry.getVirtualMetaClass(fullyQualifiedName) != null;
    }
}
