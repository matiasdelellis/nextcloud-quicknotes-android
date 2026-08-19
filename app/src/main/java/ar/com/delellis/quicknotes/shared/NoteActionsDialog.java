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

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import ar.com.delellis.quicknotes.R;
import ar.com.delellis.quicknotes.model.Note;

/**
 * What can be done to a note without opening it.
 *
 * Which entries there are depends on the note and on who is asking: only its
 * owner can trash or destroy it, somebody it was shared with personally can
 * walk away from it, and one reaching them through a group can only be
 * archived.
 */
public final class NoteActionsDialog {

    private NoteActionsDialog() {
    }

    public static void show(@NonNull Context context,
                            @NonNull Note note,
                            boolean canOpen,
                            @NonNull Callback callback) {
        List<CharSequence> labels = new ArrayList<>();
        List<Runnable> actions = new ArrayList<>();

        if (note.isTrashed()) {
            add(labels, actions, context.getString(R.string.restore), () -> callback.onRestore(note));
            if (note.isOwner()) {
                add(labels, actions, context.getString(R.string.delete_forever), () -> callback.onDestroyForever(note));
            }
        } else {
            if (canOpen) {
                add(labels, actions, context.getString(R.string.open_note), () -> callback.onOpen(note));
            }

            // The pin is the caller's own, but the api only writes it through a
            // full save, which the server refuses on a read only share.
            if (note.canEdit()) {
                add(labels, actions, context.getString(note.isPinned() ? R.string.unpin : R.string.pin),
                        () -> callback.onTogglePin(note));
            }

            add(labels, actions, context.getString(note.hasReminder() ? R.string.edit_reminder : R.string.set_reminder),
                    () -> callback.onSetReminder(note));
            if (note.hasReminder()) {
                add(labels, actions, context.getString(R.string.remove_reminder), () -> callback.onRemoveReminder(note));
            }

            if (note.isOwner() || note.canReshare()) {
                add(labels, actions, context.getString(R.string.shares), () -> callback.onShare(note));
            }

            add(labels, actions, context.getString(note.isArchived() ? R.string.unarchive : R.string.archive),
                    () -> callback.onToggleArchive(note));

            if (note.isOwner()) {
                add(labels, actions, context.getString(R.string.move_to_trash), () -> callback.onTrash(note));
            }
            if (note.canLeave()) {
                add(labels, actions, context.getString(R.string.leave_note), () -> callback.onLeave(note));
            }
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle(note.getTitle().isEmpty() ? context.getString(R.string.note_actions) : note.getTitle())
                .setItems(labels.toArray(new CharSequence[0]), (dialog, which) -> {
                    dialog.dismiss();
                    actions.get(which).run();
                })
                .show();
    }

    private static void add(List<CharSequence> labels, List<Runnable> actions, String label, Runnable action) {
        labels.add(label);
        actions.add(action);
    }

    public interface Callback {
        void onOpen(@NonNull Note note);
        void onTogglePin(@NonNull Note note);
        void onSetReminder(@NonNull Note note);
        void onRemoveReminder(@NonNull Note note);
        void onShare(@NonNull Note note);
        void onToggleArchive(@NonNull Note note);
        void onTrash(@NonNull Note note);
        void onRestore(@NonNull Note note);
        void onDestroyForever(@NonNull Note note);
        void onLeave(@NonNull Note note);
    }
}
