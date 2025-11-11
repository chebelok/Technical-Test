package com.techtask.technical_test_task.dto;

import java.time.LocalDateTime;

public class NoteDTO {

    private final String id;
    private final String title;
    private final LocalDateTime createDate;

    public NoteDTO(String id, String title, LocalDateTime createDate) {
        this.createDate = createDate;
        this.id = id;
        this.title = title;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
