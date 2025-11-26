package com.zsoft.gloody.gloodypayroll.rest.mappers;

import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSettingElement;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingElementDto;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.mapstruct.*;

import java.util.Objects;

@RequiredArgsConstructor
@Mapper(uses = {SectionMapper.class})
public abstract class PayslipSettingElementMapper {

    @Mapping(target = "payslipSettingId", ignore = true)
    @Mapping(target = "section", source = "section.code")
    public abstract PayslipSettingElementDto toDto(PayslipSettingElement setting);

    @Mapping(target = "section", source = "section")
    @BeanMapping(builder = @Builder(disableBuilder = true))
    public abstract PayslipSettingElement toEntity(PayslipSettingElementDto settingDto);

    @AfterMapping
    void afterMapping(@MappingTarget PayslipSettingElement element) {
        if (Objects.nonNull(element)) {
            element.setCompany(SecurityUtils.getCurrentCompanyId());
        }

    }
}

