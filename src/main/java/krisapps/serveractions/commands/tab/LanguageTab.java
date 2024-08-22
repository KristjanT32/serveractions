package krisapps.serveractions.commands.tab;

import krisapps.serveractions.ServerActions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LanguageTab implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Arrays.asList("set", "refreshDefault", "reimportEmbeddedLanguageFiles", "reload"));
        }
        if (args.length == 2) {
            if (args[0].equals("set")) {
                completions.addAll(ServerActions.instance.localizationUtility.getRecognizedLanguages());
            }
        }

        return completions;
    }
}
