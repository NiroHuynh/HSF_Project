package com.hsf_project.repository.admin;

import com.hsf_project.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AdminMovieRepository extends JpaRepository<Movie, Integer> {
    @Query("select distinct m from Movie m left join fetch m.genres where m.isDeleted = false or m.isDeleted is null order by m.createdAt desc, m.id desc")
    List<Movie> findAllVisible();
}
