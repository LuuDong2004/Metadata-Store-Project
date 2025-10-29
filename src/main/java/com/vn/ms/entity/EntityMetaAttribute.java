package com.vn.ms.entity;

import com.vn.ms.entity.enums.DataType;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.util.UUID;

@JmixEntity
@Table(name = "ENTITY_META_ATTRIBUTE", indexes = {
        @Index(name = "IDX_ENTITY_META_ATTRIBUTE_ENTITY_META", columnList = "ENTITY_META_ID")
})
@Entity
public class EntityMetaAttribute {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @Column(name = "TYPE_")
    private String type;

    @JoinColumn(name = "ENTITY_META_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private EntityMeta entityMeta;

    public EntityMeta getEntityMeta() {
        return entityMeta;
    }

    public void setEntityMeta(EntityMeta entityMeta) {
        this.entityMeta = entityMeta;
    }

    public void setType(DataType type) {
        this.type = type == null ? null : type.getId();
    }

    public DataType getType() {
        return type == null ? null : DataType.fromId(type);
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