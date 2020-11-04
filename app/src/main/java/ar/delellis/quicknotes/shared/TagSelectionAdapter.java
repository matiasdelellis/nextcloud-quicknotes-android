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

package ar.delellis.quicknotes.shared;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ar.delellis.quicknotes.R;
import ar.delellis.quicknotes.model.Tag;

public class TagSelectionAdapter extends RecyclerView.Adapter<TagSelectionAdapter.ViewHolder> implements Filterable {

    class ViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final View view;

        @NonNull
        private final TextView name;

        @NonNull
        private final CheckBox checkBox;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;

            this.name = itemView.findViewById(R.id.tag_name);
            this.checkBox = itemView.findViewById(R.id.tag_checkbox);
        }

        private void bind(@NonNull Tag tag) {
            name.setText(tag.getName());
            checkBox.setChecked(isSelected(tag));
            checkBox.setOnClickListener(view -> {
                CheckBox box = (CheckBox) view;
                if (box.isChecked()) {
                    tagSelection.add(tag);
                } else {
                    tagSelection.remove(tag);
                }
            });
        }
    }

    @NonNull
    private List<Tag> tags = new ArrayList<>();

    @NonNull
    private List<Tag> tagsFiltered = new ArrayList<>();

    @NonNull
    private List<Tag> tagSelection = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag_selection, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(tagsFiltered.get(position));
    }

    @Override
    public int getItemCount() {
        return tagsFiltered.size();
    }

    public void setTags(@NonNull List<Tag> tags) {
        this.tags = tags;
        this.tagsFiltered = new ArrayList<>(tags);
        notifyDataSetChanged();
    }

    public boolean tagExists(String tagName) {
        for (Tag tag: tagsFiltered) {
            if (tag.getName().toLowerCase().equals(tagName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public void setTagSelection(@NonNull List<Tag> tagSelection) {
        this.tagSelection = tagSelection;
        notifyDataSetChanged();
    }

    public List<Tag> getTagSelection() {
        return tagSelection;
    }

    public void insertTagSelection(Tag tag) {
        tags.add(tag);
        tagsFiltered.add(tag);
        tagSelection.add(tag);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults filterResults = new FilterResults();
            List <Tag> filteredTags = new ArrayList<>();
            if (charSequence.toString().isEmpty()) {
                filteredTags.addAll(tags);
            } else {
                for (Tag tag: tags) {
                    String query = charSequence.toString().toLowerCase();
                    if (tag.getName().toLowerCase().contains(query)) {
                        filteredTags.add(tag);
                    }
                }
            }
            filterResults.values = filteredTags;
            return filterResults;
        }
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            tagsFiltered.clear();
            tagsFiltered.addAll((Collection<? extends Tag>) filterResults.values);
            notifyDataSetChanged();
        }
    };

    private boolean isSelected(Tag bindTag) {
        for (Tag tag: tagSelection) {
            if (tag.getId() == bindTag.getId()) {
                return true;
            }
        }
        return false;
    }

}
