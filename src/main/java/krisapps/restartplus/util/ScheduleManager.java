package krisapps.restartplus.util;

import krisapps.restartplus.RestartPlus;
import krisapps.restartplus.types.ScheduledAction;
import krisapps.restartplus.types.ScheduledActionEntry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ScheduleManager {

    static RestartPlus main = RestartPlus.instance;
    static BukkitScheduler scheduler = main.getServer().getScheduler();
    static SimpleDateFormat fileDateFormat = new SimpleDateFormat("[dd-MM-yyyy-HH-mm-ss]");
    static KeyedBossBar bossbar;
    static NamespacedKey bossbarKey = NamespacedKey.fromString("restartplus:countdown_bar");

    private static int CURRENT_DELAYED_TASK = -1;

    public static void initialize() {
        for (String scheduledAction: getScheduledActions()) {
            try {
                Date date = fileDateFormat.parse(scheduledAction);

                // If the date is in the future, meaning the schedule hasn't been run yet
                if (date.after(Date.from(Instant.now()))) {
                    ScheduledActionEntry entry = getScheduledAction(date);
                    scheduleScheduledAction(null, entry.getScheduleDate(), entry.getAction());
                } else {
                    main.logging.log("Deleting old scheduled action: " + scheduledAction);
                    main.pluginData.set("scheduled-actions." + scheduledAction, null);
                    main.saveData();
                }
            } catch (ParseException e) {
                main.logging.log("Invalid scheduled action found: " + scheduledAction);
                continue;
            }
        }
        if (main.pluginData.getConfigurationSection("pending-action") != null) {
            if (((Date) main.pluginData.get("pending-action.scheduledAt", Date.class)).before(Date.from(Instant.now()))) {
                main.logging.log("Removed old delayed action entry.");
                main.pluginData.set("pending-action", null);
                main.saveData();
            }
        }
        initBossbar();
    }
    private static void initBossbar() {
        bossbar = Bukkit.getBossBar(bossbarKey);
        if (bossbar == null) {
            bossbar = Bukkit.createBossBar(bossbarKey, "",
                    BarColor.YELLOW,
                    BarStyle.SEGMENTED_20);
        }
        for (Player p: Bukkit.getOnlinePlayers()) {
            bossbar.addPlayer(p);
        }

        if (getScheduledActions().isEmpty()) {
            bossbar.setVisible(false);
            bossbar.setTitle("...");
        }
    }
    private static void showBossbar() {
        bossbar.setVisible(true);
    }
    private static void hideBossbar() {
        bossbar.setVisible(false);
    }

    public static void scheduleScheduledAction(@Nullable Player initiator, Date date, ScheduledAction action) {
        long delay = Duration.between(Date.from(Instant.now()).toInstant(), date.toInstant()).toSeconds() * 20L;

        // Subtract 5 minutes, to allow for a countdown to be displayed.
        if ((delay - 300 * 20L) > 0) {
            delay -= 300 * 20L;
        } else {
            delay -= 30 * 20L;
        }
        delay = Math.abs(delay);

        BukkitTask task = scheduler.runTaskLater(main, new Runnable() {
            @Override
            public void run() {
                startScheduledActionCountdown(date, action);
            }
        }, delay);

        if (initiator != null) {
            main.pluginData.set("scheduled-actions." + fileDateFormat.format(date) + ".initiator", initiator.getUniqueId().toString());
        }
        main.pluginData.set("scheduled-actions." + fileDateFormat.format(date) + ".launchTaskId", task.getTaskId());
        main.pluginData.set("scheduled-actions." + fileDateFormat.format(date) + ".actionType", action.name());
        main.saveData();

        for (Player p: Bukkit.getOnlinePlayers()) {
            main.messageUtility.sendMessage(p,
                    "&e==========================================\n" +
                    main.localizationUtility.getLocalizedString("messages.broadcast." + action.getLocalizationKey() + ".announce_scheduled")
                    .replace("%date%", new SimpleDateFormat("dd/MM/yyyy").format(date))
                    .replace("%time%", new SimpleDateFormat("HH:mm:ss").format(date))
                    + "\n&e=========================================="
            );
        }
    }

    public static void scheduleDelayedAction(int delayInSeconds, ScheduledAction action) {
        if (ConfigurationUtility.getBossbarEnabled(action)) showBossbar();

        int taskId = scheduler.runTaskTimerAsynchronously(main, new Runnable() {
            double timer = 0;
            @Override
            public void run() {
                if (timer < delayInSeconds) {
                    bossbar.setProgress((timer / delayInSeconds));
                    if ((delayInSeconds - timer) >= 30) {
                        bossbar.setTitle(ChatColor.translateAlternateColorCodes('&',
                                main.localizationUtility
                                        .getLocalizedString("messages.bossbar." + action.getLocalizationKey() + ".title_delayed")
                                        .replace("%time%",
                                                main.formatUtility.generateTimeStringFromSeconds((int) (delayInSeconds - timer))
                                        )
                        ));
                    } else {
                        if ((delayInSeconds - timer) >= 10) {
                            bossbar.setTitle(ChatColor.translateAlternateColorCodes('&',
                                    main.localizationUtility
                                            .getLocalizedString("messages.bossbar." + action.getLocalizationKey() + ".countdown")
                                            .replace("%countdown%",
                                                    main.formatUtility.generateTimeStringFromSeconds((int) (delayInSeconds - timer))
                                            )
                            ));
                        } else {
                            bossbar.setTitle(ChatColor.translateAlternateColorCodes('&',
                                    main.localizationUtility
                                            .getLocalizedString("messages.bossbar." + action.getLocalizationKey() + ".final_countdown")
                                            .replace("%countdown%",
                                                    main.formatUtility.generateTimeStringFromSeconds((int) (delayInSeconds - timer))
                                            )
                            ));
                            for (Player p: Bukkit.getOnlinePlayers()) {
                                main.messageUtility.sendMessage(p, main.localizationUtility.getLocalizedString("messages.broadcast." + action.getLocalizationKey() + ".final_countdown")
                                       .replace("%countdown%", main.formatUtility.generateTimeStringFromSeconds((int) (delayInSeconds - timer)))
                                );
                            }
                        }
                    }
                } else {
                    bossbar.setProgress(1);
                    scheduler.runTask(main, new Runnable() {
                        @Override
                        public void run() {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ConfigurationUtility.getActionCommand(action));
                            cancelPendingDelayedAction();
                            hideBossbar();
                        }
                    });
                }
                timer++;
            }
        }, 0L, 20L).getTaskId();

        CURRENT_DELAYED_TASK = taskId;
        main.logging.log("Delayed action scheduled: task - " + CURRENT_DELAYED_TASK + ", action: " + action.name());
        main.pluginData.set("pending-action.taskId", taskId);
        main.pluginData.set("pending-action.actionType", action.name());
        main.pluginData.set("pending-action.scheduledAt", Date.from(Instant.now()));
        main.saveData();

        for (Player p: Bukkit.getOnlinePlayers()) {
            main.messageUtility.sendMessage(p, main.localizationUtility.getLocalizedString("messages.broadcast." + action.getLocalizationKey() + ".announce_delayed")
                    .replace("%delay%", main.formatUtility.generateTimeStringFromSeconds(delayInSeconds))
            );
        }
    }

    public static Set<String> getScheduledActions() {
        if (!hasScheduledActions()) return new HashSet<>(0);
        return main.pluginData.getConfigurationSection("scheduled-actions").getKeys(false);
    }
    
    public static Set<String> getScheduledActions(ScheduledAction type) {
        if (!hasScheduledActions()) return new HashSet<>(0);
        return main.pluginData
                .getConfigurationSection("scheduled-actions")
                .getKeys(false)
                .stream()
                .filter(action -> main.pluginData
                        .getString("scheduled-actions." + action + ".actionType")
                        .equals(type.name()))
                .collect(Collectors.toSet());
    }

    public static boolean hasScheduledActions() {
        if (main.pluginData.getConfigurationSection("scheduled-actions") == null) return false;
        return !main.pluginData.getConfigurationSection("scheduled-actions").getKeys(false).isEmpty();
    }

    public static ScheduledAction getScheduledActionType(String scheduleKey) {
        if (!hasScheduledActions()) return null;
        return ScheduledAction.valueOf(main.pluginData.getString("scheduled-actions." + scheduleKey + ".actionType"));
    }

    public static boolean hasPendingDelayedAction() {
        return main.pluginData.getInt("pending-action.taskId", -1) != -1;
    }

    public static ScheduledActionEntry getScheduledAction(Date date) {
        if (!dateInUse(date)) return null;
        return new ScheduledActionEntry(
                date,
                main.pluginData.getString("scheduled-actions." + fileDateFormat.format(date) + ".initiator") == null
                ? null : UUID.fromString(main.pluginData.getString("scheduled-actions." + fileDateFormat.format(date) + ".initiator")),
                ScheduledAction.valueOf(main.pluginData.getString("scheduled-actions." + fileDateFormat.format(date) + ".actionType"))
        );
    }

    public static boolean dateInUse(Date date) {
        if (getScheduledActions().isEmpty()) return false;
        return getScheduledActions().contains(fileDateFormat.format(date));
    }

    private static void removeScheduledActionEntry(Date date) {
        main.pluginData.set("scheduled-actions." + fileDateFormat.format(date), null);
        main.saveData();
    }

    public static void cancelPendingDelayedAction() {
        if (main.pluginData.getConfigurationSection("pending-action") == null) return;
        int taskId = main.pluginData.getInt("pending-action.taskId");
        scheduler.cancelTask(taskId);
        main.pluginData.set("pending-action", null);
        main.saveData();
        hideBossbar();
    }

    public static void cancelScheduledActionForDate(Date date) {
        if (!dateInUse(date)) return;
        int launchTask = main.pluginData.getInt("scheduled-actions." + fileDateFormat.format(date) + ".launchTaskId", -1);
        int countdownTask = main.pluginData.getInt("scheduled-actions." + fileDateFormat.format(date) + ".countdownTaskId", -1);
        scheduler.cancelTask(launchTask);
        scheduler.cancelTask(countdownTask);

        removeScheduledActionEntry(date);
        hideBossbar();
    }

    private static void startScheduledActionCountdown(Date restartDate, ScheduledAction action) {
        if (ConfigurationUtility.getBossbarEnabled(action)) showBossbar();

        long startTime = Date.from(Instant.now()).getTime();

        int taskId = scheduler.runTaskTimerAsynchronously(main, new Runnable() {
            @Override
            public void run() {
                if (Date.from(Instant.now()).getTime() < restartDate.getTime()) {
                    bossbar.setProgress(Math.min((double) (Date.from(Instant.now()).getTime() - startTime) / (restartDate.getTime() - startTime), 1));
                    if (Duration.between(Instant.now(), restartDate.toInstant()).toSeconds() >= 60) {
                        bossbar.setTitle(ChatColor.translateAlternateColorCodes('&',
                                main.localizationUtility
                                        .getLocalizedString("messages.bossbar." + action.getLocalizationKey() + ".countdown")
                                        .replace("%countdown%",
                                                main.formatUtility.generateDurationString(restartDate,
                                                        Date.from(Instant.now())
                                                )
                                        )
                        ));
                    } else {
                        bossbar.setTitle(ChatColor.translateAlternateColorCodes('&',
                                main.localizationUtility
                                        .getLocalizedString("messages.bossbar." + action.getLocalizationKey() + ".final_countdown")
                                        .replace("%countdown%",
                                                main.formatUtility.generateDurationString(restartDate,
                                                        Date.from(Instant.now())
                                                )
                                        )
                        ));
                        if (Duration.between(Instant.now(), restartDate.toInstant()).toSeconds() <= 10) {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                main.messageUtility.sendMessage(p,
                                        "&e==========================================\n" +
                                        main.localizationUtility
                                                .getLocalizedString("messages.broadcast." + action.getLocalizationKey() + ".final_countdown")
                                                .replace("%countdown%",
                                                        main.formatUtility.generateDurationString(restartDate,
                                                                Date.from(Instant.now())
                                                        )
                                                )
                                        + "\n&e=========================================="
                                );
                            }
                        }
                    }
                } else {
                    bossbar.setProgress(1);
                    scheduler.runTask(main, new Runnable() {
                        @Override
                        public void run() {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ConfigurationUtility.getActionCommand(action));
                            hideBossbar();
                        }
                    });
                }
            }
        }, 0L, 20L).getTaskId();
        main.pluginData.set("scheduled-actions." + fileDateFormat.format(restartDate) + ".countdownTaskId", taskId);
        main.saveData();
    }



}
