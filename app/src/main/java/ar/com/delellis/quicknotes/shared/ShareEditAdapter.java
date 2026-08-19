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

import ar.com.delellis.quicknotes.R;
import ar.com.delellis.quicknotes.databinding.ItemShareEditBinding;
import ar.com.delellis.quicknotes.model.Share;
import ar.com.delellis.quicknotes.util.PermissionsUtil;

/**
 * Who has the note, with what each of them may do and a way to change it.
 */
public class ShareEditAdapter extends RecyclerView.Adapter<ShareEditAdapter.ViewHolder> {

    private final ShareActionListener listener;

    public ShareEditAdapter(@NonNull ShareActionListener listener) {
        this.listener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final ItemShareEditBinding binding;

        ViewHolder(@NonNull ItemShareEditBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.shareChangePermissions.setOnClickListener(v -> {
                int index = getBindingAdapterPosition();
                if (index != RecyclerView.NO_POSITION) {
                    listener.onChangePermissions(shares.get(index));
                }
            });

            binding.shareRemove.setOnClickListener(v -> {
                int index = getBindingAdapterPosition();
                if (index != RecyclerView.NO_POSITION) {
                    listener.onRemoveShare(shares.get(index));
                }
            });
        }

        private void bind(@NonNull Share share) {
            binding.shareName.setText(share.getLabel());
            binding.shareIcon.setImageResource(share.isGroup() ? R.drawable.ic_group : R.drawable.ic_person);
            binding.sharePermissions.setText(PermissionsUtil.labelOf(
                    binding.getRoot().getContext(), share.getPermissions()));
        }
    }

    @NonNull
    private List<Share> shares = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemShareEditBinding.inflate(
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
        this.shares = new ArrayList<>(shares);
        notifyDataSetChanged();
    }

    public void addItem(@NonNull Share share) {
        this.shares.add(share);
        notifyItemInserted(this.shares.size() - 1);
    }

    public void updateItem(@NonNull Share share) {
        for (int i = 0; i < shares.size(); i++) {
            if (shares.get(i).getId() == share.getId()) {
                shares.set(i, share);
                notifyItemChanged(i);
                return;
            }
        }
    }

    public void removeItem(int shareId) {
        for (int i = 0; i < shares.size(); i++) {
            if (shares.get(i).getId() == shareId) {
                shares.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
    }

    public interface ShareActionListener {
        void onChangePermissions(@NonNull Share share);
        void onRemoveShare(@NonNull Share share);
    }
}
