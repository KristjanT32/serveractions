package krisapps.serveractions.types;

public enum ScheduledAction {
    RESTART("settings.actions.restart", "restart"),
    RELOAD("settings.actions.reload", "reload"),
    STOP("settings.actions.shutdown", "stop"),
    SAVE("settings.actions.save", "save")
    ;

    private final String configPath;
    private final String localizationKey;
    ScheduledAction(String configPath, String localizationKey) {
        this.configPath = configPath;
        this.localizationKey = localizationKey;
    }

    public String getConfigPath() {
        return configPath;
    }

    public String getLocalizationKey() {
        return localizationKey;
    }
}
