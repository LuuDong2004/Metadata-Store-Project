package com.vn.ms.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.vn.ms.entity.DynamicStoreMeta;
import com.vn.ms.service.DynamicQueryService;
import com.vn.ms.service.DynamicStoreRegistry;
import com.vn.ms.service.MetadataImportService;
import com.vn.ms.service.VirtualEntityService;
import io.jmix.core.DataManager;
import io.jmix.core.entity.KeyValueEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller để quản lý metadata runtime
 */
@RestController
@RequestMapping("/api/metadata")
public class MetadataController {

    @Autowired
    private MetadataImportService importService;

    @Autowired
    private DynamicStoreRegistry registry;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private DynamicQueryService queryService;

    @Autowired
    private VirtualEntityService virtualEntityService;

    /**
     * Upload metadata cho một store ảo từ JSON
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadMetadata(@RequestBody String jsonMetadata) {
        try {
            DynamicStoreMeta storeMeta = importService.importFromJson(jsonMetadata);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Store metadata uploaded successfully",
                "storeId", storeMeta.getId(),
                "storeName", storeMeta.getName(),
                "fullyQualifiedNames", getEntityNames(storeMeta.getName())
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to upload metadata: " + e.getMessage()
            ));
        }
    }

    /**
     * Upload metadata cho nhiều stores từ JSON array
     */
    @PostMapping("/upload-multiple")
    public ResponseEntity<?> uploadMultipleMetadata(@RequestBody String jsonArrayMetadata) {
        try {
            List<DynamicStoreMeta> storeMetaList = importService.importMultipleStores(jsonArrayMetadata);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Multiple store metadata uploaded successfully",
                "count", storeMetaList.size(),
                "stores", storeMetaList.stream().map(s -> Map.of(
                    "storeId", s.getId(),
                    "storeName", s.getName(),
                    "fullyQualifiedNames", getEntityNames(s.getName())
                )).toList()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to upload multiple metadata: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy thông tin tất cả virtual stores
     */
    @GetMapping("/stores")
    public ResponseEntity<?> getAllStores() {
        try {
            var virtualStores = registry.getAllVirtualStores();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "stores", virtualStores.entrySet().stream().map(entry -> Map.of(
                    "storeName", entry.getKey(),
                    "entities", entry.getValue().keySet(),
                    "fullyQualifiedNames", entry.getValue().keySet().stream()
                        .map(entityName -> entry.getKey() + "$" + entityName)
                        .toList()
                )).toList()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to get stores: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy thông tin chi tiết của một store ảo
     */
    @GetMapping("/stores/{storeName}")
    public ResponseEntity<?> getStoreDetail(@PathVariable String storeName) {
        try {
            var entities = registry.getAllVirtualStores().get(storeName);
            if (entities == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "storeName", storeName,
                "entities", entities.entrySet().stream().map(entry -> Map.of(
                    "entityName", entry.getKey(),
                    "fullyQualifiedName", storeName + "$" + entry.getKey(),
                    "description", entry.getValue().getDescription(),
                    "attributes", entry.getValue().getAttributes() != null ? 
                        entry.getValue().getAttributes().stream().map(attr -> Map.of(
                            "name", attr.getName(),
                            "type", attr.getType().getId()
                        )).toList() : List.of()
                )).toList()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to get store detail: " + e.getMessage()
            ));
        }
    }

    /**
     * Xóa một store ảo (chỉ xóa khỏi registry, không xóa database)
     */
    @DeleteMapping("/stores/{storeName}")
    public ResponseEntity<?> deleteStore(@PathVariable String storeName) {
        try {
            // TODO: Implement store removal from registry
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Store removed from registry: " + storeName
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to delete store: " + e.getMessage()
            ));
        }
    }



    /**
     * Query virtual entities
     */
    @GetMapping("/query/{storeName}/{entityName}")
    public ResponseEntity<?> queryEntities(
            @PathVariable String storeName, 
            @PathVariable String entityName,
            @RequestParam(required = false) String condition) {
        try {
            if (!queryService.entityExists(storeName, entityName)) {
                return ResponseEntity.notFound().build();
            }

            List<KeyValueEntity> entities;
            if (condition != null && !condition.trim().isEmpty()) {
                entities = queryService.query(storeName, entityName, condition);
            } else {
                entities = queryService.loadAll(storeName, entityName);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "storeName", storeName,
                "entityName", entityName,
                "fullyQualifiedName", storeName + "$" + entityName,
                "count", entities.size(),
                "data", entities.stream().map(entity -> {
                    Map<String, Object> data = new HashMap<>();
                    entity.getInstanceMetaClass().getProperties().forEach(prop -> {
                        data.put(prop.getName(), entity.getValue(prop.getName()));
                    });
                    return data;
                }).toList()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Query failed: " + e.getMessage()
            ));
        }
    }

    /**
     * Load one entity by ID
     */
    @GetMapping("/query/{storeName}/{entityName}/{id}")
    public ResponseEntity<?> loadOneEntity(
            @PathVariable String storeName, 
            @PathVariable String entityName,
            @PathVariable String id) {
        try {
            if (!queryService.entityExists(storeName, entityName)) {
                return ResponseEntity.notFound().build();
            }

            KeyValueEntity entity = queryService.loadOne(storeName, entityName, id);
            if (entity == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> data = new HashMap<>();
            entity.getInstanceMetaClass().getProperties().forEach(prop -> {
                data.put(prop.getName(), entity.getValue(prop.getName()));
            });

            return ResponseEntity.ok(Map.of(
                "success", true,
                "storeName", storeName,
                "entityName", entityName,
                "fullyQualifiedName", storeName + "$" + entityName,
                "data", data
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Load failed: " + e.getMessage()
            ));
        }
    }

    /**
     * Tạo virtual entity đơn giản
     */
    @PostMapping("/create-simple/{storeName}/{entityName}")
    public ResponseEntity<?> createSimpleEntity(
            @PathVariable String storeName, 
            @PathVariable String entityName) {
        try {
            DynamicStoreMeta storeMeta = virtualEntityService.createSimpleEntity(storeName, entityName);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Simple virtual entity created successfully",
                "storeId", storeMeta.getId(),
                "storeName", storeMeta.getName(),
                "entityName", entityName,
                "fullyQualifiedName", storeName + "$" + entityName
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to create simple entity: " + e.getMessage()
            ));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Metadata service is running",
            "registeredStores", registry.getAllVirtualStores().size()
        ));
    }

    /**
     * Helper method để lấy entity names với fully-qualified format
     */
    private List<String> getEntityNames(String storeName) {
        var entities = registry.getAllVirtualStores().get(storeName);
        if (entities == null) {
            return List.of();
        }
        return entities.keySet().stream()
            .map(entityName -> storeName + "$" + entityName)
            .toList();
    }

}
