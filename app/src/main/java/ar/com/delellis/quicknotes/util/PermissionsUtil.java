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

import android.content.Context;

import androidx.annotation.NonNull;

import ar.com.delellis.quicknotes.R;
import ar.com.delellis.quicknotes.model.Permissions;

/**
 * Putting the permission bitmask of a share into words, and back.
 */
public final class PermissionsUtil {

    /** The combinations offered to the user, in the order they are shown. */
    public static final int[] CHOICES = {
            Permissions.CAN_VIEW,
            Permissions.CAN_EDIT,
            Permissions.CAN_VIEW_AND_RESHARE,
            Permissions.CAN_EDIT_AND_RESHARE,
    };

    private PermissionsUtil() {
    }

    @NonNull
    public static String labelOf(@NonNull Context context, int permissions) {
        boolean canEdit = Permissions.canEdit(permissions);
        boolean canReshare = Permissions.canReshare(permissions);

        if (canEdit && canReshare) {
            return context.getString(R.string.permission_can_edit_and_reshare);
        }
        if (canEdit) {
            return context.getString(R.string.permission_can_edit);
        }
        if (canReshare) {
            return context.getString(R.string.permission_can_view_and_reshare);
        }
        return context.getString(R.string.permission_can_view);
    }

    /**
     * The choices somebody holding {@code available} is allowed to give away:
     * nobody can pass on more than they have.
     */
    @NonNull
    public static int[] choicesFor(int available) {
        int count = 0;
        int[] allowed = new int[CHOICES.length];
        for (int choice : CHOICES) {
            if ((choice & ~available) == 0) {
                allowed[count++] = choice;
            }
        }

        int[] trimmed = new int[count];
        System.arraycopy(allowed, 0, trimmed, 0, count);
        return trimmed;
    }

    @NonNull
    public static CharSequence[] labelsOf(@NonNull Context context, @NonNull int[] choices) {
        CharSequence[] labels = new CharSequence[choices.length];
        for (int i = 0; i < choices.length; i++) {
            labels[i] = labelOf(context, choices[i]);
        }
        return labels;
    }

    public static int indexOf(@NonNull int[] choices, int permissions) {
        for (int i = 0; i < choices.length; i++) {
            if (choices[i] == permissions) {
                return i;
            }
        }
        return -1;
    }
}
