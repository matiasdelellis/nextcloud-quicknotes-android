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

package ar.com.delellis.quicknotes.util;

import static java.net.HttpURLConnection.HTTP_NOT_MODIFIED;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.nextcloud.android.sso.api.ParsedResponse;
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;

import ar.com.delellis.quicknotes.R;
import ar.com.delellis.quicknotes.api.ApiProvider;
import ar.com.delellis.quicknotes.api.helper.IResponseCallback;
import ar.com.delellis.quicknotes.model.Capabilities;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * What the server says about itself, kept in the preferences between runs and
 * refreshed with an ETag so asking again is cheap.
 */
public class CapabilitiesService {
    private static final String TAG = CapabilitiesService.class.getCanonicalName();

    private final String FAKE_ETAG = "ETAG_NONE";

    private final SharedPreferences preferences;

    private final Context context;

    private IResponseCallback responseCallback;

    /**
     * Whether this server is one this client can work with at all.
     */
    public enum Support {
        /** Good to go. */
        OK,
        /** The server is in maintenance mode. */
        MAINTENANCE,
        /** The Quick notes app is not installed or not enabled there. */
        NOT_INSTALLED,
        /** It is there, but it speaks an api older than this client's. */
        API_TOO_OLD
    }

    public CapabilitiesService(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;
    }

    public boolean isInitialized() {
        String lastEtag = preferences.getString(context.getString(R.string.cache_capabilities_etag), FAKE_ETAG);
        return (lastEtag != null && !lastEtag.equals(FAKE_ETAG));
    }

    public void refresh(IResponseCallback responseCallback) {
        this.responseCallback = responseCallback;

        String lastEtag = preferences.getString(context.getString(R.string.cache_capabilities_etag), FAKE_ETAG);
        ApiProvider.getNextcloudServerApi().getCapabilities(lastEtag)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscribeCapabilities);
    }

    public Capabilities getCapabilities() {
        Capabilities capabilities = new Capabilities();

        boolean isMaintenanceEnabled = preferences.getBoolean(context.getString(R.string.cache_maintenance_enabled), false);
        capabilities.setMaintenanceEnabled(isMaintenanceEnabled);

        String nextcloudVersion = preferences.getString(context.getString(R.string.cache_nextcloud_version), "");
        capabilities.setNextcloudVersion(nextcloudVersion);

        String quicknotesVersion = preferences.getString(context.getString(R.string.cache_quicknotes_version), "");
        capabilities.setQuicknotesVersion(quicknotesVersion);

        String quicknotesApiVersion = preferences.getString(context.getString(R.string.cache_quicknotes_api_version), "");
        capabilities.setQuicknotesApiVersion(quicknotesApiVersion);

        return capabilities;
    }

    /**
     * Reads the cached capabilities and says whether this server can be
     * talked to, and if not, what is in the way.
     */
    public Support getSupport() {
        Capabilities capabilities = getCapabilities();
        if (capabilities.isMaintenanceEnabled()) {
            return Support.MAINTENANCE;
        }
        if (!capabilities.isQuicknotesInstalled()) {
            return Support.NOT_INSTALLED;
        }
        if (!capabilities.isApiVersionSupported()) {
            return Support.API_TOO_OLD;
        }
        return Support.OK;
    }

    /**
     * What to put in front of the user about {@link #getSupport()}, or null
     * when there is nothing in the way.
     */
    public String getSupportMessage() {
        switch (getSupport()) {
            case MAINTENANCE:
                return context.getString(R.string.error_maintenance_mode);
            case NOT_INSTALLED:
                return context.getString(R.string.error_not_installed);
            case API_TOO_OLD:
                return context.getString(R.string.error_api_too_old,
                        getCapabilities().getQuicknotesVersion(),
                        Capabilities.REQUIRED_API_VERSION);
            case OK:
            default:
                return null;
        }
    }

    private void putCapabilities(Capabilities capabilities, String etag) {
        preferences.edit()
                .putString(context.getString(R.string.cache_capabilities_etag), etag)
                .putBoolean(context.getString(R.string.cache_maintenance_enabled), capabilities.isMaintenanceEnabled())
                .putString(context.getString(R.string.cache_nextcloud_version), capabilities.getNextcloudVersion())
                .putString(context.getString(R.string.cache_quicknotes_version), capabilities.getQuicknotesVersion())
                .putString(context.getString(R.string.cache_quicknotes_api_version), capabilities.getQuicknotesApiVersion())
                .apply();
    }

    private final Observer<ParsedResponse<Capabilities>> subscribeCapabilities = new Observer<ParsedResponse<Capabilities>>() {
        @Override
        public void onSubscribe(Disposable d) {
            Log.d(TAG, "onSubscribe");
        }

        @Override
        public void onNext(ParsedResponse<Capabilities> response) {
            Log.d(TAG, "onNext: " + response.getResponse().toString());
            CapabilitiesService.this.putCapabilities(response.getResponse(), response.getHeaders().get("ETag"));
        }

        @Override
        public void onError(Throwable e) {
            if (e instanceof NextcloudHttpRequestFailedException) {
                NextcloudHttpRequestFailedException requestFailedException = (NextcloudHttpRequestFailedException) e;
                if (requestFailedException.getStatusCode() == HTTP_NOT_MODIFIED) {
                    Log.d(TAG, "onError HTTP_NOT_MODIFIED");
                    responseCallback.onComplete();
                    return;
                } else if (requestFailedException.getStatusCode() == HTTP_UNAVAILABLE) {
                    Log.d(TAG, "onError HTTP_UNAVAILABLE");
                    // Retrofit don't handle response when 503. Save Fake capabilities response.
                    preferences.edit()
                            .putString(context.getString(R.string.cache_capabilities_etag), String.valueOf(System.currentTimeMillis()))
                            .putBoolean(context.getString(R.string.cache_maintenance_enabled), true)
                            .apply();
                    responseCallback.onComplete();
                    return;
                }
            }
            Log.d(TAG, "onError unknown", e);
            responseCallback.onError(e);
        }

        @Override
        public void onComplete() {
            Log.d(TAG, "onComplete " + CapabilitiesService.this.getCapabilities().toString());
            responseCallback.onComplete();
        }
    };
}
