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

package ar.com.delellis.quicknotes.activity.editor;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;
import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;
import static ar.com.delellis.quicknotes.activity.editor.AttachBottomSheetDialog.ATTACH_ADD_FILE;
import static ar.com.delellis.quicknotes.activity.editor.AttachBottomSheetDialog.ATTACH_TAKE_PHOTO;
import static ar.com.delellis.quicknotes.activity.editor.AttachBottomSheetDialog.ATTACH_TAKE_VIDEO;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.wordpress.aztec.AztecText;
import org.wordpress.aztec.AztecTextFormat;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import ar.com.delellis.quicknotes.BuildConfig;
import ar.com.delellis.quicknotes.R;
import ar.com.delellis.quicknotes.activity.editor.AttachBottomSheetDialog.OnAttachOptionListener;
import ar.com.delellis.quicknotes.activity.tags.TagsActivity;
import ar.com.delellis.quicknotes.api.ApiProvider;
import ar.com.delellis.quicknotes.model.Attachment;
import ar.com.delellis.quicknotes.model.Note;
import ar.com.delellis.quicknotes.model.Tag;
import ar.com.delellis.quicknotes.shared.AttachmentAdapter;
import ar.com.delellis.quicknotes.shared.ShareAdapter;
import ar.com.delellis.quicknotes.shared.TagAdapter;
import ar.com.delellis.quicknotes.util.ColorUtil;
import ar.com.delellis.quicknotes.util.FileUtils;
import ar.com.delellis.quicknotes.util.HtmlUtil;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import petrov.kristiyan.colorpicker.ColorPicker;

public class EditorActivity extends AppCompatActivity implements EditorView, OnAttachOptionListener {
    private final String TAG = EditorActivity.class.getCanonicalName();

    private static final int REQUEST_CODE_EDIT_TAGS = 100;
    private static final int REQUEST_CODE_ADD_FILE = 101;
    private static final int REQUEST_CODE_IMAGE_CAPTURE = 102;
    private static final int REQUEST_CODE_VIDEO_CAPTURE = 103;

    private static final int REQUEST_CODE_ADD_FILE_PERMISSION = 200;
    private static final int REQUEST_CODE_ADD_CAMERA_PERMISSION = 201;
    private static final int REQUEST_CODE_ADD_VIDEO_PERMISSION = 202;

    private static final String KEY_ACTION_VIEW_FILE_ID = "KEY_FILE_ID";
    private static final String KEY_ACTION_VIEW_ACCOUNT = "KEY_ACCOUNT";

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

    HorizontalScrollView rich_toolbar;

    Note note = new Note();
    Note shadowCopyNote;

    private List<Tag> tags = new ArrayList<>();
    private List<Tag> tagSelection = new ArrayList<>();

    // Temporary files to capture from the camera
    private File tempPhotoCamera = null;
    private File tempVideoCamera = null;

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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        mApi = new ApiProvider(getApplicationContext());

        attachmentAdapter = new AttachmentAdapter();
        attachmentRecyclerView = findViewById(R.id.editor_recyclerAttachments);

