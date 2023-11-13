package com.zsoft.gloody.gloodypayroll.rest.mappers;

import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSetting;
import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSettingElement;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingElementDto;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import org.mapstruct.*;

@Mapper(uses = {EmployeeMapper.class, SectionMapper.class})
public abstract class PayslipSettingMapper {
    public abstract PayslipSettingDto toDto(PayslipSetting setting);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    public abstract PayslipSetting toEntity(PayslipSettingDto settingDto);

    @Mapping(target = "payslipSettingId", source = "payslipSetting.id")
    @Mapping(target = "section", source = "section")
    public abstract PayslipSettingElementDto toDto(PayslipSettingElement setting);

    @Mapping(target = "section", source = "section")
    @BeanMapping(builder = @Builder(disableBuilder = true))
    public abstract PayslipSettingElement toEntity(PayslipSettingElementDto settingDto);

    @AfterMapping
    public void afterMapping(@MappingTarget PayslipSettingElement element) {
        element.setTenantId(SecurityUtils.getCurrentTenantId());
    }

    @AfterMapping
    public void afterMapping(@MappingTarget PayslipSetting setting) {
        if (setting.getElements() != null) {
            setting.getElements().forEach(element -> element.setPayslipSetting(setting));
        }
    }
}

