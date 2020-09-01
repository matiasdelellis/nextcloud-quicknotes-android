package ar.delellis.quicknotes.activity.editor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.thebluealliance.spectrum.SpectrumPalette;

import ar.delellis.quicknotes.R;
import ar.delellis.quicknotes.api.ApiProvider;

public class EditorActivity extends AppCompatActivity implements EditorView {

    EditorPresenter presenter;
    ProgressDialog progressDialog;

    protected ApiProvider mApi;

    EditText et_title;
    EditText et_content;
    SpectrumPalette palette;

    int id;
    String title;
    String content;
    String color;
    boolean is_shared;

    Menu actionMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mApi = new ApiProvider(getApplicationContext());

        et_title = findViewById(R.id.title);
        et_content = findViewById(R.id.content);
        palette = findViewById(R.id.palette);

        // Color selection
        palette.setOnColorSelectedListener(new SpectrumPalette.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color_int) {
                color = "#" + Integer.toHexString(color_int).substring(2).toUpperCase();
            }
        });

        // Create progress dialog.
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));

        presenter = new EditorPresenter(this);

        Intent intent = getIntent();
        id = intent.getIntExtra("id", 0);
        title = intent.getStringExtra("title");
        content = intent.getStringExtra("content");
        color = intent.getStringExtra("color");
        is_shared = intent.getBooleanExtra("is_shared", false);

        setDataFromIntentExtra();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_editor, menu);

        actionMenu = menu;
        if (id != 0) {
            actionMenu.findItem(R.id.delete).setVisible(!is_shared);
            actionMenu.findItem(R.id.save).setVisible(false);
            actionMenu.findItem(R.id.update).setVisible(true);
        } else {
            actionMenu.findItem(R.id.delete).setVisible(false);
            actionMenu.findItem(R.id.save).setVisible(true);
            actionMenu.findItem(R.id.update).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String title = et_title.getText().toString().trim();
        String content = et_content.getText().toString().trim();
        String color = this.color;
        switch (item.getItemId()) {
            case R.id.save:
                if (title.isEmpty()) {
                    et_title.setError(getString(R.string.enter_title));
                } else if (content.isEmpty()) {
                    et_content.setError("Please enter a note");
                } else {
                    presenter.createNote(title, content, color);
                }
                return true;
            case R.id.update:
                if (is_shared) {
                    setResult(RESULT_CANCELED);
                    finish();
                    return true;
                }
                if (title.isEmpty()) {
                    et_title.setError(getString(R.string.enter_title));
                } else if (content.isEmpty()) {
                    et_content.setError("Please enter a note");
                } else {
                    presenter.updateNote(id, title, content, color);
                }
                return true;
            case R.id.delete:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle(getString(R.string.delete_note));
                alertDialog.setMessage("Are you sure you want to delete the note?");
                alertDialog.setNegativeButton("Yes", (dialog, wich) -> {
                    dialog.dismiss();
                    presenter.deleteNote(id);
                });
                alertDialog.setPositiveButton("Cancel", ((dialog, which) -> dialog.dismiss()));
                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showProgress() {
        progressDialog.show();
    }

    @Override
    public void hideProgress() {
        progressDialog.dismiss();
    }

    @Override
    public void onRequestSuccess(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(EditorActivity.this, message, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    @Override
    public void onRequestError(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(EditorActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDataFromIntentExtra() {
        if (id != 0) {
            et_title.setText(Html.fromHtml(title));
            et_content.setText(Html.fromHtml(content));
            palette.setSelectedColor(Color.parseColor(color));
            if (is_shared) {
                readMode();
            } else {
                editMode();
            }
        } else {
            // Default color.
            palette.setSelectedColor(getResources().getColor(R.color.color_01));
            color = "#" + Integer.toHexString(R.color.color_01).substring(2).toUpperCase();
            editMode();
        }
    }

    private void editMode() {
        et_title.setFocusableInTouchMode(true);
        et_content.setFocusableInTouchMode(true);
        palette.setVisibility(View.VISIBLE);
    }

    private void readMode() {
        et_title.setFocusableInTouchMode(false);
        et_content.setFocusableInTouchMode(false);
        et_title.setFocusable(false);
        et_content.setFocusable(false);
        palette.setVisibility(View.GONE);
    }

}