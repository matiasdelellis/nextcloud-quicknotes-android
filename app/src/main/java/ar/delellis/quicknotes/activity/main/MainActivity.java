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

package ar.delellis.quicknotes.activity.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.appcompat.widget.SearchView;

import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nextcloud.android.sso.helper.SingleAccountHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ar.delellis.quicknotes.R;
import ar.delellis.quicknotes.activity.LoginActivity;
import ar.delellis.quicknotes.activity.editor.EditorActivity;
import ar.delellis.quicknotes.activity.main.NavigationAdapter.NavigationItem;
import ar.delellis.quicknotes.activity.main.NavigationAdapter.TagNavigationItem;
import ar.delellis.quicknotes.activity.main.SortingOrderDialogFragment.OnSortingOrderListener;
import ar.delellis.quicknotes.api.ApiProvider;
import ar.delellis.quicknotes.model.Note;
import ar.delellis.quicknotes.model.Tag;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity implements MainView, OnSortingOrderListener {

    private static final int INTENT_ADD = 100;
    private static final int INTENT_EDIT = 200;

    public static final String ADAPTER_KEY_ALL = "all_notes";
    public static final String ADAPTER_KEY_PINNED = "pinned";
    public static final String ADAPTER_KEY_SHARED_BY = "shared_by";
    public static final String ADAPTER_KEY_SHARED_WITH = "shared_with";
    public static final String ADAPTER_KEY_TAG_PREFIX = "tag:";
    public static final String ADAPTER_KEY_ABOUT = "about";
    public static final String ADAPTER_KEY_SWITCH_ACCOUNT = "switch_account";

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private MaterialCardView homeToolbar;
    private SearchView searchView;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private StaggeredGridLayoutManager layoutManager;
    private FloatingActionButton fab;

    private MainPresenter presenter;
    private NoteAdapter noteAdapter;
    private NoteAdapter.ItemClickListener itemClickListener;

    NavigationAdapter navigationFilterAdapter;
    NavigationAdapter navigationCommonAdapter;

    private List<Note> notes = new ArrayList<>();
    private List<Tag> tags = new ArrayList<>();
    private List<String> colors = new ArrayList<>();

    private ApiProvider mApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);
        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        presenter = new MainPresenter(this);

        noteAdapter = new NoteAdapter(getApplicationContext(), notes, itemClickListener);
        noteAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(noteAdapter);

        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(
                () -> presenter.getData()
        );

        itemClickListener = ((view, position) -> {
            Note note = notes.get(position);

            Intent intent = new Intent(this, EditorActivity.class);
            intent.putExtra("note", note);

            startActivityForResult(intent, INTENT_EDIT);
        });

        fab = findViewById(R.id.add);
        fab.setOnClickListener(view ->
                startActivityForResult(
                        new Intent(this, EditorActivity.class),
                        INTENT_ADD)
        );

        toolbar = findViewById(R.id.toolbar);
        homeToolbar = findViewById(R.id.home_toolbar);

        searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                noteAdapter.getFilter().filter(query);
                return false;
            }
        });

        searchView.setOnCloseListener(() -> {
            if (toolbar.getVisibility() == VISIBLE && TextUtils.isEmpty(searchView.getQuery())) {
                updateToolbars(true);
                return true;
            }
            return false;
        });

        setSupportActionBar(toolbar);
        setupNavigationMenu();

        homeToolbar.setOnClickListener(view -> updateToolbars(false));

        AppCompatImageView sortButton = findViewById(R.id.sort_mode);
        sortButton.setOnClickListener(view -> openSortingOrderDialogFragment(getSupportFragmentManager(), noteAdapter.getSortRule()));

        drawerLayout = findViewById(R.id.drawerLayout);
        AppCompatImageButton menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        AppCompatImageView viewButton = findViewById(R.id.view_mode);
        viewButton.setOnClickListener(view -> {
            int spanCount = layoutManager.getSpanCount() == 1 ? 2 : 1;
            layoutManager.setSpanCount(spanCount);
            viewButton.setImageResource(spanCount == 1 ? R.drawable.ic_view_module : R.drawable.ic_view_list);
        });

        mApi = new ApiProvider(getApplicationContext());
        presenter.getData();
    }

    private void setupNavigationMenu() {
        ArrayList<NavigationItem> navItems = new ArrayList<>();

        navigationFilterAdapter = new NavigationAdapter(this, item -> {
            if (item.id.equals(ADAPTER_KEY_ALL)) {
                noteAdapter.getFilter().filter("");
            } else if (item.id.equals(ADAPTER_KEY_PINNED)) {
                noteAdapter.getPinnedFilter().filter("");
            } else if (item.id.equals(ADAPTER_KEY_SHARED_BY)) {
                noteAdapter.getIsSharedFilter().filter("");
            } else if (item.id.equals(ADAPTER_KEY_SHARED_WITH)) {
                noteAdapter.getSharedWithOthersFilter().filter("");
            } else if (item.id.startsWith(ADAPTER_KEY_TAG_PREFIX)) {
                noteAdapter.getTagFilter().filter(item.label);
            }
            navigationFilterAdapter.setSelectedItem(item.id);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        RecyclerView navigationMenuFilter = findViewById(R.id.navigationFilter);
        navigationMenuFilter.setAdapter(navigationFilterAdapter);

        navigationCommonAdapter = new NavigationAdapter(this, item -> {
            if (item.id.equals(ADAPTER_KEY_SWITCH_ACCOUNT)) {
                switch_account();
            } else {
                Toast.makeText(MainActivity.this, "Selected " + item.label, Toast.LENGTH_SHORT).show();
            }
        });

        navItems.add(new NavigationItem(ADAPTER_KEY_ABOUT, getString(R.string.about), NavigationAdapter.ICON_INFO));
        navItems.add(new NavigationItem(ADAPTER_KEY_SWITCH_ACCOUNT, getString(R.string.switch_account), NavigationAdapter.ICON_LOGOUT));
        navigationCommonAdapter.setItems(navItems);

        RecyclerView navigationMenuCommon = findViewById(R.id.navigationCommon);

        navigationCommonAdapter.setSelectedItem(ADAPTER_KEY_ALL);
        navigationMenuCommon.setAdapter(navigationCommonAdapter);
    }

    private void updateNavigationMenu() {
        ArrayList<NavigationItem> navItems = new ArrayList<>();
        NavigationItem homeNav = new NavigationItem(ADAPTER_KEY_ALL, getString(R.string.all_notes), NavigationAdapter.ICON_HOME);
        navItems.add(homeNav);

        for (Note note: notes) {
            if (note.getIsPinned()) {
                navItems.add(new NavigationItem(ADAPTER_KEY_PINNED, getString(R.string.pinned), NavigationAdapter.ICON_PINNED));
                break;
            }
        }

        for (Note note: notes) {
            if (note.getIsShared()) {
                navItems.add(new NavigationItem(ADAPTER_KEY_SHARED_BY, getString(R.string.shared_with_you), NavigationAdapter.ICON_SHARED));
                break;
            }
        }

        for (Note note: notes) {
            if (note.getShareWith().size() > 0) {
                navItems.add(new NavigationItem(ADAPTER_KEY_SHARED_WITH, getString(R.string.shared_with_others), NavigationAdapter.ICON_SHARED));
                break;
            }
        }

        for (Tag tag: tags) {
            TagNavigationItem item = new TagNavigationItem(ADAPTER_KEY_TAG_PREFIX + tag.getId(), tag.getName(), NavigationAdapter.ICON_TAG, tag.getId());
            navItems.add(item);
        }

        navigationFilterAdapter.setSelectedItem(homeNav.id);
        navigationFilterAdapter.setItems(navItems);
    }

    private void updateToolbars(boolean disableSearch) {
        homeToolbar.setVisibility(disableSearch ? VISIBLE : GONE);
        toolbar.setVisibility(disableSearch ? GONE : VISIBLE);
        if (disableSearch) {
            searchView.setQuery(null, true);
        }
        searchView.setIconified(disableSearch);
    }

    private void switch_account() {
        SingleAccountHelper.setCurrentAccount(this, null);
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void openSortingOrderDialogFragment(FragmentManager supportFragmentManager, int sortOrder) {
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);

        SortingOrderDialogFragment.newInstance(sortOrder).show(fragmentTransaction, SortingOrderDialogFragment.SORTING_ORDER_FRAGMENT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_ADD && resultCode == RESULT_OK) {
            presenter.getData();
        } else if (requestCode == INTENT_EDIT && resultCode == RESULT_OK) {
            presenter.getData();
        }
    }

    @Override
    public void showLoading() {
        swipeRefresh.setRefreshing(true);
    }

    @Override
    public void hideLoading() {
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onGetResult(List<Note> note_list) {
        runOnUiThread(() -> {
            noteAdapter = new NoteAdapter(getApplicationContext(), note_list, itemClickListener);
            noteAdapter.notifyDataSetChanged();
            recyclerView.setAdapter(noteAdapter);

            // Fill tags.
            tags.clear();
            for (Note note: note_list) {
                tags.addAll(note.getTags());
            }
            HashSet<Tag> hTags = new HashSet<>(tags);
            tags.clear();
            tags.addAll(hTags);

            // Fill colors
            colors.clear();
            for (Note note: note_list) {
                colors.add(note.getColor());
            }
            HashSet<String> hColors = new HashSet<>(colors);
            colors.clear();
            colors.addAll(hColors);

            notes = note_list;

            // Update nav bar.
            updateNavigationMenu();
        });
    }

    @Override
    public void onErrorLoading(String message) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onSortingOrderChosen(int sortSelection) {
        AppCompatImageView sortButton = findViewById(R.id.sort_mode);
        switch (sortSelection) {
            case NoteAdapter.SORT_BY_TITLE:
                sortButton.setImageResource(R.drawable.ic_alphabetical_asc);
                break;
            case NoteAdapter.SORT_BY_CREATED:
            case NoteAdapter.SORT_BY_UPDATED:
                sortButton.setImageResource(R.drawable.ic_modification_asc);
                break;
        }
        noteAdapter.setSortRule(sortSelection);
        noteAdapter.notifyDataSetChanged();
    }

}