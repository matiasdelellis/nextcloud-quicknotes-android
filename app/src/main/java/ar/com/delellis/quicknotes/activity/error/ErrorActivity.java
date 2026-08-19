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

package ar.com.delellis.quicknotes.activity.error;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import java.util.Objects;

import ar.com.delellis.quicknotes.R;
import ar.com.delellis.quicknotes.databinding.ActivitySingleErrorBinding;
import ar.com.delellis.quicknotes.util.InsetsUtil;

public class ErrorActivity extends AppCompatActivity {

    public static final String EXTRA_ERROR_MESSAGE = "errorMessage";

    private ActivitySingleErrorBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySingleErrorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        String errorMessage = (String) Objects.requireNonNull(
                getIntent().getSerializableExtra(EXTRA_ERROR_MESSAGE));
        binding.errorMessage.setText(fromHtml(errorMessage));

        binding.aboutIssues.setText(fromHtml(getString(R.string.about_issues, getString(R.string.url_issues))));
        binding.aboutIssues.setOnClickListener(view ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_issues)))));
    }

    private static CharSequence fromHtml(String source) {
        return HtmlCompat.fromHtml(source, HtmlCompat.FROM_HTML_MODE_LEGACY);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}
