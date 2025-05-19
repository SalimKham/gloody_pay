package com.zsoft.gloody.gloodypayroll.rest.mappers;

import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSetting;
import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSettingElement;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingElementDto;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import org.mapstruct.*;

import java.util.Objects;

@Mapper(uses = {EmployeeMapper.class, SectionMapper.class})
public abstract class PayslipSettingMapper {
    @Mapping(target = "employee.id", source = "employee")
    @Mapping(target = "contract.id", source = "contract")
    @BeanMapping(builder = @Builder(disableBuilder = true))
    public abstract PayslipSettingDto toDto(PayslipSetting setting);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(source = "employee.id", target = "employee")
    @Mapping(source = "contract.id", target = "contract")
    @Mapping(source = "rate", target = "payslipRate")
    public abstract PayslipSetting toEntity(PayslipSettingDto settingDto);

    @Mapping(target = "payslipSettingId", ignore = true)
    @Mapping(target = "section", source = "section.code")
    public abstract PayslipSettingElementDto toDto(PayslipSettingElement setting);

    @Mapping(target = "section", source = "section")
    @BeanMapping(builder = @Builder(disableBuilder = true))
    public abstract PayslipSettingElement toEntity(PayslipSettingElementDto settingDto);

    @AfterMapping
    void afterMapping(@MappingTarget PayslipSetting setting) {
        if (Objects.nonNull(setting)) {
            setting.setCompany(SecurityUtils.getCurrentCompanyId());
        }
    }

    @AfterMapping
    void afterMapping(@MappingTarget PayslipSettingElement element) {
        if (Objects.nonNull(element)) {
            element.setCompany(SecurityUtils.getCurrentCompanyId());
        }
    }
}

