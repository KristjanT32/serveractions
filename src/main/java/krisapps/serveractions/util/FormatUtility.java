package krisapps.serveractions.util;

import krisapps.serveractions.ServerActions;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class FormatUtility {

    ServerActions main;
    public FormatUtility(ServerActions main) {
        this.main = main;
    }

    public Date generateExpirationDate(Date startingDate, int duration) {
        return new Date(startingDate.getTime() + TimeUnit.MINUTES.toMillis(duration));
    }

    public String generateDurationString(Date start, Date current) {
        Instant startInstant = start.toInstant();
        Instant endInstant = current.toInstant();

        Duration dur = Duration.between(startInstant, endInstant);

        long hours = Math.abs(dur.toHoursPart());
        long minutes = Math.abs(dur.toMinutesPart());
        long seconds = Math.abs(dur.toSecondsPart());

        return String.format("%s:%s:%s", formatTimeUnit((int) hours), formatTimeUnit((int) minutes), formatTimeUnit((int) seconds));
    }

    public String generateTimeStringFromSeconds(int sec) {
        int minutes = (sec / 60);
        int hours = (sec / 60) / 60;

        int seconds = sec - (hours * 60 * 60);
        seconds = seconds - (minutes * 60);

        return String.format("%s:%s:%s", formatTimeUnit((int) hours), formatTimeUnit((int) minutes), formatTimeUnit((int) seconds));
    }

    public static String formatTimeUnit(int unit) {
        return unit <= 9
                ? "0" + unit
                : String.valueOf(unit);
    }
}
