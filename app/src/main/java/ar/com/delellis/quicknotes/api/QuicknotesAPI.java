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

package ar.com.delellis.quicknotes.api;

import java.util.List;

import ar.com.delellis.quicknotes.api.request.NoteRequest;
import ar.com.delellis.quicknotes.api.request.PermissionsRequest;
import ar.com.delellis.quicknotes.api.request.ReminderRequest;
import ar.com.delellis.quicknotes.api.request.ShareRequest;
import ar.com.delellis.quicknotes.model.AttachmentInfo;
import ar.com.delellis.quicknotes.model.Note;
import ar.com.delellis.quicknotes.model.Share;
import ar.com.delellis.quicknotes.model.Sharee;
import ar.com.delellis.quicknotes.model.TrashResult;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * The `/api/v1` surface of the server side Quick notes app.
 *
 * The endpoints that answer with nothing answer with an empty json array,
 * which is why they are typed as Object here: there is no body worth a name.
 */
public interface QuicknotesAPI {
    String API_ENDPOINT = "/apps/quicknotes/api/v1";

    /** Every note the caller can see, archived and trashed ones included. */
    @GET("/notes")
    Call<List<Note>> getNotes();

    @GET("/notes/{id}")
    Call<Note> getNote(
            @Path("id") int id
    );

    @POST("/notes")
    Call<Note> createNote(
            @Body NoteRequest note
    );

    /**
     * Saves a note. Send the etag the note was last read with in If-Match to
     * be told about a concurrent edit (412) instead of overwriting it.
     */
    @PUT("/notes/{id}")
    Call<Note> updateNote(
            @Path("id") int id,
            @Header("If-Match") String ifMatch,
            @Body NoteRequest note
    );

    /** Destroys a note. Only its owner can. */
    @DELETE("/notes/{id}")
    Call<Object> destroyNote(
            @Path("id") int id
    );

    /** Destroys every note of the caller that is in the trash. */
    @DELETE("/notes/trash")
    Call<TrashResult> emptyTrash();

    /** Takes the note off the caller's board, without touching anybody else's. */
    @POST("/notes/{id}/archive")
    Call<Note> archiveNote(
            @Path("id") int id
    );

    @POST("/notes/{id}/unarchive")
    Call<Note> unarchiveNote(
            @Path("id") int id
    );

    /** Moves a note to the trash. The owner's alone. */
    @POST("/notes/{id}/trash")
    Call<Note> trashNote(
            @Path("id") int id
    );

    @POST("/notes/{id}/restore")
    Call<Note> restoreNote(
            @Path("id") int id
    );

    /** Sets, moves or cancels the reminder of the caller on a note. */
    @PUT("/notes/{id}/reminder")
    Call<Note> setReminder(
            @Path("id") int id,
            @Body ReminderRequest reminder
    );

    /** The shares of a note, for its owner or for somebody who may reshare. */
    @GET("/notes/{noteId}/shares")
    Call<List<Share>> getShares(
            @Path("noteId") int noteId
    );

    @POST("/notes/{noteId}/shares")
    Call<Share> createShare(
            @Path("noteId") int noteId,
            @Body ShareRequest share
    );

    /** Walks away from a note somebody shared with the caller personally. */
    @DELETE("/notes/{noteId}/shares/self")
    Call<Object> leaveNote(
            @Path("noteId") int noteId
    );

    @PUT("/shares/{shareId}")
    Call<Share> updateShare(
            @Path("shareId") int shareId,
            @Body PermissionsRequest permissions
    );

    @DELETE("/shares/{shareId}")
    Call<Object> deleteShare(
            @Path("shareId") int shareId
    );

    /** Who this note could still be shared with. */
    @GET("/notes/{noteId}/sharees")
    Call<List<Sharee>> getSharees(
            @Path("noteId") int noteId,
            @Query("search") String search,
            @Query("limit") int limit
    );

    /**
     * Uploads a file into the Files of the caller. Attaching it is a separate
     * step: send the file_id in the attachments of a save.
     */
    @Multipart
    @POST("/attachments")
    Call<AttachmentInfo> uploadAttachment(
            @Part MultipartBody.Part file
    );

    /** Describes a file the caller already has, by its path in Files. */
    @GET("/attachments/info")
    Call<AttachmentInfo> getAttachmentInfo(
            @Query("path") String path
    );
}
