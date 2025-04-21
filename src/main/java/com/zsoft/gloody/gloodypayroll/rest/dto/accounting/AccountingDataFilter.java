package com.zsoft.gloody.gloodypayroll.rest.dto.accounting;

import java.time.LocalDate;

public record AccountingDataFilter(LocalDate month,
                                   boolean detailedByEmployee) {

}
