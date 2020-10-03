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

package ar.delellis.quicknotes.util;

import android.graphics.drawable.Drawable;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.core.graphics.drawable.DrawableCompat;

public class ColorUtil {

    public static void imageViewTintColor(ImageView imageView, @ColorInt int color) {
        Drawable normalDrawable = imageView.getDrawable();
        Drawable wrapDrawable = DrawableCompat.wrap(normalDrawable);
        DrawableCompat.setTint(wrapDrawable,  color);
        imageView.setImageDrawable(wrapDrawable);
    }

    public static void menuItemTintColor(MenuItem item, @ColorInt int color) {
        Drawable normalDrawable = item.getIcon();
        Drawable wrapDrawable = DrawableCompat.wrap(normalDrawable);
        DrawableCompat.setTint(wrapDrawable, color);
        item.setIcon(wrapDrawable);
    }

    public static String getRGBColorFromInt (@ColorInt int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }
}
