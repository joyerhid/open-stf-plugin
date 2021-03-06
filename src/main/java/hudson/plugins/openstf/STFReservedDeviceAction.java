package hudson.plugins.openstf;

import hudson.model.Action;
import hudson.model.BuildBadgeAction;
import hudson.plugins.openstf.Messages;
import io.swagger.client.model.DeviceListResponseDevices;
import org.kohsuke.stapler.export.Exported;

import java.net.URL;

public class STFReservedDeviceAction implements BuildBadgeAction, Action {
  private final String stfApiEndpoint;
  private final ReservedDevice reservedDevice;

  public STFReservedDeviceAction(String stfApiEndpoint, ReservedDevice reservedDevice) {
    this.stfApiEndpoint = stfApiEndpoint;
    this.reservedDevice = reservedDevice;
  }

  /**
   * Get the device image URL.
   * This method is called by Jenkins.
   * return image URL
   */
  @Exported
  public String getDeviceIcon() {
    String path = "/static/app/devices/icon/x120/";
    if (reservedDevice.image != null) {
      path += reservedDevice.image;
    } else {
      path += "_default.jpg";
    }
    try {
      URL iconUrl = new URL(new URL(stfApiEndpoint), path);
      return iconUrl.toString();
    } catch (Exception ex) {
      return "";
    }
  }

  @Exported
  public String getSummary() {
    return Messages.PUBLISH_RESERVED_DEVICE_INFO(reservedDevice.name, reservedDevice.manufacturer, reservedDevice.model,
            reservedDevice.sdk, reservedDevice.version, reservedDevice.notes);
  }

  @Exported
  public boolean getShowBadge() {
    return reservedDevice != null;
  }

  public String getDisplayName() {
    return null;
  }

  public String getIconFileName() {
    return null;
  }

  public String getUrlName() {
    return null;
  }
}
