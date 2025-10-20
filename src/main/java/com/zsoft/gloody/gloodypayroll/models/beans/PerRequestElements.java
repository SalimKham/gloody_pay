package com.zsoft.gloody.gloodypayroll.models.beans;

import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipElementDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipFunctionElement;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingElementDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PerRequestElements {
    private boolean showIntermediate;
    private LocalDate currentDate;
    private boolean showZero;
    private Long employeeId;
    private boolean adjustBackPayments = false;
    private boolean resetElementValues = false;
    private List<PayslipSettingElementDto> settingsElements = new ArrayList<>();
    private List<PayslipFunctionElement> functionElements = new ArrayList<>();
    private TreeSet<PayslipElementDto> calculatedElements = new TreeSet<>() {
    };
}
