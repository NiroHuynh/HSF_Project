package com.hsf_project.mapper;

import com.hsf_project.dto.movie.response.GenreDTO;
import com.hsf_project.entity.Genre;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GenreMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    GenreDTO toGenreDTO(Genre genre);

    List<GenreDTO> toGenreDTOList(List<Genre> genres);
}
