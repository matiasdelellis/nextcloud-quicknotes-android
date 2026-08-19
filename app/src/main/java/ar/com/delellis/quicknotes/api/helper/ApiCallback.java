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

package ar.com.delellis.quicknotes.api.helper;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import ar.com.delellis.quicknotes.model.ApiError;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A retrofit callback that answers on the main thread, and that tells a failed
 * request from a request that never left.
 *
 * The Nextcloud SSO transport hands the answer over on a thread of its own, so
 * everything has to be posted back before it can touch a view.
 */
public abstract class ApiCallback<T> implements Callback<T> {

    /** What {@link #onError} gets when the request never reached the server. */
    public static final int NO_STATUS = 0;

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    public abstract void onSuccess(T result);

    /**
     * @param statusCode what the server answered, or {@link #NO_STATUS}.
     * @param message    what it said about it, when it said anything.
     * @param body       the error body, for whoever wants to read more out of
     *                   it. Already consumed by the time this returns.
     */
    public abstract void onError(int statusCode, @Nullable String message, @Nullable String body);

    @Override
    public final void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful()) {
            final T body = response.body();
            HANDLER.post(() -> onSuccess(body));
            return;
        }

        final String body = readBody(response);
        final String message = readMessage(body);
        final int code = response.code();
        HANDLER.post(() -> onError(code, message, body));
    }

    @Override
    public final void onFailure(Call<T> call, Throwable t) {
        final String message = t.getLocalizedMessage();
        HANDLER.post(() -> onError(NO_STATUS, message, null));
    }

    @Nullable
    private String readBody(Response<T> response) {
        try (ResponseBody errorBody = response.errorBody()) {
            return errorBody != null ? errorBody.string() : null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * The message out of an error body, which is {@code {"message": "…"}} all
     * over this api. Answers null for a body that is not one of those.
     */
    @Nullable
    public static String readMessage(@Nullable String body) {
        if (body == null || body.trim().isEmpty()) {
            return null;
        }
        try {
            ApiError error = new Gson().fromJson(body, ApiError.class);
            return error != null ? error.getMessage() : null;
        } catch (JsonSyntaxException e) {
            return null;
        }
    }
}
