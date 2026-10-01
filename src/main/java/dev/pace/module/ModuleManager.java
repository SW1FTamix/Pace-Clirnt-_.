package dev.pace.module;

import dev.pace.PaceClient;
import dev.pace.module.impl.*;
import dev.pace.util.Compat;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public static final ModuleManager INSTANCE = new ModuleManager();

    private final List<Module> modules = new ArrayList<>();
    private final boolean[] keyState = new boolean[GLFW.GLFW_KEY_LAST + 1];

    public void init() {
        // Combat
        modules.add(new AutoCrystal());
        modules.add(new CrystalMacro());
        modules.add(new AnchorMacro());
        modules.add(new AutoAnchor());
        modules.add(new AutoTotem());
        modules.add(new HoverTotem());
        modules.add(new Surround());
        modules.add(new AutoArmor());
        modules.add(new PearlThrow());
        modules.add(new FriendKey());
        modules.add(new Glow());
        // Client
        ClickGui gui = new ClickGui();
        gui.setKey(GLFW.GLFW_KEY_RIGHT_SHIFT);
        modules.add(gui);
        Hud hud = new Hud();
        modules.add(hud);
        hud.setEnabled(true);

        Config.load(modules);

        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        HudRenderCallback.EVENT.register((g, delta) -> {
            if (Minecraft.getInstance().player == null) return;
            for (Module m : modules) if (m.isEnabled()) m.onRender(g);
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> saveConfig());
    }

    private void tick(Minecraft client) {
        if (client.player == null || client.level == null) return;
        for (Module m : modules) {
            int k = m.getKey();
            if (k <= 0 || k > GLFW.GLFW_KEY_LAST) continue;
            boolean down = Compat.isKeyDown(k);
            if (down && !keyState[k] && client.screen == null) m.toggle();
            keyState[k] = down;
        }
        for (Module m : modules) {
            if (!m.isEnabled()) continue;
            try { m.onTick(); } catch (Throwable t) { PaceClient.LOG.error("Error in module " + m.name, t); }
        }
    }

    public void saveConfig() { Config.save(modules); }
    public List<Module> all() { return modules; }
    public List<Module> in(Category c) { return modules.stream().filter(m -> m.category == c).toList(); }
}
