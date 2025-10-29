package com.vn.ms.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "METADATA_DEFINITION", indexes = {
        @Index(name = "IDX_METADATA_DEFINITION_STORE_META", columnList = "STORE_META_ID")
})
@Entity
public class EntityMeta {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @JoinColumn(name = "STORE_META_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private DynamicStoreMeta storeMeta;

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @Composition
    @OneToMany(mappedBy = "entityMeta")
    private List<EntityMetaAttribute> attributes;

    public DynamicStoreMeta getStoreMeta() {
        return storeMeta;
    }

    public void setStoreMeta(DynamicStoreMeta storeMeta) {
        this.storeMeta = storeMeta;
    }

    public List<EntityMetaAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<EntityMetaAttribute> attributes) {
        this.attributes = attributes;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}