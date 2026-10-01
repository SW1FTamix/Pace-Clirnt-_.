package dev.pace.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;

public final class InvUtil {
    private static final Minecraft mc = Minecraft.getInstance();

    /** Hotbar index 0-8 or -1. */
    public static int findHotbar(Item item) {
        for (int i = 0; i < 9; i++) if (mc.player.getInventory().getItem(i).is(item)) return i;
        return -1;
    }

    /** Inventory index 0-35 (0-8 hotbar, 9-35 main) or -1. */
    public static int findInventory(Item item) {
        for (int i = 0; i < 36; i++) if (mc.player.getInventory().getItem(i).is(item)) return i;
        return -1;
    }

    /** Converts a player-inventory index (0-35) to the slot id used by the inventory menu. */
    public static int menuSlot(int invIndex) { return invIndex < 9 ? invIndex + 36 : invIndex; }
}
