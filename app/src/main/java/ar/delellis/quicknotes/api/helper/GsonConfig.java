/*
 * Nextcloud Quicknotes Android client application.
 *
 * @copyright Copyright (c) 2020 Matias De lellis <mati86dl@gmail.com>
 *
 * @author Matias De lellis <mati86dl@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ar.delellis.quicknotes.api.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import ar.delellis.quicknotes.model.Capabilities;

public class GsonConfig {

    JsonDeserializer<Capabilities> capabilitiesJsonDeserializer = new JsonDeserializer<Capabilities>() {
        @Override
        public Capabilities deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Capabilities capabilities = new Capabilities();
            JsonObject jsonCapabilities = json.getAsJsonObject();
            if (jsonCapabilities.has("ocs")) {
                JsonObject ocs = jsonCapabilities.getAsJsonObject("ocs");
                if (ocs.has("meta")) {
                    int statuscode = ocs.getAsJsonObject("meta").get("statuscode").getAsInt();
                    capabilities.setMaintenanceEnabled(statuscode == 503);
                    if (capabilities.isMaintenanceEnabled()) {
                        return capabilities;
                    }
                }
                if (ocs.has("data")) {
                    JsonObject data = ocs.getAsJsonObject("data");
                    if (data.has("version")) {
                        JsonObject version = data.getAsJsonObject("version");
                        capabilities.setNextcloudVersion(version.get("string").getAsString());
                    }
                    if (data.has("capabilities")) {
                        JsonObject caps = data.getAsJsonObject("capabilities");
                        if (caps.has("quicknotes")) {
                            JsonObject quicknotes = caps.getAsJsonObject("quicknotes");
                            if (quicknotes.has("version")) {
                                capabilities.setQuicknotesVersion(quicknotes.get("version").getAsString());
                            }
                            if (quicknotes.has("api_version")) {
                                capabilities.setQuicknotesApiVersion(quicknotes.get("api_version").getAsString());
                            }
                        }
                    }
                }
            }
            return capabilities;
        }
    };

    public Gson create() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setLenient();
        gsonBuilder.registerTypeAdapter(Capabilities.class, capabilitiesJsonDeserializer);
        return gsonBuilder.create();
    }
}
