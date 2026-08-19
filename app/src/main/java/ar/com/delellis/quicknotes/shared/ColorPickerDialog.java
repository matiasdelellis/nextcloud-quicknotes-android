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

package ar.com.delellis.quicknotes.shared;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import ar.com.delellis.quicknotes.R;
import ar.com.delellis.quicknotes.databinding.ItemColorBinding;

/**
 * The palette a note can be painted with.
 *
 * This used to be a third party picker that only ever lived on jcenter. The
 * palette is ten fixed colours, so a grid of swatches over the dialog of the
 * design system is both less code and one dependency fewer.
 */
public class ColorPickerDialog {

    /** How many swatches fit across the dialog. */
    private static final int COLUMNS = 4;

    private ColorPickerDialog() {
    }

    public static void show(@NonNull Context context,
                            @Nullable String selectedColor,
                            @NonNull OnColorSelectedListener listener) {
        List<String> colors = new ArrayList<>();
        for (String color : context.getResources().getStringArray(R.array.pallete_colors)) {
            colors.add(color);
        }
        // A note painted with something no longer in the palette keeps it, and
        // shows it as one more swatch, rather than losing it on the next save.
        if (selectedColor != null && !selectedColor.isEmpty() && !colors.contains(selectedColor)) {
            colors.add(0, selectedColor);
        }

        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new GridLayoutManager(context, COLUMNS));
        int padding = context.getResources().getDimensionPixelSize(R.dimen.spacer_2x);
        recyclerView.setPadding(padding, padding, padding, padding);
        recyclerView.setClipToPadding(false);

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.select_note_color)
                .setView(recyclerView)
                .setNegativeButton(R.string.common_cancel, (d, which) -> d.dismiss())
                .create();

        recyclerView.setAdapter(new ColorAdapter(colors, selectedColor, color -> {
            dialog.dismiss();
            listener.onColorSelected(color, Color.parseColor(color));
        }));

        dialog.show();
    }

    private static class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ViewHolder> {
        @NonNull
        private final List<String> colors;
        @Nullable
        private final String selectedColor;
        @NonNull
        private final OnSwatchClickListener listener;

        ColorAdapter(@NonNull List<String> colors,
                     @Nullable String selectedColor,
                     @NonNull OnSwatchClickListener listener) {
            this.colors = colors;
            this.selectedColor = selectedColor;
            this.listener = listener;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            @NonNull
            private final ItemColorBinding binding;

            ViewHolder(@NonNull ItemColorBinding binding) {
                super(binding.getRoot());
                this.binding = binding;

                binding.colorSwatch.setOnClickListener(v -> {
                    int index = getBindingAdapterPosition();
                    if (index != RecyclerView.NO_POSITION) {
                        listener.onSwatchClick(colors.get(index));
                    }
                });
            }

            private void bind(@NonNull String color) {
                binding.colorSwatch.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor(color)));
                binding.colorSwatch.setContentDescription(color);
                binding.colorSelected.setVisibility(color.equalsIgnoreCase(selectedColor) ? View.VISIBLE : View.GONE);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(ItemColorBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(colors.get(position));
        }

        @Override
        public int getItemCount() {
            return colors.size();
        }
    }

    private interface OnSwatchClickListener {
        void onSwatchClick(@NonNull String color);
    }

    public interface OnColorSelectedListener {
        void onColorSelected(@NonNull String color, @ColorInt int colorInt);
    }
}
