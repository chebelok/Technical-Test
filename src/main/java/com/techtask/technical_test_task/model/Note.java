package com.techtask.technical_test_task.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    private LocalDateTime createDate;

    @NotBlank(message = "Text cannot be empty")
    @Size(max = 5000, message = "Text cannot exceed 5000 characters")
    private String text;

    @Size(max = 3, message = "Cannot have more than 3 tags")
    private List<Tag> tags;
}
