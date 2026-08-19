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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ar.com.delellis.quicknotes.R;
import ar.com.delellis.quicknotes.databinding.ItemNoteBinding;
import ar.com.delellis.quicknotes.model.Note;
import ar.com.delellis.quicknotes.model.Tag;
import ar.com.delellis.quicknotes.shared.AttachmentAdapter;
import ar.com.delellis.quicknotes.shared.ShareAdapter;
import ar.com.delellis.quicknotes.shared.TagAdapter;
import ar.com.delellis.quicknotes.util.ColorUtil;
import ar.com.delellis.quicknotes.util.DateUtil;
import ar.com.delellis.quicknotes.util.HtmlUtil;
import ar.com.delellis.quicknotes.util.SearchUtil;

/**
 * The grid of notes.
 *
 * What is on screen is one scope — a drawer entry — narrowed further by
 * whatever is typed in the search box. Archived and trashed notes come down
 * with everything else and are filtered out here: they only show under their
 * own scope, and never anywhere else.
 */
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    public static final int SORT_BY_TITLE = 0;
    public static final int SORT_BY_CREATED = 1;
    public static final int SORT_BY_UPDATED = 2;

    /** Everything active: not archived, not in the trash. */
    public static final int SCOPE_ALL = 0;
    public static final int SCOPE_PINNED = 1;
    public static final int SCOPE_SHARED_WITH_ME = 2;
    public static final int SCOPE_SHARED_BY_ME = 3;
    public static final int SCOPE_REMINDERS = 4;
    public static final int SCOPE_ARCHIVED = 5;
    public static final int SCOPE_TRASH = 6;
    public static final int SCOPE_TAG = 7;
    public static final int SCOPE_COLOR = 8;

    private int sortRule = SORT_BY_UPDATED;
    private boolean firstPinned = true;

    private int scope = SCOPE_ALL;
    private String tagName = null;
    private String colorFilter = null;
    private String query = "";

    /**
     * What each note is searched through, folded once when the list arrives
     * rather than on every keystroke: the old filter parsed the html of every
     * note again for each letter typed.
     */
    private final Map<Integer, String> searchIndex = new HashMap<>();

    private final Context context;
    private final int tintColor;
    private final int defaultColor;

    private List<Note> noteList = new ArrayList<>();
    private List<Note> noteListFiltered = new ArrayList<>();

    private final ItemClickListener itemClickListener;

    public NoteAdapter(Context context, ItemClickListener itemClickListener) {
        this.context = context;
        this.itemClickListener = itemClickListener;

        this.tintColor = ContextCompat.getColor(context, R.color.defaultNoteTint);
        this.defaultColor = ContextCompat.getColor(context, R.color.defaultNoteColor);
    }

    public void setNoteList(@NonNull List<Note> noteList) {
        this.noteList = noteList;

        searchIndex.clear();
        for (Note note : noteList) {
            searchIndex.put(note.getId(), searchTextOf(note));
        }

        applyFilters();
    }

    /**
     * Everything of a note worth searching through, folded down to what a
     * query is compared against: its title, its text and the names of its
     * tags, which is what the web interface of the app matches too.
     */
    @NonNull
    private static String searchTextOf(@NonNull Note note) {
        StringBuilder text = new StringBuilder()
                .append(HtmlUtil.cleanString(note.getTitle()))
                .append(' ')
                .append(HtmlUtil.cleanString(note.getContent()));
        for (Tag tag : note.getTags()) {
            text.append(' ').append(tag.getName());
        }
        return SearchUtil.normalize(text.toString());
    }

    @NonNull
    public List<Note> getNoteList() {
        return noteList;
    }

    /** Puts a note the server sent back in place of the one held here. */
    public void replaceNote(@NonNull Note note) {
        for (int i = 0; i < noteList.size(); i++) {
            if (noteList.get(i).getId() == note.getId()) {
                noteList.set(i, note);
                searchIndex.put(note.getId(), searchTextOf(note));
                break;
            }
        }
        applyFilters();
    }

    public void removeNote(int noteId) {
        for (int i = 0; i < noteList.size(); i++) {
            if (noteList.get(i).getId() == noteId) {
                noteList.remove(i);
                searchIndex.remove(noteId);
                break;
            }
        }
        applyFilters();
    }

    public Note get(int position) {
        return noteListFiltered.get(position);
    }

    public int getSortRule() {
        return sortRule;
    }

    public void setSortRule(int sortRule) {
        this.sortRule = sortRule;
        applyFilters();
    }

    public boolean getFirstPinned() {
        return firstPinned;
    }

    public void setFirstPinned(boolean firstPinned) {
        this.firstPinned = firstPinned;
        applyFilters();
    }

    public int getScope() {
        return scope;
    }

    /** Shows one drawer entry. The search box is left as it is. */
    public void setScope(int scope) {
        this.scope = scope;
        this.tagName = null;
        this.colorFilter = null;
        applyFilters();
    }

    public void setTagScope(String tagName) {
        this.scope = SCOPE_TAG;
        this.tagName = tagName;
        this.colorFilter = null;
        applyFilters();
    }

    public void setColorScope(String color) {
        this.scope = SCOPE_COLOR;
        this.colorFilter = color;
        this.tagName = null;
        applyFilters();
    }

    @Nullable
    public String getColorScope() {
        return colorFilter;
    }

    /** Narrows whatever scope is on screen by what the user typed. */
    public void setQuery(String query) {
        this.query = query != null ? query : "";
        applyFilters();
    }

    private void applyFilters() {
        List<Note> filtered = new ArrayList<>();
        List<String> tokens = SearchUtil.tokenize(query);

        for (Note note : noteList) {
            if (!matchesScope(note)) {
                continue;
            }
            if (!tokens.isEmpty() && !matchesQuery(note, tokens)) {
                continue;
            }
            filtered.add(note);
        }

        List<Note> previous = noteListFiltered;
        noteListFiltered = filtered;
        performSort();

        // Telling the list what actually changed keeps the scroll where it was
        // and animates the one card that moved, instead of redrawing the whole
        // mosaic on every keystroke and every refresh.
        DiffUtil.calculateDiff(new NoteDiffCallback(previous, noteListFiltered), true)
                .dispatchUpdatesTo(this);
    }

    private boolean matchesScope(@NonNull Note note) {
        // The trash and the archive are places of their own: a note sitting in
        // either is out of every other scope.
        if (scope == SCOPE_TRASH) {
            return note.isTrashed();
        }
        if (note.isTrashed()) {
            return false;
        }
        if (scope == SCOPE_ARCHIVED) {
            return note.isArchived();
        }
        if (note.isArchived()) {
            return false;
        }

        switch (scope) {
            case SCOPE_PINNED:
                return note.isPinned();
            case SCOPE_SHARED_WITH_ME:
                return note.isSharedWithMe();
            case SCOPE_SHARED_BY_ME:
                return note.isSharedByMe();
            case SCOPE_REMINDERS:
                return note.hasReminder();
            case SCOPE_TAG:
                return hasTag(note, tagName);
            case SCOPE_COLOR:
                return colorFilter == null || colorFilter.equalsIgnoreCase(note.getColor());
            case SCOPE_ALL:
            default:
                return true;
        }
    }

    private static boolean hasTag(@NonNull Note note, String tagName) {
        if (tagName == null) {
            return true;
        }
        for (Tag tag : note.getTags()) {
            if (tagName.equals(tag.getName())) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesQuery(@NonNull Note note, @NonNull List<String> tokens) {
        String haystack = searchIndex.get(note.getId());
        if (haystack == null) {
            haystack = searchTextOf(note);
            searchIndex.put(note.getId(), haystack);
        }
        return SearchUtil.matchesAll(haystack, tokens);
    }

    /**
     * What tells one card from another. Only what is drawn counts: a note that
     * came back from the server with a new timestamp but the same face should
     * not blink.
     */
    private static class NoteDiffCallback extends DiffUtil.Callback {
        private final List<Note> before;
        private final List<Note> after;

        NoteDiffCallback(List<Note> before, List<Note> after) {
            this.before = before;
            this.after = after;
        }

        @Override
        public int getOldListSize() {
            return before.size();
        }

        @Override
        public int getNewListSize() {
            return after.size();
        }

        @Override
        public boolean areItemsTheSame(int oldPosition, int newPosition) {
            return before.get(oldPosition).getId() == after.get(newPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldPosition, int newPosition) {
            Note old = before.get(oldPosition);
            Note current = after.get(newPosition);

            return Objects.equals(old.getTitle(), current.getTitle())
                    && Objects.equals(old.getContent(), current.getContent())
                    && Objects.equals(old.getColor(), current.getColor())
                    && old.isPinned() == current.isPinned()
                    && old.isArchived() == current.isArchived()
                    && old.isSharedWithMe() == current.isSharedWithMe()
                    && old.isSharedByMe() == current.isSharedByMe()
                    && Objects.equals(old.getReminderAt(), current.getReminderAt())
                    && Objects.equals(old.getReminderNotifiedAt(), current.getReminderNotifiedAt())
                    && Objects.equals(old.getTags(), current.getTags())
                    && Objects.equals(old.getAttachments(), current.getAttachments())
                    && old.getSharedWith().size() == current.getSharedWith().size();
        }
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(ItemNoteBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.bind(noteListFiltered.get(position));
    }

    @Override
    public int getItemCount() {
        return noteListFiltered.size();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final ItemNoteBinding binding;

        private final AttachmentAdapter attachmentAdapter = new AttachmentAdapter();
        private final TagAdapter tagAdapter = new TagAdapter();
        private final ShareAdapter shareAdapter = new ShareAdapter();

        NoteViewHolder(@NonNull ItemNoteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.itemContent.setCalypsoMode(false);

            View.OnClickListener onClick = view -> {
                int index = getBindingAdapterPosition();
                if (index != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(noteListFiltered.get(index));
                }
            };
            View.OnLongClickListener onLongClick = view -> {
                int index = getBindingAdapterPosition();
                if (index == RecyclerView.NO_POSITION) {
                    return false;
                }
                itemClickListener.onItemLongClick(noteListFiltered.get(index));
                return true;
            };

            binding.itemNote.setOnClickListener(onClick);
            binding.itemNote.setOnLongClickListener(onLongClick);
            binding.itemContent.setOnClickListener(onClick);
            binding.itemContent.setOnLongClickListener(onLongClick);

            // A thumbnail is part of the card, not a target of its own.
            attachmentAdapter.setOnImageClickListener(position -> onClick.onClick(binding.getRoot()));

            binding.itemRecyclerAttachments.setAdapter(attachmentAdapter);
            binding.itemRecyclerTags.setAdapter(tagAdapter);
            binding.itemRecyclerShares.setAdapter(shareAdapter);
        }

        private void bind(@NonNull Note note) {
            binding.itemTitle.setText(HtmlUtil.cleanString(note.getTitle()));
            binding.itemContent.fromHtml(HtmlUtil.cleanHtml(note.getContent()), true);
            binding.itemNote.setCardBackgroundColor(ColorUtil.parseColorOr(note.getColor(), defaultColor));

            binding.itemShared.setVisibility(note.isSharedWithMe() || note.isSharedByMe() ? View.VISIBLE : View.GONE);
            binding.itemPinned.setVisibility(note.isPinned() ? View.VISIBLE : View.GONE);
            binding.itemArchived.setVisibility(note.isArchived() ? View.VISIBLE : View.GONE);
            binding.itemReminder.setVisibility(note.hasReminder() ? View.VISIBLE : View.GONE);

            String reminder = note.hasReminder() ? DateUtil.toLocalDisplay(note.getReminderAt()) : null;
            if (reminder != null) {
                // Once the notification has gone out the reminder is history,
                // and saying so tells a pending one from one already fired.
                binding.itemReminderText.setText(context.getString(
                        note.isReminderPending() ? R.string.reminder_at : R.string.reminder_notified, reminder));
                binding.itemReminderText.setVisibility(View.VISIBLE);
            } else {
                binding.itemReminderText.setVisibility(View.GONE);
            }

            attachmentAdapter.setItems(note.getAttachments());
            tagAdapter.setItems(note.getTags());
            shareAdapter.setItems(note.getSharedWith());

            ColorUtil.imageViewTintColor(binding.itemShared, tintColor);
            ColorUtil.imageViewTintColor(binding.itemPinned, tintColor);
            ColorUtil.imageViewTintColor(binding.itemArchived, tintColor);
            ColorUtil.imageViewTintColor(binding.itemReminder, tintColor);
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
        void onItemClick(@NonNull Note note);
        void onItemLongClick(@NonNull Note note);
    }
}
