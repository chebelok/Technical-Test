package com.techtask.technical_test_task.service;

import com.techtask.technical_test_task.dto.NoteDTO;
import com.techtask.technical_test_task.model.Note;
import com.techtask.technical_test_task.model.Tag;
import com.techtask.technical_test_task.repository.NoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NoteService {

    private final NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public Note createNote(Note note) {
        note.setCreateDate(LocalDateTime.now());
        return noteRepository.save(note);
    }

    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    public Page<NoteDTO> getNotes(List<Tag> tags, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"));
        Page<Note> notesPage;
        if (tags != null) {
            notesPage = noteRepository.findByTagsIn(tags, pageable);
        } else {
            notesPage = noteRepository.findAll(pageable);
        }
        return notesPage.map(note -> new NoteDTO(
                note.getId(),
                note.getTitle(),
                note.getCreateDate()
        ));
    }

    public Optional<Note> getNoteById(String id) {
        return noteRepository.findById(id);
    }

    public Note updateNote(String id, Note updatedNote) {
        Note note = noteRepository.findById(id).orElse(null);
        if (note == null){
            return null;
        }
        note.setTitle(updatedNote.getTitle());
        note.setText(updatedNote.getText());
        note.setTags(updatedNote.getTags());
        return noteRepository.save(note);
    }

    public void deleteNote(String id) {
        noteRepository.deleteById(id);
    }

    public Map<String, Long> countStat(String id){
        Note note = noteRepository.findById(id).orElseThrow();
        String initialText = note.getText();
        Map<String, Long> wordCounts = Arrays.stream(initialText.toLowerCase().split("\\s+"))
                .filter(word -> !word.isEmpty())
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));
        return wordCounts.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

}
