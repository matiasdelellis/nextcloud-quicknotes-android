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

package ar.delellis.quicknotes.activity.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.jetbrains.annotations.NotNull;

import ar.delellis.quicknotes.BuildConfig;
import ar.delellis.quicknotes.R;
import ar.delellis.quicknotes.api.ApiProvider;
import ar.delellis.quicknotes.model.Capabilities;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        findViewById(R.id.about_nextcloud_version).setVisibility(View.GONE);
        findViewById(R.id.about_quicknotes_version).setVisibility(View.GONE);

        fillAboutActivity();

        Call<Capabilities> call = ApiProvider.getNextcloudServerApi().getCapabilities();
        call.enqueue(new Callback<Capabilities>() {
            @Override
            public void onResponse(@NotNull Call<Capabilities> call, @NotNull Response<Capabilities> response) {
                AboutActivity.this.runOnUiThread(() -> {
                    if (response.body() != null)
                        fillCapabilities(response.body());
                });
            }
            @Override
            public void onFailure(@NotNull Call<Capabilities> call, @NotNull Throwable t) {
            }
        });
    }

    private void fillAboutActivity () {
        TextView tvVersion = findViewById(R.id.about_version);
        tvVersion.setText(Html.fromHtml(getString(R.string.about_version, "v" + BuildConfig.VERSION_NAME)));

        Button btLicence = findViewById(R.id.about_app_license_button);
        btLicence.setOnClickListener(view -> openUtl(getString(R.string.url_license)));

        TextView tvTranslate = findViewById(R.id.about_translate);
        tvTranslate.setText(Html.fromHtml(getString(R.string.about_translate, getString(R.string.url_translations))));
        tvTranslate.setOnClickListener(view -> openUtl(getString(R.string.url_translations)));

        TextView tvSource = findViewById(R.id.about_source);
        tvSource.setText(Html.fromHtml(getString(R.string.about_source, getString(R.string.url_source))));
        tvSource.setOnClickListener(view -> openUtl(getString(R.string.url_source)));

        TextView tvIssues = findViewById(R.id.about_issues);
        tvIssues.setText(Html.fromHtml(getString(R.string.about_issues, getString(R.string.url_issues))));
        tvIssues.setOnClickListener(view -> openUtl(getString(R.string.url_issues)));
    }

    private void fillCapabilities(Capabilities capabilities) {
        TextView tvVersion = findViewById(R.id.about_nextcloud_version);
        tvVersion.setText(Html.fromHtml(getString(R.string.about_nextcloud_version, "v" + capabilities.getNextcloudVersion())));
        tvVersion.setVisibility(View.VISIBLE);

        tvVersion = findViewById(R.id.about_quicknotes_version);
        tvVersion.setText(Html.fromHtml(getString(R.string.about_quicknotes_version, "v" + capabilities.getQuicknotesVersion())));
        tvVersion.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void openUtl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
}
