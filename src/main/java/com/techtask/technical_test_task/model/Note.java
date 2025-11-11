package com.techtask.technical_test_task.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "notes")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Note {

    @Id
    private String id;

    @NotBlank(message = "Title cannot be empty")
    private String title;

    private LocalDateTime createDate;

    @NotBlank(message = "Text cannot be empty")
    private String text;

    private List<String> tags;
}
