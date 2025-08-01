package com.zsoft.gloody.gloodypayroll.services.payrollrecord;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class PayrollRecordUtils {

    public static String formatMoney(Double amount) {
        if (amount == null) amount = 0d;
        BigDecimal decimalAmount = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRENCH);
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("0.00", symbols);
        return df.format(decimalAmount);
    }
}
