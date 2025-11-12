package com.techtask.technical_test_task.controller;

import com.techtask.technical_test_task.dto.NoteDTO;
import com.techtask.technical_test_task.model.Note;
import com.techtask.technical_test_task.model.Tag;
import com.techtask.technical_test_task.service.NoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/notes")
@Validated
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
     public ResponseEntity <Note> createNote(@Valid @RequestBody Note note){
        Note createdNote = noteService.createNote(note);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNote);
    }
//  this endpoint was created only for testing purposes during development of API
    @GetMapping("/all")
    public List<Note> getAllNotes(){
        return noteService.getAllNotes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable String id) {
        return noteService.getNoteById(id)
                .map(ResponseEntity::ok)
                .orElseGet(()->ResponseEntity.notFound().build());
//        Note note = noteService.getNoteById(id);
//        return ResponseEntity.ok(note);
    }

    @GetMapping
    public ResponseEntity<Page<NoteDTO>> getNotes(
            @RequestParam(required = false)  Tag tag,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "5") @Min(1) @Max(10) int size) {
        Page<NoteDTO> notes = noteService.getNotes(tag, page, size);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Long>> getNoteStats(@PathVariable String id) {
        return noteService.countStat(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable String id, @Valid @RequestBody Note note){
        return noteService.updateNote(id, note)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable String id){
        boolean deleted = noteService.deleteNote(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }


}
