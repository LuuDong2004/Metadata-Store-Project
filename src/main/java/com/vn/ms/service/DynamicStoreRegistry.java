package com.vn.ms.service;

import com.vn.ms.entity.DynamicStoreMeta;
import com.vn.ms.entity.EntityMeta;
import com.vn.ms.impl.DynamicDataStore;
import com.vn.ms.interfaces.VirtualEntityHandler;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DynamicStoreRegistry {

    @Autowired
    private KeyValueMetaClassBuilder metaClassBuilder;

    @Autowired
    private Metadata metadata;

    // Chỉ có một DynamicDataStore với tên "dynamic"
    private DynamicDataStore dynamicDataStore;

    // Map để quản lý các "store ảo" - khái niệm nội bộ
    private final Map<String, Map<String, EntityMeta>> virtualStores = new ConcurrentHashMap<>();

    // Map để quản lý các MetaClass của virtual entities với fully-qualified name
    private final Map<String, MetaClass> virtualMetaClasses = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Khởi tạo datastore "dynamic" duy nhất
        dynamicDataStore = new DynamicDataStore("dynamic");
    }

    /**
     * Đăng ký một store ảo mới (khái niệm nội bộ)
     */
    public void registerStore(String storeName) {
        virtualStores.putIfAbsent(storeName, new ConcurrentHashMap<>());
    }

    /**
     * Thêm entity vào store ảo và tạo MetaClass với fully-qualified name
     */
    public void addEntity(String storeName, EntityMeta entityMeta) {
        // Đăng ký store ảo nếu chưa có
        virtualStores.computeIfAbsent(storeName, k -> new ConcurrentHashMap<>())
                   .put(entityMeta.getName(), entityMeta);

        // Tạo fully-qualified name: storeName$entityName
        String fullyQualifiedName = storeName + "$" + entityMeta.getName();

        // Tạo KeyValueMetaClass
        MetaClass metaClass = metaClassBuilder.buildKeyValueMetaClass(storeName, entityMeta);
        virtualMetaClasses.put(fullyQualifiedName, metaClass);

        // Đăng ký entity vào datastore "dynamic" với fully-qualified name
        dynamicDataStore.registerEntity(fullyQualifiedName, entityMeta);
    }

    /**
     * Đăng ký handler cho entity với fully-qualified name
     */
    public void registerHandler(String storeName, String entityName, VirtualEntityHandler<?> handler) {
        String fullyQualifiedName = storeName + "$" + entityName;
        dynamicDataStore.registerHandler(fullyQualifiedName, handler);
    }

    /**
     * Lấy DynamicDataStore (chỉ có một datastore "dynamic")
     */
    public DynamicDataStore getDynamicDataStore() {
        return dynamicDataStore;
    }

    /**
     * Lấy MetaClass cho virtual entity bằng fully-qualified name
     */
    public MetaClass getVirtualMetaClass(String fullyQualifiedName) {
        return virtualMetaClasses.get(fullyQualifiedName);
    }

    /**
     * Lấy EntityMeta từ store ảo
     */
    public EntityMeta getEntityMeta(String storeName, String entityName) {
        Map<String, EntityMeta> entities = virtualStores.get(storeName);
        return entities != null ? entities.get(entityName) : null;
    }

    /**
     * Đăng ký nhiều entity từ DynamicStoreMeta (dùng cho REST API)
     */
    public void registerStore(DynamicStoreMeta storeMeta, Map<String, EntityMeta> entities) {
        String storeName = storeMeta.getName();
        registerStore(storeName);

        entities.forEach((entityName, entityMeta) -> {
            entityMeta.setStoreMeta(storeMeta);
            addEntity(storeName, entityMeta);
        });
    }

    /**
     * Lấy danh sách tất cả store ảo
     */
    public Map<String, Map<String, EntityMeta>> getAllVirtualStores() {
        return virtualStores;
    }

    /**
     * @deprecated Sử dụng getDynamicDataStore() thay thế
     */
    @Deprecated
    public DynamicDataStore getStore(String storeName) {
        return dynamicDataStore;
    }
}
