package com.techtask.technical_test_task.repository;

import com.techtask.technical_test_task.model.Note;
import com.techtask.technical_test_task.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends MongoRepository<Note, String> {
    Page<Note> findByTags(Tag tag, Pageable pageable);

}
