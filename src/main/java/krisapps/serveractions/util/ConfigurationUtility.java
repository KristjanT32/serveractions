package krisapps.serveractions.util;

import krisapps.serveractions.ServerActions;
import krisapps.serveractions.types.ScheduledAction;

public class ConfigurationUtility {

    static ServerActions main = ServerActions.instance;

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
