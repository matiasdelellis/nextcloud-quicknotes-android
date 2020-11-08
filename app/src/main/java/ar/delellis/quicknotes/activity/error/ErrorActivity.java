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

package ar.delellis.quicknotes.activity.error;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

import ar.delellis.quicknotes.R;

public class ErrorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_error);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        Intent intent = getIntent();

        String errorMessage = (String) Objects.requireNonNull(intent.getSerializableExtra("errorMessage"));
        TextView tvError = findViewById(R.id.error_message);
        tvError.setText(Html.fromHtml(errorMessage));

        TextView tvIssues = findViewById(R.id.about_issues);
        tvIssues.setText(Html.fromHtml(getString(R.string.about_issues, getString(R.string.url_issues))));
        tvIssues.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_issues)))));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}
