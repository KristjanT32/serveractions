package krisapps.restartplus.util;

import krisapps.restartplus.RestartPlus;
import krisapps.restartplus.types.ScheduledAction;

public class ConfigurationUtility {

    static RestartPlus main = RestartPlus.instance;

    public static int getDefaultRestartDelay() {
        return main.pluginConfig.getInt("settings.default-restart-delay", 30);
    }

    public static int getMinimumRestartDelay() {
        return main.pluginConfig.getInt("settings.min-restart-delay", 30);
    }

    public static boolean getBossbarEnabled(ScheduledAction action) {
        return main.pluginConfig.getBoolean(action.getConfigPath() + ".showBossbar", true);
    }

    public static String getActionCommand(ScheduledAction action) {
        return main.pluginConfig.getString(action.getConfigPath() + ".consoleCommand");
    }

}
