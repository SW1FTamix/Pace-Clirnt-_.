package dev.pace.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;

/**
 * Every call that is likely to change between Minecraft versions lives here,
 * so if a Minecraft update breaks the build you only fix this one file.
 */
public final class Compat {
    private static final Minecraft mc = Minecraft.getInstance();

    public static boolean isKeyDown(int key) {
        return InputConstants.isKeyDown(mc.getWindow(), key);
    }

    public static int slot(Inventory inv) { return inv.getSelectedSlot(); }
    public static void setSlot(Inventory inv, int slot) { inv.setSelectedSlot(slot); }

    public static void click(int containerId, int slot, int button, ClickType type) {
        mc.gameMode.handleInventoryMouseClick(containerId, slot, button, type, mc.player);
    }
}
