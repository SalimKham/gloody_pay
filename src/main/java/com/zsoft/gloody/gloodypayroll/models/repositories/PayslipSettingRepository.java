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
    List<PayslipSetting> findAll();

    @Override
    @EntityGraph(value = "PayslipSetting.elements", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT ps FROM PayslipSetting ps  WHERE ps.id = :id AND (ps.draft IS NULL OR NOT ps.draft)")
    Optional<PayslipSetting> findById(@Param("id") Long id);

    @EntityGraph(value = "PayslipSetting.elements", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT ps FROM PayslipSetting ps  WHERE ps.id = :id AND ps.draft")
    Optional<PayslipSetting> findDraftPayslip(@Param("id") Long id);

    @Nonnull
    @EntityGraph(value = "PayslipSetting.elements")
    Optional<PayslipSetting> findTopByByDefaultOrderByIdAsc(boolean template);

    @Modifying
    @Query("UPDATE PayslipSetting  p SET p.byDefault = CASE WHEN p.id =:template_id THEN true ELSE false END")
    void markAsDefault(@Param("template_id") Long templateId);

    Page<PayslipSetting> findAllByNameLikeAndTemplateIsTrue(String query, Pageable pageable);

    @EntityGraph(value = "PayslipSetting.elements")
    @Query("SELECT ps FROM PayslipSetting ps where ps.employee = :employee_id  AND not ps.draft AND MONTH(ps.paymentDate) = MONTH(:current_month ) AND YEAR(ps.paymentDate) = YEAR (:current_month ) order by ps.id desc ")
    List<PayslipSetting> findByEmployeeAndCurrentMonth(@Param("employee_id") Long employeeId, @Param("current_month") LocalDate currentMonth);

    @Modifying
    void deleteByContractAndPaymentDate(Long contractId, LocalDate paymentDate);

    @Query("SELECT ps.contract FROM PayslipSetting ps WHERE ps.closed OR ps.draft AND ps.paymentDate = :month")
    Set<Long> getAllClosedContracts(@Param("month") LocalDate month);

    @Query("SELECT EXISTS(SELECT ps.id FROM PayslipSetting ps WHERE ps.closed And ps.contract = :contract_id AND ps.paymentDate = :month)")
    Boolean isClosedPayslip(@Param("month") LocalDate month,@Param("contract_id") Long contractId);

    @Modifying
    void deleteAllByContractInAndPaymentDateAndDraftIsTrue(Collection<Long> contractIds, LocalDate paymentDate);
}