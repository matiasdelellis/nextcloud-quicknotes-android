package ar.delellis.quicknotes.api;

import java.sql.Array;

// int $id, string $title, string $content, array $attachts, bool $pinned, array $tags, string $color = "#F7EB96"
public class NoteRequest {
    final int id;
    final String title;
    final String content;
    final String[] attachts = {}; //Fake response
    final boolean pinned = false; //Fake response
    final String[] tags = {}; // Fake response
    final String[] shared_with = {}; // Fake response
    final String color;

    public NoteRequest(int id, String title, String content, String color) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.color = color;
    }
}
