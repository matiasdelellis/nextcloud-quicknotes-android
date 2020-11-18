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

package ar.delellis.quicknotes.activity.editor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import ar.delellis.quicknotes.R;

public class AttachBottomSheetDialog extends BottomSheetDialogFragment {

    public static final int ATTACH_TAKE_PHOTO = 0;
    public static final int ATTACH_ADD_FILE = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_attachments, container, false);

        View[] mTaggedViews = new View[4];
        mTaggedViews[0] = view.findViewById(R.id.takeAPhoto);
        mTaggedViews[0].setTag(ATTACH_TAKE_PHOTO);
        mTaggedViews[1] = view.findViewById(R.id.takeAPhotoText);
        mTaggedViews[1].setTag(ATTACH_TAKE_PHOTO);
        mTaggedViews[2] = view.findViewById(R.id.addFile);
        mTaggedViews[2].setTag(ATTACH_ADD_FILE);
        mTaggedViews[3] = view.findViewById(R.id.addFileText);
        mTaggedViews[3].setTag(ATTACH_ADD_FILE);

        OnAttachClickListener attachClickListener = new OnAttachClickListener();

        for (View item : mTaggedViews) {
            item.setOnClickListener(attachClickListener);
        }

        return view;
    }

    private class OnAttachClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            dismissAllowingStateLoss();
            ((OnAttachOptionListener) getActivity())
                    .onAttachOptionSelection((int) v.getTag());
        }
    }

    public interface OnAttachOptionListener {
        void onAttachOptionSelection(int attachOption);
    }

}
