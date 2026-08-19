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

/**
 * The permissions a share grants, as the bitmask the server uses
 * (see OCP\Constants). Read is always part of it.
 */
public final class Permissions {

    public static final int READ = 1;
    public static final int UPDATE = 2;
    public static final int SHARE = 16;

    /** Can view the note, and nothing else. */
    public static final int CAN_VIEW = READ;
    /** Can view and change the title and the content. */
    public static final int CAN_EDIT = READ | UPDATE;
    /** Can view it, and pass it on. */
    public static final int CAN_VIEW_AND_RESHARE = READ | SHARE;
    /** Can change it, and pass it on. */
    public static final int CAN_EDIT_AND_RESHARE = READ | UPDATE | SHARE;

    private Permissions() {
    }

    public static boolean canEdit(int permissions) {
        return (permissions & UPDATE) != 0;
    }

    public static boolean canReshare(int permissions) {
        return (permissions & SHARE) != 0;
    }

    /**
     * Whether the server would take these permissions. Read is mandatory, and
     * nothing outside of the three known bits is.
     */
    public static boolean isValid(int permissions) {
        return (permissions & READ) != 0 &&
               (permissions & ~(READ | UPDATE | SHARE)) == 0;
    }

    /**
     * The same permissions, with whatever the sharer does not hold taken out.
     * Nobody can give away more than they have.
     */
    public static int clampTo(int permissions, int available) {
        return (permissions & available) | READ;
    }
}
