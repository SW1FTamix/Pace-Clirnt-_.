package dev.pace.module;

import dev.pace.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    protected static final Minecraft mc = Minecraft.getInstance();

    public final String name;
    public final String description;
    public final Category category;
    private boolean enabled;
    private int key = -1;
    private final List<Setting> settings = new ArrayList<>();

    protected Module(String name, String description, Category category) {
        this.name = name; this.description = description; this.category = category;
    }

    protected <T extends Setting> T add(T s) { settings.add(s); return s; }

    public List<Setting> getSettings() { return settings; }
    public boolean isEnabled() { return enabled; }
    public int getKey() { return key; }
    public void setKey(int k) { key = k; }

    public void toggle() { setEnabled(!enabled); }
    public void setEnabled(boolean v) {
        if (v == enabled) return;
        enabled = v;
        if (v) onEnable(); else onDisable();
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
    public void onRender(GuiGraphics g) {}
}
