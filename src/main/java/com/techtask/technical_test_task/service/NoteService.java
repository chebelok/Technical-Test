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

    public Page<NoteDTO> getNotes(Tag tag, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"));
        Page<Note> notesPage;

        if (tag != null) {
            notesPage = noteRepository.findByTags(tag, pageable);
        } else {
            notesPage = noteRepository.findAll(pageable);
        }

        return notesPage.map(note -> new NoteDTO(
                note.getId(),
                note.getTitle(),
                note.getCreateDate()
        ));
    }

        public Note getNoteById(String id) {
        return noteRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }

    public Optional<Note> updateNote(String id, Note updatedNote) {
        Note note = noteRepository.findById(id).orElseThrow(NoSuchElementException::new);

        note.setTitle(updatedNote.getTitle());
        note.setText(updatedNote.getText());
        note.setTags(updatedNote.getTags());
        return Optional.of(noteRepository.save(note));
    }

    public boolean deleteNote(String id) {
        if (!noteRepository.existsById(id)) {
            return false;
        }
        noteRepository.deleteById(id);
        return true;
    }

    public Optional<Map<String, Long>> countStat(String id){

        Optional<Note> noteOpt = noteRepository.findById(id);
        if (noteOpt.isEmpty()) {
            return Optional.empty();
        }

        Note note = noteOpt.get();
        Map<String, Long> wordCounts = Arrays.stream(note.getText().toLowerCase()
                        .replaceAll("[^a-zA-Z\\s]", "")
                        .split("\\s+"))
                .filter(word -> !word.isEmpty())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Map<String, Long> result = wordCounts.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
        return Optional.of(result);
    }

}
