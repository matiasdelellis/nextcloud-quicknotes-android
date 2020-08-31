package ar.delellis.quicknotes.activity.main;

import java.util.List;

import ar.delellis.quicknotes.model.Note;

public interface MainView {
    void showLoading();
    void hideLoading();
    void onGetResult(List<Note> notes);
    void onErrorLoading(String message);
}
