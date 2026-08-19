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

package ar.com.delellis.quicknotes.api;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import ar.com.delellis.quicknotes.api.helper.GsonConfig;
import retrofit2.NextcloudRetrofitApiBuilder;

/**
 * Holds the api of the account that is signed in.
 *
 * Everything goes through the Files app over SSO, so there is no url and no
 * password of ours anywhere: what there is, is the account the user picked.
 */
public class ApiProvider {
    private static final String TAG = ApiProvider.class.getCanonicalName();

    @NonNull
    protected final Context context;

    protected static QuicknotesAPI quicknotesAPI;

    protected static NextcloudServerApi nextcloudServerApi;

    protected static String username;

    public ApiProvider(@NonNull Context context) {
        this.context = context;

        initSsoApi();
    }

    public void initSsoApi() {
        try {
            SingleSignOnAccount ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(context);
            NextcloudAPI nextcloudAPI = new NextcloudAPI(context, ssoAccount, new GsonConfig().create(), new NextcloudAPI.ApiConnectedListener() {
                @Override
                public void onConnected() {
                    // Ignore..
                }

                @Override
                public void onError(Exception ex) {
                    Log.w(TAG, "Could not connect to the Files app", ex);
                }
            });

            quicknotesAPI = new NextcloudRetrofitApiBuilder(nextcloudAPI, QuicknotesAPI.API_ENDPOINT).create(QuicknotesAPI.class);
            nextcloudServerApi = new NextcloudRetrofitApiBuilder(nextcloudAPI, NextcloudServerApi.NC_API_ENDPOINT).create(NextcloudServerApi.class);

            username = ssoAccount.name;
        } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
            Log.d(TAG, "setAccount() called with: ex = [" + e + "]");
        }
    }

    public static QuicknotesAPI getQuicknotesAPI() {
        return quicknotesAPI;
    }

    public static NextcloudServerApi getNextcloudServerApi() {
        return nextcloudServerApi;
    }

    /** Whether there is an account signed in and an api to talk to. */
    public static boolean isReady() {
        return quicknotesAPI != null && nextcloudServerApi != null;
    }

    @Nullable
    public static String getUsername() {
        return username;
    }
}
