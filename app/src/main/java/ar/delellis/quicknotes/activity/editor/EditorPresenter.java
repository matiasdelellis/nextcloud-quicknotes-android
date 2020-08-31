package ar.delellis.quicknotes.activity.editor;

import androidx.annotation.NonNull;

import ar.delellis.quicknotes.api.ApiProvider;
import ar.delellis.quicknotes.api.NoteRequest;
import ar.delellis.quicknotes.model.Note;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditorPresenter {

    private EditorView view;

    public EditorPresenter(EditorView view) {
        this.view = view;
    }

    void createNote(final String title, final String content, final String color) {
        view.showProgress();

        Call<Note> call = ApiProvider.getAPI().create(title, content, color);
        call.enqueue(new Callback<Note>() {
            @Override
            public void onResponse(Call<Note> call, Response<Note> response) {
                view.hideProgress();
                if (response.isSuccessful() && response.body() != null) {
                    view.onRequestSuccess(String.format("Save new note: %s", response.body().getTitle()));
                } else {
                    view.onRequestError("Error");
                }
            }

            @Override
            public void onFailure(Call<Note> call, Throwable t) {
                view.hideProgress();
                view.onRequestError(t.getLocalizedMessage());
            }
        });
    }

    void updateNote(int id, final String title, final String content, final String color) {
        view.showProgress();

        NoteRequest note = new NoteRequest(id, title, content, color);

        Call<Note> call = ApiProvider.getAPI().updateNote(id, note);
        call.enqueue(new Callback<Note>() {
            @Override
            public void onResponse(@NonNull Call<Note> call, @NonNull Response<Note> response) {
                view.hideProgress();

                if (response.isSuccessful() && response.body() != null) {
                    view.onRequestSuccess(String.format("Note '%s' saved", response.body().getTitle()));
                } else {
                    view.onRequestError("Error");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Note> call, @NonNull Throwable t) {
                view.hideProgress();
                view.onRequestError(t.getLocalizedMessage());
            }
        });

    }

    void deleteNote(int id) {
        view.showProgress();

        Call<Note> call = ApiProvider.getAPI().deleteNote(id);
        call.enqueue(new Callback<Note>() {
            @Override
            public void onResponse(@NonNull Call<Note> call, @NonNull Response<Note> response) {
                view.hideProgress();
                view.onRequestSuccess("Note deleted...");
            }
            @Override
            public void onFailure(@NonNull Call<Note> call, @NonNull Throwable t) {
                view.hideProgress();
                view.onRequestError(t.getLocalizedMessage());
            }
        });

    }
}
