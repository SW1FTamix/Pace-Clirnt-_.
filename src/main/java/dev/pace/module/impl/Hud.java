package dev.pace.module.impl;

import dev.pace.module.Category;
import dev.pace.module.Module;
import dev.pace.module.ModuleManager;
import dev.pace.setting.BoolSetting;
import dev.pace.util.Targets;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

import java.util.Comparator;
import java.util.List;

public class Hud extends Module {
    private final BoolSetting watermark  = add(new BoolSetting("Watermark", true));
    private final BoolSetting arrayList  = add(new BoolSetting("Module List", true));
    private final BoolSetting totemCount = add(new BoolSetting("Totem Count", true));
    private final BoolSetting targetHud  = add(new BoolSetting("Target HUD", true));

    public Hud() { super("HUD", "Watermark, module list, totem counter and target display", Category.CLIENT); }

    @Override public void onRender(GuiGraphics g) {
        if (mc.options.hideGui) return;
        var font = mc.font;
        int light = ClickGui.light();

        if (watermark.get()) g.drawString(font, "Pace Client", 4, 4, light, true);

        if (arrayList.get()) {
            List<Module> on = ModuleManager.INSTANCE.all().stream()
                    .filter(m -> m.isEnabled() && m != this)
                    .sorted(Comparator.comparingInt((Module m) -> font.width(m.name)).reversed())
                    .toList();
            int y = 4;
            for (Module m : on) {
                int w = font.width(m.name);
                g.drawString(font, m.name, g.guiWidth() - w - 4, y, light, true);
                y += font.lineHeight + 1;
            }
        }

        if (totemCount.get()) {
            int n = 0;
            var inv = mc.player.getInventory();
            for (int i = 0; i < 36; i++) if (inv.getItem(i).is(Items.TOTEM_OF_UNDYING)) n += inv.getItem(i).getCount();
            if (mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) n += mc.player.getOffhandItem().getCount();
            g.drawString(font, "Totems: " + n, 4, g.guiHeight() - 12, n == 0 ? 0xFFFF5555 : 0xFFFFFFFF, true);
        }

        if (targetHud.get()) {
            Player t = Targets.nearest(12);
            if (t != null) {
                String s = t.getName().getString() + "  " + String.format("%.1f", t.getHealth() + t.getAbsorptionAmount()) + " HP";
                g.drawString(font, s, g.guiWidth() / 2 - font.width(s) / 2, g.guiHeight() / 2 + 24, 0xFFFFFFFF, true);
            }
        }
    }
}
