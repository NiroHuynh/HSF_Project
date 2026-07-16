package com.hsf_project.dto.customer.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ActiveCustomerResponse {
    Long customerId;
    String avatar;
    String fullName;
    String email;
    int bookingCount;
    String latestMovie;
}
