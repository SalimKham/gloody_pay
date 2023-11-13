package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.entities.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, Long>, JpaSpecificationExecutor<Section> {
    Optional<Section> findByCode(String code);

    boolean existsByCode(String code);

    void deleteByCode(String code);
}