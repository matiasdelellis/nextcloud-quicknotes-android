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

import androidx.annotation.Nullable;

/**
 * Making a url of this app's own reachable through the Nextcloud SSO
 * transport.
 *
 * Everything loaded through SSO goes to the Files app, which hands the path to
 * the url helper of the nextcloud-commons libraries. That helper knows three
 * prefixes — {@code /index.php}, {@code /ocs} and {@code /remote.php} — and
 * takes anything else for a file inside WebDAV, rewriting it to
 * {@code /remote.php/webdav/<path>}.
 *
 * A server with {@code htaccess.IgnoreFrontController} on — pretty urls — hands
 * out app links without the {@code /index.php}, so the preview endpoint of this
 * app arrives as {@code /apps/quicknotes/…} and is asked of WebDAV, which
 * answers that no such file exists. Putting the front controller back in makes
 * the url say plainly what it is. Nextcloud serves {@code /index.php/apps/…}
 * whether pretty urls are on or off, so this is safe on every instance.
 */
public final class SsoUrlUtil {

    private static final String FRONT_CONTROLLER = "/index.php";

    /** The prefixes the url helper of the SSO libraries recognises. */
    private static final String[] KNOWN_PREFIXES = {FRONT_CONTROLLER, "/ocs", "/remote.php"};

    private SsoUrlUtil() {
    }

    /**
     * @param url an absolute url of this Nextcloud instance.
     * @return the same url, with the front controller in the path when it was
     *         missing. Anything this cannot make sense of is handed back as it
     *         came.
     */
    @Nullable
    public static String withFrontController(@Nullable String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        int schemeEnd = url.indexOf("://");
        if (schemeEnd < 0) {
            return url;
        }

        int pathStart = url.indexOf('/', schemeEnd + "://".length());
        if (pathStart < 0) {
            return url;
        }

        String path = url.substring(pathStart);
        int queryStart = path.indexOf('?');
        String pathOnly = queryStart >= 0 ? path.substring(0, queryStart) : path;

        for (String prefix : KNOWN_PREFIXES) {
            if (pathOnly.startsWith(prefix)) {
                return url;
            }
        }

        return url.substring(0, pathStart) + FRONT_CONTROLLER + path;
    }
}
