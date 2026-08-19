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

package ar.com.delellis.quicknotes.activity.tags;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ar.com.delellis.quicknotes.R;
import ar.com.delellis.quicknotes.databinding.ActivityTagsBinding;
import ar.com.delellis.quicknotes.model.Tag;
import ar.com.delellis.quicknotes.shared.TagSelectionAdapter;
import ar.com.delellis.quicknotes.util.InsetsUtil;

public class TagsActivity extends AppCompatActivity {

    public static final String EXTRA_TAGS = "tags";
    public static final String EXTRA_TAG_SELECTION = "tagSelection";

    private ActivityTagsBinding binding;

    private TagSelectionAdapter tagsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTagsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        tagsAdapter = new TagSelectionAdapter();
        binding.recyclerTagsView.setAdapter(tagsAdapter);

        binding.tagSearch.addTextChangedListener(textWatcher);

        binding.icFilterClear.setVisibility(View.GONE);
        binding.icFilterClear.setOnClickListener(view -> binding.tagSearch.setText(null));

        binding.createTag.setVisibility(View.GONE);
        binding.createTag.setOnClickListener(view -> {
            Tag newTag = new Tag();
            newTag.setId(Tag.NO_ID);
            newTag.setName(binding.tagSearch.getText().toString().trim());
            tagsAdapter.insertTagSelection(newTag);
        });

        Intent intent = getIntent();
        List<Tag> tags = readTags(intent, EXTRA_TAGS);
        List<Tag> tagSelection = readTags(intent, EXTRA_TAG_SELECTION);

        tagsAdapter.setTags(tags);
        tagsAdapter.setTagSelection(tagSelection);
    }

    @SuppressWarnings("unchecked")
    private static List<Tag> readTags(Intent intent, String key) {
        Serializable extra = intent.getSerializableExtra(key);
        return extra != null ? (List<Tag>) extra : new ArrayList<>();
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_TAG_SELECTION, (Serializable) tagsAdapter.getTagSelection());
        setResult(RESULT_OK, intent);
        finish();

        return true;
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int before, int after) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int after) {
            String query = charSequence.toString().trim();
            if (query.isEmpty()) {
                binding.createTag.setVisibility(View.GONE);
                binding.icFilterClear.setVisibility(View.GONE);
            } else if (tagsAdapter.tagExists(query)) {
                binding.createTag.setVisibility(View.GONE);
                binding.icFilterClear.setVisibility(View.VISIBLE);
            } else {
                binding.createTag.setText(getString(R.string.create_tag, query));
                binding.createTag.setVisibility(View.VISIBLE);
                binding.icFilterClear.setVisibility(View.VISIBLE);
            }
            tagsAdapter.getFilter().filter(charSequence);
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };
}
