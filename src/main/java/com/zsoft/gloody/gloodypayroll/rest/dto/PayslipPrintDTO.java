package com.zsoft.gloody.gloodypayroll.rest.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PayslipPrintDTO {
    private String content;
    @Builder.Default
    private boolean showZero = true;
    @Builder.Default
    private boolean showIntermediate = true;
}
