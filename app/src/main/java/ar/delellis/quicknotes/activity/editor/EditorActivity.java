/*
 * Nextcloud Quicknotes Android client application.
 *
 * @copyright Copyright (c) 2020 Matias De lellis <mati86dl@gmail.com>
 *
 * @author Matias De lellis <mati86dl@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ar.delellis.quicknotes.activity.editor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.imagine.colorpalette.ColorPalette;

import ar.delellis.quicknotes.R;
import ar.delellis.quicknotes.api.ApiProvider;

public class EditorActivity extends AppCompatActivity implements EditorView {

    EditorPresenter presenter;
    ProgressDialog progressDialog;

    protected ApiProvider mApi;

    EditText et_title;
    EditText et_content;
    ColorPalette palette;

    int id;
    String title;
    String content;
    String color;
    boolean is_pinned;
    boolean is_shared;

    Menu actionMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_grey);
        }

        mApi = new ApiProvider(getApplicationContext());

        et_title = findViewById(R.id.title);
        et_content = findViewById(R.id.content);
        palette = findViewById(R.id.palette);

        // Color selection
        palette.setListener(color_hex -> {
            et_content.getRootView().setBackgroundColor(Color.parseColor(color_hex));
            color = color_hex;
        });

        // Create progress dialog.
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));

        presenter = new EditorPresenter(this);

        Intent intent = getIntent();
        id = intent.getIntExtra("id", 0);
        title = intent.getStringExtra("title");
        content = intent.getStringExtra("content");
        color = intent.getStringExtra("color");
        is_pinned = intent.getBooleanExtra("is_pinned", false);
        is_shared = intent.getBooleanExtra("is_shared", false);

        setDataFromIntentExtra();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_editor, menu);

        actionMenu = menu;
        if (id != 0) {
            actionMenu.findItem(R.id.delete).setVisible(!is_shared);
            actionMenu.findItem(R.id.pin).setVisible(!is_shared);
            actionMenu.findItem(R.id.pin).setIcon(is_pinned ? R.drawable.ic_pinned : R.drawable.ic_pin);
        } else {
            actionMenu.findItem(R.id.pin).setVisible(false);
            actionMenu.findItem(R.id.delete).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String title = et_title.getText().toString().trim();
        String content = et_content.getText().toString().trim();
        String color = this.color;
        switch (item.getItemId()) {
            case R.id.pin:
                is_pinned = !is_pinned;
                item.setIcon(is_pinned ? R.drawable.ic_pinned : R.drawable.ic_pin);
                return true;
            case R.id.save:
                if (is_shared) {
                    closeEdition();
                    return true;
                }
                if (title.isEmpty()) {
                    et_title.setError(getString(R.string.enter_title));
                } else if (content.isEmpty()) {
                    et_content.setError(getString(R.string.enter_note));
                } else {
                    if (id == 0)
                        presenter.createNote(title, content, color);
                    else
                        presenter.updateNote(id, title, content, color, is_pinned);
                }
                return true;
            case R.id.delete:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle(getString(R.string.delete_note));
                alertDialog.setMessage("Are you sure you want to delete the note?");
                alertDialog.setNegativeButton("Yes", (dialog, wich) -> {
                    dialog.dismiss();
                    presenter.deleteNote(id);
                });
                alertDialog.setPositiveButton("Cancel", ((dialog, which) -> dialog.dismiss()));
                alertDialog.show();
                return true;
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showProgress() {
        progressDialog.show();
    }

    @Override
    public void hideProgress() {
        progressDialog.dismiss();
    }

    @Override
    public void onRequestSuccess(String message) {
        runOnUiThread(() -> {
            Toast.makeText(EditorActivity.this, message, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        });
    }

    @Override
    public void onRequestError(String message) {
        runOnUiThread(() -> Toast.makeText(EditorActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    private void setDataFromIntentExtra() {
        if (id != 0) {
            et_title.setText(Html.fromHtml(title));
            et_content.setText(Html.fromHtml(content));
            et_content.getRootView().setBackgroundColor(Color.parseColor(color));
            palette.setSelectedColor(color);
            if (is_shared) {
                readMode();
            } else {
                editMode();
            }
        } else {
            // Default color.
            et_content.getRootView().setBackgroundColor(getResources().getColor(R.color.defaultColor));
            palette.setSelectedColor(getResources().getColor(R.color.defaultColor));
            editMode();
        }
    }

    private void editMode() {
        et_title.setFocusableInTouchMode(true);
        et_content.setFocusableInTouchMode(true);
        palette.setVisibility(View.VISIBLE);
    }

    private void readMode() {
        et_title.setFocusableInTouchMode(false);
        et_content.setFocusableInTouchMode(false);
        et_title.setFocusable(false);
        et_content.setFocusable(false);
        palette.setVisibility(View.GONE);
    }

    private void closeEdition () {
        setResult(RESULT_CANCELED);
        finish();
    }
}