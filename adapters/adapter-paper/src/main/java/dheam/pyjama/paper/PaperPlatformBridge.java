package dheam.pyjama.paper;

import dheam.pyjama.api.platform.PlatformBridge;
import org.bukkit.plugin.Plugin;

public final class PaperPlatformBridge implements PlatformBridge {

    private final Plugin plugin;

    public PaperPlatformBridge(Plugin plugin) {
        this.plugin = plugin;
    }
}