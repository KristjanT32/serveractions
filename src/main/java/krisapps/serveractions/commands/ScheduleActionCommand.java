package krisapps.serveractions.commands;

import krisapps.serveractions.ServerActions;
import krisapps.serveractions.types.ScheduledAction;
import krisapps.serveractions.types.ScheduledActionEntry;
import krisapps.serveractions.util.ConfigurationUtility;
import krisapps.serveractions.util.ScheduleManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class ScheduleActionCommand implements CommandExecutor {
    ServerActions main = ServerActions.instance;


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Syntax: /scheduleaction <delayed/scheduled/cancel> <action> <delayInSeconds/date/delayed|scheduled>
        Optional<Player> player = Optional.empty();

        if (sender instanceof Player) {
            player = Optional.of((Player) sender);
        }

        if (args.length == 0) {
            main.messageUtility.sendMessage(sender,
                    main.localizationUtility.getLocalizedString("commands.scheduleaction.syntax")
            );
            return true;
        }

        String operation = args[0];
        switch (operation) {
            case "delayed": {
                if (args.length < 2) {
                    main.messageUtility.sendMessage(sender,
                            main.localizationUtility.getLocalizedString("commands.scheduleaction.delayed.syntax")
                    );
                    return true;
                }

                if (ScheduleManager.hasPendingDelayedAction()) {
                    main.messageUtility.sendMessage(sender,
                            main.localizationUtility.getLocalizedString(
                                    "commands.scheduleaction.delayed.another_scheduled")
                    );
                    return true;
                }

                ScheduledAction action = null;
                try {
                     action = ScheduledAction.valueOf(args[1]);
                } catch (IllegalArgumentException e) {
                    main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedString("commands.scheduleaction.unknown_action")
                            .replace("%action%", args[1])
                    );
                    return true;
                }



                int delay = Integer.parseInt(args[2]);
                if (delay < ConfigurationUtility.getMinimumRestartDelay()) {
                    main.messageUtility.sendMessage(sender,
                            main.localizationUtility
                                    .getLocalizedString("commands.scheduleaction.delayed.delay_too_short")
                                    .replace("%delay%", String.valueOf(ConfigurationUtility.getMinimumRestartDelay()))
                    );
                    return true;
                }

                ScheduleManager.scheduleDelayedAction(delay, action);
                main.messageUtility.sendMessage(sender,
                        main.localizationUtility
                                .getLocalizedString("commands.scheduleaction.delayed.success")
                                .replace("%delay%", String.valueOf(delay))
                );

                break;
            }
            case "scheduled": {
                if (args.length < 4) {
                    main.messageUtility.sendMessage(sender,
                            main.localizationUtility.getLocalizedString("commands.scheduleaction.scheduled.syntax")
                    );
                    return true;
                }
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                String date = args[2];
                String time = args[3];
                Date scheduleDate;
                try {
                    scheduleDate = format.parse(date.trim() + " " + time.trim());
                } catch (ParseException e) {
                    main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedString("commands.scheduleaction.scheduled.invalid_format"));
                    return true;
                }

                if (ScheduleManager.dateInUse(scheduleDate)) {
                    main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedString("commands.scheduleaction.scheduled.already_scheduled"));
                    return true;
                }

                ScheduledAction action = null;
                try {
                    action = ScheduledAction.valueOf(args[1]);
                } catch (IllegalArgumentException e) {
                    main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedString("commands.scheduleaction.unknown_action")
                                                                                    .replace("%action%", args[1])
                    );
                    return true;
                }

                ScheduleManager.scheduleScheduledAction(player.orElse(null), scheduleDate, action);
                main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedString("commands.scheduleaction.scheduled.success")
                        .replace("%date%", new SimpleDateFormat("dd/MM/yyyy").format(scheduleDate))
                        .replace("%time%", new SimpleDateFormat("HH:mm:ss").format(scheduleDate))
                );
            }
            case "cancel": {
                if (args.length < 2) {
                    main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedString("commands.scheduleaction.cancel.syntax"));
                    return true;
                }

                switch (args[1]) {
                    case "scheduled": {
                        if (args.length == 2) {
                            TextComponent divider = new TextComponent(main.localizationUtility.getLocalizedString("commands.scheduleaction.cancel.divider"));
                            TextComponent header = new TextComponent(main.localizationUtility.getLocalizedString("commands.scheduleaction.cancel.header")
                                                                                             .replace("%restartCount%", String.valueOf(ScheduleManager.getScheduledActions().size()))
                            );
                            TextComponent subheader = new TextComponent(main.localizationUtility.getLocalizedString("commands.scheduleaction.cancel.subheader"));
                            TextComponent item_prefix = new TextComponent(main.localizationUtility.getLocalizedString("commands.scheduleaction.cancel.item_prefix"));
                            main.messageUtility.sendComplexMessage(sender, divider, header, subheader);

                            for (String dateKey: main.pluginData.getConfigurationSection("scheduled-actions").getKeys(false)) {
                                try {
                                    TextComponent item = new TextComponent(main.localizationUtility.getLocalizedString("commands.scheduleaction.cancel.item")
                                           .replace("%datetime%", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new SimpleDateFormat("[dd-MM-yyyy-HH-mm-ss]").parse(dateKey)))
                                    );
                                    ScheduledActionEntry info = ScheduleManager.getScheduledAction(new SimpleDateFormat("[dd-MM-yyyy-HH-mm-ss]").parse(dateKey));
                                    String initiator = "";
                                    if (info.getInitiator().isPresent()) {
                                        initiator = Optional.ofNullable(Bukkit.getOfflinePlayer(info.getInitiator().get()).getPlayer())
                                                            .isPresent()
                                                ? Optional.ofNullable(Bukkit.getOfflinePlayer(info.getInitiator().get()).getPlayer()).get().getName()
                                                : info.getInitiator().toString();
                                    } else {
                                        initiator = main.localizationUtility.getLocalizedString("commands.scheduleaction.cancel.label_unknown");
                                    }

                                    item.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(main.localizationUtility.getLocalizedString("commands.scheduleaction.cancel.hovertext")
                                        .replace("%datetime%", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(info.getScheduleDate()))
                                        .replace("%initiator%", initiator)
                                        .replace("%action%", main.localizationUtility.getLocalizedString("commands.scheduleaction.cancel.action_labels." + info.getAction().getLocalizationKey()))
                                    )));
                                    item.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/scheduleaction cancel scheduled " + dateKey));

                                    main.messageUtility.sendComplexMessage(sender, item_prefix, item);
                                } catch (ParseException ignored) { }
                            }
                            main.messageUtility.sendComplexMessage(sender, divider);
                        } else {
                            String dateKey = args[2];
                            Date date;
                            try {
                                date = new SimpleDateFormat("[dd-MM-yyyy-HH-mm-ss]").parse(dateKey);
                            } catch (ParseException e) {
                                main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedString("commands.scheduleaction.cancel.invalid_format"));
                                return true;
                            }

                            if (!ScheduleManager.dateInUse(date)) {
                                main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedString("commands.scheduleaction.cancel.schedule_invalid"));
                                return true;
                            }
                            ScheduleManager.cancelScheduledActionForDate(date);
                            main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedString("commands.scheduleaction.cancel.cancelled")
                                    .replace("%datetime%", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(date))
                            );
                        }
                        break;
                    }
                    case "delayed": {
                        if (!ScheduleManager.hasPendingDelayedAction()) {
                            main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedString("commands.scheduleaction.cancel.no_delayed_restart_scheduled"));
                            return true;
                        }
                        ScheduleManager.cancelPendingDelayedAction();
                        main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedString("commands.scheduleaction.cancel.cancelled_delayed"));
                        break;
                    }
                }
            }
        }
        return true;
    }
}
