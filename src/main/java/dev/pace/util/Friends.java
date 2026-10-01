package dev.pace.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class Friends {
    private static final Set<String> NAMES = new LinkedHashSet<>();

    public static boolean is(String name) { return NAMES.contains(name.toLowerCase()); }
    public static void add(String name) { NAMES.add(name.toLowerCase()); }
    public static void clear() { NAMES.clear(); }
    public static List<String> all() { return new ArrayList<>(NAMES); }

    /** @return true if the player is now a friend. */
    public static boolean toggle(String name) {
        String n = name.toLowerCase();
        if (!NAMES.remove(n)) { NAMES.add(n); return true; }
        return false;
    }
}
