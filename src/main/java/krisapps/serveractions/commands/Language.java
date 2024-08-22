package krisapps.serveractions.commands;

import krisapps.serveractions.ServerActions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;

public class Language implements CommandExecutor {

    ServerActions main = ServerActions.instance;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Syntax: /lang <set|refreshDefault> <languageCode|null>

        if (args.length == 0) {
            main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedString("commands.lang.syntax"));
            return true;
        }

        switch (args[0]) {
            case "set": {
                if (args.length < 2) {
                    main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedString("commands.lang.set.syntax"));
                    return true;
                }

                if (!main.localizationUtility.languageFileExists(args[1])) {
                    main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedString("commands.lang.set.not_found"));
                    return true;
                }

                main.localizationUtility.changeLanguage(args[1]);
                main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedString("commands.lang.set.success")
                        .replaceAll("%lang%", args[1])
                );
                return true;
            }
            case "refreshDefault": {
                try {
                    main.localizationUtility.resetDefaultLanguageFile();
                    main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedString("commands.lang.update_default.done"));
                } catch (FileNotFoundException e) {
                    main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedString("commands.lang.update_default.nofile"));
                }
                return true;
            }
            case "reimportEmbeddedLanguageFiles": {
                if (main.localizationUtility.resetEmbeddedLanguageFiles()) {
                    main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedString("commands.lang.update_folder.done"));
                } else {
                    main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedString("commands.lang.update_folder.err"));
                }
                return true;
            }
            case "reload": {
                main.localizationUtility.loadCurrentLanguage();
                main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedString("commands.lang.reload.done"));
                return true;
            }
            default: {
                main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedString("commands.lang.syntax"));
                return true;
            }
        }
    }
}
