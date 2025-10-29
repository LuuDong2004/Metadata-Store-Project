package com.vn.ms.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vn.ms.entity.DynamicStoreMeta;
import com.vn.ms.entity.EntityMeta;
import com.vn.ms.entity.EntityMetaAttribute;
import com.vn.ms.entity.enums.DataType;
import io.jmix.core.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service để parse JSON metadata, preview (không lưu) và import (lưu + registry)
 */
@Service
public class MetadataImportService {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private DynamicStoreRegistry registry;

    @Autowired
    private ObjectMapper objectMapper;

    /** ====== DRAFT MODELS (preview-only, không dính JPA) ====== */
    public static class StoreDraft {
        public String name;
        public String description;
        public List<EntityDraft> entities = new ArrayList<>();
    }
    public static class EntityDraft {
        public String name;
        public String description;
        public List<AttributeDraft> attributes = new ArrayList<>();
    }
    public static class AttributeDraft {
        public String name;
        public DataType type;
    }

    /** Cho phép controller lấy registry để resolve MetaClass nếu cần */
    public DynamicStoreRegistry getRegistry() {
        return registry;
    }

    // ================= PREVIEW (KHÔNG SAVE DB) =================

    /** Parse từ JSON string -> draft (preview-only) */
    public StoreDraft parseDraft(String jsonString) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);
            return parseDraft(rootNode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON metadata", e);
        }
    }

    /** Parse từ JsonNode -> draft (preview-only) */
    public StoreDraft parseDraft(JsonNode rootNode) {
        StoreDraft draft = new StoreDraft();
        draft.name = getText(rootNode, "storeName", "preview");
        draft.description = getText(rootNode, "description", "");

        JsonNode entitiesNode = rootNode.get("entities");
        if (entitiesNode != null && entitiesNode.isArray()) {
            for (JsonNode entityNode : entitiesNode) {
                EntityDraft ed = new EntityDraft();
                ed.name = getText(entityNode, "name", "Entity");
                ed.description = getText(entityNode, "description", "");

                JsonNode attrsNode = entityNode.get("attributes");
                if (attrsNode != null && attrsNode.isArray()) {
                    for (JsonNode attrNode : attrsNode) {
                        AttributeDraft ad = new AttributeDraft();
                        ad.name = getText(attrNode, "name", "");
                        ad.type = parseDataType(getText(attrNode, "type", "STRING"));
                        ed.attributes.add(ad);
                    }
                }

                draft.entities.add(ed);
            }
        }
        return draft;
    }

    // ================= IMPORT (SAVE + REGISTER) =================

    /** Import metadata từ JSON string: persist JPA + register vào registry */
    @Transactional
    public DynamicStoreMeta importFromJson(String jsonString) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);
            return importFromJsonNode(rootNode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON metadata", e);
        }
    }

    /** Import metadata từ JsonNode: persist JPA + register vào registry */
    @Transactional
    public DynamicStoreMeta importFromJsonNode(JsonNode rootNode) {
        // 1) Parse ra draft (tái dùng logic preview)
        StoreDraft draft = parseDraft(rootNode);

        // 2) Persist DynamicStoreMeta
        DynamicStoreMeta storeMeta = dataManager.create(DynamicStoreMeta.class);
        storeMeta.setName(draft.name);
        storeMeta.setDescription(draft.description);
        storeMeta = dataManager.save(storeMeta);

        // 3) Persist Entities + Attributes -> map theo tên
        Map<String, EntityMeta> entityMap = new HashMap<>();
        for (EntityDraft ed : draft.entities) {
            EntityMeta em = persistEntity(ed, storeMeta);
            entityMap.put(em.getName(), em);
        }

        // 4) Đăng ký vào registry (dựa trên API hiện có của bạn)
        registry.registerStore(storeMeta, entityMap);

        return storeMeta;
    }
    @Transactional
    public List<DynamicStoreMeta> importMultipleStores(String jsonArrayString) {
        try {
            JsonNode arrayNode = objectMapper.readTree(jsonArrayString);
            List<DynamicStoreMeta> results = new ArrayList<>();

            if (arrayNode != null && arrayNode.isArray()) {
                for (JsonNode storeNode : arrayNode) {
                    results.add(importFromJsonNode(storeNode));
                }
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON array metadata", e);
        }
    }

    /** Import từ draft đã có sẵn (khi controller muốn chủ động preview trước rồi save sau) */
    @Transactional
    public DynamicStoreMeta importFromDraft(StoreDraft draft) {
        // 1) Persist store
        DynamicStoreMeta storeMeta = dataManager.create(DynamicStoreMeta.class);
        storeMeta.setName(draft.name);
        storeMeta.setDescription(draft.description);
        storeMeta = dataManager.save(storeMeta);

        // 2) Persist entities & attributes
        Map<String, EntityMeta> entityMap = new HashMap<>();
        for (EntityDraft ed : draft.entities) {
            EntityMeta em = persistEntity(ed, storeMeta);
            entityMap.put(em.getName(), em);
        }

        // 3) Register registry
        registry.registerStore(storeMeta, entityMap);
        return storeMeta;
    }

    // ================== PRIVATE HELPERS (persist) ==================

    private EntityMeta persistEntity(EntityDraft ed, DynamicStoreMeta storeMeta) {
        EntityMeta entityMeta = dataManager.create(EntityMeta.class);
        entityMeta.setName(ed.name);
        entityMeta.setDescription(ed.description);
        entityMeta.setStoreMeta(storeMeta);
        entityMeta = dataManager.save(entityMeta);

        List<EntityMetaAttribute> attrs = new ArrayList<>();
        for (AttributeDraft ad : ed.attributes) {
            attrs.add(persistAttribute(ad, entityMeta));
        }
        entityMeta.setAttributes(attrs);
        return dataManager.save(entityMeta);
    }

    private EntityMetaAttribute persistAttribute(AttributeDraft ad, EntityMeta entityMeta) {
        EntityMetaAttribute attribute = dataManager.create(EntityMetaAttribute.class);
        attribute.setName(ad.name);
        attribute.setEntityMeta(entityMeta);
        attribute.setType(ad.type != null ? ad.type : DataType.STRING);
        return dataManager.save(attribute);
    }

    // ================== UTILS ==================

    private static String getText(JsonNode node, String field, String def) {
        return node != null && node.has(field) && !node.get(field).isNull()
                ? node.get(field).asText()
                : def;
    }

    private static DataType parseDataType(String typeStr) {
        if (typeStr == null) return DataType.STRING;
        DataType t = DataType.fromId(typeStr);
        return t != null ? t : DataType.STRING;
    }
}
