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

package ar.com.delellis.quicknotes.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

import ar.com.delellis.quicknotes.util.VersionUtil;

public class Capabilities implements Serializable {
    /**
     * The api this client is written against. 1.5 is where emptying the trash
     * arrived, and it is the last of the reshapes that started at 1.1, so it
     * is the whole contract this client counts on.
     */
    public static final String REQUIRED_API_VERSION = "1.5";

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
        return quicknotesVersion != null ? quicknotesVersion : "";
    }

    public void setQuicknotesVersion(String quicknotesVersion) {
        this.quicknotesVersion = quicknotesVersion;
    }

    public String getQuicknotesApiVersion() {
        return quicknotesApiVersion != null ? quicknotesApiVersion : "";
    }

    public void setQuicknotesApiVersion(String quicknotesApiVersion) {
        this.quicknotesApiVersion = quicknotesApiVersion;
    }

    public String getNextcloudVersion() {
        return nextcloudVersion != null ? nextcloudVersion : "";
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

    /** Whether the server app is there at all. */
    public boolean isQuicknotesInstalled() {
        return !getQuicknotesVersion().isEmpty();
    }

    /**
     * Whether the server speaks the api this client talks. An older server
     * answers a different shape on shares and knows nothing about reminders,
     * archiving or the trash, so there is no half way to meet it at.
     */
    public boolean isApiVersionSupported() {
        return VersionUtil.isAtLeast(getQuicknotesApiVersion(), REQUIRED_API_VERSION);
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
