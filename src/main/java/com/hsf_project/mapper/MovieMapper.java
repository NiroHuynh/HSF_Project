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
    @Mapping(target = "roomTypes", expression = "java(mapRoomTypes(roomTypes))")
    MovieHomeDTO toMovieHomeDTO(Movie movie, List<String> roomTypes);

    @Mapping(target = "genresString", expression = "java(buildGenresString(movie))")
    @Mapping(target = "roomTypes", expression = "java(mapRoomTypes(null))")
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

    default List<String> mapRoomTypes(List<String> roomTypes) {
        if (roomTypes == null || roomTypes.isEmpty()) {
            return List.of("2D");
        }
        return roomTypes;
    }
}
