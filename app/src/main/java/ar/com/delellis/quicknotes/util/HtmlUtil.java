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

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

public class HtmlUtil {

    /**
     * Remove all html tags, and return the text.
     * @param htmlString html to clean.
     * @return title
     */
    public static String cleanString(String htmlString) {
        return Jsoup.clean(htmlString.trim(), noneWhitelist());
    }

    /**
     * Clean the html tags, and remove any unnecessary attributes
     * @param htmlString html to clean
     * @return clean html
     */
    public static String cleanHtml(String htmlString) {
        return Jsoup.clean(htmlString.trim(), basicWhitelist());
    }

    /**
     *  @return whitelist
     */
    public static Whitelist noneWhitelist() {
        return Whitelist.none();
    }

    /**
     *  @return whitelist
     */
    public static Whitelist basicWhitelist() {
        return new Whitelist()
                .addTags("p", "br")
                .addTags("b", "strong")
                .addTags("i")
                .addTags("u")
                .addTags("s", "strike")
                .addTags("li", "ol", "ul")
                .addTags("blockquote", "pre")
                .addAttributes("a", "href")
                .addProtocols("a", "href", "ftp", "http", "https", "mailto")
                .addEnforcedAttribute("a", "rel", "nofollow")
                ;
    }

}
