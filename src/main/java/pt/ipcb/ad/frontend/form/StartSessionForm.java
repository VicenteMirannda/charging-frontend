package pt.ipcb.ad.frontend.form;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import pt.ipcb.ad.frontend.client.StartSessionCommand;

/**
 * Data typed by the user in the "start session" form.
 * A class with getters/setters (not a record) because Thymeleaf form binding uses setters.
 */
public class StartSessionForm {

    @NotNull(message = "Choose a vehicle")
    private Long vehicleId;

    @NotNull(message = "Choose a charging point")
    private Long chargingPointId;

    @NotNull(message = "Required")
    @Min(value = 0, message = "Must be between 0 and 99")
    @Max(value = 99, message = "Must be between 0 and 99")
    private Integer startBatteryPercent = 20;

    @NotNull(message = "Required")
    @Min(value = 1, message = "Must be between 1 and 100")
    @Max(value = 100, message = "Must be between 1 and 100")
    private Integer targetBatteryPercent = 80;

    @AssertTrue(message = "Target must be greater than the current battery level")
    public boolean isTargetAboveStart() {
        return startBatteryPercent == null || targetBatteryPercent == null
                || targetBatteryPercent > startBatteryPercent;
    }

    public StartSessionCommand toCommand() {
        return new StartSessionCommand(vehicleId, chargingPointId, startBatteryPercent, targetBatteryPercent);
    }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public Long getChargingPointId() { return chargingPointId; }
    public void setChargingPointId(Long chargingPointId) { this.chargingPointId = chargingPointId; }

    public Integer getStartBatteryPercent() { return startBatteryPercent; }
    public void setStartBatteryPercent(Integer startBatteryPercent) { this.startBatteryPercent = startBatteryPercent; }

    public Integer getTargetBatteryPercent() { return targetBatteryPercent; }
    public void setTargetBatteryPercent(Integer targetBatteryPercent) { this.targetBatteryPercent = targetBatteryPercent; }
}
