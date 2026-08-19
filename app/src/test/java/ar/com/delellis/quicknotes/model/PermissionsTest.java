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

package ar.com.delellis.quicknotes.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PermissionsTest {

    @Test
    public void theCombinationsMeanWhatTheServerSays() {
        assertEquals(1, Permissions.CAN_VIEW);
        assertEquals(3, Permissions.CAN_EDIT);
        assertEquals(17, Permissions.CAN_VIEW_AND_RESHARE);
        assertEquals(19, Permissions.CAN_EDIT_AND_RESHARE);
    }

    @Test
    public void readsTheBitsOutOfTheMask() {
        assertFalse(Permissions.canEdit(Permissions.CAN_VIEW));
        assertTrue(Permissions.canEdit(Permissions.CAN_EDIT));
        assertFalse(Permissions.canReshare(Permissions.CAN_EDIT));
        assertTrue(Permissions.canReshare(Permissions.CAN_VIEW_AND_RESHARE));
        assertTrue(Permissions.canReshare(Permissions.CAN_EDIT_AND_RESHARE));
    }

    @Test
    public void readIsMandatoryAndNothingUnknownIsAllowed() {
        assertFalse(Permissions.isValid(0));
        assertFalse(Permissions.isValid(Permissions.UPDATE));
        assertFalse(Permissions.isValid(Permissions.CAN_EDIT | 4));
        assertTrue(Permissions.isValid(Permissions.CAN_VIEW));
        assertTrue(Permissions.isValid(Permissions.CAN_EDIT_AND_RESHARE));
    }

    @Test
    public void nobodyGivesAwayMoreThanTheyHold() {
        assertEquals(Permissions.CAN_VIEW, Permissions.clampTo(Permissions.CAN_EDIT, Permissions.CAN_VIEW));
        assertEquals(Permissions.CAN_EDIT, Permissions.clampTo(Permissions.CAN_EDIT_AND_RESHARE, Permissions.CAN_EDIT));
        assertEquals(Permissions.CAN_EDIT, Permissions.clampTo(Permissions.CAN_EDIT, Permissions.CAN_EDIT_AND_RESHARE));
    }
}
