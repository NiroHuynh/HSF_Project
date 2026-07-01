package com.hsf_project.mapper;

import com.hsf_project.dto.MovieHomeDTO;
import com.hsf_project.entity.Genre;
import com.hsf_project.entity.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface MovieMapper {

    @Mapping(target = "genresString", expression = "java(buildGenresString(movie))")
    MovieHomeDTO toMovieHomeDTO(Movie movie);

    default String buildGenresString(Movie movie) {
        List<Genre> genres = movie.getGenres();
        if (genres == null || genres.isEmpty()) {
            return "Đang cập nhật";
        }
        return genres.stream()
                .map(Genre::getName)
                .collect(Collectors.joining(", "));
    }
}
