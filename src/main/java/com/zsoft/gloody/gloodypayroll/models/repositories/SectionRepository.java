package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.beans.SectionMapEntry;
import com.zsoft.gloody.gloodypayroll.models.entities.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long>, JpaSpecificationExecutor<Section> {

    @Query("SELECT distinct new com.zsoft.gloody.gloodypayroll.models.beans.SectionMapEntry(s.code,s) FROM Section s" )
    List<SectionMapEntry> findAllAsMap();

    Optional<Section> findByCode(String code);

    boolean existsByCode(String code);

    void deleteByCode(String code);
}