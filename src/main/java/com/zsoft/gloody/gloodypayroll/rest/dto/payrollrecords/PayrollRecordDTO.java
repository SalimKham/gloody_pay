package com.zsoft.gloody.gloodypayroll.rest.dto.payrollrecords;

import com.zsoft.gloody.gloodypayroll.models.entities.PayrollRecord;
import com.zsoft.gloody.gloodypayroll.models.enums.PayrollRecordType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class PayrollRecordDTO {
    private Long id;
    private String fileName;
    private String filePath;
    private String fileContentType;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PayrollRecord.GeneratedFileStatus status = PayrollRecord.GeneratedFileStatus.IN_PROGRESS;
    @Enumerated(EnumType.STRING)
    private PayrollRecordType type ;
    private Instant generatedAt;
}
