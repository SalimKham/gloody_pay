package com.zsoft.gloody.gloodypayroll.models.beans;

import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipElementDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingElementDto;
import lombok.*;

import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PerRequestElements {
    private boolean showIntermediate;
    private List<PayslipSettingElementDto> settingsElements = new ArrayList<>();
    private TreeSet<PayslipElementDto> calculatedElements = new TreeSet<>() {
    };
}
