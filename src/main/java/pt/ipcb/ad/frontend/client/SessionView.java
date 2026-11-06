package pt.ipcb.ad.frontend.client;

import java.time.LocalDateTime;

/** A charging session as returned by the charging-session-service API. */
public record SessionView(
        Long id,
        Long vehicleId,
        Long chargingPointId,
        String status,
        int startBatteryPercent,
        int targetBatteryPercent,
        double chargingPowerKw,
        double energyNeededKwh,
        double energyDeliveredKwh,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Long durationMinutes) {

    public boolean isInProgress() {
        return "IN_PROGRESS".equals(status);
    }
}
