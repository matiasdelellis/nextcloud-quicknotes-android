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

package ar.delellis.quicknotes.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class Capabilities implements Serializable {
    @Expose
    @SerializedName("quicknotesVersion") private String quicknotesVersion;

    @Expose
    @SerializedName("quicknotesApiVersion") private String quicknotesApiVersion;

    @Expose
    @SerializedName("nextcloudVersion") private String nextcloudVersion;

    @Expose
    @SerializedName("maintenanceEnabled") private boolean maintenanceEnabled;

    public Capabilities() {

    }

    public String getQuicknotesVersion() {
        return quicknotesVersion;
    }

    public void setQuicknotesVersion(String quicknotesVersion) {
        this.quicknotesVersion = quicknotesVersion;
    }

    public String getQuicknotesApiVersion() {
        return quicknotesApiVersion;
    }

    public void setQuicknotesApiVersion(String quicknotesApiVersion) {
        this.quicknotesApiVersion = quicknotesApiVersion;
    }

    public String getNextcloudVersion() {
        return nextcloudVersion;
    }

    public void setNextcloudVersion(String nextcloudVersion) {
        this.nextcloudVersion = nextcloudVersion;
    }

    public boolean isMaintenanceEnabled() {
        return maintenanceEnabled;
    }

    public void setMaintenanceEnabled(boolean maintenanceEnabled) {
        this.maintenanceEnabled = maintenanceEnabled;
    }

    @NotNull
    @Override
    public String toString() {
        return "Capabilities {" +
                "quicknotes-version='" + quicknotesVersion + '\'' +
                ", quicknotes-api-version='" + quicknotesApiVersion + '\'' +
                ", nextcloud-version='" + nextcloudVersion + '\'' +
                ", maintenance-enabled='" + maintenanceEnabled + '\'' +
                '}';
    }

}
