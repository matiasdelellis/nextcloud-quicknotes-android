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
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static java.net.HttpURLConnection.HTTP_NOT_MODIFIED;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;

public class CapabilitiesService {
    private static final String TAG = CapabilitiesService.class.getCanonicalName();

    private final String FAKE_ETAG = "ETAG_NONE";

    private SharedPreferences preferences;

    private Context context;
    IResponseCallback responseCallback;

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

    private void putCapabilities(Capabilities capabilities, String etag) {
        preferences.edit().putString(context.getString(R.string.cache_capabilities_etag), etag).apply();

        boolean isMaintenanceEnabled = capabilities.isMaintenanceEnabled();
        preferences.edit().putBoolean(context.getString(R.string.cache_maintenance_enabled), isMaintenanceEnabled).apply();

        String nextcloudVersion = capabilities.getNextcloudVersion();
        preferences.edit().putString(context.getString(R.string.cache_nextcloud_version), nextcloudVersion).apply();

        String quicknotesVersion = capabilities.getQuicknotesVersion();
        preferences.edit().putString(context.getString(R.string.cache_quicknotes_version), quicknotesVersion).apply();

        String quicknotesApiVersion = capabilities.getQuicknotesApiVersion();
        preferences.edit().putString(context.getString(R.string.cache_quicknotes_api_version), quicknotesApiVersion).apply();
    }

    private Observer<ParsedResponse<Capabilities>> subscribeCapabilities = new Observer<ParsedResponse<Capabilities>>() {
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
                } else if (requestFailedException.getStatusCode() == HTTP_UNAVAILABLE) {
                    Log.d(TAG, "onError HTTP_UNAVAILABLE");
                    // Retrofit don't handle response when 503. Save Fake capabilities response.
                    preferences.edit().putString(context.getString(R.string.cache_capabilities_etag), String.valueOf(System.currentTimeMillis())).apply();
                    preferences.edit().putBoolean(context.getString(R.string.cache_maintenance_enabled), true).apply();
                    responseCallback.onComplete();
                }
            } else {
                Log.d(TAG, "onError unknown");
                responseCallback.onError(e);
            }
        }

        @Override
        public void onComplete() {
            Log.d(TAG, "onComplete " + CapabilitiesService.this.getCapabilities().toString());
            responseCallback.onComplete();
        }
    };
}
