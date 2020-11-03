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

package ar.delellis.quicknotes.api;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.GsonBuilder;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import retrofit2.NextcloudRetrofitApiBuilder;

public class ApiProvider {
    private final String TAG = ApiProvider.class.getCanonicalName();

    private static final String QUICKNOTES_API_ENDPOINT = "/index.php/apps/quicknotes/api/v1";

    @NonNull
    protected Context context;
    protected static QuicknotesApi quicknotesApi;

    protected static String ssoAccountName;

    public ApiProvider(@NotNull Context context) {
        this.context = context;
        initSsoApi(new NextcloudAPI.ApiConnectedListener() {
            @Override
            public void onConnected() {
                // Ignore..
            }

            @Override
            public void onError(Exception ex) {
                // Ignore...
            }
        });
    }

    public void initSsoApi(final NextcloudAPI.ApiConnectedListener callback) {
        try {
            SingleSignOnAccount ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(context);
            NextcloudAPI nextcloudAPI = new NextcloudAPI(context, ssoAccount, new GsonBuilder().create(), callback);

            ssoAccountName = ssoAccount.name;
            quicknotesApi = new NextcloudRetrofitApiBuilder(nextcloudAPI, QUICKNOTES_API_ENDPOINT).create(QuicknotesApi.class);
        } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
            Log.d(TAG, "setAccout() called with: ex = [" + e + "]");
        }
    }

    public static QuicknotesApi getQuicknotesAPI() {
        return quicknotesApi;
    }

    public static String getAccountName() {
        return ssoAccountName;
    }

}