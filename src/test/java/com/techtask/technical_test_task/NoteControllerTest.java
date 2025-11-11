package com.techtask.technical_test_task;

import com.techtask.technical_test_task.model.Note;
import com.techtask.technical_test_task.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NoteRepository noteRepository;

    @BeforeEach
    void cleanDb() {
        noteRepository.deleteAll();
    }

    @Test
    void shouldCreateNoteSuccessfully() throws Exception {
        String json = """
                {
                  "title": "Test Note",
                  "text": "This is a test note text",
                  "tags": ["BUSINESS"]
                }
                """;

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Note"))
                .andExpect(jsonPath("$.text").value("This is a test note text"));
    }

    @Test
    void shouldNotAllowEmptyTitle() throws Exception {
        String json = """
                {
                  "title": "",
                  "text": "Missing title"
                }
                """;

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotesList() throws Exception {
        Note note = new Note();
        note.setTitle("List Test");
        note.setText("text");
        noteRepository.save(note);

        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("List Test"));
    }

    @Test
    void shouldReturnStatsCorrectly() throws Exception {
        Note note = new Note();
        note.setTitle("Stats Test");
        note.setText("note is just a note");
        noteRepository.save(note);

        mockMvc.perform(get("/api/notes/" + note.getId() + "/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value(2))
                .andExpect(jsonPath("$.is").value(1))
                .andExpect(jsonPath("$.just").value(1))
                .andExpect(jsonPath("$.a").value(1));
    }
}

