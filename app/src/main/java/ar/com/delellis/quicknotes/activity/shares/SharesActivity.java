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

package ar.com.delellis.quicknotes.activity.shares;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ar.com.delellis.quicknotes.R;
import ar.com.delellis.quicknotes.databinding.ActivitySharesBinding;
import ar.com.delellis.quicknotes.model.Note;
import ar.com.delellis.quicknotes.model.Permissions;
import ar.com.delellis.quicknotes.model.Share;
import ar.com.delellis.quicknotes.model.Sharee;
import ar.com.delellis.quicknotes.shared.ShareEditAdapter;
import ar.com.delellis.quicknotes.shared.ShareeAdapter;
import ar.com.delellis.quicknotes.util.InsetsUtil;
import ar.com.delellis.quicknotes.util.PermissionsUtil;

/**
 * Who else has a note, and what they may do with it.
 *
 * Nobody can give away more than they hold, so what a recipient with the right
 * to pass the note on may offer is narrowed to their own share.
 */
public class SharesActivity extends AppCompatActivity implements SharesView,
        ShareEditAdapter.ShareActionListener, ShareeAdapter.ShareeClickListener {

    public static final String EXTRA_NOTE = "note";

    /** How long to sit on a keystroke before asking the server about it. */
    private static final long SEARCH_DELAY_MS = 350L;

    private ActivitySharesBinding binding;

    private SharesPresenter presenter;
    private ShareEditAdapter shareAdapter;
    private ShareeAdapter shareeAdapter;

    private Note note;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySharesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(binding.getRoot());

        note = (Note) Objects.requireNonNull(getIntent().getSerializableExtra(EXTRA_NOTE));

        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setSubtitle(note.isOwner() || note.getSharedBy() == null
                    ? note.getTitle()
                    : getString(R.string.shared_by, note.getSharedBy().getLabel()));
        }

        shareAdapter = new ShareEditAdapter(this);
        binding.recyclerShares.setAdapter(shareAdapter);

        shareeAdapter = new ShareeAdapter(this);
        binding.recyclerSharees.setAdapter(shareeAdapter);

        binding.shareeSearch.addTextChangedListener(searchWatcher);

        presenter = new SharesPresenter(this, note.getId());
        presenter.getShares();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        if (pendingSearch != null) {
            searchHandler.removeCallbacks(pendingSearch);
        }
        super.onDestroy();
    }

    private final TextWatcher searchWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (pendingSearch != null) {
                searchHandler.removeCallbacks(pendingSearch);
            }

            final String search = s.toString().trim();
            if (search.isEmpty()) {
                binding.recyclerSharees.setVisibility(GONE);
                return;
            }

            pendingSearch = () -> presenter.searchSharees(search);
            searchHandler.postDelayed(pendingSearch, SEARCH_DELAY_MS);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    /** What this caller is in a position to grant. */
    private int availablePermissions() {
        return note.isOwner() ? Permissions.CAN_EDIT_AND_RESHARE : note.getPermissions();
    }

    private void askPermissions(@NonNull String title, int current, @NonNull OnPermissionsChosen listener) {
        int[] choices = PermissionsUtil.choicesFor(availablePermissions());
        if (choices.length == 0) {
            Toast.makeText(this, R.string.note_is_read_only, Toast.LENGTH_LONG).show();
            return;
        }

        int checked = PermissionsUtil.indexOf(choices, current);
        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setSingleChoiceItems(PermissionsUtil.labelsOf(this, choices), checked, (dialog, which) -> {
                    dialog.dismiss();
                    listener.onPermissionsChosen(choices[which]);
                })
                .setNegativeButton(R.string.common_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    // ------------------------------------------------------------------
    // Adding, changing and taking back
    // ------------------------------------------------------------------

    @Override
    public void onShareeClick(@NonNull Sharee sharee) {
        askPermissions(getString(R.string.shared_with_name, sharee.getLabel()),
                Permissions.CAN_VIEW,
                permissions -> {
                    binding.shareeSearch.setText(null);
                    binding.recyclerSharees.setVisibility(GONE);
                    presenter.createShare(sharee, permissions);
                });
    }

    @Override
    public void onChangePermissions(@NonNull Share share) {
        askPermissions(share.getLabel(), share.getPermissions(),
                permissions -> presenter.updatePermissions(share, permissions));
    }

    @Override
    public void onRemoveShare(@NonNull Share share) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.remove_share)
                .setMessage(getString(R.string.shared_with_name, share.getLabel()))
                .setPositiveButton(R.string.common_yes, (dialog, which) -> {
                    dialog.dismiss();
                    presenter.deleteShare(share);
                })
                .setNegativeButton(R.string.common_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    // ------------------------------------------------------------------
    // SharesView
    // ------------------------------------------------------------------

    @Override
    public void showLoading() {
        binding.sharesProgress.setVisibility(VISIBLE);
    }

    @Override
    public void hideLoading() {
        binding.sharesProgress.setVisibility(GONE);
    }

    @Override
    public void onSharesLoaded(List<Share> shares) {
        shareAdapter.setItems(shares != null ? shares : Collections.emptyList());
        updateSharesEmptyState();
    }

    @Override
    public void onShareesLoaded(List<Sharee> sharees) {
        shareeAdapter.setItems(sharees != null ? sharees : Collections.emptyList());
        binding.recyclerSharees.setVisibility(shareeAdapter.getItemCount() > 0 ? VISIBLE : GONE);
        if (shareeAdapter.getItemCount() == 0) {
            Toast.makeText(this, R.string.no_matches, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onShareCreated(Share share) {
        shareAdapter.addItem(share);
        updateSharesEmptyState();
        Toast.makeText(this, getString(R.string.shared_with_name, share.getLabel()), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShareUpdated(Share share) {
        shareAdapter.updateItem(share);
    }

    @Override
    public void onShareDeleted(int shareId) {
        shareAdapter.removeItem(shareId);
        updateSharesEmptyState();
    }

    @Override
    public void onError(@Nullable String message) {
        Toast.makeText(this, message != null ? message : getString(R.string.error_loading_shares),
                Toast.LENGTH_LONG).show();
    }

    private void updateSharesEmptyState() {
        binding.sharesEmpty.setVisibility(shareAdapter.getItemCount() == 0 ? VISIBLE : GONE);
    }

    private interface OnPermissionsChosen {
        void onPermissionsChosen(int permissions);
    }
}
