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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

public class SearchUtilTest {

    @Test
    public void foldsCaseAndAccentsTogether() {
        assertEquals("cafe", SearchUtil.normalize("Café"));
        assertEquals("cafe", SearchUtil.normalize("CAFÉ"));
        assertEquals("manana", SearchUtil.normalize("Mañana"));
        assertEquals("accion insolita", SearchUtil.normalize("Acción Insólita"));
    }

    @Test
    public void cafeFindsCafe() {
        String haystack = SearchUtil.normalize("Comprar café en el almacén");

        assertTrue(SearchUtil.matchesAll(haystack, SearchUtil.tokenize("cafe")));
        assertTrue(SearchUtil.matchesAll(haystack, SearchUtil.tokenize("CAFÉ")));
        assertTrue(SearchUtil.matchesAll(haystack, SearchUtil.tokenize("almacen")));
    }

    @Test
    public void everyWordHasToAppearButInAnyOrder() {
        String haystack = SearchUtil.normalize("Comprar café en el almacén");

        assertTrue(SearchUtil.matchesAll(haystack, SearchUtil.tokenize("cafe almacen")));
        assertTrue(SearchUtil.matchesAll(haystack, SearchUtil.tokenize("almacen cafe")));
        assertFalse(SearchUtil.matchesAll(haystack, SearchUtil.tokenize("cafe verdulería")));
    }

    @Test
    public void anEmptyQueryMatchesEverything() {
        List<String> none = SearchUtil.tokenize("   ");

        assertTrue(none.isEmpty());
        assertTrue(SearchUtil.matchesAll(SearchUtil.normalize("cualquier cosa"), none));
    }

    @Test
    public void extraSpacesAreNotWords() {
        assertEquals(2, SearchUtil.tokenize("  cafe   almacen  ").size());
    }

    @Test
    public void survivesNothingAtAll() {
        assertEquals("", SearchUtil.normalize(null));
        assertEquals("", SearchUtil.normalize(""));
        assertTrue(SearchUtil.tokenize(null).isEmpty());
    }
}
