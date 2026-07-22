package dheam.pyjama.api.placeholder;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public interface PlaceholderBridge {

    TagResolver globalPlaceholders();

    TagResolver audiencePlaceholders(Audience audience);
}