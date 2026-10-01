package dev.pace;

import dev.pace.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaceClient implements ClientModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("PaceClient");

    @Override
    public void onInitializeClient() {
        ModuleManager.INSTANCE.init();
        LOG.info("Pace Client loaded. Press RIGHT SHIFT for the ClickGUI.");
    }
}
