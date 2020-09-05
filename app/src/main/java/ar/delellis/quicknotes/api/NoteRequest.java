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

// int $id, string $title, string $content, array $attachts, bool $pinned, array $tags, string $color = "#F7EB96"
public class NoteRequest {
    int id;
    String title;
    String content;
    String color;
    boolean pinned;
    final String[] attachts = {}; //Fake response
    final String[] tags = {}; // Fake response
    final String[] shared_with = {}; // Fake response

    public NoteRequest(int id, String title, String content, String color, boolean pinned) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.color = color;
        this.pinned = pinned;
    }
}
