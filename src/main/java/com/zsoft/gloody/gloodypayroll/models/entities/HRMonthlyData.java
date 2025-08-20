package com.zsoft.gloody.gloodypayroll.models.entities;

import com.zsoft.gloody.gloodypayroll.models.enums.SummaryDataType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_monthly_data")
public class HRMonthlyData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "resource_id")
    private Long resourceId;
    @Column(name = "contract_id")
    private Long contractId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SummaryDataType type;

    @Column(name = "pay_month")
    private LocalDate month;
    @Column(name = "hr_variable")
    private String variable;
    @Column(name = "hr_value")
    private double value;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HRMonthlyData that = (HRMonthlyData) o;
        return Objects.equals(contractId, that.contractId) && Objects.equals(month, that.month) && Objects.equals(variable, that.variable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contractId, month, variable);
    }
    public static void main(String[] args) {
        UUID uuid = UUID.nameUUIDFromBytes("hello-2025-02-02".getBytes());
        long id = uuid.getMostSignificantBits();
        System.out.println(id);
    }
}
