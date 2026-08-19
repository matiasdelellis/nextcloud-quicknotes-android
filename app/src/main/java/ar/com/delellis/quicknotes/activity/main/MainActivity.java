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

package ar.com.delellis.quicknotes.activity.main;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static ar.com.delellis.quicknotes.activity.main.NoteAdapter.SORT_BY_CREATED;
import static ar.com.delellis.quicknotes.activity.main.NoteAdapter.SORT_BY_TITLE;
import static ar.com.delellis.quicknotes.activity.main.NoteAdapter.SORT_BY_UPDATED;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nextcloud.android.sso.helper.SingleAccountHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ar.com.delellis.quicknotes.BuildConfig;
import ar.com.delellis.quicknotes.R;
import ar.com.delellis.quicknotes.activity.about.AboutActivity;
import ar.com.delellis.quicknotes.activity.editor.EditorActivity;
import ar.com.delellis.quicknotes.activity.error.ErrorActivity;
import ar.com.delellis.quicknotes.activity.login.LoginActivity;
import ar.com.delellis.quicknotes.activity.main.NavigationAdapter.NavigationItem;
import ar.com.delellis.quicknotes.activity.main.NavigationAdapter.TagNavigationItem;
import ar.com.delellis.quicknotes.activity.main.SortingOrderDialogFragment.OnSortingOrderListener;
import ar.com.delellis.quicknotes.activity.shares.SharesActivity;
import ar.com.delellis.quicknotes.api.ApiProvider;
import ar.com.delellis.quicknotes.api.helper.IResponseCallback;
import ar.com.delellis.quicknotes.databinding.ActivityListViewBinding;
import ar.com.delellis.quicknotes.databinding.ActivityMainBinding;
import ar.com.delellis.quicknotes.model.Note;
import ar.com.delellis.quicknotes.model.Tag;
import ar.com.delellis.quicknotes.shared.ColorPickerDialog;
import ar.com.delellis.quicknotes.shared.NoteActionsDialog;
import ar.com.delellis.quicknotes.shared.ReminderPicker;
import androidx.core.content.ContextCompat;

import ar.com.delellis.quicknotes.util.CapabilitiesService;
import ar.com.delellis.quicknotes.util.ColorUtil;
import ar.com.delellis.quicknotes.util.DateUtil;
import ar.com.delellis.quicknotes.util.InsetsUtil;

