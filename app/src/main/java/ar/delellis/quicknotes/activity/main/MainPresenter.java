package ar.delellis.quicknotes.activity.main;

import androidx.annotation.NonNull;

import java.util.List;

import ar.delellis.quicknotes.api.ApiProvider;
import ar.delellis.quicknotes.model.Note;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainPresenter {
    private MainView view;

    public MainPresenter(MainView view) {
        this.view = view;
    }

    void getData() {
        view.showLoading();
        Call<List<Note>> call = ApiProvider.getAPI().getNotes();
        call.enqueue(new Callback<List<Note>>() {
            @Override
            public void onResponse(@NonNull Call<List<Note>> call, @NonNull Response<List<Note>> response) {
                view.hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    view.onGetResult(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Note>> call, @NonNull Throwable t) {
                view.hideLoading();
                view.onErrorLoading(t.getLocalizedMessage());
            }
        });
    }
}
