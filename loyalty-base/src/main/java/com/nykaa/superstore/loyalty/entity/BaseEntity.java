package com.nykaa.superstore.loyalty.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@MappedSuperclass
public class BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @CreatedDate
    @Column(name = "created", columnDefinition = "timestamp default current_timestamp")
    private Date created;

    @LastModifiedDate
    @Column(name = "updated", columnDefinition = "timestamp default current_timestamp on update current_timestamp")
    private Date updated;

    @Column(name = "deleted", columnDefinition = "tinyint(1) default 0")
    private Boolean deleted = false;

    @PrePersist
    public void prePersist() {
        this.created = this.updated = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        this.updated = new Date();
    }

}