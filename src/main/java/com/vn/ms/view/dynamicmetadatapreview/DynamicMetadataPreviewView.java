package com.vn.ms.view.dynamicmetadatapreview;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import com.vn.ms.entity.DynamicStoreMeta;
import com.vn.ms.entity.EntityMeta;
import com.vn.ms.entity.EntityMetaAttribute;
import com.vn.ms.entity.enums.DataType;
import com.vn.ms.service.MetadataImportService;
import com.vn.ms.view.main.MainView;
import io.jmix.core.DataManager;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.KeyValueCollectionContainer;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@Route(value = "dynamic-metadata-preview-view", layout = MainView.class)
@ViewController("DynamicMetadataPreviewView")
@ViewDescriptor("dynamic-metadata-preview-view.xml")
public class DynamicMetadataPreviewView extends StandardView {

    @ViewComponent
    private TextArea metadataJsonArea;
    @ViewComponent
    private ComboBox<String> entityCombo;
    @ViewComponent
    private Button previewBtn;
    @ViewComponent
    private Button saveBtn;
    @ViewComponent
    private DataGrid<KeyValueEntity> previewGrid;
    @ViewComponent
    private KeyValueCollectionContainer previewDc;

    @Autowired private Notifications notifications;
    @Autowired private MetadataImportService metadataImportService;
    @Autowired private DataManager dataManager;

    private final ObjectMapper om = new ObjectMapper();

    /** cấu trúc tạm giữ metadata parse ra */
    private static class ParsedMeta {
        String storeName;
        String description;
        List<EntityMeta> entities;
        Map<String, List<Map<String, Object>>> dataByEntity = new HashMap<>();
    }
    private ParsedMeta lastParsed;

    @Subscribe
    public void onInit(InitEvent event) {
        previewBtn.addClickListener(e -> doPreview());
        saveBtn.addClickListener(e -> doSave());
        saveBtn.setEnabled(false);
    }

    /** === Preview: hiển thị cột + dữ liệu từ JSON === */
    private void doPreview() {
        try {
            String metaJson = StringUtils.defaultString(metadataJsonArea.getValue()).trim();
            if (metaJson.isEmpty()) { warn("Nhập Metadata JSON trước nhé."); return; }

            lastParsed = parseToParsedMeta(metaJson);
            if (lastParsed.entities == null || lastParsed.entities.isEmpty()) {
                warn("Metadata chưa có entities."); return;
            }

            // Chọn entity
            List<String> names = lastParsed.entities.stream().map(EntityMeta::getName).collect(Collectors.toList());
            entityCombo.setItems(names);
            if (entityCombo.getValue() == null && !names.isEmpty()) entityCombo.setValue(names.get(0));

            String entityName = entityCombo.getValue();
            EntityMeta em = lastParsed.entities.stream()
                    .filter(x -> Objects.equals(x.getName(), entityName))
                    .findFirst().orElse(null);
            if (em == null) { warn("Không tìm thấy entity: " + entityName); return; }

            // Tập cột
            LinkedHashSet<String> cols = new LinkedHashSet<>();
            if (em.getAttributes() != null) {
                for (EntityMetaAttribute a : em.getAttributes()) {
                    String col = Objects.toString(a.getName(), "").trim();
                    if (!col.isEmpty()) cols.add(col);
                }
            }

            // Dựng cột cho grid
            previewGrid.removeAllColumns();
            for (String col : cols) {
                previewGrid.addColumn(kve -> {
                    Object v = kve.getValue(col);
                    return v == null ? "" : String.valueOf(v);
                }).setHeader(col).setKey(col).setAutoWidth(true).setResizable(true).setFlexGrow(1);
            }

            // Dữ liệu (nếu có "data" trong JSON)
            List<Map<String, Object>> rawData = lastParsed.dataByEntity.getOrDefault(entityName, List.of());
            List<KeyValueEntity> items = new ArrayList<>();
            for (Map<String, Object> r : rawData) {
                KeyValueEntity e = new KeyValueEntity();
                for (String c : cols) if (r.containsKey(c)) e.setValue(c, r.get(c));
                items.add(e);
            }
            previewGrid.setItems(items);
            saveBtn.setEnabled(true);
            ok("Preview OK (" + items.size() + " dòng).");

        } catch (Exception ex) {
            saveBtn.setEnabled(false);
            err("Preview lỗi: " + ex.getMessage());
        }
    }

    /** === Save === */
    private void doSave() {
        if (lastParsed == null) { warn("Chưa preview. Hãy Preview trước khi Save."); return; }
        try {
            String json = StringUtils.defaultString(metadataJsonArea.getValue()).trim();
            DynamicStoreMeta stored = metadataImportService.importFromJson(json);
            ok("Đăng ký thành công store: " + stored.getName());
            saveBtn.setEnabled(false);
        } catch (Exception ex) {
            err("Save lỗi: " + ex.getMessage());
        }
    }

    /** Parse JSON metadata (có thể kèm data) */
    @SuppressWarnings("unchecked")
    private ParsedMeta parseToParsedMeta(String json) throws Exception {
        Map<String, Object> raw = om.readValue(json, new TypeReference<Map<String, Object>>() {});
        ParsedMeta pm = new ParsedMeta();
        pm.storeName = Objects.toString(raw.get("storeName"), "preview");
        pm.description = Objects.toString(raw.get("description"), "");

        List<Map<String,Object>> entityMaps;
        if (raw.containsKey("entities") && raw.get("entities") instanceof List) {
            entityMaps = (List<Map<String,Object>>) raw.get("entities");
        } else if (raw.containsKey("attributes")) {
            entityMaps = List.of(raw);
        } else {
            entityMaps = List.of();
        }

        List<EntityMeta> entities = new ArrayList<>();
        for (Map<String,Object> emap : entityMaps) {
            EntityMeta em = new EntityMeta();
            em.setName(Objects.toString(emap.get("name"), "Entity"));
            em.setDescription(Objects.toString(emap.get("description"), ""));
            List<EntityMetaAttribute> aList = new ArrayList<>();
            Object attrsObj = emap.get("attributes");
            if (attrsObj instanceof List) {
                List<Map<String,Object>> attrs = (List<Map<String,Object>>) attrsObj;
                for (Map<String,Object> a : attrs) {
                    EntityMetaAttribute att = new EntityMetaAttribute();
                    att.setName(Objects.toString(a.get("name"), ""));
                    String typeStr = Objects.toString(a.get("type"), "STRING");
                    DataType dt = DataType.fromId(typeStr);
                    if (dt == null) dt = DataType.STRING;
                    att.setType(dt);
                    aList.add(att);
                }
            }
            em.setAttributes(aList);
            entities.add(em);

            // parse "data"
            List<Map<String,Object>> dataList = new ArrayList<>();
            Object dataObj = emap.get("data");
            if (dataObj instanceof List<?>) {
                for (Object row : (List<?>) dataObj) {
                    if (row instanceof Map<?,?>) {
                        Map<String,Object> map = new LinkedHashMap<>();
                        ((Map<?,?>) row).forEach((k,v)-> map.put(String.valueOf(k), v));
                        dataList.add(map);
                    }
                }
            }
            pm.dataByEntity.put(em.getName(), dataList);
        }
        pm.entities = entities;
        return pm;
    }

    private void ok(String m){ notifications.create(m).withType(Notifications.Type.SUCCESS).show(); }
    private void warn(String m){ notifications.create(m).withType(Notifications.Type.WARNING).show(); }
    private void err(String m){ notifications.create(m).withType(Notifications.Type.ERROR).show(); }
}
