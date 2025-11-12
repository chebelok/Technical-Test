package com.techtask.technical_test_task.service;

import com.techtask.technical_test_task.dto.NoteDTO;
import com.techtask.technical_test_task.model.Note;
import com.techtask.technical_test_task.model.Tag;
import com.techtask.technical_test_task.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private NoteService noteService;

    private Note mockNote;
    private Note mockUpdatedNote;
    private final String NOTE_ID = "note-id-1";

    @BeforeEach
    void setUp() {
        mockNote = Note.builder()
                .id(NOTE_ID)
                .title("Initial Title")
                .text("This is the original text content.")
                .createDate(LocalDateTime.of(2023, 1, 1, 10, 0))
                .tags(List.of(Tag.PERSONAL))
                .build();

        mockUpdatedNote = Note.builder()
                .title("New Title")
                .text("Updated text content with new information.")
                .tags(List.of(Tag.BUSINESS, Tag.IMPORTANT))
                .build();
    }

    @Test
    void createNote_setsCreationDateAndSaves() {
        ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> {
            Note savedNote = invocation.getArgument(0);
            savedNote.setId(NOTE_ID);
            return savedNote;
        });

        Note result = noteService.createNote(mockNote);

        verify(noteRepository, times(1)).save(noteCaptor.capture());

        Note capturedNote = noteCaptor.getValue();
        assertNotNull(capturedNote.getCreateDate(), "Create date must be set by the service.");
        assertTrue(capturedNote.getCreateDate().isAfter(LocalDateTime.now().minusSeconds(1)), "Create date should be set to a very recent time.");
        assertEquals(NOTE_ID, result.getId(), "The saved note should have an ID.");
    }

    @Test
    void getAllNotes_returnsAllNotes() {
        List<Note> expectedNotes = List.of(mockNote, Note.builder().id("2").title("Second").build());
        when(noteRepository.findAll()).thenReturn(expectedNotes);

        List<Note> actualNotes = noteService.getAllNotes();

        assertEquals(2, actualNotes.size());
        assertEquals(expectedNotes, actualNotes);
        verify(noteRepository, times(1)).findAll();
    }

    @Test
    void getNotes_withNullTag_callsFindAllAndMapsToDTO() {
        int page = 1;
        int size = 5;
        Pageable expectedPageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"));
        Page<Note> notesPage = new PageImpl<>(List.of(mockNote), expectedPageable, 1);

        when(noteRepository.findAll(eq(expectedPageable))).thenReturn(notesPage);

        Page<NoteDTO> resultPage = noteService.getNotes(null, page, size);

        verify(noteRepository, times(1)).findAll(eq(expectedPageable));
        verify(noteRepository, never()).findByTags(any(Tag.class), any(Pageable.class));

        assertEquals(1, resultPage.getContent().size());
        NoteDTO resultDto = resultPage.getContent().get(0);
        assertEquals(mockNote.getId(), resultDto.getId());
        assertEquals(mockNote.getTitle(), resultDto.getTitle());
        assertEquals(mockNote.getCreateDate(), resultDto.getCreateDate());
    }

    @Test
    void getNotes_withNonNullTag_callsFindByTags() {
        Tag tag = Tag.BUSINESS;
        int page = 0;
        int size = 10;
        Pageable expectedPageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"));
        Page<Note> notesPage = new PageImpl<>(List.of(mockNote), expectedPageable, 1);

        when(noteRepository.findByTags(eq(tag), eq(expectedPageable))).thenReturn(notesPage);

        noteService.getNotes(tag, page, size);

        verify(noteRepository, times(1)).findByTags(eq(tag), eq(expectedPageable));
        verify(noteRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getNoteById_noteFound_returnsNote() {
        when(noteRepository.findById(NOTE_ID)).thenReturn(Optional.of(mockNote));

        Note result = noteService.getNoteById(NOTE_ID);

        assertEquals(mockNote, result);
    }

    @Test
    void getNoteById_noteNotFound_throwsException() {
        when(noteRepository.findById(NOTE_ID)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> noteService.getNoteById(NOTE_ID));
    }

    @Test
    void updateNote_noteFound_updatesFieldsAndSaves() {
        when(noteRepository.findById(NOTE_ID)).thenReturn(Optional.of(mockNote));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Note> resultOpt = noteService.updateNote(NOTE_ID, mockUpdatedNote);

        assertTrue(resultOpt.isPresent());
        Note result = resultOpt.get();

        assertEquals(mockUpdatedNote.getTitle(), result.getTitle());
        assertEquals(mockUpdatedNote.getText(), result.getText());
        assertEquals(mockUpdatedNote.getTags(), result.getTags());
        assertEquals(NOTE_ID, result.getId());
        assertNotNull(result.getCreateDate());

        verify(noteRepository, times(1)).save(mockNote);
    }

    @Test
    void updateNote_noteNotFound_throwsException() {
        when(noteRepository.findById(NOTE_ID)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> noteService.updateNote(NOTE_ID, mockUpdatedNote));
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void deleteNote_noteExists_deletesAndReturnsTrue() {
        when(noteRepository.existsById(NOTE_ID)).thenReturn(true);
        doNothing().when(noteRepository).deleteById(NOTE_ID);

        boolean result = noteService.deleteNote(NOTE_ID);

        assertTrue(result);
        verify(noteRepository, times(1)).existsById(NOTE_ID);
        verify(noteRepository, times(1)).deleteById(NOTE_ID);
    }

    @Test
    void deleteNote_noteDoesNotExist_returnsFalseAndDoesNotDelete() {
        when(noteRepository.existsById(NOTE_ID)).thenReturn(false);

        boolean result = noteService.deleteNote(NOTE_ID);

        assertFalse(result);
        verify(noteRepository, times(1)).existsById(NOTE_ID);
        verify(noteRepository, never()).deleteById(anyString());
    }

    @Test
    void countStat_noteNotFound_returnsEmptyOptional() {
        when(noteRepository.findById(NOTE_ID)).thenReturn(Optional.empty());

        Optional<Map<String, Long>> result = noteService.countStat(NOTE_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    void countStat_calculatesWordFrequency_correctlyAndSortsByCount() {
        String text = "Hello world, this is a test. WORLD! TEST, hello? One-two three.";
        mockNote.setText(text);
        when(noteRepository.findById(NOTE_ID)).thenReturn(Optional.of(mockNote));

        Optional<Map<String, Long>> resultOpt = noteService.countStat(NOTE_ID);

        assertTrue(resultOpt.isPresent());
        Map<String, Long> result = resultOpt.get();


        assertEquals(8, result.size());
        assertEquals(2L, result.get("hello"));
        assertEquals(2L, result.get("world"));
        assertEquals(2L, result.get("test"));
        assertEquals(1L, result.get("this"));
        assertEquals(1L, result.get("onetwo"));

        List<Long> counts = new ArrayList<>(result.values());
        assertEquals(2L, counts.get(0));
        assertEquals(2L, counts.get(1));
        assertEquals(2L, counts.get(2));
        assertEquals(1L, counts.get(3));

        List<String> orderedKeys = result.keySet().stream().collect(Collectors.toList());

        assertTrue(orderedKeys.contains("hello"));
        assertTrue(orderedKeys.contains("world"));
        assertTrue(orderedKeys.contains("test"));

        assertTrue(orderedKeys.indexOf("hello") < orderedKeys.indexOf("this"));
        assertTrue(orderedKeys.indexOf("world") < orderedKeys.indexOf("is"));
    }

    @Test
    void countStat_handlesEmptyText() {
        mockNote.setText("");
        when(noteRepository.findById(NOTE_ID)).thenReturn(Optional.of(mockNote));

        Optional<Map<String, Long>> resultOpt = noteService.countStat(NOTE_ID);

        assertTrue(resultOpt.isPresent());
        assertTrue(resultOpt.get().isEmpty(), "Word count map should be empty for empty text.");
    }
}