package com.hsf_project.mapper;

import com.hsf_project.dto.movie.request.CreateMovieRequest;
import com.hsf_project.dto.movie.request.UpdateMovieRequest;
import com.hsf_project.dto.movie.response.MovieResponse;
import com.hsf_project.entity.Movie;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MovieAdminMapper {

    @Mapping(target = "genres", expression = "java(mapGenres(movie))")
    MovieResponse toMovieResponse(Movie movie);

    Movie toMovie(CreateMovieRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateMovie(UpdateMovieRequest request, @MappingTarget Movie movie);

    default List<String> mapGenres(Movie movie) {
        if (movie.getGenres() == null) return List.of();
        return movie.getGenres().stream()
                .map(g -> g.getName())
                .collect(Collectors.toList());
    }
}
