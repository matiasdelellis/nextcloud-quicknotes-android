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

package ar.delellis.quicknotes.activity.editor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import org.wordpress.aztec.AztecText;
import org.wordpress.aztec.AztecTextFormat;

import com.imagine.colorpalette.ColorPalette;

import java.util.Objects;

import ar.delellis.quicknotes.R;
import ar.delellis.quicknotes.shared.AttachmentAdapter;
import ar.delellis.quicknotes.shared.ShareAdapter;
import ar.delellis.quicknotes.shared.TagAdapter;
import ar.delellis.quicknotes.api.ApiProvider;
import ar.delellis.quicknotes.model.Note;
import ar.delellis.quicknotes.util.ColorUtil;

public class EditorActivity extends AppCompatActivity implements EditorView {

    EditorPresenter presenter;
    ProgressDialog progressDialog;

    protected ApiProvider mApi;

    AttachmentAdapter attachmentAdapter;
    RecyclerView attachmentRecyclerView;

    EditText et_title;
    AztecText et_content;

    TagAdapter tagAdapter;
    RecyclerView tagRecyclerView;

    ShareAdapter shareAdapter;
    RecyclerView shareRecyclerView;

    ColorPalette palette;
    Toolbar rich_toolbar;

    Note note = new Note();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);

            Drawable drawable = ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_back_grey, null);
            if (drawable != null) {
                DrawableCompat.setTint(drawable, getResources().getColor(R.color.defaultNoteTint));
                actionBar.setHomeAsUpIndicator(drawable);
            } else {
                actionBar.setHomeAsUpIndicator(R.drawable.ic_back_grey);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        ActionMenuView actionMenuView =  findViewById(R.id.rich_action_menu);
        Menu actionMenu = actionMenuView.getMenu();
        getMenuInflater().inflate(R.menu.rich_editor, actionMenu);
        actionMenuView.setOnMenuItemClickListener(item -> OnMenuItemClick(item));

        mApi = new ApiProvider(getApplicationContext());

        attachmentAdapter = new AttachmentAdapter();
        attachmentRecyclerView = findViewById(R.id.recyclerAttachments);

        et_title = findViewById(R.id.title);
        et_content = findViewById(R.id.content);
        palette = findViewById(R.id.palette);

        tagAdapter = new TagAdapter();
        tagRecyclerView = findViewById(R.id.recyclerTags);

        shareAdapter = new ShareAdapter();
        shareRecyclerView = findViewById(R.id.recyclerShares);

        // Color selection
        palette.setListener(color_hex -> {
            et_content.getRootView().setBackgroundColor(Color.parseColor(color_hex));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(Color.parseColor(color_hex));
            }
            note.setColor(color_hex);
        });

        rich_toolbar = findViewById(R.id.rich_toolbar);
        rich_toolbar.setTitle(null);

        // Create progress dialog.
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));

        presenter = new EditorPresenter(this);

        Intent intent = getIntent();
        if (intent.hasExtra("note")) {
            note = (Note) Objects.requireNonNull(intent.getSerializableExtra("note"));
        }

        setDataFromIntentExtra();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int tintColor = this.getResources().getColor(R.color.defaultNoteTint);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_editor, menu);

        MenuItem deleteItem = menu.findItem(R.id.delete);
        deleteItem.setVisible(note.getId() != 0 && !note.getIsShared());
        ColorUtil.menuItemTintColor(deleteItem, tintColor);

        MenuItem pinItem = menu.findItem(R.id.pin);
        pinItem.setIcon(note.getIsPinned() ? R.drawable.ic_pinned : R.drawable.ic_pin);
        pinItem.setVisible(!note.getIsShared());
        ColorUtil.menuItemTintColor(pinItem, tintColor);

        ColorUtil.menuItemTintColor(menu.findItem(R.id.save), tintColor);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pin:
                note.setIsPinned(!note.getIsPinned());
                item.setIcon(note.getIsPinned() ? R.drawable.ic_pinned : R.drawable.ic_pin);
                ColorUtil.menuItemTintColor(item, this.getResources().getColor(R.color.defaultNoteTint));
                return true;
            case R.id.save:
                if (note.getIsShared()) {
                    closeEdition();
                    return true;
                }
                // Update note from view.
                note.setTitle(et_title.getText().toString().trim());
                note.setContent(et_content.toFormattedHtml().trim());

                if (note.getTitle().isEmpty()) {
                    et_title.setError(getString(R.string.enter_title));
                } else if (note.getContent().isEmpty()) {
                    et_content.setError(getString(R.string.enter_note));
                } else {
                    if (note.getId() == 0)
                        presenter.createNote(note);
                    else
                        presenter.updateNote(note);
                }
                return true;
            case R.id.delete:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle(getString(R.string.delete_note));
                alertDialog.setMessage("Are you sure you want to delete the note?");
                alertDialog.setNegativeButton("Yes", (dialog, wich) -> {
                    dialog.dismiss();
                    presenter.deleteNote(note.getId());
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

    public boolean OnMenuItemClick(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bold:
                et_content.toggleFormatting(AztecTextFormat.FORMAT_BOLD);
                return true;
            case R.id.italic:
                et_content.toggleFormatting(AztecTextFormat.FORMAT_ITALIC);
                return true;
            case R.id.underline:
                et_content.toggleFormatting(AztecTextFormat.FORMAT_UNDERLINE);
                return true;
            case R.id.strike:
                et_content.toggleFormatting(AztecTextFormat.FORMAT_STRIKETHROUGH);
                return true;
            case R.id.quote:
                et_content.toggleFormatting(AztecTextFormat.FORMAT_QUOTE);
                return true;
            case R.id.ordered_list:
                et_content.toggleFormatting(AztecTextFormat.FORMAT_ORDERED_LIST);
                return true;
            case R.id.unordered_list:
                et_content.toggleFormatting(AztecTextFormat.FORMAT_UNORDERED_LIST);
                return true;
            default:
                return false;
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
        Toast.makeText(EditorActivity.this, message, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onRequestError(String message) {
        Toast.makeText(EditorActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void setDataFromIntentExtra() {
        if (note.getId() != 0) {
            attachmentAdapter.setItems(note.getAttachtments());
            attachmentAdapter.notifyDataSetChanged();
            attachmentRecyclerView.setAdapter(attachmentAdapter);

            et_title.setText(Html.fromHtml(note.getTitle()));
            et_content.fromHtml(note.getContent().trim(), true);
            et_content.getRootView().setBackgroundColor(Color.parseColor(note.getColor()));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(Color.parseColor(note.getColor()));
            }

            tagAdapter.setItems(note.getTags());
            tagAdapter.notifyDataSetChanged();
            tagRecyclerView.setAdapter(tagAdapter);

            shareAdapter.setItems(note.getShareWith());
            shareAdapter.notifyDataSetChanged();
            shareRecyclerView.setAdapter(shareAdapter);

            palette.setSelectedColor(note.getColor());

            if (note.getIsShared()) {
                readMode();
            } else {
                editMode();
            }
        } else {
            // Default color.
            int defaultColor = getResources().getColor(R.color.defaultNoteColor);
            et_content.getRootView().setBackgroundColor(defaultColor);
            palette.setSelectedColor(defaultColor);
            note.setColor(ColorUtil.getRGBColorFromInt(defaultColor));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(defaultColor);
            }

            // Focus to title and edit
            et_title.requestFocus();
            editMode();
        }
    }

    private void editMode() {
        et_title.setFocusableInTouchMode(true);
        et_content.setFocusableInTouchMode(true);
        palette.setVisibility(View.VISIBLE);
        rich_toolbar.setVisibility(View.VISIBLE);
    }

    private void readMode() {
        et_title.setFocusableInTouchMode(false);
        et_content.setFocusableInTouchMode(false);
        et_title.setFocusable(false);
        et_content.setFocusable(false);
        palette.setVisibility(View.GONE);
        rich_toolbar.setVisibility(View.GONE);
    }

    private void closeEdition () {
        setResult(RESULT_CANCELED);
        finish();
    }
}