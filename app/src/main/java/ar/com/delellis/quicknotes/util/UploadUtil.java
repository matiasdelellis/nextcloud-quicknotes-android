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

package ar.com.delellis.quicknotes.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Turning something the user picked into the one file part an upload is.
 *
 * Whatever the picker hands back is a content uri, and there is no path
 * behind it worth chasing: it can be a cloud provider, a scoped media store
 * entry or a file another app is holding. What always works is asking the
 * resolver for the bytes, so the file is copied into the cache first and
 * uploaded from there.
 */
public final class UploadUtil {

    private static final String PART_NAME = "file";
    private static final String FALLBACK_NAME = "attachment";
    private static final MediaType FALLBACK_TYPE = MediaType.parse("application/octet-stream");

    private UploadUtil() {
    }

    /**
     * Reads a picked file into the cache and describes it as a multipart part.
     *
     * @throws IOException when the file could not be read.
     */
    @NonNull
    public static MultipartBody.Part partFromUri(@NonNull Context context, @NonNull Uri uri) throws IOException {
        String name = queryDisplayName(context, uri);
        String mime = context.getContentResolver().getType(uri);

        File cached = copyToCache(context, uri, name);
        return partFromFile(cached, name, mime);
    }

    /**
     * The same for a file this app made itself, such as a photo just taken.
     */
    @NonNull
    public static MultipartBody.Part partFromFile(@NonNull File file, @Nullable String name, @Nullable String mime) {
        String fileName = (name != null && !name.isEmpty()) ? name : file.getName();
        MediaType mediaType = mediaTypeOf(mime, fileName);

        RequestBody body = RequestBody.create(file, mediaType);
        return MultipartBody.Part.createFormData(PART_NAME, fileName, body);
    }

    /**
     * The name the provider gives the file, which is the one the user knows it
     * by, and the one the note should show.
     */
    @NonNull
    public static String queryDisplayName(@NonNull Context context, @NonNull Uri uri) {
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            String[] projection = {OpenableColumns.DISPLAY_NAME};
            try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int column = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (column >= 0) {
                        String name = cursor.getString(column);
                        if (name != null && !name.isEmpty()) {
                            return name;
                        }
                    }
                }
            } catch (Exception e) {
                // Some providers answer nothing at all; the fallback below is fine.
            }
        }

        String lastSegment = uri.getLastPathSegment();
        return (lastSegment != null && !lastSegment.isEmpty()) ? lastSegment : FALLBACK_NAME;
    }

    private static File copyToCache(@NonNull Context context, @NonNull Uri uri, @NonNull String name) throws IOException {
        File uploads = new File(context.getCacheDir(), "uploads");
        if (!uploads.exists() && !uploads.mkdirs()) {
            throw new IOException("Could not make room for the upload");
        }

        File target = new File(uploads, name);
        try (InputStream input = context.getContentResolver().openInputStream(uri)) {
            if (input == null) {
                throw new IOException("Could not read " + uri);
            }
            try (OutputStream output = new FileOutputStream(target)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
            }
        }
        return target;
    }

    @NonNull
    private static MediaType mediaTypeOf(@Nullable String mime, @NonNull String fileName) {
        MediaType mediaType = mime != null ? MediaType.parse(mime) : null;
        if (mediaType == null) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(fileName);
            String guessed = extension != null
                    ? MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase(Locale.ROOT))
                    : null;
            mediaType = guessed != null ? MediaType.parse(guessed) : null;
        }
        return mediaType != null ? mediaType : FALLBACK_TYPE;
    }
}
