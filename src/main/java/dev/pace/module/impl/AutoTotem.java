package dev.pace.module.impl;

import dev.pace.module.Category;
import dev.pace.module.Module;
import dev.pace.setting.*;
import dev.pace.util.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;

public class AutoTotem extends Module {
    private final ModeSetting mode  = add(new ModeSetting("Mode", "Always", "Health"));
    private final NumSetting health = add(new NumSetting("Health", 10, 1, 20, 0.5));
    private final NumSetting delay  = add(new NumSetting("Delay", 0, 0, 10, 1));

    private int timer;

    public AutoTotem() { super("AutoTotem", "Keeps a totem of undying in your offhand", Category.COMBAT); }

    @Override public void onTick() {
        if (timer > 0) { timer--; return; }
        if (mc.screen != null && !(mc.screen instanceof InventoryScreen)) return;
        if (mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) return;
        if (mode.is("Health") && mc.player.getHealth() + mc.player.getAbsorptionAmount() > health.get()) return;
        int slot = InvUtil.findInventory(Items.TOTEM_OF_UNDYING);
        if (slot < 0) return;
        Compat.click(mc.player.inventoryMenu.containerId, InvUtil.menuSlot(slot), 40, ClickType.SWAP);
        timer = delay.getInt();
    }
}
