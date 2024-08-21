package krisapps.restartplus.types;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class ScheduledActionEntry {

    private final Date scheduleDate;
    private final UUID initiator;
    private final ScheduledAction action;

    public ScheduledActionEntry(Date scheduleDate, UUID initiator, ScheduledAction action) {
        this.scheduleDate = scheduleDate;
        this.initiator = initiator;
        this.action = action;
    }

    @NotNull
    public Date getScheduleDate() {
        return scheduleDate;
    }

    public Optional<UUID> getInitiator() {
        return Optional.ofNullable(initiator);
    }

    @NotNull
    public ScheduledAction getAction() {
        return action;
    }
}
