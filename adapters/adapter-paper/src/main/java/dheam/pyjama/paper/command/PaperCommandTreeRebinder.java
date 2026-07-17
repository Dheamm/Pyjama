package dheam.pyjama.paper.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import dheam.pyjama.api.platform.command.CommandTreeRebinder;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public final class PaperCommandTreeRebinder implements CommandTreeRebinder {

    private final JavaPlugin plugin;
    private final CommandDispatcher<CommandSourceStack> dispatcher;
    private final Function<String, LiteralCommandNode<CommandSourceStack>> nodeFactory;
    private final Field childrenField;
    private final Field literalsField;

    public PaperCommandTreeRebinder(JavaPlugin plugin,
                                    CommandDispatcher<CommandSourceStack> dispatcher,
                                    Function<String, LiteralCommandNode<CommandSourceStack>> nodeFactory) {
        this.plugin = plugin;
        this.dispatcher = dispatcher;
        this.nodeFactory = nodeFactory;
        this.childrenField = resolveField("children");
        this.literalsField = resolveField("literals");
    }

    private static Field resolveField(String name) {
        try {
            Field field = CommandNode.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException exception) {
            throw new IllegalStateException(
                    "Brigadier CommandNode layout changed, cannot resolve field: " + name, exception);
        }
    }

    @Override
    public void rebind(String oldName, List<String> oldAliases, String newName, List<String> newAliases) {
        if (oldName.equals(newName) && oldAliases.equals(newAliases)) {
            return;
        }

        RootCommandNode<CommandSourceStack> root = dispatcher.getRoot();
        removeNode(root, oldName);
        oldAliases.forEach(alias -> removeNode(root, alias));
        removeFromCommandMap(oldName, oldAliases);

        LiteralCommandNode<CommandSourceStack> node = nodeFactory.apply(newName);
        root.addChild(node);
        newAliases.forEach(alias -> root.addChild(buildRedirect(alias, node)));

        plugin.getServer().getOnlinePlayers().forEach(Player::updateCommands);
    }

    private LiteralCommandNode<CommandSourceStack> buildRedirect(String alias,
                                                                 LiteralCommandNode<CommandSourceStack> target) {
        return Commands.literal(alias).redirect(target).build();
    }

    private void removeFromCommandMap(String name, List<String> aliases) {
        String pluginPrefix = plugin.getName().toLowerCase(Locale.ROOT) + ":";
        Bukkit.getCommandMap().getKnownCommands().keySet().removeIf(key ->
                key.equalsIgnoreCase(name)
                        || key.equalsIgnoreCase(pluginPrefix + name)
                        || aliases.stream().anyMatch(alias ->
                        key.equalsIgnoreCase(alias) || key.equalsIgnoreCase(pluginPrefix + alias)));
    }

    @SuppressWarnings("unchecked")
    private void removeNode(RootCommandNode<CommandSourceStack> root, String name) {
        try {
            Map<String, CommandNode<CommandSourceStack>> children =
                    (Map<String, CommandNode<CommandSourceStack>>) childrenField.get(root);
            Map<String, LiteralCommandNode<CommandSourceStack>> literals =
                    (Map<String, LiteralCommandNode<CommandSourceStack>>) literalsField.get(root);
            children.remove(name);
            literals.remove(name);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to remove Brigadier command node: " + name, exception);
        }
    }
}