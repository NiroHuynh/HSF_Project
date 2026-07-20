package com.hsf_project.service.impl;

import com.hsf_project.dto.customer.response.ActiveCustomerResponse;
import com.hsf_project.dto.customer.response.CustomerDetailResponse;
import com.hsf_project.dto.customer.response.CustomerGrowthResponse;
import com.hsf_project.dto.customer.response.CustomerSummaryResponse;
import com.hsf_project.dto.customer.response.TransactionResponse;
import com.hsf_project.entity.Booking;
import com.hsf_project.entity.Ticket;
import com.hsf_project.entity.User;
import com.hsf_project.exception.AppException;
import com.hsf_project.exception.ErrorCode;
import com.hsf_project.mapper.CustomerMapper;
import com.hsf_project.repository.BookingRepository;
import com.hsf_project.repository.auth.UserRepository;
import com.hsf_project.service.CustomerDashboardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CustomerDashboardServiceImpl implements CustomerDashboardService {

    UserRepository userRepository;
    BookingRepository bookingRepository;
    CustomerMapper customerMapper;

    @Override
    @Transactional(readOnly = true)
    public CustomerSummaryResponse getCustomerSummary(LocalDate from, LocalDate to) {
        validateDateRange(from, to);

        long totalCustomers = userRepository.countByIsDeletedFalseOrIsDeletedNull();

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(LocalTime.MAX);

        List<User> newUsers = userRepository.findByCreatedAtBetweenAndIsDeletedFalse(fromDateTime, toDateTime);
        long newCustomers = newUsers.size();

        List<Booking> periodBookings = bookingRepository.findByBookingDateBetweenAndIsDeletedFalse(fromDateTime, toDateTime);
        List<Booking> paidBookings = periodBookings.stream()
                .filter(b -> b.getStatus() != null && Set.of("CONFIRMED", "EXPORTED").contains(b.getStatus()))
                .toList();

        BigDecimal totalSpending = paidBookings.stream()
                .map(Booking::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long distinctUsersWithBookings = paidBookings.stream()
                .map(b -> b.getUser().getId())
                .distinct()
                .count();

        BigDecimal averageSpending = distinctUsersWithBookings > 0
                ? totalSpending.divide(BigDecimal.valueOf(distinctUsersWithBookings), 0, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long monthlyVisits = distinctUsersWithBookings;

        return CustomerSummaryResponse.builder()
                .totalCustomers(totalCustomers)
                .newCustomers(newCustomers)
                .averageSpending(averageSpending)
                .monthlyVisits(monthlyVisits)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerGrowthResponse> getCustomerGrowth(String type, LocalDate from, LocalDate to) {
        validateDateRange(from, to);

        if (type == null || !List.of("month", "quarter", "year").contains(type.toLowerCase())) {
            throw new AppException(ErrorCode.INVALID_GROWTH_TYPE);
        }

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(LocalTime.MAX);

        List<User> users = userRepository.findByCreatedAtBetweenAndIsDeletedFalse(fromDateTime, toDateTime);

        return switch (type.toLowerCase()) {
            case "quarter" -> groupByQuarter(users, from, to);
            case "year" -> groupByYear(users, from, to);
            default -> groupByMonth(users, from, to);
        };
    }

    private List<CustomerGrowthResponse> groupByMonth(List<User> users, LocalDate from, LocalDate to) {
        Map<YearMonth, Long> grouped = users.stream()
                .collect(Collectors.groupingBy(
                        u -> YearMonth.from(u.getCreatedAt()),
                        Collectors.counting()
                ));
        List<CustomerGrowthResponse> result = new ArrayList<>();
        YearMonth start = YearMonth.from(from);
        YearMonth end = YearMonth.from(to);
        while (!start.isAfter(end)) {
            long count = grouped.getOrDefault(start, 0L);
            result.add(CustomerGrowthResponse.builder()
                    .label(start.format(DateTimeFormatter.ofPattern("MMM")))
                    .value((int) count)
                    .build());
            start = start.plusMonths(1);
        }
        return result;
    }

    private List<CustomerGrowthResponse> groupByQuarter(List<User> users, LocalDate from, LocalDate to) {
        Map<String, Long> grouped = users.stream()
                .collect(Collectors.groupingBy(
                        u -> {
                            int month = u.getCreatedAt().getMonthValue();
                            int q = (month - 1) / 3 + 1;
                            return "Q" + q + "/" + u.getCreatedAt().getYear();
                        },
                        Collectors.counting()
                ));
        List<CustomerGrowthResponse> result = new ArrayList<>();
        YearMonth start = YearMonth.from(from);
        YearMonth end = YearMonth.from(to);
        int startQ = (start.getMonthValue() - 1) / 3;
        start = start.withMonth(startQ * 3 + 1);
        while (!start.isAfter(end)) {
            int q = (start.getMonthValue() - 1) / 3 + 1;
            String label = "Q" + q + "/" + start.getYear();
            long count = grouped.getOrDefault(label, 0L);
            result.add(CustomerGrowthResponse.builder().label(label).value((int) count).build());
            start = start.plusMonths(3);
        }
        return result;
    }

    private List<CustomerGrowthResponse> groupByYear(List<User> users, LocalDate from, LocalDate to) {
        Map<String, Long> grouped = users.stream()
                .collect(Collectors.groupingBy(
                        u -> String.valueOf(u.getCreatedAt().getYear()),
                        Collectors.counting()
                ));
        List<CustomerGrowthResponse> result = new ArrayList<>();
        for (int y = from.getYear(); y <= to.getYear(); y++) {
            String label = String.valueOf(y);
            long count = grouped.getOrDefault(label, 0L);
            result.add(CustomerGrowthResponse.builder().label(label).value((int) count).build());
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActiveCustomerResponse> getActiveCustomers(Pageable pageable) {
        List<User> users = userRepository.findByIsDeletedFalseOrIsDeletedNull();

        List<Map.Entry<User, ActiveCustomerData>> entries = users.stream()
                .map(user -> {
                    List<Booking> bookings = bookingRepository.findByUserIdAndIsDeletedFalse(user.getId());
                    int count = bookings.size();
                    BigDecimal spent = bookings.stream()
                            .filter(b -> b.getStatus() != null && Set.of("CONFIRMED", "EXPORTED").contains(b.getStatus()))
                            .map(Booking::getFinalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    String latestMovie = getLatestMovie(bookings);
                    return new AbstractMap.SimpleEntry<>(user, new ActiveCustomerData(count, spent, latestMovie));
                })
                .collect(Collectors.toList());

        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            Sort.Order order = sort.iterator().next();
            String property = order.getProperty();
            boolean ascending = order.isAscending();
            Comparator<Map.Entry<User, ActiveCustomerData>> comparator = getActiveEntryComparator(property);
            if (!ascending) {
                comparator = comparator.reversed();
            }
            entries.sort(comparator);
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), entries.size());
        List<Map.Entry<User, ActiveCustomerData>> pageEntries = start < entries.size()
                ? entries.subList(start, end)
                : List.of();

        List<ActiveCustomerResponse> pageContent = pageEntries.stream()
                .map(e -> customerMapper.toActiveCustomerResponse(e.getKey(), e.getValue().bookingCount, e.getValue().latestMovie))
                .toList();

        return new PageImpl<>(pageContent, pageable, entries.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActiveCustomerResponse> searchActiveCustomers(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return getActiveCustomers(pageable);
        }

        List<User> allUsers = userRepository.findByIsDeletedFalseOrIsDeletedNull();
        String lowerKeyword = keyword.toLowerCase().trim();

        List<Map.Entry<User, ActiveCustomerData>> filtered = allUsers.stream()
                .filter(u -> {
                    String name = u.getFullName();
                    String email = u.getEmail();
                    return (name != null && name.toLowerCase().contains(lowerKeyword)) ||
                           (email != null && email.toLowerCase().contains(lowerKeyword));
                })
                .map(u -> {
                    List<Booking> bookings = bookingRepository.findByUserIdAndIsDeletedFalse(u.getId());
                    int count = bookings.size();
                    BigDecimal spent = bookings.stream()
                            .filter(b -> b.getStatus() != null && Set.of("CONFIRMED", "EXPORTED").contains(b.getStatus()))
                            .map(Booking::getFinalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    String latestMovie = getLatestMovie(bookings);
                    return new AbstractMap.SimpleEntry<>(u, new ActiveCustomerData(count, spent, latestMovie));
                })
                .collect(Collectors.toList());

        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            Sort.Order order = sort.iterator().next();
            Comparator<Map.Entry<User, ActiveCustomerData>> comparator = getActiveEntryComparator(order.getProperty());
            if (!order.isAscending()) comparator = comparator.reversed();
            filtered.sort(comparator);
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<Map.Entry<User, ActiveCustomerData>> pageEntries = start < filtered.size()
                ? filtered.subList(start, end) : List.of();

        List<ActiveCustomerResponse> content = pageEntries.stream()
                .map(e -> customerMapper.toActiveCustomerResponse(e.getKey(), e.getValue().bookingCount, e.getValue().latestMovie))
                .toList();

        return new PageImpl<>(content, pageable, filtered.size());
    }

    private record ActiveCustomerData(int bookingCount, BigDecimal totalSpending, String latestMovie) {}

    private Comparator<Map.Entry<User, ActiveCustomerData>> getActiveEntryComparator(String property) {
        return switch (property) {
            case "bookingCount" -> Comparator.comparingInt(e -> e.getValue().bookingCount);
            case "totalSpending" -> Comparator.comparing(e -> e.getValue().totalSpending);
            case "joinDate" -> Comparator.comparing(e -> e.getKey().getCreatedAt(), Comparator.nullsLast(Comparator.naturalOrder()));
            default -> throw new AppException(ErrorCode.INVALID_SORT_FIELD);
        };
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDetailResponse getCustomerDetail(Long id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        List<Booking> bookings = bookingRepository.findByUserIdAndIsDeletedFalse(user.getId());

        int totalBookings = bookings.size();

        BigDecimal totalSpent = bookings.stream()
                .filter(b -> b.getStatus() != null && Set.of("CONFIRMED", "EXPORTED").contains(b.getStatus()))
                .map(Booking::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String memberLevel = determineMemberLevel(totalSpent);

        LocalDateTime latestBooking = bookings.stream()
                .map(Booking::getBookingDate)
                .filter(d -> d != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return customerMapper.toCustomerDetailResponse(user, totalBookings, totalSpent, memberLevel, latestBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getRecentTransactions(Long userId, int limit) {
        List<Booking> bookings = bookingRepository.findByUserIdAndIsDeletedFalse(userId);
        return bookings.stream()
                .sorted(Comparator.comparing(Booking::getBookingDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .map(this::toTransactionResponse)
                .toList();
    }

    private TransactionResponse toTransactionResponse(Booking b) {
        int ticketCount = b.getTickets() != null ? b.getTickets().size() : 0;
        int comboCount = b.getBookingCombos() != null ? b.getBookingCombos().size() : 0;
        String details = ticketCount + " vé";
        if (comboCount > 0) details += " + " + comboCount + " combo";

        String movieTitle = "";
        String theater = "";
        if (b.getTickets() != null && !b.getTickets().isEmpty()) {
            var ticket = b.getTickets().get(0);
            if (ticket.getShowtime() != null) {
                if (ticket.getShowtime().getMovie() != null) {
                    movieTitle = ticket.getShowtime().getMovie().getTitle();
                }
                if (ticket.getShowtime().getRoom() != null
                        && ticket.getShowtime().getRoom().getCinema() != null) {
                    theater = ticket.getShowtime().getRoom().getCinema().getName();
                }
            }
        }

        String statusLabel = switch (b.getStatus()) {
            case "CONFIRMED" -> "Hoàn thành";
            case "EXPORTED" -> "Đã xuất vé";
            case "PENDING" -> "Chờ thanh toán";
            case "CANCELED" -> "Đã hủy";
            default -> b.getStatus() != null ? b.getStatus() : "---";
        };

        return TransactionResponse.builder()
                .date(b.getBookingDate() != null
                        ? b.getBookingDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                        : "")
                .productName(movieTitle)
                .productDetails(details)
                .theater(theater)
                .amount(b.getFinalAmount())
                .status(statusLabel)
                .build();
    }

    private String determineMemberLevel(BigDecimal totalSpent) {
        long amount = totalSpent.longValue();
        if (amount >= 10_000_000) return "Platinum";
        if (amount >= 5_000_000) return "Gold";
        if (amount >= 1_000_000) return "Silver";
        return "Bronze";
    }

    private String getLatestMovie(List<Booking> bookings) {
        return bookings.stream()
                .filter(b -> b.getTickets() != null && !b.getTickets().isEmpty())
                .max((a, b) -> {
                    LocalDateTime da = a.getBookingDate();
                    LocalDateTime db = b.getBookingDate();
                    if (da == null && db == null) return 0;
                    if (da == null) return -1;
                    if (db == null) return 1;
                    return da.compareTo(db);
                })
                .map(b -> {
                    Ticket ticket = b.getTickets().get(0);
                    if (ticket.getShowtime() != null && ticket.getShowtime().getMovie() != null) {
                        return ticket.getShowtime().getMovie().getTitle();
                    }
                    return null;
                })
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportCustomers(LocalDate from, LocalDate to, String format) {
        validateDateRange(from, to);

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(LocalTime.MAX);

        List<Booking> bookings = bookingRepository.findByBookingDateBetweenAndIsDeletedFalse(fromDateTime, toDateTime);

        if (bookings.isEmpty()) {
            throw new AppException(ErrorCode.NO_BOOKING_DATA);
        }

        if ("pdf".equalsIgnoreCase(format)) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Định dạng PDF chưa được hỗ trợ");
        }

        return generateExcel(bookings);
    }

    private byte[] generateExcel(List<Booking> bookings) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Customer Report");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Customer ID", "Full Name", "Email", "Booking Code", "Booking Date", "Total Amount", "Status"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            int rowNum = 1;
            for (Booking booking : bookings) {
                Row row = sheet.createRow(rowNum++);
                User user = booking.getUser();
                row.createCell(0).setCellValue(user.getId() != null ? user.getId().doubleValue() : 0);
                row.createCell(1).setCellValue(user.getFullName() != null ? user.getFullName() : "");
                row.createCell(2).setCellValue(user.getEmail() != null ? user.getEmail() : "");
                row.createCell(3).setCellValue(booking.getBookingCode() != null ? booking.getBookingCode() : "");
                row.createCell(4).setCellValue(booking.getBookingDate() != null ? booking.getBookingDate().toString() : "");
                row.createCell(5).setCellValue(booking.getFinalAmount() != null ? booking.getFinalAmount().doubleValue() : 0);
                row.createCell(6).setCellValue(booking.getStatus() != null ? booking.getStatus() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Lỗi khi tạo file Excel: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void toggleCustomerStatus(Long id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
        if ("ACTIVE".equals(user.getStatus())) {
            user.setStatus("LOCKED");
        } else {
            user.setStatus("ACTIVE");
        }
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null) from = LocalDate.now().minusMonths(1);
        if (to == null) to = LocalDate.now();
        if (from.isAfter(to)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
    }
}
