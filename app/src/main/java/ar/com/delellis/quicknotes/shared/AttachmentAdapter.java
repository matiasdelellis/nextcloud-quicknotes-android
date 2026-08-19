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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import ar.com.delellis.quicknotes.R;
import ar.com.delellis.quicknotes.databinding.ItemAttachmentBinding;
import ar.com.delellis.quicknotes.model.Attachment;
import ar.com.delellis.quicknotes.util.SsoUrlUtil;

public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.ViewHolder> {

    private ImageItemClickListener imageItemClickListener;
    private ImageItemLongClickListener imageItemLongClickListener;
    private DeleteItemClickListener deleteItemClickListener;

    private boolean disableDeletion = false;

    class ViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final ItemAttachmentBinding binding;

        ViewHolder(@NonNull ItemAttachmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.attachmentThumbnail.setOnClickListener(v -> {
                int itemIndex = getBindingAdapterPosition();
                if (itemIndex != RecyclerView.NO_POSITION && imageItemClickListener != null) {
                    imageItemClickListener.onImageItemClick(itemIndex);
                }
            });

            binding.attachmentThumbnail.setOnLongClickListener(v -> {
                int itemIndex = getBindingAdapterPosition();
                if (itemIndex == RecyclerView.NO_POSITION || imageItemLongClickListener == null) {
                    return false;
                }
                imageItemLongClickListener.onImageItemLongClick(itemIndex);
                return true;
            });

            binding.deleteAttachment.setOnClickListener(v -> {
                int itemIndex = getBindingAdapterPosition();
                if (itemIndex != RecyclerView.NO_POSITION && deleteItemClickListener != null) {
                    deleteItemClickListener.onDeleteItemClick(itemIndex);
                }
            });
        }

        private void bind(@NonNull Attachment attachment) {
            // Somebody else's attachment is not the caller's to take off the
            // note: the server only ever detaches rows of whoever is saving.
            boolean canDelete = deleteItemClickListener != null && !disableDeletion && attachment.isMine();
            binding.deleteAttachment.setVisibility(canDelete ? View.VISIBLE : View.GONE);

            Context context = binding.getRoot().getContext();
            binding.attachmentThumbnail.setContentDescription(attachment.getBasename() != null
                    ? attachment.getBasename()
                    : context.getString(R.string.attachment_thumbnail));

            // A file the preview manager cannot draw is answered with a 303 to
            // the icon of its mime type, and the SSO transport does not follow
            // redirects: asking for that preview can only fail, and failing
            // sends Glide to its plain http loader, which has no credentials
            // and is answered 401. That is what has_preview is there to avoid,
            // so draw the icon from here and make no request at all.
            if (!attachment.hasPreview()) {
                Glide.with(context).clear(binding.attachmentThumbnail);
                binding.attachmentThumbnail.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                binding.attachmentThumbnail.setImageResource(iconFor(attachment.getMime()));
                return;
            }

            binding.attachmentThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(context)
                    .load(SsoUrlUtil.withFrontController(attachment.getPreviewUrl()))
                    .error(iconFor(attachment.getMime()))
                    .into(binding.attachmentThumbnail);
        }

        /** What to show for a file there is no thumbnail of. */
        @DrawableRes
        private int iconFor(String mime) {
            if (mime != null) {
                if (mime.startsWith("image/")) {
                    return R.drawable.ic_attach_photo;
                }
                if (mime.startsWith("video/")) {
                    return R.drawable.ic_attach_video;
                }
            }
            return R.drawable.ic_attach_file;
        }
    }

    @NonNull
    private List<Attachment> attachments = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemAttachmentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
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
        notifyItemInserted(this.attachments.size() - 1);
    }

    public void removeItem(Attachment attachment) {
        int index = this.attachments.indexOf(attachment);
        if (index >= 0) {
            this.attachments.remove(index);
            notifyItemRemoved(index);
        }
    }

    @NonNull
    public List<Attachment> getItems() {
        return attachments;
    }

    public Attachment get(int position) {
        return this.attachments.get(position);
    }

    public void setDisableDeletion(boolean disableDeletion) {
        this.disableDeletion = disableDeletion;
    }

    public void setOnImageClickListener(ImageItemClickListener onImageClickListener) {
        this.imageItemClickListener = onImageClickListener;
    }

    public interface ImageItemClickListener {
        void onImageItemClick(int position);
    }

    public void setOnImageLongClickListener(ImageItemLongClickListener onImageLongClickListener) {
        this.imageItemLongClickListener = onImageLongClickListener;
    }

    public interface ImageItemLongClickListener {
        void onImageItemLongClick(int position);
    }

    public void setOnDeleteClickListener(DeleteItemClickListener onDeleteClickListener) {
        this.deleteItemClickListener = onDeleteClickListener;
    }

    public interface DeleteItemClickListener {
        void onDeleteItemClick(int position);
    }

}
