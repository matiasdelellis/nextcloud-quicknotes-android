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

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static ar.com.delellis.quicknotes.activity.editor.AttachBottomSheetDialog.ATTACH_ADD_FILE;
import static ar.com.delellis.quicknotes.activity.editor.AttachBottomSheetDialog.ATTACH_TAKE_PHOTO;
import static ar.com.delellis.quicknotes.activity.editor.AttachBottomSheetDialog.ATTACH_TAKE_VIDEO;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.wordpress.aztec.AztecTextFormat;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ar.com.delellis.quicknotes.BuildConfig;
import ar.com.delellis.quicknotes.R;
import ar.com.delellis.quicknotes.activity.editor.AttachBottomSheetDialog.OnAttachOptionListener;
import ar.com.delellis.quicknotes.activity.shares.SharesActivity;
import ar.com.delellis.quicknotes.activity.tags.TagsActivity;
import ar.com.delellis.quicknotes.api.ApiProvider;
import ar.com.delellis.quicknotes.databinding.ActivityEditorBinding;
import ar.com.delellis.quicknotes.model.Attachment;
import ar.com.delellis.quicknotes.model.AttachmentInfo;
import ar.com.delellis.quicknotes.model.Note;
import ar.com.delellis.quicknotes.model.Tag;
import ar.com.delellis.quicknotes.shared.AttachmentAdapter;
import ar.com.delellis.quicknotes.shared.ColorPickerDialog;
import ar.com.delellis.quicknotes.shared.ReminderPicker;
import ar.com.delellis.quicknotes.shared.ShareAdapter;
import ar.com.delellis.quicknotes.shared.TagAdapter;
import ar.com.delellis.quicknotes.util.ColorUtil;
import ar.com.delellis.quicknotes.util.DateUtil;
import ar.com.delellis.quicknotes.util.HtmlUtil;
import ar.com.delellis.quicknotes.util.InsetsUtil;
import ar.com.delellis.quicknotes.util.UploadUtil;
import okhttp3.MultipartBody;

public class EditorActivity extends AppCompatActivity implements EditorView, OnAttachOptionListener {
    private static final String TAG = EditorActivity.class.getCanonicalName();

    public static final String EXTRA_NOTE = "note";
    public static final String EXTRA_TAGS = "tags";

    private static final String KEY_ACTION_VIEW_FILE_ID = "KEY_FILE_ID";
    private static final String KEY_ACTION_VIEW_ACCOUNT = "KEY_ACCOUNT";

    private ActivityEditorBinding binding;

    private EditorPresenter presenter;

    private AttachmentAdapter attachmentAdapter;
    private TagAdapter tagAdapter;
    private ShareAdapter shareAdapter;

    private Note note = new Note();
    private Note shadowCopyNote;

    private List<Tag> tags = new ArrayList<>();
    private List<Tag> tagSelection = new ArrayList<>();

    /** Whether the share this note reaches the user through lets them write. */
    private boolean readOnly = false;

    // Temporary files to capture from the camera
    private File tempPhotoCamera = null;
    private File tempVideoCamera = null;

    private ActivityResultLauncher<Intent> tagsLauncher;
    private ActivityResultLauncher<Intent> sharesLauncher;
    private ActivityResultLauncher<String> pickFileLauncher;
    private ActivityResultLauncher<Uri> takePhotoLauncher;
    private ActivityResultLauncher<Uri> takeVideoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(binding.getRoot());

        setupActionBar();
        registerLaunchers();

        new ApiProvider(getApplicationContext());

        attachmentAdapter = new AttachmentAdapter();
        attachmentAdapter.setOnImageClickListener(position -> openAttachment(attachmentAdapter.get(position)));
        attachmentAdapter.setOnDeleteClickListener(position ->
                attachmentAdapter.removeItem(attachmentAdapter.get(position)));
        binding.editorRecyclerAttachments.setAdapter(attachmentAdapter);

        binding.editorContent.setCalypsoMode(false);

