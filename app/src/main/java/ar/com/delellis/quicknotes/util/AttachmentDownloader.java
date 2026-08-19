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
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.nextcloud.android.sso.aidl.NextcloudRequest;
import com.nextcloud.android.sso.api.NextcloudAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ar.com.delellis.quicknotes.api.ApiProvider;
import ar.com.delellis.quicknotes.api.QuicknotesAPI;
import ar.com.delellis.quicknotes.model.Attachment;

/**
 * Getting the bytes of an attachment onto the phone.
 *
 * For a note somebody shared, this is the only way to them: the file is not in
 * the caller's Files, and the app deliberately does not put it there.
 *
 * It does not go through Retrofit. The SSO transport hands every answer to
 * Gson — {@code @Streaming} is logged and otherwise ignored — so a file would
 * be parsed as json and fail. What sso-glide does for thumbnails is what works
 * here too: ask the Files app for the request and read the stream it returns.
 */
public final class AttachmentDownloader {

    private static final String TAG = AttachmentDownloader.class.getCanonicalName();

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private static final String FALLBACK_NAME = "attachment";
    private static final String FALLBACK_MIME = "application/octet-stream";

    private AttachmentDownloader() {
    }

    public interface Callback {
        /**
         * The file is on the phone and can be handed to whatever opens it.
         *
         * @param uri  a content uri of this app, readable by the app it is
         *             passed to.
         * @param mime what to tell that app the file is.
         */
        void onReadyToOpen(@NonNull Uri uri, @NonNull String mime);

        /**
         * @param whereTo the name the file was kept under, to say so.
         */
        void onSaved(@NonNull String whereTo);

        void onFailed(@Nullable String message);
    }

    /** Downloads into the cache, to be opened once and forgotten. */
    public static void open(@NonNull Context context,
                            @NonNull Attachment attachment,
                            @NonNull Callback callback) {
        download(context, attachment, false, callback);
    }

    /** Downloads into the Downloads of the phone, to be kept. */
    public static void saveToDownloads(@NonNull Context context,
                                       @NonNull Attachment attachment,
                                       @NonNull Callback callback) {
        download(context, attachment, true, callback);
    }

    private static void download(@NonNull Context context,
                                 @NonNull Attachment attachment,
                                 boolean keep,
                                 @NonNull Callback callback) {
        final Context appContext = context.getApplicationContext();
        final String name = nameOf(attachment);
        final String mime = mimeOf(attachment);

        EXECUTOR.execute(() -> {
            NextcloudAPI api = ApiProvider.getNextcloudAPI();
            if (api == null) {
                fail(callback, null);
                return;
            }

            NextcloudRequest request = new NextcloudRequest.Builder()
                    .setMethod("GET")
                    .setUrl(QuicknotesAPI.API_ENDPOINT + "/notes/" + attachment.getNoteId()
                            + "/attachments/" + attachment.getFileId() + "/download")
                    .build();

            try (InputStream body = api.performNetworkRequestV2(request).getBody()) {
                if (keep) {
                    saveStream(appContext, body, name, mime);
                    MAIN.post(() -> callback.onSaved(name));
                } else {
                    Uri uri = cacheStream(appContext, body, name);
                    MAIN.post(() -> callback.onReadyToOpen(uri, mime));
                }
            } catch (Exception e) {
                Log.w(TAG, "Could not download " + name, e);
                fail(callback, e.getLocalizedMessage());
            }
        });
    }

    private static void fail(@NonNull Callback callback, @Nullable String message) {
        MAIN.post(() -> callback.onFailed(message));
    }

    /**
     * Writes the file where another app can be handed it, which is the cache
     * of this one, reached through its file provider.
     */
    @NonNull
    private static Uri cacheStream(@NonNull Context context,
                                   @NonNull InputStream body,
                                   @NonNull String name) throws IOException {
        File folder = new File(context.getCacheDir(), "attachments");
        if (!folder.exists() && !folder.mkdirs()) {
            throw new IOException("Could not make room for the download");
        }

        File target = new File(folder, name);
        try (OutputStream out = new FileOutputStream(target)) {
            copy(body, out);
        }

        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", target);
    }

    /**
     * Keeps the file among the downloads of the phone. From Android 10 that is
     * the media store, which asks for no permission; before it, a folder of
     * this app's own on the shared storage, which asks for none either.
     */
    private static void saveStream(@NonNull Context context,
                                   @NonNull InputStream body,
                                   @NonNull String name,
                                   @NonNull String mime) throws IOException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            File folder = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (folder == null) {
                throw new IOException("There is no shared storage to write to");
            }
            try (OutputStream out = new FileOutputStream(new File(folder, name))) {
                copy(body, out);
            }
            return;
        }

        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, name);
        values.put(MediaStore.Downloads.MIME_TYPE, mime);
        // Held back from other apps until the bytes are all there.
        values.put(MediaStore.Downloads.IS_PENDING, 1);

        Uri item = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (item == null) {
            throw new IOException("The downloads of the phone would not take the file");
        }

        try (OutputStream out = resolver.openOutputStream(item)) {
            if (out == null) {
                throw new IOException("Could not write " + name);
            }
            copy(body, out);
        } catch (IOException e) {
            resolver.delete(item, null, null);
            throw e;
        }

        values.clear();
        values.put(MediaStore.Downloads.IS_PENDING, 0);
        resolver.update(item, values, null, null);
    }

    private static void copy(@NonNull InputStream in, @NonNull OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    @NonNull
    private static String nameOf(@NonNull Attachment attachment) {
        String basename = attachment.getBasename();
        return (basename != null && !basename.trim().isEmpty()) ? basename : FALLBACK_NAME;
    }

    @NonNull
    private static String mimeOf(@NonNull Attachment attachment) {
        String mime = attachment.getMime();
        return (mime != null && !mime.trim().isEmpty()) ? mime : FALLBACK_MIME;
    }
}
