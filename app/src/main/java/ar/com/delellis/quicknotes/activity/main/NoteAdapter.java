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

package ar.com.delellis.quicknotes.activity.main;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.wordpress.aztec.AztecText;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ar.com.delellis.quicknotes.R;
import ar.com.delellis.quicknotes.model.Note;
import ar.com.delellis.quicknotes.model.Tag;
import ar.com.delellis.quicknotes.shared.AttachmentAdapter;
import ar.com.delellis.quicknotes.shared.ShareAdapter;
import ar.com.delellis.quicknotes.shared.TagAdapter;
import ar.com.delellis.quicknotes.util.ColorUtil;
import ar.com.delellis.quicknotes.util.HtmlUtil;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.RecyclerViewAdapter> implements Filterable {

    public static final int SORT_BY_TITLE = 0;
    public static final int SORT_BY_CREATED = 1;
    public static final int SORT_BY_UPDATED = 2;

    private int sortRule = SORT_BY_UPDATED;
    private boolean firstPinned = true;

    private Context context;
    int tintColor;

    private List<Note> noteList = new ArrayList<>();
    private List<Note> noteListFiltered = new ArrayList<>();

    private ItemClickListener itemClickListener;

    public NoteAdapter(Context context, ItemClickListener itemClickListener) {
        this.context = context;
        this.itemClickListener = itemClickListener;

        this.tintColor = context.getResources().getColor(R.color.defaultNoteTint);
    }

    public void setNoteList(@NonNull List<Note> noteList) {
        this.noteList = noteList;
        this.noteListFiltered = new ArrayList<>(noteList);

        performSort();
        notifyDataSetChanged();
    }

    public Note get(int position) {
        return noteListFiltered.get(position);
    }

    public int getSortRule() {
        return sortRule;
    }

    public void setSortRule(int sortRule) {
        this.sortRule = sortRule;

        performSort();
        notifyDataSetChanged();
    }

    public boolean getFirstPinned() {
        return firstPinned;
    }

    public void setFirstPinned(boolean firstPinned) {
        this.firstPinned = firstPinned;

        performSort();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerViewAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new RecyclerViewAdapter(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter holder, int position) {
        Note note = noteListFiltered.get(position);

        holder.tv_title.setText(HtmlUtil.cleanString(note.getTitle()));
        holder.tv_content.fromHtml(HtmlUtil.cleanHtml(note.getContent()), true);
        holder.card_item.setCardBackgroundColor(Color.parseColor(note.getColor()));
        holder.im_shared.setVisibility(note.getIsShared() ? View.VISIBLE : View.GONE);
        holder.im_pinned.setVisibility(note.getIsPinned() ? View.VISIBLE : View.GONE);

        holder.attachmentAdapter.setItems(note.getAttachtments());
        holder.attachmentAdapter.notifyDataSetChanged();
        holder.attachmentRecyclerView.setAdapter(holder.attachmentAdapter);

        holder.tagAdapter.setItems(note.getTags());
        holder.tagAdapter.notifyDataSetChanged();
        holder.tagRecyclerView.setAdapter(holder.tagAdapter);

        holder.shareAdapter.setItems(note.getShareWith());
        holder.shareAdapter.notifyDataSetChanged();
        holder.shareRecyclerView.setAdapter(holder.shareAdapter);

        ColorUtil.imageViewTintColor(holder.im_shared, tintColor);
        ColorUtil.imageViewTintColor(holder.im_pinned, tintColor);
    }

    @Override
    public int getItemCount() {
        return noteListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        // Run on Background thread.
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults filterResults = new FilterResults();
            List <Note> filteredNotes = new ArrayList<>();

            if (charSequence.toString().isEmpty()) {
                filteredNotes.addAll(noteList);
            } else {
                String query = charSequence.toString().toLowerCase();
                for (Note note: noteList) {
                    if (HtmlUtil.cleanString(note.getTitle()).contains(query)) {
                        filteredNotes.add(note);
                    } else if (HtmlUtil.cleanString(note.getContent()).contains(query)) {
                        filteredNotes.add(note);
                    }
                }
            }

            filterResults.values = filteredNotes;
            return filterResults;
        }
        //Run on ui thread
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            noteListFiltered.clear();
            noteListFiltered.addAll((Collection<? extends Note>) filterResults.values);

            performSort();
            notifyDataSetChanged();
        }
    };

    public Filter getTagFilter() {
        return tagFilter;
    }

    Filter tagFilter = new Filter() {
        @Override
        // Run on Background thread.
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults filterResults = new FilterResults();
            List <Note> filteredNotes = new ArrayList<>();

            if (charSequence.toString().isEmpty()) {
                filteredNotes.addAll(noteList);
            } else {
                String query = charSequence.toString();
                for (Note note: noteList) {
                    for (Tag tag: note.getTags()) {
                        if (tag.getName().equals(query)) {
                            filteredNotes.add(note);
                            break;
                        }
                    }
                }
            }
            filterResults.values = filteredNotes;
            return filterResults;
        }
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            noteListFiltered.clear();
            noteListFiltered.addAll((Collection<? extends Note>) filterResults.values);

            performSort();
            notifyDataSetChanged();
        }
    };

    public Filter getIsSharedFilter() {
        return isSharedFilter;
    }

    Filter isSharedFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults filterResults = new FilterResults();
            List <Note> filteredNotes = new ArrayList<>();

            for (Note note: noteList) {
                if (note.getIsShared()) {
                    filteredNotes.add(note);
                }
            }

            filterResults.values = filteredNotes;
            return filterResults;
        }
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            noteListFiltered.clear();
            noteListFiltered.addAll((Collection<? extends Note>) filterResults.values);

            performSort();
            notifyDataSetChanged();
        }
    };

    public Filter getSharedWithOthersFilter() {
        return sharedWithOthersFilter;
    }

    Filter sharedWithOthersFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults filterResults = new FilterResults();
            List <Note> filteredNotes = new ArrayList<>();

            for (Note note: noteList) {
                if (note.getShareWith().size() > 0) {
                    filteredNotes.add(note);
                }
            }

            filterResults.values = filteredNotes;
            return filterResults;
        }
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            noteListFiltered.clear();
            noteListFiltered.addAll((Collection<? extends Note>) filterResults.values);

            performSort();
            notifyDataSetChanged();
        }
    };

    public Filter getPinnedFilter() {
        return pinnedFilter;
    }

    Filter pinnedFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults filterResults = new FilterResults();
            List <Note> filteredNotes = new ArrayList<>();

            for (Note note: noteList) {
                if (note.getIsPinned()) {
                    filteredNotes.add(note);
                }
            }

            filterResults.values = filteredNotes;
            return filterResults;
        }
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            noteListFiltered.clear();
            noteListFiltered.addAll((Collection<? extends Note>) filterResults.values);

            performSort();
            notifyDataSetChanged();
        }
    };

    class RecyclerViewAdapter extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView card_item;
        TextView tv_title;
        AztecText tv_content;
        ImageView im_shared;
        ImageView im_pinned;

        AttachmentAdapter attachmentAdapter;
        RecyclerView attachmentRecyclerView;

        TagAdapter tagAdapter;
        RecyclerView tagRecyclerView;

        ShareAdapter shareAdapter;
        RecyclerView shareRecyclerView;

        ItemClickListener itemClickListener;

        RecyclerViewAdapter(@NonNull View itemView, ItemClickListener itemClickListener) {
            super(itemView);
            this.itemClickListener = itemClickListener;

            card_item = itemView.findViewById(R.id.item_note);
            tv_title = itemView.findViewById(R.id.item_title);
            tv_content = itemView.findViewById(R.id.item_content);
            im_shared = itemView.findViewById(R.id.item_shared);
            im_pinned = itemView.findViewById(R.id.item_pinned);

            attachmentAdapter = new AttachmentAdapter();
            attachmentRecyclerView = itemView.findViewById(R.id.item_recyclerAttachments);

            tagAdapter = new TagAdapter();
            tagRecyclerView = itemView.findViewById(R.id.item_recyclerTags);

            shareAdapter = new ShareAdapter();
            shareRecyclerView = itemView.findViewById(R.id.item_recyclerShares);

            card_item.setOnClickListener(this);
            tv_content.setOnClickListener(this);
            tv_content.setCalypsoMode(false);

            attachmentAdapter.setOnImageClickListener(position -> this.onClick(itemView));
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    private void performSort() {
        if (sortRule == SORT_BY_TITLE) {
            Collections.sort(noteListFiltered, Note.ByTitleAZ);
        } else if (sortRule == SORT_BY_CREATED) {
            Collections.sort(noteListFiltered, Note.ByLastCreated);
        } else if (sortRule == SORT_BY_UPDATED) {
            Collections.sort(noteListFiltered, Note.ByLastUpdated);
        }

        if (firstPinned) {
            Collections.sort(noteListFiltered, Note.ByPinned);
        }
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
