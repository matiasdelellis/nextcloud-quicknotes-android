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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Matching what the user typed against a note, the same way the web interface
 * of the app does it.
 *
 * Case and accents are ignored, so "cafe" finds "Café", and several words all
 * have to appear, in any order, anywhere in what the note is made of — its
 * title, its text and the names of its tags.
 */
public final class SearchUtil {

    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private SearchUtil() {
    }

    /**
     * Folds a string down to what two words are compared by: lower case, and
     * with the accents taken off the letters that carry them.
     */
    @NonNull
    public static String normalize(@Nullable String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        // Splitting each accented letter into letter plus mark is what makes
        // the marks something that can be dropped on their own.
        String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD);
        return COMBINING_MARKS.matcher(decomposed).replaceAll("").toLowerCase(Locale.ROOT);
    }

    /**
     * The words of a query, already folded. An empty query has no words, which
     * is what makes it match everything.
     */
    @NonNull
    public static List<String> tokenize(@Nullable String query) {
        List<String> tokens = new ArrayList<>();
        String normalized = normalize(query).trim();
        if (normalized.isEmpty()) {
            return tokens;
        }
        for (String token : WHITESPACE.split(normalized)) {
            if (!token.isEmpty()) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    /**
     * Whether every word of the query is somewhere in {@code haystack}, which
     * must already be normalized.
     */
    public static boolean matchesAll(@NonNull String haystack, @NonNull List<String> tokens) {
        for (String token : tokens) {
            if (!haystack.contains(token)) {
                return false;
            }
        }
        return true;
    }
}
