package hudson.plugins.openstf;

import net.sf.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JEP-200 workaround
 */
public class DeviceConditions implements Serializable {
    private Map<String, String> conditions = new HashMap<>();

    public void putCondition(String conditionName, String value) {
        conditions.put(conditionName, value);
    }

    public JSONObject toJsonObject() {
        return JSONObject.fromObject(conditions);
    }
}
