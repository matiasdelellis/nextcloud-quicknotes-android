package ar.delellis.quicknotes.activity.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import ar.delellis.quicknotes.R;
import ar.delellis.quicknotes.activity.editor.EditorActivity;
import ar.delellis.quicknotes.api.ApiProvider;
import ar.delellis.quicknotes.model.Note;

public class MainActivity extends AppCompatActivity implements MainView {

    private static final int INTENT_ADD = 100;
    private static final int INTENT_EDIT = 200;

    FloatingActionButton fab;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefresh;

    MainPresenter presenter;
    MainAdapter adapter;
    MainAdapter.ItemClickListener itemClickListener;

    List<Note> note;

    protected ApiProvider mApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefresh = findViewById(R.id.swipe_refresh);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        presenter = new MainPresenter(this);

        swipeRefresh.setOnRefreshListener(
                () -> presenter.getData()
        );

        itemClickListener = ((view, position) -> {
            int id = note.get(position).getId();
            String title = note.get(position).getTitle();
            String content = note.get(position).getContent();
            String color = note.get(position).getColor();
            boolean is_shared = note.get(position).getIsShared();

            Intent intent = new Intent(this, EditorActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("title", title);
            intent.putExtra("content", content);
            intent.putExtra("color", color);
            intent.putExtra("is_shared", is_shared);

            startActivityForResult(intent, INTENT_EDIT);
        });

        fab = findViewById(R.id.add);
        fab.setOnClickListener(view ->
                startActivityForResult(
                        new Intent(this, EditorActivity.class),
                        INTENT_ADD)
        );

        mApi = new ApiProvider(getApplicationContext());

        presenter.getData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_ADD && resultCode == RESULT_OK) {
            presenter.getData();
        } else if (requestCode == INTENT_EDIT && resultCode == RESULT_OK) {
            presenter.getData();
        }
    }

    @Override
    public void showLoading() {
        swipeRefresh.setRefreshing(true);
    }

    @Override
    public void hideLoading() {
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onGetResult(List<Note> notes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter = new MainAdapter(getApplicationContext(), notes, itemClickListener);
                adapter.notifyDataSetChanged();
                recyclerView.setAdapter(adapter);

                note = notes;
            }
        });
    }

    @Override
    public void onErrorLoading(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}