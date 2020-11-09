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

package ar.delellis.quicknotes.activity.splash;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import ar.delellis.quicknotes.R;
import ar.delellis.quicknotes.activity.error.ErrorActivity;
import ar.delellis.quicknotes.activity.login.LoginActivity;
import ar.delellis.quicknotes.activity.main.MainActivity;
import ar.delellis.quicknotes.api.ApiProvider;
import ar.delellis.quicknotes.api.helper.IResponseCallback;
import ar.delellis.quicknotes.model.Capabilities;
import ar.delellis.quicknotes.util.CapabilitiesService;

public class SplashscreenActivity extends AppCompatActivity {

    CapabilitiesService capabilitiesService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            SingleSignOnAccount ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(getApplicationContext());
            SingleAccountHelper.setCurrentAccount(getApplicationContext(), ssoAccount.name);
            launchMainActivity();
        } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void launchMainActivity () {
        ApiProvider mApi = new ApiProvider(getApplicationContext());
        capabilitiesService = new CapabilitiesService(this);
        capabilitiesService.refresh(new IResponseCallback() {
            @Override
            public void onComplete() {
                Capabilities capabilities = capabilitiesService.getCapabilities();
                if (capabilities.isMaintenanceEnabled()) {
                    Intent intent = new Intent(SplashscreenActivity.this, ErrorActivity.class);
                    intent.putExtra("errorMessage", getString(R.string.error_maintenance_mode));
                    startActivity(intent);
                    finish();
                } else if (capabilities.getQuicknotesVersion().isEmpty()) {
                    Intent intent = new Intent(getApplicationContext(), ErrorActivity.class);
                    intent.putExtra("errorMessage", getString(R.string.error_not_installed));
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(SplashscreenActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }
}