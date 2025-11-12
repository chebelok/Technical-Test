package com.techtask.technical_test_task;

import com.techtask.technical_test_task.model.Note;
import com.techtask.technical_test_task.model.Tag;
import com.techtask.technical_test_task.repository.NoteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDb() {
        noteRepository.deleteAll();
    }

    private String createNoteJson(String title, String text, List<Tag> tags) throws Exception {
        Note n = new Note();
        n.setTitle(title);
        n.setText(text);
        n.setTags(tags);
        return objectMapper.writeValueAsString(n);
    }

    @Test
    void createNote_success() throws Exception {
        String body = createNoteJson("Integration create", "Text of note", List.of(Tag.valueOf("BUSINESS")));

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title").value("Integration create"))
                .andExpect(jsonPath("$.text").value("Text of note"))
                .andExpect(jsonPath("$.tags[0]").value("BUSINESS"))
                .andExpect(jsonPath("$.createDate", notNullValue()));
    }

    @Test
    void createNote_validationFails_whenTitleMissing() throws Exception {
        String body = createNoteJson("", "Text present", null);

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }


    @Test
    void createNote_invalidTagReturnsBadRequest() throws Exception {

        String body = """
        {
            "title": "Bad tag",
            "text": "Some text",
            "tags": ["WRONG_TAG"]
        }
        """;

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Invalid value")));
    }



    @Test
    void getNoteById_success_andNotFound() throws Exception {
        Note note = new Note();
        note.setTitle("GetById");
        note.setText("some text");
        note.setCreateDate(LocalDateTime.now());
        noteRepository.save(note);

        mockMvc.perform(get("/api/notes/{id}", note.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(note.getId()))
                .andExpect(jsonPath("$.title").value("GetById"))
                .andExpect(jsonPath("$.text").value("some text"));

        mockMvc.perform(get("/api/notes/{id}", "000000000000000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateNote_success() throws Exception {
        Note note = new Note();
        note.setTitle("Before");
        note.setText("Before text");
        note.setCreateDate(LocalDateTime.now());
        noteRepository.save(note);

        Note update = new Note();
        update.setTitle("After");
        update.setText("After text");
        update.setTags(List.of(Tag.valueOf("PERSONAL")));

        mockMvc.perform(put("/api/notes/{id}", note.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("After"))
                .andExpect(jsonPath("$.text").value("After text"))
                .andExpect(jsonPath("$.tags[0]").value("PERSONAL"));
    }

    @Test
    void deleteNote_success() throws Exception {
        Note note = new Note();
        note.setTitle("To delete");
        note.setText("text");
        note.setCreateDate(LocalDateTime.now());
        noteRepository.save(note);

        mockMvc.perform(delete("/api/notes/{id}", note.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/notes/{id}", note.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void listNotes_paginationAndSorting_andFilterByTag() throws Exception {
        for (int i = 0; i < 6; i++) {
            Note n = new Note();
            n.setTitle("Note " + i);
            n.setText("text " + i);
            n.setCreateDate(LocalDateTime.now().minusDays(i));
            n.setTags(i < 3 ? List.of(Tag.valueOf("BUSINESS")) : List.of(Tag.valueOf("PERSONAL")));
            noteRepository.save(n);
        }

        mockMvc.perform(get("/api/notes")
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].title").value("Note 0"));

        mockMvc.perform(get("/api/notes")
                        .param("tag", "PERSONAL")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[*].title", containsInAnyOrder("Note 3", "Note 4", "Note 5")));

        mockMvc.perform(get("/api/notes")
                        .param("tag", "BUSINESS")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[*].title", containsInAnyOrder("Note 0", "Note 1", "Note 2")));
    }

    @Test
    void statsEndpoint_countsWordsAndSortsDescending() throws Exception {
        Note note = new Note();
        note.setTitle("Stats");
        note.setText("note is just a note, note!");
        note.setCreateDate(LocalDateTime.now());
        noteRepository.save(note);

        mockMvc.perform(get("/api/notes/{id}/stats", note.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value(3))
                .andExpect(jsonPath("$.is").value(1))
                .andExpect(jsonPath("$.just").value(1));
    }
}