public class MainActivity extends AppCompatActivity implements MainView, OnSortingOrderListener,
        NoteAdapter.ItemClickListener, NoteActionsDialog.Callback {

    public static final String ADAPTER_KEY_ALL = "all_notes";
    public static final String ADAPTER_KEY_PINNED = "pinned";
    public static final String ADAPTER_KEY_SHARED_BY = "shared_by";
    public static final String ADAPTER_KEY_SHARED_WITH = "shared_with";
    public static final String ADAPTER_KEY_REMINDERS = "reminders";
    public static final String ADAPTER_KEY_ARCHIVED = "archived";
    public static final String ADAPTER_KEY_TRASH = "trash";
    public static final String ADAPTER_KEY_TAG_PREFIX = "tag:";
    public static final String ADAPTER_KEY_COLORS = "colors";
    public static final String ADAPTER_KEY_ABOUT = "about";
    public static final String ADAPTER_KEY_DONATE = "donate";
    public static final String ADAPTER_KEY_SWITCH_ACCOUNT = "switch_account";

    private ActivityMainBinding binding;
    private ActivityListViewBinding listBinding;

    private SharedPreferences preferences;

    private StaggeredGridLayoutManager layoutManager;

    private MainPresenter presenter;
    private NoteAdapter noteAdapter;

    private NavigationAdapter navigationFilterAdapter;
    private NavigationAdapter navigationCommonAdapter;

    private final List<Tag> tags = new ArrayList<>();

    /** The colours actually on the board, for the colour filter. */
    private final List<String> colors = new ArrayList<>();

    private ActivityResultLauncher<Intent> editorLauncher;
    private ActivityResultLauncher<Intent> sharesLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        listBinding = binding.activityListView;
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(listBinding.getRoot());

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        int sortRule = preferences.getInt(getString(R.string.setting_sort_by), SORT_BY_UPDATED);
        boolean pinnedFirst = preferences.getBoolean(getString(R.string.setting_pinned_first), true);
        boolean gridViewEnabled = preferences.getBoolean(getString(R.string.setting_grid_view_enabled), true);

        registerLaunchers();

        layoutManager = new StaggeredGridLayoutManager(gridViewEnabled ? 2 : 1, StaggeredGridLayoutManager.VERTICAL);
        listBinding.recyclerView.setLayoutManager(layoutManager);

        presenter = new MainPresenter(this);

        noteAdapter = new NoteAdapter(this, this);
        noteAdapter.setSortRule(sortRule);
        noteAdapter.setFirstPinned(pinnedFirst);
        noteAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                updateEmptyState();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                updateEmptyState();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                updateEmptyState();
            }
        });
        listBinding.recyclerView.setAdapter(noteAdapter);

        listBinding.swipeRefresh.setOnRefreshListener(() -> presenter.getNotes());

        listBinding.add.setOnClickListener(view -> {
            Intent intent = new Intent(this, EditorActivity.class);
            intent.putExtra(EditorActivity.EXTRA_TAGS, (Serializable) tags);
            editorLauncher.launch(intent);
        });

        listBinding.emptyTrash.setOnClickListener(view -> confirmEmptyTrash());

        setupSearch();

        setSupportActionBar(listBinding.toolbar);
        setupNavigationMenu();

        listBinding.homeToolbar.setOnClickListener(view -> updateToolbars(false));
        listBinding.sortMode.setOnClickListener(view ->
                openSortingOrderDialogFragment(getSupportFragmentManager(), noteAdapter.getSortRule()));
        listBinding.menuButton.setOnClickListener(view -> binding.drawerLayout.openDrawer(GravityCompat.START));
        listBinding.viewMode.setOnClickListener(view -> onGridIconChosen(layoutManager.getSpanCount() == 1));

        updateSortingIcon(sortRule);
        updateGridIcon(gridViewEnabled);

        new ApiProvider(getApplicationContext());
        if (!ApiProvider.isReady()) {
            showError(getString(R.string.error_no_account));
            return;
        }

        checkServerSupport();
        presenter.getNotes();
    }

    private void registerLaunchers() {
        editorLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                presenter.getNotes();
            }
        });
        sharesLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            // Sharing a note changes what the list has to show about it, and
            // sharing it with oneself is not a thing, so a plain reload does.
            presenter.getNotes();
        });
    }

    private void setupSearch() {
        listBinding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                noteAdapter.setQuery(query);
                return false;
            }
        });

        listBinding.searchView.setOnCloseListener(() -> {
            if (listBinding.toolbar.getVisibility() == VISIBLE && TextUtils.isEmpty(listBinding.searchView.getQuery())) {
                updateToolbars(true);
                return true;
            }
            return false;
        });
    }

    /**
     * An older server answers a different shape on shares and knows nothing
     * about reminders, archiving or the trash, so there is nothing sensible to
     * show until it is updated.
     */
    private void checkServerSupport() {
        CapabilitiesService capabilitiesService = new CapabilitiesService(this);
        capabilitiesService.refresh(new IResponseCallback() {
            @Override
            public void onComplete() {
                String message = capabilitiesService.getSupportMessage();
                if (message != null) {
                    showError(message);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                // Nothing to say yet: reading the notes is what will tell.
                throwable.printStackTrace();
            }
        });
    }

    private void setupNavigationMenu() {
        navigationFilterAdapter = new NavigationAdapter(this, item -> {
            if (item.id.equals(ADAPTER_KEY_ALL)) {
                noteAdapter.setScope(NoteAdapter.SCOPE_ALL);
            } else if (item.id.equals(ADAPTER_KEY_PINNED)) {
                noteAdapter.setScope(NoteAdapter.SCOPE_PINNED);
            } else if (item.id.equals(ADAPTER_KEY_SHARED_BY)) {
                noteAdapter.setScope(NoteAdapter.SCOPE_SHARED_WITH_ME);
            } else if (item.id.equals(ADAPTER_KEY_SHARED_WITH)) {
                noteAdapter.setScope(NoteAdapter.SCOPE_SHARED_BY_ME);
            } else if (item.id.equals(ADAPTER_KEY_REMINDERS)) {
                noteAdapter.setScope(NoteAdapter.SCOPE_REMINDERS);
            } else if (item.id.equals(ADAPTER_KEY_ARCHIVED)) {
                noteAdapter.setScope(NoteAdapter.SCOPE_ARCHIVED);
            } else if (item.id.equals(ADAPTER_KEY_TRASH)) {
                noteAdapter.setScope(NoteAdapter.SCOPE_TRASH);
            } else if (item.id.equals(ADAPTER_KEY_COLORS)) {
                // Which colour is not something a single entry can say, so it
                // is asked for, and only then does the board change.
                binding.drawerLayout.closeDrawer(GravityCompat.START);
                showColorFilter();
                return;
            } else if (item.id.startsWith(ADAPTER_KEY_TAG_PREFIX)) {
                noteAdapter.setTagScope(item.label);
            }
            navigationFilterAdapter.setSelectedItem(item.id);
            updateScopeChrome();
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        });
        binding.navigationFilter.setAdapter(navigationFilterAdapter);

        navigationCommonAdapter = new NavigationAdapter(this, item -> {
            switch (item.id) {
                case ADAPTER_KEY_ABOUT:
                    startActivity(new Intent(this, AboutActivity.class));
                    break;
                case ADAPTER_KEY_DONATE:
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_donate))));
                    break;
                case ADAPTER_KEY_SWITCH_ACCOUNT:
                    switchAccount();
                    break;
            }
        });

        ArrayList<NavigationItem> navItems = new ArrayList<>();
        navItems.add(new NavigationItem(ADAPTER_KEY_ABOUT, getString(R.string.about), NavigationAdapter.ICON_INFO));
        if (!BuildConfig.FLAVOR.equals("play"))
            navItems.add(new NavigationItem(ADAPTER_KEY_DONATE, getString(R.string.donate), NavigationAdapter.ICON_FAVORITE));
        navItems.add(new NavigationItem(ADAPTER_KEY_SWITCH_ACCOUNT, getString(R.string.switch_account), NavigationAdapter.ICON_LOGOUT));
        navigationCommonAdapter.setItems(navItems);

        // The starting selection belongs to the filter list, not to this one.
        navigationFilterAdapter.setSelectedItem(ADAPTER_KEY_ALL);
        binding.navigationCommon.setAdapter(navigationCommonAdapter);
    }

    /**
     * The drawer only offers what there is something behind: no archive entry
     * when nothing is archived, no trash entry when the trash is empty.
     */
    private void updateNavigationMenu(List<Note> notes) {
        collectTagsAndColors(notes);

        ArrayList<NavigationItem> navItems = new ArrayList<>();
        navItems.add(new NavigationItem(ADAPTER_KEY_ALL, getString(R.string.all_notes), NavigationAdapter.ICON_HOME));

        boolean anyPinned = false;
        boolean anySharedWithMe = false;
        boolean anySharedByMe = false;
        boolean anyReminder = false;
        boolean anyArchived = false;
        boolean anyTrashed = false;

        for (Note note : notes) {
            if (note.isTrashed()) {
                anyTrashed = true;
                continue;
            }
            if (note.isArchived()) {
                anyArchived = true;
                continue;
            }
            anyPinned |= note.isPinned();
            anySharedWithMe |= note.isSharedWithMe();
            anySharedByMe |= note.isSharedByMe();
            anyReminder |= note.hasReminder();
        }

        if (anyPinned)
            navItems.add(new NavigationItem(ADAPTER_KEY_PINNED, getString(R.string.pinned), NavigationAdapter.ICON_PINNED));
        if (anySharedWithMe)
            navItems.add(new NavigationItem(ADAPTER_KEY_SHARED_BY, getString(R.string.shared_with_you), NavigationAdapter.ICON_SHARED));
        if (anySharedByMe)
            navItems.add(new NavigationItem(ADAPTER_KEY_SHARED_WITH, getString(R.string.shared_with_others), NavigationAdapter.ICON_SHARED));
        if (anyReminder)
            navItems.add(new NavigationItem(ADAPTER_KEY_REMINDERS, getString(R.string.with_reminder), NavigationAdapter.ICON_REMINDER));

        // Only worth offering when there is something to tell apart by it.
        if (colors.size() > 1) {
            String selected = noteAdapter.getColorScope();
            navItems.add(new NavigationAdapter.ColorNavigationItem(ADAPTER_KEY_COLORS,
                    getString(R.string.colors), R.drawable.ic_color_circle,
                    ColorUtil.parseColorOr(selected, ContextCompat.getColor(this, R.color.defaultBrand))));
        }

        for (Tag tag : tags) {
            navItems.add(new TagNavigationItem(ADAPTER_KEY_TAG_PREFIX + tag.getId(), tag.getName(),
                    NavigationAdapter.ICON_TAG, tag.getId()));
        }

        if (anyArchived)
            navItems.add(new NavigationItem(ADAPTER_KEY_ARCHIVED, getString(R.string.archived), NavigationAdapter.ICON_ARCHIVE));
        if (anyTrashed)
            navItems.add(new NavigationItem(ADAPTER_KEY_TRASH, getString(R.string.trash), NavigationAdapter.ICON_TRASH));

        // Whatever was being looked at may not be on the list any more.
        String selected = navigationFilterAdapter.getSelectedItem();
        boolean stillThere = false;
        for (NavigationItem item : navItems) {
            stillThere |= item.id.equals(selected);
        }
        if (!stillThere) {
            noteAdapter.setScope(NoteAdapter.SCOPE_ALL);
            navigationFilterAdapter.setSelectedItem(ADAPTER_KEY_ALL);
            updateScopeChrome();
        }

        navigationFilterAdapter.setItems(navItems);
    }

    /** The tags and the colours of the drawer are whatever the board carries. */
    private void collectTagsAndColors(List<Note> notes) {
        Set<Tag> uniqueTags = new LinkedHashSet<>();
        Set<String> uniqueColors = new LinkedHashSet<>();

        for (Note note : notes) {
            if (note.isTrashed() || note.isArchived()) {
                continue;
            }
            uniqueTags.addAll(note.getTags());
            if (note.getColor() != null) {
                uniqueColors.add(note.getColor());
            }
        }

        tags.clear();
        tags.addAll(uniqueTags);
        colors.clear();
        colors.addAll(uniqueColors);
    }

    /** The trash is not a place to add notes to, but it is one to empty. */
    private void updateScopeChrome() {
        boolean inTrash = noteAdapter.getScope() == NoteAdapter.SCOPE_TRASH;
        listBinding.add.setVisibility(inTrash ? GONE : VISIBLE);
        listBinding.emptyTrash.setVisibility(inTrash ? VISIBLE : GONE);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (noteAdapter.getItemCount() > 0) {
            listBinding.emptyState.setVisibility(GONE);
            return;
        }

        int message;
        if (!TextUtils.isEmpty(listBinding.searchView.getQuery())) {
            message = R.string.no_notes_found;
        } else if (noteAdapter.getScope() == NoteAdapter.SCOPE_TRASH) {
            message = R.string.trash_is_empty;
        } else {
            message = R.string.no_notes_yet;
        }
        listBinding.emptyState.setText(message);
        listBinding.emptyState.setVisibility(VISIBLE);
    }

    private void updateToolbars(boolean disableSearch) {
        listBinding.homeToolbar.setVisibility(disableSearch ? VISIBLE : GONE);
        listBinding.toolbar.setVisibility(disableSearch ? GONE : VISIBLE);
        if (disableSearch) {
            listBinding.searchView.setQuery(null, true);
        }
        listBinding.searchView.setIconified(disableSearch);
    }

    private void switchAccount() {
        SingleAccountHelper.commitCurrentAccount(this, null);
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }

    private void openSortingOrderDialogFragment(FragmentManager supportFragmentManager, int sortOrder) {
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);

        SortingOrderDialogFragment.newInstance(sortOrder).show(fragmentTransaction, SortingOrderDialogFragment.SORTING_ORDER_FRAGMENT);
    }

    /** Picks a colour out of the ones on the board, and shows only those. */
    private void showColorFilter() {
        if (colors.isEmpty()) {
            return;
        }
        ColorPickerDialog.show(this, R.string.filter_by_color, colors, noteAdapter.getColorScope(),
                (color, colorInt) -> {
                    noteAdapter.setColorScope(color);
                    navigationFilterAdapter.setSelectedItem(ADAPTER_KEY_COLORS);
                    updateNavigationMenu(noteAdapter.getNoteList());
                    updateScopeChrome();
                });
    }

    private void confirmEmptyTrash() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.empty_trash)
                .setMessage(R.string.sure_want_empty_trash)
                .setPositiveButton(R.string.common_yes, (dialog, which) -> {
                    dialog.dismiss();
                    presenter.emptyTrash();
                })
                .setNegativeButton(R.string.common_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showError(String errorMessage) {
        Intent intent = new Intent(getApplicationContext(), ErrorActivity.class);
        intent.putExtra(ErrorActivity.EXTRA_ERROR_MESSAGE, errorMessage);
        startActivity(intent);
        finish();
    }

    // ------------------------------------------------------------------
    // What a note can be asked to do from the list
    // ------------------------------------------------------------------

    @Override
    public void onItemClick(@NonNull Note note) {
        // A note in the trash is not open for reading: what it offers is
        // coming back, or going for good.
        if (note.isTrashed()) {
            NoteActionsDialog.show(this, note, false, this);
            return;
        }
        onOpen(note);
    }

    @Override
    public void onItemLongClick(@NonNull Note note) {
        NoteActionsDialog.show(this, note, !note.isTrashed(), this);
    }

    @Override
    public void onOpen(@NonNull Note note) {
        Intent intent = new Intent(this, EditorActivity.class);
        intent.putExtra(EditorActivity.EXTRA_NOTE, note);
        intent.putExtra(EditorActivity.EXTRA_TAGS, (Serializable) tags);
        editorLauncher.launch(intent);
    }

    @Override
    public void onTogglePin(@NonNull Note note) {
        presenter.setPinned(note, !note.isPinned());
    }

    @Override
    public void onSetReminder(@NonNull Note note) {
        ReminderPicker.pick(this, note.getReminderAt(), (reminderAt, isInTheFuture) -> {
            if (!isInTheFuture) {
                Toast.makeText(this, R.string.reminder_must_be_in_the_future, Toast.LENGTH_LONG).show();
                return;
            }
            presenter.setReminder(note, reminderAt);
            Toast.makeText(this, getString(R.string.reminder_set_for, DateUtil.toLocalDisplay(reminderAt)),
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onRemoveReminder(@NonNull Note note) {
        presenter.setReminder(note, null);
        Toast.makeText(this, R.string.reminder_removed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShare(@NonNull Note note) {
        Intent intent = new Intent(this, SharesActivity.class);
        intent.putExtra(SharesActivity.EXTRA_NOTE, note);
        sharesLauncher.launch(intent);
    }

    @Override
    public void onToggleArchive(@NonNull Note note) {
        if (note.isArchived()) {
            presenter.unarchive(note);
        } else {
            presenter.archive(note);
        }
    }

    @Override
    public void onTrash(@NonNull Note note) {
        presenter.trash(note);
    }

    @Override
    public void onRestore(@NonNull Note note) {
        presenter.restore(note);
    }

    @Override
    public void onDestroyForever(@NonNull Note note) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_forever)
                .setMessage(R.string.sure_want_destroy)
                .setPositiveButton(R.string.common_yes, (dialog, which) -> {
                    dialog.dismiss();
                    presenter.destroy(note);
                })
                .setNegativeButton(R.string.common_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onLeave(@NonNull Note note) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.leave_note)
                .setMessage(R.string.sure_want_leave)
                .setPositiveButton(R.string.common_yes, (dialog, which) -> {
                    dialog.dismiss();
                    presenter.leave(note);
                })
                .setNegativeButton(R.string.common_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    // ------------------------------------------------------------------
    // MainView
    // ------------------------------------------------------------------

    @Override
    public void showLoading() {
        listBinding.swipeRefresh.setRefreshing(true);
    }

    @Override
    public void hideLoading() {
        listBinding.swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onGetResult(List<Note> noteList) {
        noteAdapter.setNoteList(noteList);

        updateNavigationMenu(noteList);
        updateScopeChrome();
    }

    @Override
    public void onErrorLoading(String errorMessage) {
        CapabilitiesService capabilitiesService = new CapabilitiesService(this);
        capabilitiesService.refresh(new IResponseCallback() {
            @Override
            public void onComplete() {
                String message = capabilitiesService.getSupportMessage();
                if (message == null) {
                    message = errorMessage != null && !errorMessage.isEmpty()
                            ? errorMessage
                            : getString(R.string.error_unknown);
                }
                showError(message);
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                showError(errorMessage != null && !errorMessage.isEmpty()
                        ? errorMessage
                        : getString(R.string.error_unknown));
            }
        });
    }

    @Override
    public void onNoteUpdated(Note note) {
        noteAdapter.replaceNote(note);
        updateNavigationMenu(noteAdapter.getNoteList());
        updateScopeChrome();
    }

    @Override
    public void onNoteRemoved(int noteId) {
        noteAdapter.removeNote(noteId);
        updateNavigationMenu(noteAdapter.getNoteList());
        updateScopeChrome();
    }

    @Override
    public void onTrashEmptied(int destroyed) {
        Toast.makeText(this, getResources().getQuantityString(R.plurals.trash_emptied, destroyed, destroyed),
                Toast.LENGTH_SHORT).show();
        presenter.getNotes();
    }

    @Override
    public void onActionError(String errorMessage) {
        Toast.makeText(this, errorMessage != null ? errorMessage : getString(R.string.error_unknown),
                Toast.LENGTH_LONG).show();
    }

    // ------------------------------------------------------------------
    // Sorting and layout
    // ------------------------------------------------------------------

    @Override
    public void onSortingOrderChosen(int sortSelection) {
        noteAdapter.setSortRule(sortSelection);
        updateSortingIcon(sortSelection);

        preferences.edit().putInt(getString(R.string.setting_sort_by), sortSelection).apply();
    }

    public void updateSortingIcon(int sortSelection) {
        switch (sortSelection) {
            case SORT_BY_TITLE:
                listBinding.sortMode.setImageResource(R.drawable.ic_alphabetical_asc);
                break;
            case SORT_BY_CREATED:
            case SORT_BY_UPDATED:
                listBinding.sortMode.setImageResource(R.drawable.ic_modification_asc);
                break;
        }
    }

    public void onGridIconChosen(boolean gridEnabled) {
        layoutManager.setSpanCount(gridEnabled ? 2 : 1);
        updateGridIcon(gridEnabled);

        preferences.edit().putBoolean(getString(R.string.setting_grid_view_enabled), gridEnabled).apply();
    }

    public void updateGridIcon(boolean gridEnabled) {
        listBinding.viewMode.setImageResource(gridEnabled ? R.drawable.ic_view_list : R.drawable.ic_view_module);
    }
}
