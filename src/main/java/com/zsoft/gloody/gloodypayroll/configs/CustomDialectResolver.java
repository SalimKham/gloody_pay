package com.zsoft.gloody.gloodypayroll.configs;

import org.hibernate.dialect.MariaDBDialect;

public class CustomDialectResolver extends MariaDBDialect {

    public CustomDialectResolver() {
        super();
        registerKeyword("day"); //Keyword has to be in lowercase.
        registerKeyword("interval");
        registerKeyword("true");
        registerKeyword("month");
        registerKeyword("year");
        registerKeyword("false");
        registerKeyword("cte");
        registerKeyword("combinedFormula");
        registerKeyword("rlike");
        registerKeyword("COALESCE");
        registerKeyword("activities_status");
        registerKeyword("temp_table");
        registerKeyword("current_date");
    }
}