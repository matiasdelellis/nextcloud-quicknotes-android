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

import retrofit2.NextcloudRetrofitApiBuilder;

public class ApiProvider {
    private final String TAG = ApiProvider.class.getCanonicalName();

    @NonNull
    protected Context context;
    protected static API mApi;

    protected static String ssoAccountName;

    public ApiProvider(Context context) {
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
            mApi = new NextcloudRetrofitApiBuilder(nextcloudAPI, API.mApiEndpoint).create(API.class);
        } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
            Log.d(TAG, "setAccout() called with: ex = [" + e + "]");
        }
    }

    public static API getAPI() {
        return mApi;
    }

    public static String getAccountName() {
        return ssoAccountName;
    }

}