package dheam.pyjama.core.service.locale;

import io.github.miniplaceholders.api.MiniPlaceholders;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

final class MiniPlaceholdersBridge {

    private MiniPlaceholdersBridge() {
    }

    static TagResolver globalPlaceholders() {
        return MiniPlaceholders.globalPlaceholders();
    }

    static TagResolver audiencePlaceholders() {
        return MiniPlaceholders.audiencePlaceholders();
    }
}