package com.zsoft.gloody.gloodypayroll.services;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
public class IrgCalculatorService {
    public static String formatDouble(double value) {
        return String.format("%-8s", BigDecimal.valueOf(value).setScale(2, RoundingMode.DOWN));
    }

    public static void main(String[] args) {
        double b = 72839.5;
        double irg = new IrgCalculatorService().calculateIrg( 72839.51, 2023, false);
        System.out.println("irg: "+  irg);
    }

    public double calculateIrg(double baseValue, int year, boolean isHandicap) {
        int base = BigDecimal.valueOf(baseValue).setScale(0, RoundingMode.HALF_DOWN).intValue();
        base = base - (base % 10);
        if (year >= 2023) {
            return calculateIrg2023(base, isHandicap);
        } else if (year >= 2020) {
            return calculateIrg2020(base, isHandicap);
        } else {
            return calculateIrg2008(base, isHandicap);
        }
    }

    public double calculateIrg2023(double baseValue, boolean isHandicap) {
        if (baseValue <= 30000) {
            return 0d;
        }
        double irg;
        if (baseValue <= 40000) {
            irg = (baseValue - 20000) * 0.23;
        } else if (baseValue <= 80000) {
            irg = ((baseValue - 40000) * 0.27) + (20000 * 0.23);
        } else if (baseValue <= 160000) {
            irg = ((baseValue - 80000) * 0.30) + (40000 * 0.27) + (20000 * 0.23);
        } else if (baseValue <= 320000) {
            irg = ((baseValue - 160000) * 0.33) + (80000 * 0.30) + (40000 * 0.27) + (20000 * 0.23);
        } else {
            irg = ((baseValue - 320000) * 0.35) + (160000 * 0.33) + (80000 * 0.30) + (40000 * 0.27) + (20000 * 0.23);
        }
        double reduction = irg * 0.40;
        if (reduction < 1000) reduction = 1000;
        if (reduction > 1500) reduction = 1500;
        irg = irg - reduction;
        if (isHandicap) {
            if (baseValue < 42500) {
                irg = (irg * 93 / 61) - (81213 / 41d);
            }
        } else {
            if (baseValue <= 35000) {
                irg = (irg * 137 / 51) - (27925 / 8d);
            }
        }
        return BigDecimal.valueOf(irg)
                .setScale(1, RoundingMode.DOWN)
                .doubleValue();
    }

    public double calculateIrg2020(double baseValue, boolean isHandicap) {
        if (baseValue <= 30000) {
            return 0d;
        }
        double irg;
        if (isHandicap) {
            if (baseValue <= 40000) {
                irg = (calculateIrg2008(baseValue, false) * 5 / 3) - (12500 / 3d);
            } else {
                irg = calculateIrg2008(baseValue, false);
            }
        } else {
            if (baseValue <= 35000) {
                irg = (calculateIrg2008(baseValue, false) * 8 / 3) - (20000 / 3d);
            } else {
                irg = calculateIrg2008(baseValue, false);
            }
        }
        return BigDecimal.valueOf(irg).setScale(0, RoundingMode.CEILING).doubleValue();
    }

    public double calculateIrg2008(double baseValue, boolean isHandicap) {
        double irg;
        if (isHandicap) {
            if (baseValue <= 15000) {
                return 0d;
            }
            if (baseValue <= 22500) {
                irg = ((baseValue - 15000) * 0.20) + 1000;
            } else if (baseValue <= 120000) {
                irg = (baseValue - 30000) * 0.30 + (7500 * 0.2 + 1000);
            } else {
                irg = (baseValue - 120000) * 0.35 + (90000 * 0.3) + (7500 * 0.2 + 1000);
            }
        } else {
            if (baseValue <= 10000) {
                return 0d;
            }
            if (baseValue <= 30000) {
                irg = (baseValue - 10000) * 0.20;
            } else if (baseValue <= 120000) {
                irg = (baseValue - 30000) * 0.30 + (20000 * 0.20);
            } else {
                irg = (baseValue - 120000) * 0.35 + (90000 * 0.30) + (20000 * 0.20);
            }
        }
        double reduction = irg * 0.40;
        if (reduction < 1000) reduction = 1000;
        if (reduction > 1500) reduction = 1500;
        irg = irg - reduction;
        return BigDecimal.valueOf(irg).setScale(0, RoundingMode.CEILING).doubleValue();
    }

}
