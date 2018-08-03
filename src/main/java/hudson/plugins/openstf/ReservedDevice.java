package hudson.plugins.openstf;

import io.swagger.client.model.DeviceListResponseDevices;

import java.io.Serializable;

/**
 * JEP-200 workaround
 */
public class ReservedDevice implements Serializable {
    String serial;
    String name;
    String model;
    String version;
    String sdk;
    String image;
    boolean present;
    Owner owner;
    String remoteConnectUrl;
    String notes;
    String manufacturer;
    Provider provider;
    String abi;
    Battery battery;

    public static ReservedDevice from(DeviceListResponseDevices device) {
        ReservedDevice reservedDevice = new ReservedDevice();
        reservedDevice.serial = device.serial;
        reservedDevice.name = device.name;
        reservedDevice.model = device.model;
        reservedDevice.version = device.version;
        reservedDevice.sdk = device.sdk;
        reservedDevice.image = device.image;
        reservedDevice.present = device.present;
        reservedDevice.owner = new Owner(device.owner.name, device.owner.group, device.owner.email);
        reservedDevice.remoteConnectUrl = device.remoteConnectUrl;
        reservedDevice.notes = device.notes;
        reservedDevice.manufacturer = device.manufacturer;
        reservedDevice.provider = new Provider(device.provider.name, device.provider.channel);
        reservedDevice.abi = device.abi;
        reservedDevice.battery = new Battery(
                device.battery.health,
                device.battery.level,
                device.battery.scale,
                device.battery.source,
                device.battery.status,
                device.battery.temp,
                device.battery.voltage);
        return reservedDevice;
    }

    public static class Owner {
        String name;
        String group;
        String email;

        public Owner(String name, String group, String email) {
            this.name = name;
            this.group = group;
            this.email = email;
        }
    }

    public static class Provider {
        String name;
        String channel;

        public Provider(String name, String channel) {
            this.name = name;
            this.channel = channel;
        }
    }

    public static class Battery {
        String health;
        int level;
        int scale;
        String source;
        String status;
        float temp;
        float voltage;

        public Battery(String health, int level, int scale, String source, String status, float temp, float voltage) {
            this.health = health;
            this.level = level;
            this.scale = scale;
            this.source = source;
            this.status = status;
            this.temp = temp;
            this.voltage = voltage;
        }
    }
}
