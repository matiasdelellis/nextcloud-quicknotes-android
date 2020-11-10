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

import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import ar.delellis.quicknotes.api.helper.GsonConfig;
import retrofit2.NextcloudRetrofitApiBuilder;

public class ApiProvider {
    private final String TAG = ApiProvider.class.getCanonicalName();

    @NonNull
    protected Context context;

    protected static QuicknotesAPI quicknotesAPI;

    protected static NextcloudServerApi nextcloudServerApi;

    public ApiProvider(@NotNull Context context) {
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
                    // Ignore...
                }
            });

            quicknotesAPI = new NextcloudRetrofitApiBuilder(nextcloudAPI, QuicknotesAPI.API_ENDPOINT).create(QuicknotesAPI.class);
            nextcloudServerApi = new NextcloudRetrofitApiBuilder(nextcloudAPI, NextcloudServerApi.NC_API_ENDPOINT).create(NextcloudServerApi.class);
        } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
            Log.d(TAG, "setAccout() called with: ex = [" + e + "]");
        }
    }

    public static QuicknotesAPI getQuicknotesAPI() {
        return quicknotesAPI;
    }

    public static NextcloudServerApi getNextcloudServerApi() {
        return nextcloudServerApi;
    }

}