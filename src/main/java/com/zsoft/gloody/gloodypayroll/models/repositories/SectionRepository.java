package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.beans.SectionMapEntry;
import com.zsoft.gloody.gloodypayroll.models.entities.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long>, JpaSpecificationExecutor<Section> {

    @Query("SELECT distinct new com.zsoft.gloody.gloodypayroll.models.beans.SectionMapEntry(s.code,s) FROM Section s WHERE s.company = :company_id" )
    List<SectionMapEntry> findAllByCompanyAsMap(@Param("company_id") Long companyId);

    List<Section> findAllByCompany(@Param("company_id") Long companyId);

    @Query("""
            SELECT s FROM Section s LEFT JOIN PayslipSettingElement  pse ON pse.section.id = s.id WHERE pse.value IS NOT NULL AND pse.payslipSetting.employee IN :resource_ids AND pse.payslipSetting.closed AND  pse.payslipSetting.paymentDate = :selected_month
            ORDER BY pse.payslipSetting.paymentDate ASC
            """)
    List<Section> getMonthlySection(@Param("resource_ids") Set<Long> resourceIds, @Param("selected_month") LocalDate month);

    Boolean existsByIdentifierAndCompany(Integer code, Long company_id);

    Optional<Section> findTopByCompanyOrderByIdentifierDesc(Long company_id);


    Boolean existsByCodeAndCompany(String code, Long company_id);

    @Query("SELECT s FROM Section  s WHERE s.code LIKE :code AND s.company = :companyId")
    Optional<Section> findByCodeAndCompany(@Param("code")  String  code, @Param("companyId") Long CompanyId);

    boolean existsByCode(String code);

    void deleteByCode(String code);
}