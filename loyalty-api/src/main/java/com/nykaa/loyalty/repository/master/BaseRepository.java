package com.nykaa.loyalty.repository.master;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

    @Modifying
    void deleteById(ID id);

    T findByIdAndDeletedFalse(ID id);

    List<T> findByIdInAndDeletedFalse(List<ID> id);

    List<T> findByIdInAndDeletedFalse(Set<ID> id);
}