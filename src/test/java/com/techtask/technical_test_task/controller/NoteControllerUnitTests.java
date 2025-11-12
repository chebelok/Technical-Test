package com.techtask.technical_test_task.controller;

import com.techtask.technical_test_task.dto.NoteDTO;
import com.techtask.technical_test_task.model.Note;
import com.techtask.technical_test_task.model.Tag;
import com.techtask.technical_test_task.service.NoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NoteControllerUnitTests {

    @Mock
    private NoteService noteService;

    @InjectMocks
    private NoteController noteController;

    private Note mockNote;
    private final String NOTE_ID = "test-id-123";

    @BeforeEach
    void setUp() {
        mockNote = Note.builder()
                .id(NOTE_ID)
                .title("Test Title")
                .text("Test text content.")
                .createDate(LocalDateTime.now())
                .tags(List.of(Tag.PERSONAL))
                .build();
    }

    @Test
    void createNote_returns201Created() {
        when(noteService.createNote(any(Note.class))).thenReturn(mockNote);

        ResponseEntity<Note> response = noteController.createNote(mockNote);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockNote, response.getBody());
        verify(noteService, times(1)).createNote(mockNote);
    }

    @Test
    void getAllNotes_returnsListOfNotes() {
        List<Note> notes = List.of(mockNote);
        when(noteService.getAllNotes()).thenReturn(notes);

        List<Note> result = noteController.getAllNotes();

        assertEquals(1, result.size());
        assertEquals(notes, result);
        verify(noteService, times(1)).getAllNotes();
    }

    @Test
    void getNoteById_noteFound_returns200Ok() {
        when(noteService.getNoteById(NOTE_ID)).thenReturn(mockNote);

        ResponseEntity<Note> response = noteController.getNoteById(NOTE_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockNote, response.getBody());
        verify(noteService, times(1)).getNoteById(NOTE_ID);
    }

    @Test
    void getNoteById_noteNotFound_throwsNoSuchElementException() {

        when(noteService.getNoteById(NOTE_ID)).thenThrow(NoSuchElementException.class);

        assertThrows(NoSuchElementException.class, () -> noteController.getNoteById(NOTE_ID));
        verify(noteService, times(1)).getNoteById(NOTE_ID);
    }

    @Test
    void getNotes_returns200OkWithPagedNotes() {
        int page = 0;
        int size = 5;
        NoteDTO mockDto = new NoteDTO(NOTE_ID, mockNote.getTitle(), mockNote.getCreateDate());
        Page<NoteDTO> notesPage = new PageImpl<>(List.of(mockDto), PageRequest.of(page, size), 1);

        when(noteService.getNotes(null, page, size)).thenReturn(notesPage);

        ResponseEntity<Page<NoteDTO>> response = noteController.getNotes(null, page, size);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(notesPage, response.getBody());
        verify(noteService, times(1)).getNotes(null, page, size);
    }

    @Test
    void getNotes_withTag_callsServiceWithTag() {
        int page = 0;
        int size = 5;
        Tag tag = Tag.BUSINESS;
        Page<NoteDTO> notesPage = Page.empty();

        when(noteService.getNotes(tag, page, size)).thenReturn(notesPage);

        ResponseEntity<Page<NoteDTO>> response = noteController.getNotes(tag, page, size);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(noteService, times(1)).getNotes(tag, page, size);
    }

    @Test
    void getNoteStats_noteFound_returns200OkWithMap() {
        Map<String, Long> stats = Map.of("word", 2L, "count", 1L);
        when(noteService.countStat(NOTE_ID)).thenReturn(Optional.of(stats));

        ResponseEntity<Map<String, Long>> response = noteController.getNoteStats(NOTE_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(stats, response.getBody());
        verify(noteService, times(1)).countStat(NOTE_ID);
    }

    @Test
    void getNoteStats_noteNotFound_returns404NotFound() {
        when(noteService.countStat(NOTE_ID)).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Long>> response = noteController.getNoteStats(NOTE_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(noteService, times(1)).countStat(NOTE_ID);
    }

    @Test
    void updateNote_noteFound_returns200Ok() {
        Note updatedNote = mockNote;
        when(noteService.updateNote(NOTE_ID, mockNote)).thenReturn(Optional.of(updatedNote));

        ResponseEntity<Note> response = noteController.updateNote(NOTE_ID, mockNote);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedNote, response.getBody());
        verify(noteService, times(1)).updateNote(NOTE_ID, mockNote);
    }

    @Test
    void updateNote_noteNotFound_returns404NotFound() {
        when(noteService.updateNote(NOTE_ID, mockNote)).thenReturn(Optional.empty());

        ResponseEntity<Note> response = noteController.updateNote(NOTE_ID, mockNote);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(noteService, times(1)).updateNote(NOTE_ID, mockNote);
    }

    @Test
    void deleteNote_successfulDeletion_returns204NoContent() {
        when(noteService.deleteNote(NOTE_ID)).thenReturn(true);

        ResponseEntity<Void> response = noteController.deleteNote(NOTE_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(noteService, times(1)).deleteNote(NOTE_ID);
    }

    @Test
    void deleteNote_noteNotFound_returns404NotFound() {
        when(noteService.deleteNote(NOTE_ID)).thenReturn(false);

        ResponseEntity<Void> response = noteController.deleteNote(NOTE_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(noteService, times(1)).deleteNote(NOTE_ID);
    }
}
