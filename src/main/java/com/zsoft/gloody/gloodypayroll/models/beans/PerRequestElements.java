package com.zsoft.gloody.gloodypayroll.models.beans;

import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSettingElement;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipElementDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingElementDto;
import lombok.*;

import java.time.LocalDate;
import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PerRequestElements {
    private boolean showIntermediate;
    private LocalDate currentDate;
    private boolean showZero;
    private Long employeeId;
    private List<PayslipSettingElementDto> settingsElements = new ArrayList<>();
    private List<PayslipSettingElement> functionElements = new ArrayList<>();
    private TreeSet<PayslipElementDto> calculatedElements = new TreeSet<>() {
    };
}
