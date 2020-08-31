package ar.delellis.quicknotes.api;

import java.util.List;

import ar.delellis.quicknotes.model.Note;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface API {
    String mApiEndpoint = "/index.php/apps/quicknotes/api/v1";

    @GET("/notes")
    Call<List<Note>> getNotes();

    @FormUrlEncoded
    @POST("/notes")
    Call<Note> create(
            @Field("title") String title,
            @Field("content") String content,
            @Field("color") String color
    );

    @PUT("/notes/{id}")
    Call<Note> updateNote(
            @Path("id") int id,
            @Body NoteRequest noteRequest
    );

    @DELETE("/notes/{id}")
    Call<Note> deleteNote(
            @Path("id") int id
    );
}
