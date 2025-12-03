package com.zsoft.gloody.gloodypayroll.configs;

import org.springframework.stereotype.Component;

@Component
public class CacheControl {
    private static final ThreadLocal<Boolean> CACHE_ENABLED = ThreadLocal.withInitial(() -> false);

    public static void enable() { CACHE_ENABLED.set(true); }
    public static void disable() { CACHE_ENABLED.set(false); }
    public static boolean isEnabled() { return CACHE_ENABLED.get(); }
}