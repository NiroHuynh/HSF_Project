package com.hsf_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BƯỚC 4 (cuối) trong luồng Đặt vé: Thanh toán (MÔ PHỎNG — chưa nối cổng
 * thanh toán thật, theo đúng phạm vi module đã chốt).
 *
 * Tiếp tục đúng quy ước đã thống nhất ở các bước trước:
 * 1) showtimeId, seatIds, seatTotal được mang từ bước Combo qua QUERY PARAM.
 * 2) Combo đã chọn được mang qua dưới dạng combo_{id}=qty (form ẩn ở trang
 *    combo.html) — nhận gộp vào Map<String,String> rồi tự lọc key bắt đầu
 *    bằng "combo_", đối chiếu với catalog combo để tính lại tên + giá.
 * 3) Vẫn là BẢN DỰNG NHANH DEMO: catalog combo, danh sách phương thức thanh
 *    toán, thông tin phim/suất chiếu, mã khuyến mãi... đều là mock data.
 *    Khi nối DB thật:
 *      - catalog combo  -> ComboService (bảng `combo`)
 *      - showtimeInfo   -> ShowtimeService (join show_time + movie + cinema_room)
 *      - paymentMethods -> PaymentMethodService (bảng `payment_method`)
 *      - áp mã khuyến mãi -> PromotionService (bảng `promotion`, kiểm tra
 *        start_date/end_date/usage_limit) — hiện đang mock 100% phía JS.
 * 4) Nút "Thanh toán" submit sang /booking/confirm — ĐÂY LÀ BƯỚC TIẾP THEO
 *    CHƯA CODE (sẽ là nơi thật sự ghi booking + ticket + booking_combo +
 *    payment với status PAID, theo create.sql). Sẽ làm khi tới lượt theo
 *    đúng thứ tự đã thống nhất.
 */
@Controller
@RequestMapping("/booking")
public class BookingPaymentController {

    private static final String DEFAULT_PAYMENT_METHOD = "INTL_CARD";
    private static final BigDecimal SERVICE_FEE = new BigDecimal("3000");

    @GetMapping("/payment")
    public String showPaymentPage(
            @RequestParam Long showtimeId,
            @RequestParam String seatIds,
            @RequestParam(defaultValue = "0") BigDecimal seatTotal,
            @RequestParam Map<String, String> allParams,
            Model model) {

        List<String> selectedSeats = Arrays.stream(seatIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        Map<Long, ComboCatalogItem> catalog = mockComboCatalog();

        List<SelectedCombo> selectedCombos = new ArrayList<>();
        Map<Long, Integer> comboQueryParams = new LinkedHashMap<>(); // dùng để render lại hidden input cho bước sau
        BigDecimal comboTotal = BigDecimal.ZERO;

        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (!entry.getKey().startsWith("combo_")) {
                continue;
            }
            Long comboId = Long.valueOf(entry.getKey().substring("combo_".length()));
            int qty = Integer.parseInt(entry.getValue());
            if (qty > 0 && catalog.containsKey(comboId)) {
                ComboCatalogItem item = catalog.get(comboId);
                BigDecimal lineTotal = item.price().multiply(BigDecimal.valueOf(qty));
                selectedCombos.add(new SelectedCombo(item.name(), qty, lineTotal));
                comboQueryParams.put(comboId, qty);
                comboTotal = comboTotal.add(lineTotal);
            }
        }

        BigDecimal discount = BigDecimal.ZERO; // mã khuyến mãi áp dụng phía JS (mock), giá trị khởi tạo = 0
        BigDecimal grandTotal = seatTotal.add(comboTotal).add(SERVICE_FEE).subtract(discount);

        model.addAttribute("showtimeId", showtimeId);
        model.addAttribute("seatIds", seatIds);
        model.addAttribute("selectedSeats", selectedSeats);
        model.addAttribute("seatTotal", seatTotal);
        model.addAttribute("selectedCombos", selectedCombos);
        model.addAttribute("comboQueryParams", comboQueryParams);
        model.addAttribute("comboTotal", comboTotal);
        model.addAttribute("serviceFee", SERVICE_FEE);
        model.addAttribute("discount", discount);
        model.addAttribute("grandTotal", grandTotal);
        model.addAttribute("showtimeInfo", mockShowtimeInfo(showtimeId));
        model.addAttribute("paymentMethods", mockPaymentMethods());
        model.addAttribute("defaultPaymentMethod", DEFAULT_PAYMENT_METHOD);

        return "bookingPayment";
    }

    /** Trùng dữ liệu với ComboController.mockCombos() để tính lại giá combo đã chọn. */
    private Map<Long, ComboCatalogItem> mockComboCatalog() {
        List<ComboCatalogItem> list = List.of(
                new ComboCatalogItem(1L, "P CGV Combo", new BigDecimal("135000")),
                new ComboCatalogItem(2L, "BT21 VN Single", new BigDecimal("299000")),
                new ComboCatalogItem(3L, "Hotdog Combo", new BigDecimal("64000")),
                new ComboCatalogItem(4L, "Michael Combo", new BigDecimal("259000")),
                new ComboCatalogItem(5L, "Topokki Combo", new BigDecimal("110000")),
                new ComboCatalogItem(6L, "BT21 VN Full Set", new BigDecimal("1599000"))
        );
        Map<Long, ComboCatalogItem> map = new LinkedHashMap<>();
        list.forEach(c -> map.put(c.id(), c));
        return map;
    }

    private ShowtimeInfo mockShowtimeInfo(Long showtimeId) {
        // Mock cố định — khi nối DB thật: query show_time JOIN movie JOIN cinema_room theo showtimeId.
        return new ShowtimeInfo("Dune: Part Two", "20/05/2026 19:30", "Screen 04", "CINEMAX IMAX");
    }

    private List<PaymentMethodOption> mockPaymentMethods() {
        // Mock theo đúng 5 phương thức trong mockup — khi nối DB thật, lấy từ bảng `payment_method`.
        return List.of(
                new PaymentMethodOption("MOMO", "Ví MoMo",
                        "Thanh toán nhanh qua ví điện tử MoMo", "account_balance_wallet"),
                new PaymentMethodOption("ZALOPAY", "ZaloPay",
                        "Giảm giá thêm 10k cho chủ thẻ ZaloPay", "account_balance_wallet"),
                new PaymentMethodOption("SHOPEEPAY", "ShopeePay",
                        "Sử dụng Shopee xu để được giảm giá", "account_balance_wallet"),
                new PaymentMethodOption("INTL_CARD", "Thẻ Quốc Tế",
                        "Visa, Mastercard, JCB, Amex", "credit_card"),
                new PaymentMethodOption("DOMESTIC_CARD", "Thẻ ATM Nội Địa",
                        "Hỗ trợ 40+ ngân hàng tại Việt Nam", "credit_card")
        );
    }

    public record ComboCatalogItem(Long id, String name, BigDecimal price) {
    }

    public record SelectedCombo(String name, int quantity, BigDecimal lineTotal) {
    }

    public record ShowtimeInfo(String movieTitle, String startTime, String roomName, String formatLabel) {
    }

    public record PaymentMethodOption(String code, String name, String description, String icon) {
    }
}