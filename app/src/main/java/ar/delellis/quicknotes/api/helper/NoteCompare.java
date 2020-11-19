package ar.delellis.quicknotes.api.helper;

import ar.delellis.quicknotes.model.Note;

public final class NoteCompare {
    public static boolean compareNotes(Note a, Note b) {

        // Lets first check if one field is null and the other not (difference between a and b)
        // This is so we don't run into a NPE later when accessing methods of fields.
        if (a.getTitle() == null ^ b.getTitle() == null
                || a.getTags() == null ^ b.getTags() == null
                || a.getColor() == null ^ b.getColor() == null
                || a.getAttachtments() == null ^ b.getAttachtments() == null
                || a.getContent() == null ^ b.getContent() == null) {
            return true;
        }

        // Now lets compare the fields itself
        return ((a.getTitle() != null
                && !a.getTitle().equals(b.getTitle()))
                || (a.getTags() != null
                && a.getTags().size() != b.getTags().size())
                || (a.getColor() != null
                && !a.getColor().equals(b.getColor()))
                || a.getAttachtments() != null
                && a.getAttachtments().size() != b.getAttachtments().size())
                || (a.getContent() != null
                && !a.getContent().equals(b.getContent()))
                || a.getIsPinned() != b.getIsPinned()
                || a.getIsShared() != b.getIsShared();
    }
}
