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

package ar.com.delellis.quicknotes.activity.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import ar.com.delellis.quicknotes.BuildConfig;
import ar.com.delellis.quicknotes.R;
import ar.com.delellis.quicknotes.api.helper.IResponseCallback;
import ar.com.delellis.quicknotes.databinding.ActivityAboutBinding;
import ar.com.delellis.quicknotes.model.Capabilities;
import ar.com.delellis.quicknotes.util.CapabilitiesService;
import ar.com.delellis.quicknotes.util.InsetsUtil;

public class AboutActivity extends AppCompatActivity {

    private ActivityAboutBinding binding;

    private CapabilitiesService capabilitiesService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        fillAboutActivity();

        capabilitiesService = new CapabilitiesService(this);
        if (capabilitiesService.isInitialized()) {
            fillCapabilities(capabilitiesService.getCapabilities());
        }

        capabilitiesService.refresh(new IResponseCallback() {
            @Override
            public void onComplete() {
                fillCapabilities(capabilitiesService.getCapabilities());
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    private void fillAboutActivity() {
        binding.aboutNextcloudVersion.setVisibility(View.GONE);
        binding.aboutQuicknotesVersion.setVisibility(View.GONE);

        binding.aboutVersion.setText(fromHtml(getString(R.string.about_version, "v" + BuildConfig.VERSION_NAME)));

        binding.aboutAppLicenseButton.setOnClickListener(view -> openUrl(getString(R.string.url_license)));

        binding.aboutTranslate.setText(fromHtml(getString(R.string.about_translate, getString(R.string.url_translations))));
        binding.aboutTranslate.setOnClickListener(view -> openUrl(getString(R.string.url_translations)));

        binding.aboutSource.setText(fromHtml(getString(R.string.about_source, getString(R.string.url_source))));
        binding.aboutSource.setOnClickListener(view -> openUrl(getString(R.string.url_source)));

        binding.aboutIssues.setText(fromHtml(getString(R.string.about_issues, getString(R.string.url_issues))));
        binding.aboutIssues.setOnClickListener(view -> openUrl(getString(R.string.url_issues)));
    }

    private void fillCapabilities(Capabilities capabilities) {
        binding.aboutNextcloudVersion.setText(fromHtml(
                getString(R.string.about_nextcloud_version, "v" + capabilities.getNextcloudVersion())));
        binding.aboutNextcloudVersion.setVisibility(View.VISIBLE);

        binding.aboutQuicknotesVersion.setText(fromHtml(
                getString(R.string.about_quicknotes_version, "v" + capabilities.getQuicknotesVersion())));
        binding.aboutQuicknotesVersion.setVisibility(View.VISIBLE);
    }

    private static CharSequence fromHtml(String source) {
        return HtmlCompat.fromHtml(source, HtmlCompat.FROM_HTML_MODE_LEGACY);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void openUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
}
