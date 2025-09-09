package com.zsoft.gloody.gloodypayroll.models.entities;

import com.zsoft.gloody.gloodypayroll.models.enums.PayrollRecordType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
@Table(name = "payroll_record")
public class PayrollRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_content_type")
    private String fileContentType;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status")
    private GeneratedFileStatus status = GeneratedFileStatus.IN_PROGRESS;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type")
    private PayrollRecordType type ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    @ToString.Exclude
    public Company company;

    @Column(name = "generated_at", updatable = false)
    private Instant generatedAt;



    @Column(name = "generated_by")
    private Long generatedBy;

    public enum GeneratedFileStatus {
        FAILED, IN_PROGRESS, DONE
    }
}
