package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSetting;
import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@Transactional
public interface PayslipSettingRepository extends JpaRepository<PayslipSetting, Long> {

    @EntityGraph(value = "PayslipSetting.elements")
    List<PayslipSetting> findAllByDraftIsFalseAndClosedIsFalse();

    Boolean existsByCompanyAndByDefaultIsTrue(Long company);

    @Override
    @EntityGraph(value = "PayslipSetting.elements", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT ps FROM PayslipSetting ps  WHERE ps.id = :id AND (ps.draft IS NULL OR NOT ps.draft)")
    Optional<PayslipSetting> findById(@Param("id") Long id);

    @EntityGraph(value = "PayslipSetting.elements", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT ps FROM PayslipSetting ps  WHERE ps.id = :id AND ps.draft")
    Optional<PayslipSetting> findDraftPayslip(@Param("id") Long id);

    @Nonnull
    @EntityGraph(value = "PayslipSetting.elements")
    Optional<PayslipSetting> findTopByTemplateAndByDefaultOrderByIdAsc(boolean template, boolean byDefault);


    @Modifying
    @Query("UPDATE PayslipSetting  p SET p.byDefault = CASE WHEN p.id =:template_id THEN true ELSE false END")
    void markAsDefault(@Param("template_id") Long templateId);

    @EntityGraph(value = "PayslipSetting.elements")
    Page<PayslipSetting> findAllByNameLikeAndTemplateIsTrue(String query, Pageable pageable);

    @Modifying
    void deleteByContractAndPaymentDateAndIdNot(Long contractId, LocalDate paymentDate, Long id);

    @Modifying
    void deleteByContractInAndPaymentDate(Collection<Long> contractId, LocalDate paymentDate);

    Optional<PayslipSetting> findTopByContractAndPaymentDateAndByDefaultOrderByIdDesc(Long contractId, LocalDate paymentDate, Boolean byDefault);

    Optional<PayslipSetting> findTopByContractAndPaymentDateOrderByIdDesc(Long contractId, LocalDate paymentDate);

    @Query("SELECT EXISTS(SELECT ps.contract FROM PayslipSetting ps WHERE ps.closed AND ps.paymentDate = :month)")
    Boolean hasClosedPayslips(@Param("month") LocalDate month);

    @Query("SELECT ps.employee FROM PayslipSetting ps WHERE ps.closed AND ps.paymentDate = :month")
    Set<Long> getAllClosedPayslipResources(@Param("month") LocalDate month);

    @Query("SELECT ps.employee FROM PayslipSetting ps WHERE (ps.closed OR ps.draft) AND ps.paymentDate = :month")
    Set<Long> getAllClosedOrDraftPayslipResources(@Param("month") LocalDate month);

    @Query("SELECT ps.contract FROM PayslipSetting ps WHERE  ps.draft AND ps.paymentDate = :month")
    Set<Long> getAllDraftPayslipResources(@Param("month") LocalDate month);

    @Query("SELECT EXISTS(SELECT ps.id FROM PayslipSetting ps WHERE ps.closed And ps.contract = :contract_id AND ps.paymentDate = :month)")
    Boolean isClosedPayslip(@Param("month") LocalDate month, @Param("contract_id") Long contractId);

    @Modifying
    void deleteAllByContractInAndPaymentDateAndDraftIsTrue(Collection<Long> contractIds, LocalDate paymentDate);


    @Query("SELECT ps FROM PayslipSetting ps WHERE ps.closed AND ps.paymentDate = :month AND (:employee_ids IS NULL OR ps.employee IN :employee_ids)")
    Page<PayslipSetting> findAllClosed(@Param("month") LocalDate month,  @Param("employee_ids") Set<Long> employeeIds, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT ps.employee) FROM PayslipSetting ps LEFT JOIN Employee e on e.id  = ps.employee WHERE ps.closed AND e.affiliate = :affiliate AND ps.paymentDate BETWEEN :start_date AND :end_date ")
    Long countAllClosed(@Param("start_date") LocalDate startDate, @Param("end_date") LocalDate endDate,  @Param("affiliate") String affiliate);

    @Modifying
    @Query(value = """
            INSERT INTO payslip_setting_element( payslip_setting_id, section, base_value, rate_value, value, fix, element_order, created_by, created_at, updated_by, updated_at, company_id)
            SELECT template.id, s.id, pc.base_value, pc.rate_value, pc.value, true, el.element_order, null , now(), null, now() , s.company_id 
            FROM payslip_setting_element el INNER join section s on s.id = el.section
            INNER JOIN payslip_setting template on template.parent_id = el.payslip_setting_id 
            INNER join payslip_close pc on pc.code = s.code AND pc.contract_id = template.contract_id where template.closed 
            AND template.payment_date = :month AND  pc.contract_id in :contract_ids and pc.month = :month GROUP BY template.id,  s.id
            """, nativeQuery = true)
    void insertClosedPayslip(@Param("month") LocalDate month, @Param("contract_ids") Set<Long> contractIds);

    @Query("""
            SELECT count(distinct ps.employee) FROM PayslipSetting ps WHERE ps.closed AND ps.paymentDate = :month AND ps.company = :company_id
            """)
    long countClosedByPaymentDate(@Param("month") LocalDate month, @Param("company_id") Long companyId);
}