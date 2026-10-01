package dev.pace.module.impl;

import dev.pace.gui.ClickGuiScreen;
import dev.pace.module.Category;
import dev.pace.module.Module;
import dev.pace.setting.ModeSetting;

public class ClickGui extends Module {
    //                                  accent       dark         light
    private static final int[][] THEMES = {
        { 0xFF6C4BD6, 0xFF3D2E7A, 0xFFB48CFF }, // Purple
        { 0xFF2F7BE0, 0xFF1F4A8A, 0xFF8CC0FF }, // Blue
        { 0xFFD64B4B, 0xFF7A2E2E, 0xFFFF8C8C }, // Red
        { 0xFF3FBF6A, 0xFF24703C, 0xFF8CFFB0 }, // Green
        { 0xFFE08A2F, 0xFF8A531F, 0xFFFFC48C }  // Orange
    };
    private static ClickGui instance;
    private final ModeSetting theme = add(new ModeSetting("Theme", "Purple", "Blue", "Red", "Green", "Orange"));

    public ClickGui() {
        super("ClickGUI", "Opens the Pace Client menu", Category.CLIENT);
        instance = this;
    }

    private static int pick(int i) { return THEMES[instance == null ? 0 : instance.theme.index()][i]; }
    public static int accent() { return pick(0); }
    public static int dark()   { return pick(1); }
    public static int light()  { return pick(2); }

    @Override public void toggle() {
        if (mc.screen instanceof ClickGuiScreen) return;
        mc.setScreen(new ClickGuiScreen());
    }
    @Override public void setEnabled(boolean v) { /* never stays "enabled" */ }
}
