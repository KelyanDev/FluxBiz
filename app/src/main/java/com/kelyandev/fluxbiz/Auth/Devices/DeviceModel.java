package com.kelyandev.fluxbiz.Auth.Devices;

public class DeviceModel {
    private String deviceId, deviceName, lastLogin;

    public DeviceModel(String deviceId, String deviceName, String lastLogin) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.lastLogin = lastLogin;
    }

    /**
     * Gets the device's ID
     * @return The device's ID
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Gets the device's name
     * @return The device's name
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * Gets the last time the device was logged in the account.
     * @return The last time the device was logged in
     */
    public String getLastLogin() {
        return lastLogin;
    }
}
