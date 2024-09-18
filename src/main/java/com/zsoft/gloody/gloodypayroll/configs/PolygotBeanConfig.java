package com.zsoft.gloody.gloodypayroll.configs;

public class PolygotBeanConfig {



/*    @Bean
    public Context context() {
        Context ctx = Context.newBuilder("js").allowAllAccess(true).allowHostClassLookup((v) -> true).build();
        initDefaultFunctions(ctx);
        return ctx;
    }

    private void initDefaultFunctions(Context context) {
        try {
            Value binding = context.getBindings("js");
            binding.putMember("irgCalculator", new IrgCalculatorService());
            binding.putMember(IS_HANDICAP, false);
            binding.putMember(IRG_YEAR, LocalDate.now().getYear());
            context.eval("js","function IRG(b, y, h) {" +
                    "   if(b == undefined){b = 0;} " +
                    "   if(y == undefined){y = " + IRG_YEAR + ";} " +
                    "   if(h == undefined){h = " + IS_HANDICAP + ";} " +
                    "   return irgCalculator.calculateIrg(b, y, h);" +
                    "}");
            context.eval("js", "var max = MAX = Math.max;");
            context.eval("js","var min = MIN = Math.min;");
            context.eval("js","var ceil = CEIL = Math.ceil;");
            context.eval("js","var floor = FLOOR = Math.floor;");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }*/
}
