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
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ar.com.delellis.quicknotes.R;
import ar.com.delellis.quicknotes.databinding.ItemShareeBinding;
import ar.com.delellis.quicknotes.model.Sharee;

/**
 * Candidates out of the collaborator search of the server.
 */
public class ShareeAdapter extends RecyclerView.Adapter<ShareeAdapter.ViewHolder> {

    private final ShareeClickListener listener;

    public ShareeAdapter(@NonNull ShareeClickListener listener) {
        this.listener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final ItemShareeBinding binding;

        ViewHolder(@NonNull ItemShareeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int index = getBindingAdapterPosition();
                if (index != RecyclerView.NO_POSITION) {
                    listener.onShareeClick(sharees.get(index));
                }
            });
        }

        private void bind(@NonNull Sharee sharee) {
            binding.shareeLabel.setText(sharee.getLabel());
            binding.shareeIcon.setImageResource(sharee.isGroup() ? R.drawable.ic_group : R.drawable.ic_person);

            String subline = sharee.isGroup()
                    ? binding.getRoot().getContext().getString(R.string.group)
                    : sharee.getSubline();
            binding.shareeSubline.setText(subline);
            binding.shareeSubline.setVisibility(subline != null && !subline.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    @NonNull
    private List<Sharee> sharees = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemShareeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(sharees.get(position));
    }

    @Override
    public int getItemCount() {
        return sharees.size();
    }

    public void setItems(@NonNull List<Sharee> sharees) {
        this.sharees = new ArrayList<>(sharees);
        notifyDataSetChanged();
    }

    public interface ShareeClickListener {
        void onShareeClick(@NonNull Sharee sharee);
    }
}
