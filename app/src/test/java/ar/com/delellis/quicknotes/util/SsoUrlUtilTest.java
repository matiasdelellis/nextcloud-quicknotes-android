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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class SsoUrlUtilTest {

    @Test
    public void putsTheFrontControllerBackOnAnAppUrl() {
        // What a server with pretty urls hands out, and what the url helper of
        // the SSO libraries would otherwise ask WebDAV for.
        assertEquals(
                "https://delellis.com.ar/index.php/apps/quicknotes/api/v1/notes/227/attachments/187228/preview?x=512&y=512",
                SsoUrlUtil.withFrontController(
                        "https://delellis.com.ar/apps/quicknotes/api/v1/notes/227/attachments/187228/preview?x=512&y=512"));
    }

    @Test
    public void leavesAlonePathsTheHelperAlreadyKnows() {
        String withFrontController = "https://cloud.example.com/index.php/apps/quicknotes/api/v1/notes/1/attachments/2/preview";
        assertEquals(withFrontController, SsoUrlUtil.withFrontController(withFrontController));

        String ocs = "https://cloud.example.com/ocs/v2.php/cloud/capabilities";
        assertEquals(ocs, SsoUrlUtil.withFrontController(ocs));

        String webdav = "https://cloud.example.com/remote.php/webdav/Photos/Birdie.jpg";
        assertEquals(webdav, SsoUrlUtil.withFrontController(webdav));
    }

    @Test
    public void keepsTheQueryStringWhereItWas() {
        assertEquals("https://cloud.example.com/index.php/core/preview?fileId=7&x=32",
                SsoUrlUtil.withFrontController("https://cloud.example.com/core/preview?fileId=7&x=32"));
    }

    @Test
    public void survivesAnythingItCannotMakeSenseOf() {
        assertNull(SsoUrlUtil.withFrontController(null));
        assertEquals("", SsoUrlUtil.withFrontController(""));
        assertEquals("not a url", SsoUrlUtil.withFrontController("not a url"));
        // No path to speak of.
        assertEquals("https://cloud.example.com", SsoUrlUtil.withFrontController("https://cloud.example.com"));
    }

    @Test
    public void doesNotConfuseTheHostWithThePath() {
        // The host contains "index.php" nowhere, but the check must look at
        // the path and not at the url as a whole.
        assertEquals("https://ocs.example.com/index.php/apps/quicknotes/x",
                SsoUrlUtil.withFrontController("https://ocs.example.com/apps/quicknotes/x"));
    }
}
