package com.hsf_project.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CreateReviewRequest {

    @NotNull
    @Min(1)
    @Max(10)
    private Short ratingStar;

    private String comment;

    public Short getRatingStar() {
        return ratingStar;
    }

    public void setRatingStar(Short ratingStar) {
        this.ratingStar = ratingStar;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
