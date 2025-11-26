package com.zsoft.gloody.gloodypayroll.models.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
@Table(name = "back_payment")
public class BackPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "code")
    private String code;

    @Column(name = "contract_id")
    private Long contractId;

    @Column(name = "resource_id")
    private Long resourceId;


    @Column(name = "start_month")
    private LocalDate startMonth;

    @Column(name = "end_month")
    private LocalDate endMonth;

    @Column(name = "month")
    private LocalDate month;

    @Column(name = "section_code")
    private String sectionCode;

    @Column(name = "title")
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="source_id")
    private BackPayment source;

    @Column(name = "value")
    private Double value;
}
