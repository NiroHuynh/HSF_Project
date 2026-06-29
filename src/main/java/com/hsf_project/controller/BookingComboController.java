package com.hsf_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * BƯỚC 3 trong luồng Đặt vé: Chọn combo bắp nước.
 *
 * Ghi chú theo các quyết định đã chốt với chủ dự án:
 * 1) Dữ liệu từ bước 1 (lịch chiếu) + bước 2 (ghế) được truyền sang trang này
 *    qua QUERY PARAM trên URL: showtimeId, seatIds (vd: "J12,J13"), seatTotal
 *    (tổng tiền vé đã tính ở bước chọn ghế). Trang này KHÔNG đọc/ghi DB cho
 *    phần lịch chiếu/ghế — chỉ hiển thị lại để người dùng theo dõi tiến trình.
 * 2) Combo CHƯA có ảnh trong DB (bảng `combo` không có cột image) -> dùng
 *    icon (Material Symbols) để minh hoạ thay ảnh, gắn cứng trong mockCombos().
 * 3) Đây là BẢN DỰNG NHANH ĐỂ DEMO Ý TƯỞNG: danh sách combo đang là mock data,
 *    chưa nối ComboRepository/ComboService thật. Khi nối DB, chỉ cần thay
 *    mockCombos() bằng comboService.findAllActive() (lọc status='ACTIVE' và
 *    is_deleted=0 theo đúng bảng `combo` trong create.sql) và bổ sung mapping
 *    sang icon nếu vẫn muốn giữ icon thay ảnh.
 * 4) Bước "Tiếp tục" trỏ tạm tới /booking/payment (chưa code controller này,
 *    sẽ làm ở bước kế tiếp theo đúng thứ tự đã thống nhất).
 */
@Controller
@RequestMapping("/booking")
public class BookingComboController {

    @GetMapping("/combo")
    public String showComboPage(
            @RequestParam Long showtimeId,
            @RequestParam String seatIds,
            @RequestParam(defaultValue = "0") BigDecimal seatTotal,
            Model model) {

        List<String> selectedSeats = Arrays.stream(seatIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        List<ComboItem> combos = mockCombos();

        model.addAttribute("showtimeId", showtimeId);
        model.addAttribute("seatIds", seatIds);
        model.addAttribute("selectedSeats", selectedSeats);
        model.addAttribute("seatTotal", seatTotal);
        model.addAttribute("combos", combos);

        return "bookingCombo";
    }

    /**
     * Mock data — đúng nội dung 6 combo trong mockup "Bắp Nước".
     * icon: tên ligature của Material Symbols, dùng để hiển thị thay ảnh.
     * badge: nhãn nhỏ góc trên (null nếu combo không có nhãn).
     */
    private List<ComboItem> mockCombos() {
        return List.of(
                new ComboItem(1L, "P CGV COMBO",
                        "01 Bắp ngọt lớn, 02 Nước ngọt siêu lớn, 01 Snack",
                        new BigDecimal("135000"), "local_movies", "FREE CARAMEL"),
                new ComboItem(2L, "BT21 VN SINGLE",
                        "01 Ly BT21 Vietnam Edition, 01 Nước ngọt siêu lớn, 01 Bắp ngọt lớn",
                        new BigDecimal("299000"), "icecream", "SPECIAL EDITION"),
                new ComboItem(3L, "HOTDOG COMBO",
                        "01 Hotdog, 01 Nước ngọt lớn (Tặng +2.000 Upsize nước)",
                        new BigDecimal("64000"), "fastfood", null),
                new ComboItem(4L, "MICHAEL COMBO",
                        "01 Hộp bắp nón fedora Michael, 01 Nước ngọt siêu lớn, 01 Bắp ngọt lớn",
                        new BigDecimal("259000"), "theater_comedy", null),
                new ComboItem(5L, "TOPOKKI COMBO",
                        "01 Topokki phô mai lắc, 01 Nước ngọt lớn",
                        new BigDecimal("110000"), "ramen_dining", null),
                new ComboItem(6L, "BT21 VN FULL SET",
                        "07 Ly BT21 Vietnam Edition, 02 Nước ngọt siêu lớn, 01 Bắp ngọt lớn",
                        new BigDecimal("1599000"), "diamond", "ULTIMATE COLLECTION")
        );
    }

    /** DTO tạm cho mock data. Khi nối DB thật, map từ entity Combo (create.sql) sang DTO này. */
    public record ComboItem(Long id, String name, String description,
                            BigDecimal price, String icon, String badge) {
    }
}