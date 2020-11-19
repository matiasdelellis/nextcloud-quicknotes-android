package ar.delellis.quicknotes.api.helper;

import com.google.gson.Gson;

import ar.delellis.quicknotes.model.Note;

/**
 * Helper to compare different versions of notes for major changes
 */
public final class NoteCompare {

    /**
     * Compare specific fields from two Note elements.
     * If there is any difference or fields are present in one,
     * but not the other objects, this will return true.
     *
     * @param shadowCopy saved copy Note
     * @param current    currently edited Note
     * @return true if there is shadowCopy difference in the relevant fields
     */
    public static boolean compareNotes(Note shadowCopy, Note current) {

        // Lets first check if one field is null and the other not (difference between shadowCopy and current)
        // This is so we don't run into shadowCopy NPE later when accessing methods of fields.
        if (shadowCopy.getTitle() == null ^ current.getTitle() == null
                || shadowCopy.getTags() == null ^ current.getTags() == null
                || shadowCopy.getColor() == null ^ current.getColor() == null
                || shadowCopy.getAttachtments() == null ^ current.getAttachtments() == null
                || shadowCopy.getContent() == null ^ current.getContent() == null) {
            return true;
        }

        // Now lets compare the fields itself
        return ((shadowCopy.getTitle() != null
                && !shadowCopy.getTitle().equals(current.getTitle()))
                || (shadowCopy.getTags() != null
                && shadowCopy.getTags().size() != current.getTags().size())
                || (shadowCopy.getColor() != null
                && !shadowCopy.getColor().equals(current.getColor()))
                || shadowCopy.getAttachtments() != null
                && shadowCopy.getAttachtments().size() != current.getAttachtments().size())
                || (shadowCopy.getContent() != null
                && !shadowCopy.getContent().equals(current.getContent()))
                || shadowCopy.getIsPinned() != current.getIsPinned()
                || shadowCopy.getIsShared() != current.getIsShared();
    }

    /**
     * serialize the object and create a new object from the serialization string
     *
     * @param noteToCopyFrom Note to base our copy on
     * @return a new Note object which is a copy from the input but no reference
     */
    public static Note createNoteCopy(Note noteToCopyFrom) {
        Gson gson = new Gson(); // We create a new instance every time to save some memory... :)
        return gson.fromJson(gson.toJson(noteToCopyFrom), Note.class);
    }
}
