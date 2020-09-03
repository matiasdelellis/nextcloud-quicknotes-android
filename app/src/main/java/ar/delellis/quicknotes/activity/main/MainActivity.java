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
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.appcompat.widget.SearchView;

import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ar.delellis.quicknotes.R;
import ar.delellis.quicknotes.activity.editor.EditorActivity;
import ar.delellis.quicknotes.activity.main.NavigationAdapter.NavigationItem;
import ar.delellis.quicknotes.activity.main.NavigationAdapter.TagNavigationItem;
import ar.delellis.quicknotes.api.ApiProvider;
import ar.delellis.quicknotes.model.Note;
import ar.delellis.quicknotes.model.Tag;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity implements MainView {

    private static final int INTENT_ADD = 100;
    private static final int INTENT_EDIT = 200;

    public static final String ADAPTER_KEY_ALL = "all_notes";
    public static final String ADAPTER_KEY_SHARED_BY = "shared_by";
    public static final String ADAPTER_KEY_SHARED_WITH = "shared_with";
    public static final String ADAPTER_KEY_TAG_PREFIX = "tag:";
    public static final String ADAPTER_KEY_ABOUT = "about";
    public static final String ADAPTER_KEY_LOGOUT = "logout";

    DrawerLayout drawerLayout;
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private SearchView searchView;
    private MaterialCardView homeToolbar;
    private Toolbar toolbar;
    private AppBarLayout appBar;

    private MainPresenter presenter;
    private MainAdapter adapter;
    private MainAdapter.ItemClickListener itemClickListener;

    NavigationAdapter navigationFilterAdapter;
    NavigationAdapter navigationCommonAdapter;

    private List<Note> notes;
    private List<Tag> tags;
    private List<String> colors;

    private  ApiProvider mApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefresh = findViewById(R.id.swipe_refresh);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tags = new ArrayList<>();

        presenter = new MainPresenter(this);

        swipeRefresh.setOnRefreshListener(
                () -> presenter.getData()
        );

        itemClickListener = ((view, position) -> {
            int id = notes.get(position).getId();
            String title = notes.get(position).getTitle();
            String content = notes.get(position).getContent();
            String color = notes.get(position).getColor();
            boolean is_shared = notes.get(position).getIsShared();

            Intent intent = new Intent(this, EditorActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("title", title);
            intent.putExtra("content", content);
            intent.putExtra("color", color);
            intent.putExtra("is_shared", is_shared);

            startActivityForResult(intent, INTENT_EDIT);
        });

        fab = findViewById(R.id.add);
        fab.setOnClickListener(view ->
                startActivityForResult(
                        new Intent(this, EditorActivity.class),
                        INTENT_ADD)
        );

        appBar = findViewById(R.id.appBar);
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
                adapter.getFilter().filter(query);
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

        drawerLayout = findViewById(R.id.drawerLayout);
        AppCompatImageButton menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        mApi = new ApiProvider(getApplicationContext());
        presenter.getData();
    }

    private void setupNavigationMenu() {
        ArrayList<NavigationItem> navItems = new ArrayList<>();

        navigationFilterAdapter = new NavigationAdapter(this, item -> {
            if (item.id == ADAPTER_KEY_ALL) {
                adapter.getFilter().filter("");
            } else if (item.id == ADAPTER_KEY_SHARED_BY) {
                adapter.getIsSharedFilter().filter("");
            } else if (item.id == ADAPTER_KEY_SHARED_WITH) {
                adapter.getIsSharedFilter().filter("");
            } else if (item.id.startsWith(ADAPTER_KEY_TAG_PREFIX)) {
                adapter.getTagFilter().filter(item.label);
            }
            navigationFilterAdapter.setSelectedItem(item.id);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        RecyclerView navigationMenuFilter = findViewById(R.id.navigationFilter);
        navigationMenuFilter.setAdapter(navigationFilterAdapter);

        navigationCommonAdapter = new NavigationAdapter(this, item -> {
            //TODO:
            Toast.makeText(MainActivity.this, "Selected " + item.label, Toast.LENGTH_SHORT).show();
        });

        navItems.add(new NavigationItem(ADAPTER_KEY_ABOUT, "About", NavigationAdapter.ICON_INFO));
        navItems.add(new NavigationItem(ADAPTER_KEY_LOGOUT, "Logout", NavigationAdapter.ICON_LOGOUT));
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
            if (note.getIsShared()) {
                navItems.add(new NavigationItem(ADAPTER_KEY_SHARED_BY, getString(R.string.shared_with_you), NavigationAdapter.ICON_SHARED));
                break;
            }
        }

        // TODO:
        //navItems.add(new NavigationItem(ADAPTER_KEY_SHARED_WITH, getString(R.string.shared_with_others), NavigationAdapter.ICON_SHARED));

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
            adapter = new MainAdapter(getApplicationContext(), note_list, itemClickListener);
            adapter.notifyDataSetChanged();
            recyclerView.setAdapter(adapter);

            tags.clear();
            for (Note note: note_list) {
                tags.addAll(note.getTags());
            }
            HashSet<Tag> hTags = new HashSet<>(tags);
            tags.clear();
            tags.addAll(hTags);

            notes = note_list;

            updateNavigationMenu();
        });
    }

    @Override
    public void onErrorLoading(String message) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
    }
}