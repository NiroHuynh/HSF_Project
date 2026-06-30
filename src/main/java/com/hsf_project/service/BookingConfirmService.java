package com.hsf_project.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface BookingConfirmService {
    String confirmBooking(Long showtimeId, List<String> seatCodes, Map<Long, Integer> comboQuantities,
                          Long paymentMethodId, Long promotionId,
                          BigDecimal discountAmount, BigDecimal serviceFee);
}
