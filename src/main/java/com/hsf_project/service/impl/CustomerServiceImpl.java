package com.hsf_project.service.impl;

import com.hsf_project.dto.response.CustomerRowDTO;
import com.hsf_project.entity.User;
import com.hsf_project.repository.auth.UserRepository;
import com.hsf_project.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private UserRepository userRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter ISO_FMT  = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Transactional(readOnly = true)
    public List<CustomerRowDTO> getCustomersByCinema(Integer cinemaId) {
        List<Object[]> rows = userRepository.findCustomersWithStatsByCinema(cinemaId);

        // Dùng AtomicLong để tạo số thứ tự KH-001, KH-002...
        AtomicLong seq = new AtomicLong(1);

        return rows.stream()
                .map(row -> toDto(row, seq.getAndIncrement()))
                .collect(Collectors.toList());
    }

    private CustomerRowDTO toDto(Object[] row, long seq) {
        User       u     = (User)       row[0];
        Long       count = (Long)       row[1];
        BigDecimal sum   = (BigDecimal) row[2];

        CustomerRowDTO dto = new CustomerRowDTO();

        // ID dạng "KH-001"
        dto.setId(String.format("KH-%03d", seq));

        // Họ tên
        dto.setName(u.getLastName() + " " + u.getFirstName());

        dto.setEmail(u.getEmail());
        dto.setPhone(u.getPhoneNumber() != null ? u.getPhoneNumber() : "—");

        // Ngày đăng ký
        if (u.getCreatedAt() != null) {
            dto.setDateISO(u.getCreatedAt().format(ISO_FMT));
            dto.setDate(u.getCreatedAt().format(DATE_FMT));
        } else {
            dto.setDateISO("");
            dto.setDate("—");
        }

        // Số booking tại chi nhánh
        dto.setTickets(count != null ? count : 0L);

        // Tổng chi tiêu
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMaximumFractionDigits(0);
        dto.setSpend(sum != null && sum.compareTo(BigDecimal.ZERO) > 0
                ? nf.format(sum) + " đ"
                : "0 đ");

        return dto;
    }
}