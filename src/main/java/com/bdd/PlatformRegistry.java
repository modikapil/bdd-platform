package com.bdd;

import java.util.HashMap;
import java.util.Map;

/**
 * @author KumaKbz
 */
public class PlatformRegistry {

    private static final PlatformRegistry INSTANCE = new PlatformRegistry();

    private ThreadLocal<Map<String, Object>> registry;

    /**
     * <p>
     * initialise the thread local registry
     * </p>
     */
    private PlatformRegistry() {
        registry = new ThreadLocal<Map<String, Object>>() {
            @Override
            protected Map<String, Object> initialValue() {
                return new HashMap<>();
            }
        };
    }

    public static PlatformRegistry getInstance() {
        return INSTANCE;
    }

    public Object getValue(String name) {
        return INSTANCE.registry.get().get(name);
    }

    public void putValue(String name, Object object) {
        INSTANCE.registry.get().put(name, object);
    }

    public void removeValue(String name) {
        INSTANCE.registry.get().remove(name);
    }

    public void clear() {
        INSTANCE.registry.get().clear();
    }

}