package com.example.cs179j_ble;

public class DeviceItem
{
    public static String deviceName;
    public static String deviceAddress;

    public DeviceItem (String newDeviceName, String newDeviceAddress)
    {
        deviceName = newDeviceName;
        deviceAddress = newDeviceAddress;
    }
    public DeviceItem()
    {
        deviceName = "";
        deviceAddress = "";
    }


    public static String getDeviceAddress() {
        return deviceAddress;
    }

    public static String getDeviceName() {
        return deviceName;
    }

}
