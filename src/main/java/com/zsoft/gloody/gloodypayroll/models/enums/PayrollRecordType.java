package com.zsoft.gloody.gloodypayroll.models.enums;

public enum PayrollRecordType {
    /** A debit note issued for payroll adjustments */
    DEBIT_NOTE,

    /** Journal Légal de paie */
    LEGAL_LEDGER,

    /** The primary journal recording all payroll transactions */
    PAYROLL_JOURNAL,

    /** Breakdown of salary components (gross, deductions, net, etc.) */
    PAYROLL_ELEMENTS,

    DAS_DETAILS,

    /** DAS_ Entête */
    DAS_HEADER,

    /** Individual employee records from the Automated Salary Declaration */
    DAS_INDIVIDUAL,

    /** French supplementary payroll tax form (301BIS) */
    FORM_301BIS,

    PAYROLL_TRANSFER
}