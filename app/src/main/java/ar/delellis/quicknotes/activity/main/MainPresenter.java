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

package ar.delellis.quicknotes.activity.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import ar.delellis.quicknotes.api.ApiProvider;
import ar.delellis.quicknotes.model.Note;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;

public class MainPresenter {
    private MainView view;

    public MainPresenter(MainView view) {
        this.view = view;
    }

    public void getNotes() {
        view.showLoading();
        Call<List<Note>> call = ApiProvider.getQuicknotesAPI().getNotes();
        call.enqueue(new Callback<List<Note>>() {
            @Override
            public void onResponse(@NonNull Call<List<Note>> call, @NonNull Response<List<Note>> response) {
                ((AppCompatActivity) view).runOnUiThread(() -> {
                    view.hideLoading();
                    if (response.isSuccessful() && response.body() != null) {
                        view.onGetResult(response.body());
                    } else {
                        // Server under maintenance.
                        if (response.code() == HTTP_UNAVAILABLE) {
                            view.onErrorLoading(null);
                        }
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<List<Note>> call, @NonNull Throwable t) {
                ((AppCompatActivity) view).runOnUiThread(() -> {
                    view.hideLoading();
                    view.onErrorLoading(t.getLocalizedMessage());
                });
            }
        });
    }
}
