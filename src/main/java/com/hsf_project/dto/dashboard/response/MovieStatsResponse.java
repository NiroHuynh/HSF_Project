package com.hsf_project.dto.dashboard.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MovieStatsResponse {
    long totalMovies;
    long nowShowingCount;
    long comingSoonCount;
    BigDecimal dailyRevenue;
    double averageRating;
}
