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
        @Index(name = "IDX_METADATA_DEFINITION_DATA_STORE", columnList = "DATA_STORE_ID")
})
@Entity
public class MetadataDefinition {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @JoinColumn(name = "DATA_STORE_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private DynamicDataStore dataStore;

    @Composition
    @OneToMany(mappedBy = "metadataDefinition")
    private List<MetadataAttributeDefinition> attributes;

    public List<MetadataAttributeDefinition> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<MetadataAttributeDefinition> attributes) {
        this.attributes = attributes;
    }


    public DynamicDataStore getDataStore() {
        return dataStore;
    }

    public void setDataStore(DynamicDataStore dataStore) {
        this.dataStore = dataStore;
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