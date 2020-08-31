package ar.delellis.quicknotes.activity.main;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ar.delellis.quicknotes.R;
import ar.delellis.quicknotes.model.Note;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.RecyclerViewAdapter> {

    private Context context;
    private List<Note> notes;
    private ItemClickListener itemClickListener;

    public MainAdapter(Context context, List<Note> notes, ItemClickListener itemClickListener) {
        this.context = context;
        this.notes = notes;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public RecyclerViewAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new RecyclerViewAdapter(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter holder, int position) {
        Note note = notes.get(position);
        holder.tv_title.setText(Html.fromHtml(note.getTitle().trim()));
        holder.tv_content.setText(Html.fromHtml(note.getContent().trim()));
        holder.card_item.setBackgroundColor(Color.parseColor(note.getColor()));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    class RecyclerViewAdapter extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_title, tv_content;
        CardView card_item;
        ItemClickListener itemClickListener;

        RecyclerViewAdapter(@NonNull View itemView, ItemClickListener itemClickListener) {
            super(itemView);

            card_item = itemView.findViewById(R.id.card_item);
            tv_title = itemView.findViewById(R.id.title);
            tv_content = itemView.findViewById(R.id.content);

            this.itemClickListener = itemClickListener;
            card_item.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
