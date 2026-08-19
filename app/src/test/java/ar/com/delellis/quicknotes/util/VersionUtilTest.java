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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ar.com.delellis.quicknotes.model.Capabilities;

public class VersionUtilTest {

    @Test
    public void anOlderApiIsNotEnough() {
        assertFalse(VersionUtil.isAtLeast("1.4", Capabilities.REQUIRED_API_VERSION));
        assertFalse(VersionUtil.isAtLeast("1.0", Capabilities.REQUIRED_API_VERSION));
        assertFalse(VersionUtil.isAtLeast("0.9", Capabilities.REQUIRED_API_VERSION));
    }

    @Test
    public void theRequiredApiAndNewerAreFine() {
        assertTrue(VersionUtil.isAtLeast("1.5", Capabilities.REQUIRED_API_VERSION));
        assertTrue(VersionUtil.isAtLeast("1.6", Capabilities.REQUIRED_API_VERSION));
        assertTrue(VersionUtil.isAtLeast("2.0", Capabilities.REQUIRED_API_VERSION));
        assertTrue(VersionUtil.isAtLeast("1.10", Capabilities.REQUIRED_API_VERSION));
    }

    @Test
    public void aMissingVersionIsNeverGoodEnough() {
        assertFalse(VersionUtil.isAtLeast("", Capabilities.REQUIRED_API_VERSION));
        assertFalse(VersionUtil.isAtLeast(null, Capabilities.REQUIRED_API_VERSION));
    }

    @Test
    public void partsThatOneSideDoesNotHaveCountAsZero() {
        assertTrue(VersionUtil.isAtLeast("1.5.0", "1.5"));
        assertTrue(VersionUtil.isAtLeast("1.5", "1.5.0"));
        assertTrue(VersionUtil.isAtLeast("1.5.1", "1.5"));
    }
}
