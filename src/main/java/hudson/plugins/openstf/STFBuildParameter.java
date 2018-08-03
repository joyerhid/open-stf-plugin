package hudson.plugins.openstf;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.plugins.openstf.exception.ApiFailedException;
import hudson.plugins.openstf.util.Utils;
import hudson.tasks.BuildWrapper;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.swagger.client.model.DeviceListResponseDevices;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import javax.annotation.CheckForNull;
import java.util.List;

/**
 * TODO: WIP
 */
public class STFBuildParameter extends ParameterDefinition {

    private JSONObject deviceCondition;
    private int deviceReleaseWaitTime;

    public STFBuildParameter(JSONObject deviceCondition, int deviceReleaseWaitTime) {
        super("BuildDevice");
        this.deviceCondition = deviceCondition;
        this.deviceReleaseWaitTime = deviceReleaseWaitTime;
    }

    @CheckForNull
    @Override
    public ParameterValue createValue(StaplerRequest staplerRequest, JSONObject jsonObject) {
        return null;
    }

    @CheckForNull
    @Override
    public ParameterValue createValue(StaplerRequest staplerRequest) {
        return null;
    }

    @Extension
    public static class DescriptorImpl extends ParameterDescriptor {
        public String stfApiEndpoint = "";
        public String stfToken = "";
        public Boolean useSpecificKey = false;
        public String adbPublicKey;
        public String adbPrivateKey;
        public boolean ignoreCertError = false;

        public String getDisplayName() {
            return "STF Device Parameter";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            stfApiEndpoint = json.optString("stfApiEndpoint");
            stfToken = json.optString("stfToken");
            useSpecificKey = json.optBoolean("useSpecificKey", false);
            if (useSpecificKey) {
                adbPublicKey = Util.fixEmptyAndTrim(json.optString("adbPublicKey"));
                adbPrivateKey = Util.fixEmptyAndTrim(json.optString("adbPrivateKey"));
            } else {
                adbPublicKey = null;
                adbPrivateKey = null;
            }
            ignoreCertError = json.optBoolean("ignoreCertError", false);
            save();
            return true;
        }

        @Override
        public STFBuildParameter newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            int deviceReleaseWaitTime = 0;
            JSONObject deviceCondition = new JSONObject();

            try {
                deviceReleaseWaitTime = Integer.parseInt(formData.getString("deviceReleaseWaitTime"));
                if (deviceReleaseWaitTime < 0) {
                    deviceReleaseWaitTime = 0;
                }
            } catch (NumberFormatException ex) {
                // ignore
            } finally {
                formData.discard("deviceReleaseWaitTime");
            }

            JSONArray conditionArray = formData.optJSONArray("condition");
            if (conditionArray != null) {
                for (Object conditionObj: conditionArray) {
                    JSONObject condition = JSONObject.fromObject(conditionObj);
                    deviceCondition
                            .put(condition.getString("conditionName"), condition.getString("conditionValue"));
                }
            } else {
                JSONObject condition = formData.optJSONObject("condition");
                if (condition != null) {
                    deviceCondition
                            .put(condition.getString("conditionName"), condition.getString("conditionValue"));
                }
            }

            return new STFBuildParameter(deviceCondition, deviceReleaseWaitTime);
        }

        public ListBoxModel doFillConditionNameItems() {
            Utils.setupSTFApiClient(stfApiEndpoint, ignoreCertError, stfToken);
            return Utils.getSTFDeviceAttributeListBoxItems();
        }

        /**
         * Fill condition value items on Jenkins web view.
         * This method is called by Jenkins.
         * @param conditionName Condition name to get values.
         * @return condition value items.
         */
        public ComboBoxModel doFillConditionValueItems(@QueryParameter String conditionName) {
            if (Util.fixEmpty(stfApiEndpoint) == null || Util.fixEmpty(stfToken) == null) {
                return new ComboBoxModel();
            } else {
                Utils.setupSTFApiClient(stfApiEndpoint, ignoreCertError, stfToken);
                return Utils.getSTFDeviceAttributeValueComboBoxItems(conditionName);
            }
        }

        /**
         * Checking whether the given condition value is valid.
         * This method is called by Jenkins.
         * @return validation result.
         */
        public FormValidation doCheckConditionValue(@QueryParameter String value) {
            if (value.matches(Constants.REGEX_ESCAPED_REGEX_VALUE)) {
                if (!Utils.validateRegexValue(value)) {
                    return FormValidation.error(Messages.INVALID_REGEXP_VALUE());
                }
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckSTFApiEndpoint(@QueryParameter String stfApiEndpoint,
                                                    @QueryParameter boolean ignoreCertError) {
            return Utils.validateSTFApiEndpoint(stfApiEndpoint, ignoreCertError);
        }

        public FormValidation doCheckSTFToken(@QueryParameter String stfApiEndpoint,
                                              @QueryParameter boolean ignoreCertError, @QueryParameter String stfToken) {
            return Utils.validateSTFToken(stfApiEndpoint, ignoreCertError, stfToken);
        }

        /**
         * Display a warning message if 'useSpecificKey' option is selected.
         * This method is called by Jenkins.
         * @return validation result.
         */
        public FormValidation doCheckUseSpecificKey(@QueryParameter Boolean value) {
            if (value) {
                return FormValidation.warning(Messages.ADBKEY_FILE_WILL_BE_OVERWRITTEN());
            } else {
                return FormValidation.ok();
            }
        }

        /**
         * Gets a list of devices that match the given filter, as JSON Array.
         * This method called by javascript in jelly.
         * @param filter Conditions of the STF device you want to get.
         * @return List of STF devices that meet the filter.
         */
        @JavaScriptMethod
        public JSONArray getDeviceListJSON(JSONObject filter) {

            if (Util.fixEmpty(stfApiEndpoint) == null || Util.fixEmpty(stfToken) == null) {
                return new JSONArray();
            }

            if (!Utils.validateDeviceFilter(filter)) {
                return new JSONArray();
            }

            Utils.setupSTFApiClient(stfApiEndpoint, ignoreCertError, stfToken);

            try {
                List<DeviceListResponseDevices> deviceList = Utils.getDeviceList(filter);
                return JSONArray.fromObject(deviceList);
            } catch (ApiFailedException ex) {
                return new JSONArray();
            }
        }

        @JavaScriptMethod
        public synchronized String getStfApiEndpoint() {
            return String.valueOf(stfApiEndpoint);
        }
    }
}
