package com.nykaa.loyalty.entity;

import com.nykaa.superstore.loyalty.entity.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@Entity
@Table(name = "loyalty_system_property")
public class SystemProperty extends BaseEntity {
    private static final long serialVersionUID = 9179361198384769473L;

    @Column(name = "name")
    private String name;

    @Lob
    @Column(name = "value")
    private String value;
}
