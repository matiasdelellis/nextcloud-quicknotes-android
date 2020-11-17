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
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import ar.delellis.quicknotes.R;
import ar.delellis.quicknotes.model.Attachment;

public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.ViewHolder>{

    private ImageItemClickListener imageItemClickListener;
    private DeleteItemClickListener deleteItemClickListener;

    class ViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final View view;

        @NonNull
        private final ImageView ivThumbnail;

        @NonNull
        private final ImageButton imDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;

            this.ivThumbnail = itemView.findViewById(R.id.attachment_thumbnail);
            this.ivThumbnail.setOnClickListener(v -> {
                int itemIndex = getAdapterPosition();
                imageItemClickListener.onImageItemClick(itemIndex);
            });

            this.imDelete = itemView.findViewById(R.id.delete_attachment);
            this.imDelete.setVisibility(deleteItemClickListener == null ? View.GONE : View.VISIBLE);
            this.imDelete.setOnClickListener(v -> {
                int itemIndex = getAdapterPosition();
                deleteItemClickListener.onDeleteItemClick(itemIndex);
            });
        }

        private void bind(@NonNull Attachment attachment) {
            Glide.with(view.getContext())
                    .load(attachment.getPreviewUrl())
                    .error(R.drawable.ic_delete_grey)
                    .into(ivThumbnail);
        }
    }

    @NonNull
    private List<Attachment> attachments = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attachment, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(attachments.get(position));
    }

    @Override
    public int getItemCount() {
        return attachments.size();
    }

    public void setItems(@NonNull List<Attachment> attachments) {
        this.attachments = attachments;
        notifyDataSetChanged();
    }

    public void addItem(Attachment attachment) {
        this.attachments.add(attachment);
        notifyDataSetChanged();
    }

    public void removeItem(Attachment attachment) {
        this.attachments.remove(attachment);
        notifyDataSetChanged();
    }

    public Attachment get(int position) {
        return this.attachments.get(position);
    }

    public void setOnImageClickListener(ImageItemClickListener onImageClickListener) {
        this.imageItemClickListener = onImageClickListener;
    }

    public interface ImageItemClickListener {
        void onImageItemClick(int position);
    }

    public void setOnDeleteClickListener(DeleteItemClickListener onDeleteClickListener) {
        this.deleteItemClickListener = onDeleteClickListener;
    }

    public interface DeleteItemClickListener {
        void onDeleteItemClick(int position);
    }

}
