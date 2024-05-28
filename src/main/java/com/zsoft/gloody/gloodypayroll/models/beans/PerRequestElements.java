package com.zsoft.gloody.gloodypayroll.models.beans;

import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipElementDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingElementDto;
import lombok.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PerRequestElements {
    private boolean showIntermediate;
    private List<PayslipSettingElementDto> settingsElements = new ArrayList<>();
    private LinkedList<PayslipElementDto> calculatedElements = new LinkedList<>();
}
