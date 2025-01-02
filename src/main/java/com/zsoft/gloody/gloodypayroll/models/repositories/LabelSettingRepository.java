package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.entities.LabelSetting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface LabelSettingRepository extends JpaRepository<LabelSetting, Long> {
    Page<LabelSetting> findAllByLabelLikeOrKeyLike(String label, String key, Pageable pageable);
}