package dheam.pyjama.paper.placeholder;

import dheam.pyjama.api.placeholder.PlaceholderBridge;
import io.github.miniplaceholders.api.MiniPlaceholders;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public final class PaperPlaceholderBridge implements PlaceholderBridge {

    public static PlaceholderBridge createIfAvailable() {
        try {
            Class.forName("io.github.miniplaceholders.api.MiniPlaceholders");
            return new PaperPlaceholderBridge();
        } catch (ClassNotFoundException exception) {
            return null;
        }
    }

    private PaperPlaceholderBridge() {
    }

    @Override
    public TagResolver globalPlaceholders() {
        return MiniPlaceholders.globalPlaceholders();
    }

    @Override
    public TagResolver audiencePlaceholders(Audience audience) {
        return MiniPlaceholders.audiencePlaceholders();
    }
}