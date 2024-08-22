package krisapps.serveractions.commands.tab;

import krisapps.serveractions.types.ScheduledAction;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ScheduleActionTab implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // Syntax: /schedulerestart <delayed/scheduled/cancel> <action> <delayInSeconds/date/delayed|scheduled> <time>
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("delayed", "scheduled", "cancel"));
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("scheduled") || args[0].equalsIgnoreCase("delayed")) {
                completions.addAll(Arrays
                        .stream(ScheduledAction.values())
                        .map(ScheduledAction::name)
                        .collect(Collectors.toList()));
            }
        }
        if (args.length == 3) {
            switch (args[0]) {
                case "delayed": {
                    completions.add("[<delayInSeconds>]");
                    break;
                }
                case "scheduled": {
                    completions.add("<date (dd/MM/yyyy)>");
                    break;
                }
                case "cancel": {
                    completions.addAll(Arrays.asList("delayed", "scheduled"));
                    break;
                }
            }
        }
        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("scheduled")) {
                completions.add("<time (HH:mm:ss)>");
            }
        }



        return completions;
    }
}
