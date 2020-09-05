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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ar.delellis.quicknotes.R;
import ar.delellis.quicknotes.model.Tag;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final View view;

        @NonNull
        private final TextView name;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;

            this.name = itemView.findViewById(R.id.tag_name);
        }

        private void bind(@NonNull Tag tag) {
            name.setText(tag.getName());
        }

    }

    @NonNull
    private List<Tag> tags = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(tags.get(position));
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    public void setItems(@NonNull List<Tag> tags) {
        this.tags = tags;
        notifyDataSetChanged();
    }
}
