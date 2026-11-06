package pt.ipcb.ad.frontend.client;

/** Request body sent to the API to start a session. */
public record StartSessionCommand(Long vehicleId, Long chargingPointId,
                                  Integer startBatteryPercent, Integer targetBatteryPercent) {
}
