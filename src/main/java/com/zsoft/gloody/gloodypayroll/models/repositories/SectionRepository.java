package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.beans.SectionMapEntry;
import com.zsoft.gloody.gloodypayroll.models.entities.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long>, JpaSpecificationExecutor<Section> {

    @Query("SELECT distinct new com.zsoft.gloody.gloodypayroll.models.beans.SectionMapEntry(s.code,s) FROM Section s WHERE s.company = :company_id" )
    List<SectionMapEntry> findAllByCompanyAsMap(@Param("company_id") Long companyId);


    Boolean existsByCodeAndCompany(String code, Long company_id);

    @Query("SELECT s FROM Section  s WHERE s.code LIKE :code AND s.company = :companyId")
    Optional<Section> findByCodeAndCompany(@Param("code")  String  code, @Param("companyId") Long CompanyId);

    boolean existsByCode(String code);

    void deleteByCode(String code);
}