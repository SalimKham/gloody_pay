package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.entities.SectionSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SectionSettingRepository extends JpaRepository<SectionSetting, Long> {
    Optional<SectionSetting> findByKey(String key);
}