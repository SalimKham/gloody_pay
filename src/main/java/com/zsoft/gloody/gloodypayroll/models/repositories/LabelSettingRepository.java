package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.entities.LabelSetting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
@Transactional
public interface LabelSettingRepository extends JpaRepository<LabelSetting, Long> {
    Page<LabelSetting> findAllByLabelLikeOrKeyLike(String label, String key, Pageable pageable);

    Boolean existsByKeyAndCompany(String key, Long company);

    @Modifying
    @Query(value = "update label_setting ls set ls.parent_id = (select pls.id from label_setting pls where pls.`key` = :parent_code limit 1) where ls.`key` = :label_code AND ls.company_id = :company_id", nativeQuery = true)
    void updateParentId(@Param("label_code") String labelCode, @Param("parent_code") String parentCode, @Param("company_id") Long companyId);
}