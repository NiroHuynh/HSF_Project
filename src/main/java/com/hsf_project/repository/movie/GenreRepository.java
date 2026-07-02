package com.hsf_project.repository.movie;

import com.hsf_project.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GenreRepository extends JpaRepository<Genre, Integer> {
    List<Genre> findByIsDeletedFalse();
}
