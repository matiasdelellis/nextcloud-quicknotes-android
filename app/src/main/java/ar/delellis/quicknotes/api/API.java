/*
 * Nextcloud Quicknotes Android client application.
 *
 * @copyright Copyright (c) 2020 Matias De lellis <mati86dl@gmail.com>
 *
 * @author Matias De lellis <mati86dl@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
            @Body Note note
    );

    @DELETE("/notes/{id}")
    Call<Note> deleteNote(
            @Path("id") int id
    );
}
