package com.zsoft.gloody.gloodypayroll.rest.mappers;

import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSetting;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingDto;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import org.mapstruct.*;

import java.util.Objects;

@Mapper(uses = {EmployeeMapper.class, SectionMapper.class, PayslipSettingElementMapper.class})
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

    @AfterMapping
    void afterMapping(@MappingTarget PayslipSetting setting) {
        if (Objects.nonNull(setting)) {
            setting.setCompany(SecurityUtils.getCurrentCompanyId());
        }
    }
}

