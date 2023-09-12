package com.nykaa.loyalty.entity;

import com.nykaa.superstore.loyalty.entity.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Map;

@Data
@Entity
@Table(name = "pincode_cluster_mapping")
public class PincodeClusterMapping extends BaseEntity {
    @Column(name = "pincode")
    private String pincode;

    @Column(name = "cluster")
    private String cluster = "";

    @Column(name = "is_active", columnDefinition = "tinyint(1) default 1")
    private Boolean isActive;

    @Convert(converter = com.nykaa.base.converter.HashMapConverter.class)
    @Column(name = "uploader_name")
    private Map<String, String> uploaderName;
}
