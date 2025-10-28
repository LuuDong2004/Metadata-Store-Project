package com.vn.ms.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.util.UUID;

@JmixEntity
@Table(name = "METADATA_ATTRIBUTE_DEFINITION", indexes = {
        @Index(name = "IDX_METADATA_ATTRIBUTE_DEFINITION_METADATA_DEFINITION", columnList = "METADATA_DEFINITION_ID")
})
@Entity
public class MetadataAttributeDefinition {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @Column(name = "TYPE_")
    private String type;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "METADATA_DEFINITION_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private MetadataDefinition metadataDefinition;

    public MetadataDefinition getMetadataDefinition() {
        return metadataDefinition;
    }

    public void setMetadataDefinition(MetadataDefinition metadataDefinition) {
        this.metadataDefinition = metadataDefinition;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

}