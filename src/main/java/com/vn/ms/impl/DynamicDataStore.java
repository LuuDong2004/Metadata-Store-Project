package com.vn.ms.impl;

import com.vn.ms.entity.EntityMeta;
import com.vn.ms.interfaces.VirtualEntityHandler;
import com.vn.ms.service.DynamicStoreRegistry;
import io.jmix.core.LoadContext;
import io.jmix.core.SaveContext;
import io.jmix.core.ValueLoadContext;
import io.jmix.core.datastore.AbstractDataStore;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicDataStore extends AbstractDataStore {
    // Map với fully-qualified name làm key
    private final Map<String, EntityMeta> entities = new ConcurrentHashMap<>();
    private final Map<String, VirtualEntityHandler> handlers = new ConcurrentHashMap<>();

    private DynamicStoreRegistry registry;

    public DynamicDataStore(String name) {
       setName(name);
    }
    public DynamicDataStore(DynamicStoreRegistry registry) {
        setName("dynamic");
        this.registry = registry;
    }

    /**
     * Đăng ký entity với fully-qualified name: storeName$entityName
     */
    public void registerEntity(String fullyQualifiedName, EntityMeta meta) {
        entities.put(fullyQualifiedName, meta);
    }

    /**
     * Đăng ký handler với fully-qualified name
     */
    public void registerHandler(String fullyQualifiedName, VirtualEntityHandler handler) {
        handlers.put(fullyQualifiedName, handler);
    }

    /**
     * @deprecated Sử dụng registerEntity(String fullyQualifiedName, EntityMeta meta) thay thế
     */
    @Deprecated
    public void registerEntity(EntityMeta meta) {
        entities.put(meta.getName(), meta);
    }


    @Override
    protected Object loadOne(LoadContext<?> context) {
        String entityName = context.getEntityMetaClass().getName();
        VirtualEntityHandler handler = getHandler(entityName);
        return handler != null ? handler.loadOne(context) : null;
    }

    @Override
    protected List<Object> loadAll(LoadContext<?> context) {
        String entityName = context.getEntityMetaClass().getName();
        VirtualEntityHandler handler = getHandler(entityName);
        return handler != null ? handler.loadAll(context) : List.of();
    }

    @SuppressWarnings("unchecked")
    private VirtualEntityHandler<?> getHandler(String fullyQualifiedName) {
        return (VirtualEntityHandler<?>) handlers.get(fullyQualifiedName);
    }

    /**
     * Lấy EntityMeta bằng fully-qualified name
     */
    public EntityMeta getEntityMeta(String fullyQualifiedName) {
        return entities.get(fullyQualifiedName);
    }

    /**
     * Kiểm tra xem entity có tồn tại không
     */
    public boolean hasEntity(String fullyQualifiedName) {
        return entities.containsKey(fullyQualifiedName);
    }

    @Override
    protected long countAll(LoadContext<?> context) {
        return 0;
    }

    @Override
    protected Set<Object> saveAll(SaveContext context) {
        return Set.of();
    }

    @Override
    protected Set<Object> deleteAll(SaveContext context) {
        return Set.of();
    }

    @Override
    protected List<Object> loadAllValues(ValueLoadContext context) {
        return List.of();
    }

    @Override
    protected long countAllValues(ValueLoadContext context) {
        return 0;
    }

    @Override
    protected Object beginLoadTransaction(boolean joinTransaction) {
        return null;
    }

    @Override
    protected Object beginSaveTransaction(boolean joinTransaction) {
        return null;
    }

    @Override
    protected void commitTransaction(Object transaction) {

    }

    @Override
    protected void rollbackTransaction(Object transaction) {

    }

    @Override
    protected TransactionContextState getTransactionContextState(boolean isJoinTransaction) {
        return null;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void setName(String name) {

    }
}