        attachmentAdapter.setOnImageClickListener(position -> {
            Attachment attachment = attachmentAdapter.get(position);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(attachment.getDeepLinkUrl()));
            intent.putExtra(KEY_ACTION_VIEW_FILE_ID, attachment.getFileId());
            intent.putExtra(KEY_ACTION_VIEW_ACCOUNT, ApiProvider.getUsername());
            startActivity(intent);
        });

        attachmentAdapter.setOnDeleteClickListener(position -> {
            Attachment attachment = attachmentAdapter.get(position);
            attachmentAdapter.removeItem(attachment);
        });

        et_title = findViewById(R.id.editor_title);
        et_content = findViewById(R.id.editor_content);
        et_content.setCalypsoMode(false);
        rich_toolbar = findViewById(R.id.editor_rich_toolbar);

        tagAdapter = new TagAdapter();
        tagRecyclerView = findViewById(R.id.editor_recyclerTags);

        shareAdapter = new ShareAdapter();
        shareRecyclerView = findViewById(R.id.editor_recyclerShares);

        initToolbar();

        // Create progress dialog.
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));

        presenter = new EditorPresenter(this);

        Intent intent = getIntent();
        if (intent.hasExtra("note")) {
            note = (Note) Objects.requireNonNull(intent.getSerializableExtra("note"));
        }

        tags = (List<Tag>) Objects.requireNonNull(intent.getSerializableExtra("tags"));

        setDataFromIntentExtra();

        // Store the either loaded or just created note as a copy so we can compare for modifications later
        shadowCopyNote = note.clone();
        getSupportActionBar().setElevation(0);
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
        int itemId = item.getItemId();
        if (itemId == R.id.pin) {
            note.setIsPinned(!note.getIsPinned());
            item.setIcon(note.getIsPinned() ? R.drawable.ic_pinned : R.drawable.ic_pin);
            ColorUtil.menuItemTintColor(item, this.getResources().getColor(R.color.defaultNoteTint));
            return true;
        } else if (itemId == R.id.save) {
            if (note.getIsShared()) {
                closeEdition();
                return true;
            }

            fetchDataToNoteObject();

            if (note.getTitle().isEmpty()) {
                et_title.setError(getString(R.string.must_enter_title));
            } else {
                if (note.getId() == 0)
                    presenter.createNote(note);
                else
                    presenter.updateNote(note);
            }
            return true;
        } else if (itemId == R.id.delete) {
            new MaterialAlertDialogBuilder(this).setTitle(R.string.delete_note)
                    .setMessage(R.string.sure_want_delete)
                    .setPositiveButton(R.string.common_yes, ((dialog, which) -> {
                        dialog.dismiss();
                        presenter.deleteNote(note.getId());
                    }))
                    .setNegativeButton(R.string.common_cancel, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
            return true;
        } else if (itemId == android.R.id.home) {
            if (hasModifications()) {
                showDiscardDialog(() -> {
                    setResult(RESULT_CANCELED);
                    finish();
                }, null);
            } else {
                setResult(RESULT_OK);
                finish();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void fetchDataToNoteObject() {
        // Clean html from view and update note to save.
        note.setTitle(HtmlUtil.cleanString(et_title.getText().toString()));
        note.setContent(HtmlUtil.cleanHtml(et_content.toPlainHtml(false)));
    }

    public void initToolbar() {
        ImageButton button = findViewById(R.id.action_bold);
        button.setOnClickListener(view -> et_content.toggleFormatting(AztecTextFormat.FORMAT_BOLD));

        button = findViewById(R.id.action_italic);
        button.setOnClickListener(view -> et_content.toggleFormatting(AztecTextFormat.FORMAT_ITALIC));

        button = findViewById(R.id.action_underline);
        button.setOnClickListener(view -> et_content.toggleFormatting(AztecTextFormat.FORMAT_UNDERLINE));

        button = findViewById(R.id.action_strike);
        button.setOnClickListener(view -> et_content.toggleFormatting(AztecTextFormat.FORMAT_STRIKETHROUGH));

        button = findViewById(R.id.action_quote);
        button.setOnClickListener(view -> et_content.toggleFormatting(AztecTextFormat.FORMAT_QUOTE));

        button = findViewById(R.id.action_numbered_list);
        button.setOnClickListener(view -> et_content.toggleFormatting(AztecTextFormat.FORMAT_ORDERED_LIST));

        button = findViewById(R.id.action_bulleted_list);
        button.setOnClickListener(view -> et_content.toggleFormatting(AztecTextFormat.FORMAT_UNORDERED_LIST));

        button = findViewById(R.id.action_note_color);
        button.setOnClickListener(view -> showColorPicker());

        button = findViewById(R.id.action_attach);
        button.setOnClickListener(view -> showAttachOptions());

        button = findViewById(R.id.action_tags);
        button.setOnClickListener(view -> showTagsSelection());
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
    public void addAttachment(Attachment attachment) {
        attachmentAdapter.addItem(attachment);

        if (tempPhotoCamera != null) {
            tempPhotoCamera.delete();
            tempPhotoCamera = null;
        }
        if (tempVideoCamera != null) {
            tempVideoCamera.delete();
            tempVideoCamera = null;
        }
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

    @Override
    public void onAttachOptionSelection(int attachOption) {
        switch (attachOption) {
            case ATTACH_ADD_FILE:
                pickFile();
                break;
            case ATTACH_TAKE_PHOTO:
                takePhoto();
                break;
            case ATTACH_TAKE_VIDEO:
                takeVideo();
                break;
            default:
                break;
        }
    }

    private void showAttachOptions() {
        AttachBottomSheetDialog attachBottomSheetDialog = new AttachBottomSheetDialog();
        attachBottomSheetDialog.show(getSupportFragmentManager(), "AttachBottomSheetDialog");
    }

    private void showTagsSelection() {
        Intent intent = new Intent(this, TagsActivity.class);
        intent.putExtra("tags", (Serializable) tags);
        intent.putExtra("tagSelection", (Serializable) tagSelection);
        startActivityForResult(intent, REQUEST_CODE_EDIT_TAGS);
    }

    private void showColorPicker() {
        String[] colors = getResources().getStringArray(R.array.pallete_colors);
        ArrayList<String> palette_colors = new ArrayList<>(Arrays.asList(colors));

        ColorPicker colorPicker = new ColorPicker(this);
        colorPicker.setColors(palette_colors);
        colorPicker.setTitle(getString(R.string.select_note_color));
        colorPicker.setDefaultColorButton(Color.parseColor(note.getColor()));
        colorPicker.setRoundColorButton(true);
        colorPicker.disableDefaultButtons(true);
        colorPicker.setOnFastChooseColorListener(new ColorPicker.OnFastChooseColorListener() {
            @Override
            public void setOnFastChooseColorListener(int position, int color) {
                note.setColor(ColorUtil.getRGBColorFromInt(color));
                tintActivityColor(color);
            }

            @Override
            public void onCancel() {
                //
            }
        });
        colorPicker.show();
    }

    private void takePhoto() {
        if (SDK_INT >= M && ContextCompat.checkSelfPermission(this, CAMERA) != PERMISSION_GRANTED) {
            requestPermissions(new String[]{CAMERA}, REQUEST_CODE_ADD_CAMERA_PERMISSION);
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                tempPhotoCamera = File.createTempFile("IMG_CAMERA_", ".jpg", getCacheDir());
            } catch (IOException e) {
                tempPhotoCamera = null;
            }

            if (tempPhotoCamera == null)
                return;

            Uri photoURI = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileProvider", tempPhotoCamera);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_CODE_IMAGE_CAPTURE);
        }
    }

    private void takeVideo() {
        if (SDK_INT >= M && ContextCompat.checkSelfPermission(this, CAMERA) != PERMISSION_GRANTED) {
            requestPermissions(new String[]{CAMERA}, REQUEST_CODE_ADD_VIDEO_PERMISSION);
            return;
        }

        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            try {
                tempVideoCamera = File.createTempFile("VID_CAMERA_", ".mp4", getCacheDir());
            } catch (IOException e) {
                tempVideoCamera = null;
            }

            if (tempVideoCamera == null)
                return;

            Uri videoURI = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileProvider", tempVideoCamera);
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoURI);
            startActivityForResult(takeVideoIntent, REQUEST_CODE_VIDEO_CAPTURE);
        }
    }

    public void pickFile() {
        if (SDK_INT >= M && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, REQUEST_CODE_ADD_FILE_PERMISSION);
        } else {
            startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setType("*/*"), REQUEST_CODE_ADD_FILE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ADD_FILE_PERMISSION:
                if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
                    pickFile();
                } else {
                    Toast.makeText(this, getString(R.string.need_permission_to_attach), Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_CODE_ADD_CAMERA_PERMISSION:
                if (ContextCompat.checkSelfPermission(this, CAMERA) == PERMISSION_GRANTED) {
                    takePhoto();
                } else {
                    Toast.makeText(this, getString(R.string.need_permission_to_attach), Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_CODE_ADD_VIDEO_PERMISSION:
                if (ContextCompat.checkSelfPermission(this, CAMERA) == PERMISSION_GRANTED) {
                    takeVideo();
                } else {
                    Toast.makeText(this, getString(R.string.need_permission_to_attach), Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void setDataFromIntentExtra() {
        if (note.getId() != 0) {
            attachmentAdapter.setItems(note.getAttachtments());
            attachmentAdapter.setDisableDeletion(note.getIsShared());
            attachmentAdapter.notifyDataSetChanged();
            attachmentRecyclerView.setAdapter(attachmentAdapter);
            note.setTitle(HtmlUtil.cleanString(note.getTitle()));
            et_title.setText(note.getTitle());
            note.setContent(HtmlUtil.cleanHtml(note.getContent()));
            et_content.fromHtml(note.getContent(), true);

            tintActivityColor(Color.parseColor(note.getColor()));

            tagSelection = note.getTags();

            tagAdapter.setItems(tagSelection);
            tagAdapter.notifyDataSetChanged();
            tagRecyclerView.setAdapter(tagAdapter);

            shareAdapter.setItems(note.getShareWith());
            shareAdapter.notifyDataSetChanged();
            shareRecyclerView.setAdapter(shareAdapter);

            if (note.getIsShared()) {
                readMode();
            } else {
                editMode();
            }
        } else {
            // Default color.
            int defaultColor = getResources().getColor(R.color.defaultNoteColor);
            tintActivityColor(defaultColor);

            // Fill default note.
            note.setTitle("");
            note.setContent("");
            note.setColor(ColorUtil.getRGBColorFromInt(defaultColor));

            // Focus to title and edit
            et_title.requestFocus();
            editMode();

            shareRecyclerView.setAdapter(shareAdapter);
            tagRecyclerView.setAdapter(tagAdapter);
        }
    }

    private void editMode() {
        et_title.setFocusableInTouchMode(true);
        et_content.setFocusableInTouchMode(true);
        rich_toolbar.setVisibility(View.VISIBLE);
    }

    private void readMode() {
        et_title.setFocusableInTouchMode(false);
        et_content.setFocusableInTouchMode(false);
        et_title.setFocusable(false);
        et_content.setFocusable(false);
        rich_toolbar.setVisibility(View.GONE);
    }

    private void tintActivityColor(int noteColor) {
        et_content.getRootView().setBackgroundColor(noteColor);
        getWindow().setStatusBarColor(noteColor);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(noteColor));
    }

    private void closeEdition() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_EDIT_TAGS:
                if (resultCode == RESULT_OK) {
                    tagSelection = (List<Tag>) Objects.requireNonNull(data.getSerializableExtra("tagSelection"));
                    note.setTags(tagSelection);
                    tagAdapter.setItems(tagSelection);
                    tagAdapter.notifyDataSetChanged();
                }
                break;
            case REQUEST_CODE_ADD_FILE:
                if (resultCode == RESULT_OK) {
                    if (data == null) {
                        return;
                    }

                    final Uri fileUri = data.getData();
                    if (fileUri == null) {
                        return;
                    }

                    if (!ContentResolver.SCHEME_CONTENT.equals(fileUri.getScheme())) {
                        return;
                    }

                    File file = FileUtils.getFile(this, fileUri);

                    RequestBody requestBody = RequestBody.create(MediaType.parse(getContentResolver().getType(fileUri)), file);
                    MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), requestBody);

                    presenter.uploadAttachment(filePart);
                }
                break;
            case REQUEST_CODE_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), tempPhotoCamera);
                    MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", tempPhotoCamera.getName(), requestBody);
                    presenter.uploadAttachment(filePart);
                }
                break;
            case REQUEST_CODE_VIDEO_CAPTURE:
                if (resultCode == RESULT_OK) {
                    RequestBody requestBody = RequestBody.create(MediaType.parse("video/mp4"), tempVideoCamera);
                    MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", tempVideoCamera.getName(), requestBody);
                    presenter.uploadAttachment(filePart);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Checks if the note has been modified (only comparing major fields);
     */
    private boolean hasModifications() {
        if (shadowCopyNote == null || note == null) {
            return false;
        }

        if (note.getIsShared()) {
            return false;
        }

        fetchDataToNoteObject();

        return !note.equals(shadowCopyNote);
    }

    /**
     * Presents a dialog to the user which then can decide if he wants to discard the changes or not.
     * If discard or stay is chosen, the Runnable given as parameter is executed
     *
     * @param discardAction What to execute when user wants to discard
     * @param stayAction    What to do when user wants to stay (can be null)
     */
    private void showDiscardDialog(Runnable discardAction, Runnable stayAction) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.note_confirm_discard_unsaved_changes_title)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(R.string.note_confirm_discard_unsaved_changes_text)
                .setPositiveButton(R.string.common_yes, ((dialog, which) -> {
                    discardAction.run();
                }))
                .setNegativeButton(R.string.common_cancel, (dialog, which) -> {
                    if (stayAction != null) stayAction.run();
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        if (hasModifications()) {
            showDiscardDialog(EditorActivity.super::onBackPressed, null);
        } else {
            super.onBackPressed();
        }
    }
}