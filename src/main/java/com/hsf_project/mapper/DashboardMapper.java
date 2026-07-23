package com.hsf_project.mapper;

import com.hsf_project.dto.dashboard.response.MovieRevenueRankingResponse;
import com.hsf_project.entity.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DashboardMapper {

    @Mapping(target = "rank", ignore = true)
    @Mapping(target = "movieId", source = "movie.id")
    @Mapping(target = "title", source = "movie.title")
    @Mapping(target = "posterUrl", source = "movie.posterUrl")
    @Mapping(target = "revenue", source = "revenue")
    @Mapping(target = "ticketsSold", expression = "java((long) ticketIds.size())")
    @Mapping(target = "averageRating", source = "movie.averageRating")
    MovieRevenueRankingResponse toRevenueRankingResponse(Movie movie, BigDecimal revenue, Set<Long> ticketIds);
}
