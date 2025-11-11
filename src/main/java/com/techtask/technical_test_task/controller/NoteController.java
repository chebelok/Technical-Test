package com.techtask.technical_test_task.controller;

import com.techtask.technical_test_task.dto.NoteDTO;
import com.techtask.technical_test_task.model.Note;
import com.techtask.technical_test_task.model.Tag;
import com.techtask.technical_test_task.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
     public ResponseEntity <Object> createNote(@Valid @RequestBody Note note, BindingResult result){
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        return ResponseEntity.ok(noteService.createNote(note));
    }

    @GetMapping("/all")
    public List<Note> getAllNotes(){
        return noteService.getAllNotes();
    }

    @GetMapping("/{id}")
    public Optional<Note> getNoteById(@PathVariable String id){
        return noteService.getNoteById(id);
    }

    @GetMapping
    public Page<NoteDTO> getNotes(
            @RequestParam(required = false) List<Tag> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return noteService.getNotes(tags, page, size);
    }

    @GetMapping("/{id}/stats")
    public Map<String, Long> getNoteStats(@PathVariable String id) {
        return noteService.countStat(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateNote(@PathVariable String id, @Valid @RequestBody Note note, BindingResult result){
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        return ResponseEntity.ok(noteService.updateNote(id, note));
    }

    @DeleteMapping("/{id}")
    public void deleteNote(@PathVariable String id){
        noteService.deleteNote(id);
    }


}
