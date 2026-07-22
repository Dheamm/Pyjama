package dheam.pyjama.core.service.placeholder;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class PlaceholderService {

    private final Map<String, Supplier<String>> global = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Supplier<String>>> local = new ConcurrentHashMap<>();

    public void registerGlobal(String key, Supplier<String> value) {
        global.put(key, value);
    }

    public void registerLocal(String namespace, String key, Supplier<String> value) {
        local.computeIfAbsent(namespace, namespaceKey -> new ConcurrentHashMap<>()).put(key, value);
    }

    public void promoteToGlobal(String namespace, String key) {
        Map<String, Supplier<String>> scope = local.get(namespace);
        if (scope == null) {
            return;
        }
        Supplier<String> value = scope.remove(key);
        if (value != null) {
            global.put(key, value);
        }
    }

    public TagResolver globalResolver() {
        return build(global);
    }

    public TagResolver resolverFor(String namespace) {
        return TagResolver.resolver(globalResolver(), namespaceResolver(namespace));
    }

    private TagResolver namespaceResolver(String namespace) {
        return TagResolver.resolver(namespace, (argumentQueue, context) -> {
            String key = argumentQueue.popOr("placeholder key required").value();
            Supplier<String> value = local.getOrDefault(namespace, Map.of()).get(key);
            return Tag.inserting(Component.text(value != null ? value.get() : ""));
        });
    }

    private TagResolver build(Map<String, Supplier<String>> source) {
        TagResolver.Builder builder = TagResolver.builder();
        source.forEach((key, value) -> builder.resolver(Placeholder.unparsed(key, value.get())));
        return builder.build();
    }
}