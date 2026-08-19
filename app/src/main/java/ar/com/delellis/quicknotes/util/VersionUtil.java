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

/**
 * Comparing the dotted version strings the server publishes.
 */
public final class VersionUtil {

    private VersionUtil() {
    }

    /**
     * Whether {@code version} is {@code minimum} or newer. An empty or
     * unreadable version is never good enough.
     */
    public static boolean isAtLeast(String version, String minimum) {
        return compare(version, minimum) >= 0;
    }

    /**
     * The usual three-way comparison, part by part. Parts that are not
     * numbers, and parts that one of the two does not have, count as zero.
     */
    public static int compare(String left, String right) {
        String[] leftParts = split(left);
        String[] rightParts = split(right);

        int length = Math.max(leftParts.length, rightParts.length);
        for (int i = 0; i < length; i++) {
            int leftPart = i < leftParts.length ? toInt(leftParts[i]) : 0;
            int rightPart = i < rightParts.length ? toInt(rightParts[i]) : 0;
            if (leftPart != rightPart) {
                return Integer.compare(leftPart, rightPart);
            }
        }
        return 0;
    }

    private static String[] split(String version) {
        if (version == null || version.trim().isEmpty()) {
            return new String[0];
        }
        return version.trim().split("\\.");
    }

    private static int toInt(String part) {
        try {
            return Integer.parseInt(part.replaceAll("\\D.*$", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