        tagAdapter = new TagAdapter();
        binding.editorRecyclerTags.setAdapter(tagAdapter);

        shareAdapter = new ShareAdapter();
        binding.editorRecyclerShares.setAdapter(shareAdapter);

        initToolbar();

        presenter = new EditorPresenter(this);

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_NOTE)) {
            note = (Note) Objects.requireNonNull(intent.getSerializableExtra(EXTRA_NOTE));
        }
        Serializable extraTags = intent.getSerializableExtra(EXTRA_TAGS);
        if (extraTags != null) {
            tags = (List<Tag>) extraTags;
        }

        showNote();

        // Store the either loaded or just created note as a copy so we can
        // compare for modifications later.
        shadowCopyNote = note.clone();

        getOnBackPressedDispatcher().addCallback(this, onBackPressed);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            return;
        }

        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setElevation(0);

        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_back_grey, null);
        if (drawable != null) {
            DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.defaultNoteTint));
            actionBar.setHomeAsUpIndicator(drawable);
        } else {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_grey);
        }
    }

    private void registerLaunchers() {
        tagsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                return;
            }
            tagSelection = (List<Tag>) Objects.requireNonNull(
                    result.getData().getSerializableExtra(TagsActivity.EXTRA_TAG_SELECTION));
            note.setTags(tagSelection);
            tagAdapter.setItems(tagSelection);
        });

        sharesLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            // Who has the note is the server's to tell; nothing of the note
            // being edited here changed.
        });

        // The picker hands back a content uri and nothing else: no storage
        // permission is asked for, and none is needed.
        pickFileLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri == null) {
                return;
            }
            try {
                presenter.uploadAttachment(UploadUtil.partFromUri(this, uri));
            } catch (IOException e) {
                Log.w(TAG, "Could not read the picked file", e);
                onRequestError(getString(R.string.error_uploading_attachment));
            }
        });

        takePhotoLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), taken -> {
            if (Boolean.TRUE.equals(taken) && tempPhotoCamera != null) {
                presenter.uploadAttachment(UploadUtil.partFromFile(tempPhotoCamera, tempPhotoCamera.getName(), "image/jpeg"));
            }
        });

        takeVideoLauncher = registerForActivityResult(new ActivityResultContracts.CaptureVideo(), taken -> {
            if (Boolean.TRUE.equals(taken) && tempVideoCamera != null) {
                presenter.uploadAttachment(UploadUtil.partFromFile(tempVideoCamera, tempVideoCamera.getName(), "video/mp4"));
            }
        });
    }

    private final OnBackPressedCallback onBackPressed = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (hasModifications()) {
                showDiscardDialog();
            } else {
                setResult(RESULT_OK);
                finish();
            }
        }
    };

    // ------------------------------------------------------------------
    // Menu
    // ------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        int tintColor = ContextCompat.getColor(this, R.color.defaultNoteTint);
        boolean isSaved = !note.isNew();

        // The pin and the reminder are the caller's own, so they are offered
        // on anybody's note; the trash belongs to whoever owns it.
        setItem(menu, R.id.pin, !readOnly, note.isPinned() ? R.drawable.ic_pinned : R.drawable.ic_pin, tintColor);
        setItem(menu, R.id.save, !readOnly, tintColor);
        setItem(menu, R.id.reminder, isSaved, tintColor);
        setItem(menu, R.id.remove_reminder, isSaved && note.hasReminder(), tintColor);
        setItem(menu, R.id.share, isSaved && (note.isOwner() || note.canReshare()), tintColor);
        setItem(menu, R.id.archive, isSaved && !note.isArchived() && !note.isTrashed(), tintColor);
        setItem(menu, R.id.unarchive, isSaved && note.isArchived(), tintColor);
        setItem(menu, R.id.trash, isSaved && note.isOwner() && !note.isTrashed(), tintColor);
        setItem(menu, R.id.restore, isSaved && note.isTrashed(), tintColor);
        setItem(menu, R.id.leave, isSaved && note.canLeave(), tintColor);

        return super.onPrepareOptionsMenu(menu);
    }

    private void setItem(Menu menu, int itemId, boolean visible, int tintColor) {
        setItem(menu, itemId, visible, 0, tintColor);
    }

    private void setItem(Menu menu, int itemId, boolean visible, int iconRes, int tintColor) {
        MenuItem item = menu.findItem(itemId);
        if (item == null) {
            return;
        }
        item.setVisible(visible);
        if (iconRes != 0) {
            item.setIcon(iconRes);
        }
        if (visible && item.getIcon() != null) {
            ColorUtil.menuItemTintColor(item, tintColor);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.pin) {
            note.setPinned(!note.isPinned());
            invalidateOptionsMenu();
            return true;
        } else if (itemId == R.id.save) {
            saveNote();
            return true;
        } else if (itemId == R.id.reminder) {
            pickReminder();
            return true;
        } else if (itemId == R.id.remove_reminder) {
            presenter.setReminder(note.getId(), null);
            return true;
        } else if (itemId == R.id.share) {
            Intent intent = new Intent(this, SharesActivity.class);
            intent.putExtra(SharesActivity.EXTRA_NOTE, note);
            sharesLauncher.launch(intent);
            return true;
        } else if (itemId == R.id.archive) {
            presenter.archiveNote(note.getId());
            return true;
        } else if (itemId == R.id.unarchive) {
            presenter.unarchiveNote(note.getId());
            return true;
        } else if (itemId == R.id.trash) {
            confirm(R.string.move_to_trash, R.string.sure_want_delete, () -> presenter.trashNote(note.getId()));
            return true;
        } else if (itemId == R.id.restore) {
            presenter.restoreNote(note.getId());
            return true;
        } else if (itemId == R.id.leave) {
            confirm(R.string.leave_note, R.string.sure_want_leave, () -> presenter.leaveNote(note.getId()));
            return true;
        } else if (itemId == android.R.id.home) {
            onBackPressed.handleOnBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveNote() {
        if (readOnly) {
            // Nothing of this note is the caller's to write, but their pin and
            // their tags are: those are saved through the same call, which the
            // server allows on a read only share.
            setResult(RESULT_OK);
            finish();
            return;
        }

        fetchDataToNoteObject();

        if (note.getTitle().isEmpty()) {
            binding.editorTitle.setError(getString(R.string.must_enter_title));
            return;
        }

        if (note.isNew()) {
            presenter.createNote(note);
        } else {
            presenter.updateNote(note);
        }
    }

    private void confirm(int title, int message, Runnable action) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.common_yes, (dialog, which) -> {
                    dialog.dismiss();
                    action.run();
                })
                .setNegativeButton(R.string.common_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void pickReminder() {
        ReminderPicker.pick(this, note.getReminderAt(), (reminderAt, isInTheFuture) -> {
            if (!isInTheFuture) {
                Toast.makeText(this, R.string.reminder_must_be_in_the_future, Toast.LENGTH_LONG).show();
                return;
            }
            presenter.setReminder(note.getId(), reminderAt);
        });
    }

    private void fetchDataToNoteObject() {
        // Clean html from view and update note to save.
        note.setTitle(HtmlUtil.cleanString(binding.editorTitle.getText().toString()));
        note.setContent(HtmlUtil.cleanHtml(binding.editorContent.toPlainHtml(false)));
        note.setTags(tagSelection);
        note.setAttachments(attachmentAdapter.getItems());
    }

    private boolean hasModifications() {
        if (readOnly) {
            return false;
        }
        fetchDataToNoteObject();
        return !note.equals(shadowCopyNote);
    }

    private void showDiscardDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.note_confirm_discard_unsaved_changes_title)
                .setMessage(R.string.note_confirm_discard_unsaved_changes_text)
                .setPositiveButton(R.string.common_yes, (dialog, which) -> {
                    dialog.dismiss();
                    setResult(RESULT_CANCELED);
                    finish();
                })
                .setNegativeButton(R.string.common_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void initToolbar() {
        binding.editorToolbar.actionBold.setOnClickListener(view ->
                binding.editorContent.toggleFormatting(AztecTextFormat.FORMAT_BOLD));
        binding.editorToolbar.actionItalic.setOnClickListener(view ->
                binding.editorContent.toggleFormatting(AztecTextFormat.FORMAT_ITALIC));
        binding.editorToolbar.actionUnderline.setOnClickListener(view ->
                binding.editorContent.toggleFormatting(AztecTextFormat.FORMAT_UNDERLINE));
        binding.editorToolbar.actionStrike.setOnClickListener(view ->
                binding.editorContent.toggleFormatting(AztecTextFormat.FORMAT_STRIKETHROUGH));
        binding.editorToolbar.actionQuote.setOnClickListener(view ->
                binding.editorContent.toggleFormatting(AztecTextFormat.FORMAT_QUOTE));
        binding.editorToolbar.actionNumberedList.setOnClickListener(view ->
                binding.editorContent.toggleFormatting(AztecTextFormat.FORMAT_ORDERED_LIST));
        binding.editorToolbar.actionBulletedList.setOnClickListener(view ->
                binding.editorContent.toggleFormatting(AztecTextFormat.FORMAT_UNORDERED_LIST));

        binding.editorToolbar.actionNoteColor.setOnClickListener(view -> showColorPicker());
        binding.editorToolbar.actionAttach.setOnClickListener(view -> showAttachOptions());
        binding.editorToolbar.actionTags.setOnClickListener(view -> showTagsSelection());
    }

    // ------------------------------------------------------------------
    // EditorView
    // ------------------------------------------------------------------

    @Override
    public void showProgress() {
        binding.editorProgress.setVisibility(VISIBLE);
    }

    @Override
    public void hideProgress() {
        binding.editorProgress.setVisibility(GONE);
    }

    @Override
    public void addAttachment(AttachmentInfo info) {
        attachmentAdapter.addItem(Attachment.fromInfo(info));

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
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onRequestError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNoteUpdated(Note updated) {
        // Whatever is half written here stays: only what the call actually
        // changed is taken from the answer.
        note.mergeServerState(updated);
        shareAdapter.setItems(note.getSharedWith());
        showReminder();
        invalidateOptionsMenu();
    }

    /**
     * Somebody else saved this note while it was open here. Neither version is
     * the right one to pick for the user, so both are offered: keeping theirs
     * saves on top of what is on the server now, taking theirs throws away
     * what was written here.
     */
    @Override
    public void onConflict(Note current) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.conflict_title)
                .setMessage(R.string.conflict_message)
                .setCancelable(false)
                .setPositiveButton(R.string.conflict_keep_mine, (dialog, which) -> {
                    dialog.dismiss();
                    note.setEtag(current.getEtag());
                    presenter.updateNote(note);
                })
                .setNegativeButton(R.string.conflict_take_theirs, (dialog, which) -> {
                    dialog.dismiss();
                    onNoteUpdated(current);
                })
                .show();
    }

    // ------------------------------------------------------------------
    // Attaching
    // ------------------------------------------------------------------

    @Override
    public void onAttachOptionSelection(int attachOption) {
        switch (attachOption) {
            case ATTACH_ADD_FILE:
                pickFileLauncher.launch("*/*");
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
        new AttachBottomSheetDialog().show(getSupportFragmentManager(), "AttachBottomSheetDialog");
    }

    private void takePhoto() {
        tempPhotoCamera = createTempFile("IMG_CAMERA_", ".jpg");
        if (tempPhotoCamera != null) {
            takePhotoLauncher.launch(uriFor(tempPhotoCamera));
        }
    }

    private void takeVideo() {
        tempVideoCamera = createTempFile("VID_CAMERA_", ".mp4");
        if (tempVideoCamera != null) {
            takeVideoLauncher.launch(uriFor(tempVideoCamera));
        }
    }

    @Nullable
    private File createTempFile(String prefix, String suffix) {
        try {
            return File.createTempFile(prefix, suffix, getCacheDir());
        } catch (IOException e) {
            Log.w(TAG, "Could not make room for the capture", e);
            onRequestError(getString(R.string.error_uploading_attachment));
            return null;
        }
    }

    private Uri uriFor(File file) {
        return FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileProvider", file);
    }

    private void openAttachment(Attachment attachment) {
        String url = attachment.getLinkUrl();
        if (url == null || url.isEmpty()) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.putExtra(KEY_ACTION_VIEW_FILE_ID, String.valueOf(attachment.getFileId()));
        intent.putExtra(KEY_ACTION_VIEW_ACCOUNT, ApiProvider.getUsername());
        try {
            startActivity(intent);
        } catch (Exception e) {
            Log.w(TAG, "Nothing could open " + url, e);
            onRequestError(getString(R.string.error_unknown));
        }
    }

    private void showTagsSelection() {
        Intent intent = new Intent(this, TagsActivity.class);
        intent.putExtra(TagsActivity.EXTRA_TAGS, (Serializable) tags);
        intent.putExtra(TagsActivity.EXTRA_TAG_SELECTION, (Serializable) tagSelection);
        tagsLauncher.launch(intent);
    }

    private void showColorPicker() {
        ColorPickerDialog.show(this, note.getColor(), (color, colorInt) -> {
            note.setColor(color);
            tintActivityColor(colorInt);
        });
    }

    // ------------------------------------------------------------------
    // Filling the screen
    // ------------------------------------------------------------------

    private void showNote() {
        if (note.isNew()) {
            int defaultColor = ContextCompat.getColor(this, R.color.defaultNoteColor);
            note.setColor(ColorUtil.getRGBColorFromInt(defaultColor));
            tintActivityColor(defaultColor);

            binding.editorTitle.requestFocus();
            editMode();
            return;
        }

        readOnly = !note.canEdit();

        attachmentAdapter.setDisableDeletion(readOnly);
        attachmentAdapter.setItems(new ArrayList<>(note.getAttachments()));

        note.setTitle(HtmlUtil.cleanString(note.getTitle()));
        binding.editorTitle.setText(note.getTitle());

        note.setContent(HtmlUtil.cleanHtml(note.getContent()));
        binding.editorContent.fromHtml(note.getContent(), true);

        tintActivityColor(ColorUtil.parseColorOr(note.getColor(),
                ContextCompat.getColor(this, R.color.defaultNoteColor)));

        tagSelection = note.getTags();
        tagAdapter.setItems(tagSelection);
        shareAdapter.setItems(note.getSharedWith());

        showReminder();

        if (readOnly) {
            readMode();
        } else {
            editMode();
        }
    }

    private void showReminder() {
        String reminder = note.hasReminder() ? DateUtil.toLocalDisplay(note.getReminderAt()) : null;
        if (reminder == null) {
            binding.editorReminderBar.setVisibility(GONE);
            return;
        }

        binding.editorReminderText.setText(getString(
                note.isReminderPending() ? R.string.reminder_at : R.string.reminder_notified, reminder));
        binding.editorReminderBar.setVisibility(VISIBLE);
    }

    private void editMode() {
        binding.editorTitle.setFocusableInTouchMode(true);
        binding.editorContent.setFocusableInTouchMode(true);
        binding.editorRichToolbar.setVisibility(VISIBLE);

        // The colour is a property of the note, and the note is its owner's.
        binding.editorToolbar.actionNoteColor.setVisibility(note.isNew() || note.isOwner() ? VISIBLE : GONE);
    }

    private void readMode() {
        binding.editorTitle.setFocusableInTouchMode(false);
        binding.editorContent.setFocusableInTouchMode(false);
        binding.editorTitle.setFocusable(false);
        binding.editorContent.setFocusable(false);
        binding.editorRichToolbar.setVisibility(GONE);
    }

    private void tintActivityColor(int noteColor) {
        binding.editorContent.getRootView().setBackgroundColor(noteColor);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(noteColor));
        }
    }
}
