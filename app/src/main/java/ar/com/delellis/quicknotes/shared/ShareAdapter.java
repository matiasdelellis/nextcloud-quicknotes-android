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

package ar.com.delellis.quicknotes.shared;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ar.com.delellis.quicknotes.databinding.ItemShareBinding;
import ar.com.delellis.quicknotes.model.Share;

/**
 * The read only row of names a note card shows: who else has this note.
 */
public class ShareAdapter extends RecyclerView.Adapter<ShareAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final ItemShareBinding binding;

        ViewHolder(@NonNull ItemShareBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bind(@NonNull Share share) {
            binding.shareUser.setText(share.getLabel());
        }
    }

    @NonNull
    private List<Share> shares = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemShareBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(shares.get(position));
    }

    @Override
    public int getItemCount() {
        return shares.size();
    }

    public void setItems(@NonNull List<Share> shares) {
        this.shares = shares;
        notifyDataSetChanged();
    }
}
