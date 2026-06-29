/* ============================================================
   CINEMAX / CINEPREMIER — DATA SEED SCRIPT
   File: data.sql
   Mục đích: Insert dữ liệu mẫu cho TẤT CẢ bảng trong create.sql,
             khớp với nội dung trong 4 ảnh mockup đã cung cấp
             (trang Thanh toán, Chọn ghế, Chi tiết phim, Bắp nước).

   QUY ƯỚC & GIẢ ĐỊNH (đọc trước khi chạy):
   1. Mọi FK đều được resolve bằng SUBQUERY theo khóa tự nhiên
      (email, code, name, title...) thay vì hard-code ID, để script
      chạy đúng ngay cả khi IDENTITY không bắt đầu từ 1.
   2. Cột password chỉ là CHUỖI GIẢ LẬP (không phải hash thật).
      Khi tích hợp thật, hãy thay bằng BCrypt hash từ module User.
   3. Bảng `seat` cho phòng "Cinema 7" (IMAX, 82 ghế) được sinh
      TỰ ĐỘNG bằng Recursive CTE để khớp đúng layout A–G trong
      ảnh "Chọn ghế" (82/82 ghế), thay vì viết tay 82 dòng.
      Phòng "Screen 04" chỉ seed 2 ghế J12, J13 (đúng ảnh Thanh
      toán) — các ghế còn lại của phòng này bạn seed thêm tương tự
      khi cần.
   4. Bảng `ticket` (vé) chỉ được tạo chi tiết cho 2 đơn có sẵn ghế
      thật (booking Dune: Part Two và booking Joker @ Cinema 7).
      Các booking còn lại (2, 4, 5, 6) là đơn hàng hợp lệ để demo
      combo/thanh toán, nhưng KHÔNG có vé chi tiết vì chưa seed
      sơ đồ ghế cho các phòng chiếu tương ứng.
   5. LƯU Ý SCHEMA: `combo.name/description`, `promotion.code/name`,
      `payment_method.method_name/description`, `booking.note` đang
      khai báo VARCHAR (không phải NVARCHAR) nhưng cần chứa tiếng
      Việt có dấu. Tuỳ collation của DB, dấu tiếng Việt có thể bị
      lỗi. Nếu gặp lỗi hiển thị, nên đề xuất đổi các cột này sang
      NVARCHAR với người phụ trách bảng đó (mình không tự đổi cấu
      trúc bảng vì đó là entity nhóm đã làm sẵn).
   6. Phim "Joker: Folie à Deux" và "Dune: Part Two" lấy đúng tên/
      đạo diễn/diễn viên thật (thông tin công khai, giống mọi app
      đặt vé thực tế) để khớp ảnh mockup; phần mô tả phim do mình
      viết lại ngắn gọn, không sao chép nguyên văn từ nguồn nào.
      5 phim còn lại là phim HƯ CẤU để lấp đầy danh sách.
   ============================================================ */

USE HSF_PROJECT;
GO

/* ============================================================
   1. ROLE
   ============================================================ */
INSERT INTO role (role_name, is_deleted) VALUES
(N'ADMIN', 0),
(N'STAFF', 0),
(N'CUSTOMER', 0);
GO

/* ============================================================
   2. USERS
   ============================================================ */
INSERT INTO users (role_id, first_name, last_name, email, phone_number, password, date_of_birth, gender, status, is_deleted) VALUES
((SELECT role_id FROM role WHERE role_name = N'ADMIN'),    N'Minh',  N'Nguyễn Quang', 'admin@cinemax.vn',        '0900000001', '$2a$10$placeholderHashAdmin0001', '1990-01-15', N'Nam', 'ACTIVE', 0),
((SELECT role_id FROM role WHERE role_name = N'STAFF'),    N'Hà',    N'Trần Thị',     'staff.ha@cinemax.vn',     '0900000002', '$2a$10$placeholderHashStaff0002', '1996-03-22', N'Nữ',  'ACTIVE', 0),
((SELECT role_id FROM role WHERE role_name = N'CUSTOMER'), N'Phong', N'Huỳnh Tấn',    'phong.huynh@gmail.com',   '0901234567', '$2a$10$placeholderHashCust0003',  '1999-07-08', N'Nam', 'ACTIVE', 0),
((SELECT role_id FROM role WHERE role_name = N'CUSTOMER'), N'Mai',   N'Nguyễn Thị',   'mai.nguyen@gmail.com',    '0912345678', '$2a$10$placeholderHashCust0004',  '2001-11-02', N'Nữ',  'ACTIVE', 0),
((SELECT role_id FROM role WHERE role_name = N'CUSTOMER'), N'Khánh', N'Trần Gia',     'khanh.tran@gmail.com',    '0923456789', '$2a$10$placeholderHashCust0005',  '1998-05-19', N'Nam', 'ACTIVE', 0),
((SELECT role_id FROM role WHERE role_name = N'CUSTOMER'), N'Linh',  N'Phạm Thuỳ',    'linh.pham@gmail.com',     '0934567890', '$2a$10$placeholderHashCust0006',  '2000-09-30', N'Nữ',  'INACTIVE', 0);
GO

/* ============================================================
   3. NOTIFICATION
   ============================================================ */
INSERT INTO notification (title, content, type, is_deleted) VALUES
(N'Đặt vé thành công', N'Bạn đã đặt vé thành công cho phim Joker: Folie à Deux. Vui lòng đến rạp trước giờ chiếu 15 phút.', 'BOOKING', 0),
(N'Ưu đãi mới', N'Nhập mã SUMMER10 để được giảm 10% cho mọi đơn đặt vé trong tháng này.', 'PROMOTION', 0),
(N'Bảo trì hệ thống', N'Hệ thống sẽ bảo trì từ 00:00 đến 02:00 ngày 01/07/2026. Mong bạn thông cảm.', 'SYSTEM', 0),
(N'Nhắc lịch chiếu', N'Suất chiếu Dune: Part Two của bạn sẽ bắt đầu trong 1 giờ nữa.', 'REMINDER', 0),
(N'Vé đã được xác nhận', N'Đơn hàng CMX20261020J01 của bạn đã được xác nhận thanh toán.', 'BOOKING', 0),
(N'Flash Sale 24h', N'Săn mã FLASHSALE30 giảm ngay 30.000đ, chỉ áp dụng trong 24 giờ.', 'PROMOTION', 0);
GO

/* ============================================================
   4. USER_NOTIFICATION
   ============================================================ */
INSERT INTO user_notification (user_id, notification_id) VALUES
((SELECT user_id FROM users WHERE email = 'phong.huynh@gmail.com'), (SELECT notification_id FROM notification WHERE title = N'Đặt vé thành công')),
((SELECT user_id FROM users WHERE email = 'phong.huynh@gmail.com'), (SELECT notification_id FROM notification WHERE title = N'Nhắc lịch chiếu')),
((SELECT user_id FROM users WHERE email = 'mai.nguyen@gmail.com'),  (SELECT notification_id FROM notification WHERE title = N'Vé đã được xác nhận')),
((SELECT user_id FROM users WHERE email = 'mai.nguyen@gmail.com'),  (SELECT notification_id FROM notification WHERE title = N'Ưu đãi mới')),
((SELECT user_id FROM users WHERE email = 'khanh.tran@gmail.com'),  (SELECT notification_id FROM notification WHERE title = N'Flash Sale 24h')),
((SELECT user_id FROM users WHERE email = 'linh.pham@gmail.com'),   (SELECT notification_id FROM notification WHERE title = N'Bảo trì hệ thống')),
((SELECT user_id FROM users WHERE email = 'phong.huynh@gmail.com'), (SELECT notification_id FROM notification WHERE title = N'Flash Sale 24h'));
GO

/* ============================================================
   5. MOVIE
   ============================================================ */
INSERT INTO movie (
    title,
    description,
    duration_minutes,
    director,
    cast,
    release_date,
    poster_url,
    age_rating,
    average_rating,
    status,
    is_deleted
) VALUES
      (N'Joker: Folie à Deux',
       N'Arthur Fleck đối diện thế giới nội tâm hỗn loạn của chính mình khi mối quan hệ với Harley Quinn ngày càng sâu đậm, giữa bối cảnh phiên tòa xét xử căng thẳng tại Gotham.',
       138, N'Todd Phillips', N'Joaquin Phoenix, Lady Gaga, Brendan Gleeson',
       '2026-09-15', '/assets/posters/joker-folie-a-deux.jpg', 'T18', 8.9, 'NOW_SHOWING', 0),

      (N'Dune: Part Two',
       N'Paul Atreides tiếp tục hành trình báo thù và tìm cách ngăn chặn tương lai đen tối, đồng thời học cách đoàn kết người Fremen để giành lại quyền kiểm soát Arrakis.',
       166, N'Denis Villeneuve', N'Timothée Chalamet, Zendaya, Rebecca Ferguson',
       '2026-04-01', '/assets/posters/dune-part-two.jpg', 'T13', 8.7, 'NOW_SHOWING', 0),

      (N'Lằn Ranh Sinh Tử',
       N'Một cựu cảnh sát buộc phải truy đuổi tổ chức buôn người xuyên quốc gia để cứu con gái mình trước khi quá muộn.',
       124, N'Lê Minh Khoa', N'Trấn Thành, Ngọc Lan, Hữu Long',
       '2026-08-01', '/assets/posters/lan-ranh-sinh-tu.jpg', 'T16', 7.8, 'NOW_SHOWING', 0),

      (N'Đường Đua Tốc Độ',
       N'Một tay đua trẻ phải vượt qua quá khứ lỗi lầm để giành chiến thắng trong giải đua xuyên Việt lớn nhất từ trước đến nay.',
       110, N'Phan Đăng Vũ', N'Quốc Anh, Thuý Diễm, Bảo Long',
       '2026-09-01', '/assets/posters/duong-dua-toc-do.jpg', 'P', 7.5, 'NOW_SHOWING', 0),

      (N'Vũ Trụ Song Hành',
       N'Hai nhà vật lý phát hiện cánh cổng dẫn đến một vũ trụ song song, nơi mọi lựa chọn của họ đã rẽ theo hướng khác.',
       132, N'Đỗ Gia Linh', N'Mai Tài Phến, Nhã Phương',
       '2026-12-10', '/assets/posters/vu-tru-song-hanh.jpg', 'T13', 0, 'COMING_SOON', 0),

      (N'Mùa Hè Của Em',
       N'Một câu chuyện tình nhẹ nhàng giữa hai người trẻ gặp lại nhau sau 10 năm xa cách tại chính nơi họ từng chia tay.',
       105, N'Nguyễn Hữu Tuấn', N'Khả Ngân, Thanh Sơn',
       '2026-11-20', '/assets/posters/mua-he-cua-em.jpg', 'P', 0, 'COMING_SOON', 0),

      (N'Bóng Ma Quá Khứ',
       N'Một gia đình chuyển đến căn nhà cũ và dần phát hiện những bí mật rùng rợn bị chôn giấu suốt 30 năm.',
       98, N'Trần Bửu Lộc', N'Lan Ngọc, Việt Hương',
       '2026-02-14', '/assets/posters/bong-ma-qua-khu.jpg', 'T18', 6.9, 'ENDED', 0);
GO

/* ============================================================
   6. GENRE
   ============================================================ */
INSERT INTO genre (name, is_deleted) VALUES
(N'Action', 0),
(N'Sci-Fi', 0),
(N'Drama', 0),
(N'Romance', 0),
(N'Horror', 0),
(N'Thriller', 0),
(N'Comedy', 0);
GO

/* ============================================================
   7. MOVIE_GENRE
   ============================================================ */
INSERT INTO movie_genre (movie_id, genre_id) VALUES
((SELECT id FROM movie WHERE title = N'Joker: Folie à Deux'), (SELECT id FROM genre WHERE name = N'Action')),
((SELECT id FROM movie WHERE title = N'Joker: Folie à Deux'), (SELECT id FROM genre WHERE name = N'Sci-Fi')),
((SELECT id FROM movie WHERE title = N'Dune: Part Two'),      (SELECT id FROM genre WHERE name = N'Sci-Fi')),
((SELECT id FROM movie WHERE title = N'Dune: Part Two'),      (SELECT id FROM genre WHERE name = N'Drama')),
((SELECT id FROM movie WHERE title = N'Lằn Ranh Sinh Tử'),    (SELECT id FROM genre WHERE name = N'Action')),
((SELECT id FROM movie WHERE title = N'Lằn Ranh Sinh Tử'),    (SELECT id FROM genre WHERE name = N'Thriller')),
((SELECT id FROM movie WHERE title = N'Đường Đua Tốc Độ'),    (SELECT id FROM genre WHERE name = N'Action')),
((SELECT id FROM movie WHERE title = N'Vũ Trụ Song Hành'),    (SELECT id FROM genre WHERE name = N'Sci-Fi')),
((SELECT id FROM movie WHERE title = N'Mùa Hè Của Em'),       (SELECT id FROM genre WHERE name = N'Romance')),
((SELECT id FROM movie WHERE title = N'Bóng Ma Quá Khứ'),     (SELECT id FROM genre WHERE name = N'Horror'));
GO

/* ============================================================
   8. MOVIE_REVIEW
   ============================================================ */
INSERT INTO movie_review (movie_id, user_id, rating_star, comment) VALUES
((SELECT id FROM movie WHERE title = N'Joker: Folie à Deux'), (SELECT user_id FROM users WHERE email = 'phong.huynh@gmail.com'), 5, N'Diễn xuất của Joaquin Phoenix quá đỉnh, xem ở IMAX cực kỳ đáng tiền.'),
((SELECT id FROM movie WHERE title = N'Joker: Folie à Deux'), (SELECT user_id FROM users WHERE email = 'mai.nguyen@gmail.com'),   4, N'Phim hay nhưng nhịp phim hơi chậm ở giữa.'),
((SELECT id FROM movie WHERE title = N'Joker: Folie à Deux'), (SELECT user_id FROM users WHERE email = 'khanh.tran@gmail.com'),  5, N'Âm thanh Dolby Atmos rất sống động.'),
((SELECT id FROM movie WHERE title = N'Dune: Part Two'),      (SELECT user_id FROM users WHERE email = 'phong.huynh@gmail.com'), 5, N'Hình ảnh hoành tráng, đáng xem trên màn IMAX.'),
((SELECT id FROM movie WHERE title = N'Dune: Part Two'),      (SELECT user_id FROM users WHERE email = 'linh.pham@gmail.com'),   4, N'Cốt truyện sâu nhưng cần xem phần 1 trước.'),
((SELECT id FROM movie WHERE title = N'Lằn Ranh Sinh Tử'),    (SELECT user_id FROM users WHERE email = 'mai.nguyen@gmail.com'),   4, N'Hành động mãn nhãn, kịch bản ổn.'),
((SELECT id FROM movie WHERE title = N'Đường Đua Tốc Độ'),    (SELECT user_id FROM users WHERE email = 'khanh.tran@gmail.com'),  3, N'Xem giải trí cuối tuần thì ổn.');
GO

/* ============================================================
   9. COMBO  (khớp đúng 6 combo trong ảnh "Bắp Nước")
   ============================================================ */
INSERT INTO combo (name, description, price, quantity, status, is_deleted) VALUES
('P CGV Combo',     '01 Bap ngot lon, 02 Nuoc ngot sieu lon, 01 Snack', 135000, 200, 'ACTIVE', 0),
('BT21 VN Single',  '01 Ly BT21 Vietnam Edition, 01 Nuoc ngot sieu lon, 01 Bap ngot lon', 299000, 80, 'ACTIVE', 0),
('Hotdog Combo',    '01 Hotdog, 01 Nuoc ngot lon (Tang +2.000 Upsize nuoc)', 64000, 150, 'ACTIVE', 0),
('Michael Combo',   '01 Hop bap non fedora Michael, 01 Nuoc ngot sieu lon, 01 Bap ngot lon', 259000, 60, 'ACTIVE', 0),
('Topokki Combo',   '01 Topokki pho mai lac, 01 Nuoc ngot lon', 110000, 100, 'ACTIVE', 0),
('BT21 VN Full Set','07 Ly BT21 Vietnam Edition, 02 Nuoc ngot sieu lon, 01 Bap ngot lon', 1599000, 20, 'ACTIVE', 0);
GO
-- Ghi chú: tên/mô tả combo viết không dấu vì cột combo.name/description là VARCHAR (xem mục 5 ghi chú đầu file).

/* ============================================================
   10. PROMOTION
   ============================================================ */
INSERT INTO promotion (code, name, description, discount_type, discount_value, start_date, end_date, usage_limit, used_count, status, is_deleted) VALUES
('SUMMER10',    'Uu dai mua he',        'Giam 10% cho moi don dat ve',           'PERCENT', 10,    '2026-06-01', '2026-09-30', 1000, 12, 'ACTIVE', 0),
('WELCOME50K',  'Chao mung thanh vien', 'Giam ngay 50.000d cho don dau tien',    'FIXED',   50000, '2026-01-01', '2026-12-31', 500,  45, 'ACTIVE', 0),
('VIPMEMBER15', 'Uu dai hoi vien VIP',  'Giam 15% danh cho hoi vien VIP',        'PERCENT', 15,    '2026-01-01', '2026-12-31', NULL, 8,  'ACTIVE', 0),
('FLASHSALE30', 'Flash Sale 24h',       'Giam ngay 30.000d, chi trong 24 gio',   'FIXED',   30000, '2026-07-01', '2026-07-02', 200,  5,  'ACTIVE', 0),
('COMBO20',     'Uu dai bap nuoc',      'Giam 20% khi mua combo bap nuoc',       'PERCENT', 20,    '2026-03-01', '2026-12-31', NULL, 30, 'INACTIVE', 0);
GO

/* ============================================================
   11. CITY
   ============================================================ */
INSERT INTO city (name, is_deleted) VALUES
(N'Hồ Chí Minh', 0),
(N'Hà Nội', 0),
(N'Đà Nẵng', 0),
(N'Cần Thơ', 0),
(N'Hải Phòng', 0);
GO

/* ============================================================
   12. CINEMA  (khớp đúng tên rạp trong ảnh "Chi tiết phim" & "Chọn ghế")
   ============================================================ */
INSERT INTO cinema (name, address, city_id, is_deleted) VALUES
(N'CGV Vincom Nguyễn Chí Thanh', N'Vincom Nguyễn Chí Thanh, Đống Đa', (SELECT id FROM city WHERE name = N'Hà Nội'), 0),
(N'CGV Pandora City',            N'1/1 Trường Chinh, P. Tây Thạnh, Q. Tân Phú', (SELECT id FROM city WHERE name = N'Hồ Chí Minh'), 0),
(N'CGV Liberty Citypoint',       N'Tầng M-1, 59-61 Pasteur, Quận 1', (SELECT id FROM city WHERE name = N'Hồ Chí Minh'), 0),
(N'CGV Gigamall Thủ Đức',        N'Tầng 6, Gigamall, 240-242 Phạm Văn Đồng, Thủ Đức', (SELECT id FROM city WHERE name = N'Hồ Chí Minh'), 0),
(N'CGV Vincom Đà Nẵng',          N'910A Ngô Quyền, Sơn Trà', (SELECT id FROM city WHERE name = N'Đà Nẵng'), 0),
(N'CGV Sense City Cần Thơ',      N'1 Đại lộ Hòa Bình, Ninh Kiều', (SELECT id FROM city WHERE name = N'Cần Thơ'), 0);
GO

/* ============================================================
   13. CINEMA_ROOM
   ============================================================ */
INSERT INTO cinema_room (name, room_type, total_seats, cinema_id, is_deleted) VALUES
(N'Cinema 7',                'IMAX',     82,  (SELECT id FROM cinema WHERE name = N'CGV Vincom Nguyễn Chí Thanh'), 0),
(N'Screen 04',                'IMAX',     144, (SELECT id FROM cinema WHERE name = N'CGV Vincom Nguyễn Chí Thanh'), 0),
(N'Pandora - Room 1',         'STANDARD', 120, (SELECT id FROM cinema WHERE name = N'CGV Pandora City'), 0),
(N'Liberty - Room 2 (3D)',    'STANDARD', 100, (SELECT id FROM cinema WHERE name = N'CGV Liberty Citypoint'), 0),
(N'Liberty - Room IMAX',      'IMAX',     150, (SELECT id FROM cinema WHERE name = N'CGV Liberty Citypoint'), 0),
(N'Gigamall - Room 1',        'STANDARD', 110, (SELECT id FROM cinema WHERE name = N'CGV Gigamall Thủ Đức'), 0),
(N'Da Nang - Room 1',         'STANDARD', 100, (SELECT id FROM cinema WHERE name = N'CGV Vincom Đà Nẵng'), 0),
(N'Can Tho - Room 1',         'STANDARD', 90,  (SELECT id FROM cinema WHERE name = N'CGV Sense City Cần Thơ'), 0);
GO

/* ============================================================
   14. SHOW_TIME
   ============================================================ */
INSERT INTO show_time (start_time, end_time, room_id, movie_id) VALUES
('2026-05-20 22:50:00', '2026-05-21 00:42:00', (SELECT id FROM cinema_room WHERE name = N'Cinema 7'),             (SELECT id FROM movie WHERE title = N'Joker: Folie à Deux')),
('2026-05-20 19:30:00', '2026-05-20 22:16:00', (SELECT id FROM cinema_room WHERE name = N'Screen 04'),            (SELECT id FROM movie WHERE title = N'Dune: Part Two')),
('2026-10-20 10:00:00', '2026-10-20 12:18:00', (SELECT id FROM cinema_room WHERE name = N'Pandora - Room 1'),    (SELECT id FROM movie WHERE title = N'Joker: Folie à Deux')),
('2026-10-20 19:45:00', '2026-10-20 22:03:00', (SELECT id FROM cinema_room WHERE name = N'Pandora - Room 1'),    (SELECT id FROM movie WHERE title = N'Joker: Folie à Deux')),
('2026-10-20 18:30:00', '2026-10-20 20:48:00', (SELECT id FROM cinema_room WHERE name = N'Liberty - Room 2 (3D)'),(SELECT id FROM movie WHERE title = N'Joker: Folie à Deux')),
('2026-10-20 20:30:00', '2026-10-20 22:48:00', (SELECT id FROM cinema_room WHERE name = N'Liberty - Room IMAX'), (SELECT id FROM movie WHERE title = N'Joker: Folie à Deux')),
('2026-10-20 17:45:00', '2026-10-20 20:03:00', (SELECT id FROM cinema_room WHERE name = N'Gigamall - Room 1'),   (SELECT id FROM movie WHERE title = N'Joker: Folie à Deux')),
('2026-10-21 20:00:00', '2026-10-21 22:10:00', (SELECT id FROM cinema_room WHERE name = N'Gigamall - Room 1'),   (SELECT id FROM movie WHERE title = N'Lằn Ranh Sinh Tử')),
('2026-10-21 21:00:00', '2026-10-21 23:05:00', (SELECT id FROM cinema_room WHERE name = N'Pandora - Room 1'),    (SELECT id FROM movie WHERE title = N'Đường Đua Tốc Độ')),
('2026-10-22 20:00:00', '2026-10-22 22:46:00', (SELECT id FROM cinema_room WHERE name = N'Liberty - Room IMAX'), (SELECT id FROM movie WHERE title = N'Dune: Part Two'));
GO

/* ============================================================
   15. SEAT
   -- Sinh tự động 82 ghế cho phòng "Cinema 7" bằng Recursive CTE,
   -- khớp layout A-G trong ảnh "Chọn ghế" (82/82 ghế):
   --   A,B,C,F = STANDARD (Ghế thường) x12
   --   D,E     = VIP x12
   --   G       = SWEETBOX x10
   -- Tổng = 12*6 + 10 = 82
   ============================================================ */
DECLARE @room_cinema7 INT = (SELECT id FROM cinema_room WHERE name = N'Cinema 7');

;WITH seat_rows (row_label, max_num, seat_type) AS (
    SELECT 'A', 12, 'STANDARD' UNION ALL
    SELECT 'B', 12, 'STANDARD' UNION ALL
    SELECT 'C', 12, 'STANDARD' UNION ALL
    SELECT 'D', 12, 'VIP'      UNION ALL
    SELECT 'E', 12, 'VIP'      UNION ALL
    SELECT 'F', 12, 'STANDARD' UNION ALL
    SELECT 'G', 10, 'SWEETBOX'
),
      numbers (n) AS (
          SELECT 1
          UNION ALL
          SELECT n + 1 FROM numbers WHERE n < 12
      )
 INSERT INTO seat (room_id, row_label, seat_number, seat_code, type, is_active, is_deleted)
SELECT @room_cinema7,
       sr.row_label,
       num.n,
       sr.row_label + CAST(num.n AS VARCHAR(2)),
       sr.seat_type,
       1,
       0
FROM seat_rows sr
         JOIN numbers num ON num.n <= sr.max_num
    OPTION (MAXRECURSION 100);
GO

-- Phòng "Screen 04": chỉ seed 2 ghế J12, J13 đúng như ảnh Thanh toán
-- (đây là ghế VIP vì phòng dán nhãn "CINEPREMIER IMAX" trong mockup).
INSERT INTO seat (room_id, row_label, seat_number, seat_code, type, is_active, is_deleted) VALUES
((SELECT id FROM cinema_room WHERE name = N'Screen 04'), 'J', 12, 'J12', 'VIP', 1, 0),
((SELECT id FROM cinema_room WHERE name = N'Screen 04'), 'J', 13, 'J13', 'VIP', 1, 0);
GO

/* ============================================================
   16. TICKET_PRICE
   ============================================================ */
INSERT INTO ticket_price (seat_type, screen_format, day_type, time_slot, price, effective_from, effective_to) VALUES
('STANDARD', '2D',   'WEEKDAY', 'MORNING',    60000,  '2026-01-01', NULL),
('STANDARD', '2D',   'WEEKEND', 'EVENING',    110000, '2026-01-01', NULL),
('VIP',      '3D',   'WEEKEND', 'EVENING',    150000, '2026-01-01', NULL),
('STANDARD', '4DX',  'HOLIDAY', 'EVENING',    180000, '2026-01-01', NULL),
('VIP',      'IMAX', 'WEEKEND', 'AFTERNOON',  180000, '2026-01-01', NULL),
('SWEETBOX', 'IMAX', 'WEEKEND', 'LATE_NIGHT', 260000, '2026-01-01', NULL),
('VIP',      'IMAX', 'WEEKDAY', 'EVENING',    120000, '2026-01-01', NULL),   -- dùng cho vé Dune (Screen 04)
('STANDARD', 'IMAX', 'WEEKDAY', 'LATE_NIGHT', 160000, '2026-01-01', NULL);  -- dùng cho vé Joker (Cinema 7)
GO

/* ============================================================
   17. BOOKING
   ============================================================ */
INSERT INTO booking (user_id, promotion_id, booking_code, total_amount, discount_amount, final_amount, status, note) VALUES
((SELECT user_id FROM users WHERE email = 'phong.huynh@gmail.com'), NULL,
 'CMX20260520D01', 539000, 0, 542000, 'PENDING', N'Don hang dang cho thanh toan (khop anh trang Thanh toan)'),

((SELECT user_id FROM users WHERE email = 'mai.nguyen@gmail.com'), (SELECT id FROM promotion WHERE code = 'SUMMER10'),
 'CMX20261020J01', 450000, 45000, 408000, 'PAID', NULL),

((SELECT user_id FROM users WHERE email = 'khanh.tran@gmail.com'), NULL,
 'CMX20260520J03', 384000, 0, 387000, 'PAID', NULL),

((SELECT user_id FROM users WHERE email = 'linh.pham@gmail.com'), (SELECT id FROM promotion WHERE code = 'WELCOME50K'),
 'CMX20261020M01', 300000, 50000, 253000, 'CANCELLED', N'Khach huy do doi lich'),

((SELECT user_id FROM users WHERE email = 'phong.huynh@gmail.com'), NULL,
 'CMX20261021L01', 410000, 0, 413000, 'PAID', NULL),

((SELECT user_id FROM users WHERE email = 'mai.nguyen@gmail.com'), NULL,
 'CMX20261021D02', 519000, 0, 522000, 'PENDING', NULL);
GO

/* ============================================================
   18. BOOKING_COMBO
   ============================================================ */
INSERT INTO booking_combo (booking_id, combo_id, quantity, unit_price, total_price) VALUES
((SELECT id FROM booking WHERE booking_code = 'CMX20260520D01'), (SELECT id FROM combo WHERE name = 'BT21 VN Single'), 1, 299000, 299000),
((SELECT id FROM booking WHERE booking_code = 'CMX20261020J01'), (SELECT id FROM combo WHERE name = 'P CGV Combo'),    2, 135000, 270000),
((SELECT id FROM booking WHERE booking_code = 'CMX20260520J03'), (SELECT id FROM combo WHERE name = 'Hotdog Combo'),   1, 64000,  64000),
((SELECT id FROM booking WHERE booking_code = 'CMX20261021L01'), (SELECT id FROM combo WHERE name = 'Topokki Combo'),  1, 110000, 110000),
((SELECT id FROM booking WHERE booking_code = 'CMX20261021D02'), (SELECT id FROM combo WHERE name = 'Michael Combo'),  1, 259000, 259000);
GO

/* ============================================================
   19. TICKET
   -- Lưu ý: bảng ticket KHÔNG có FK tới booking trong create.sql,
   -- chỉ liên kết "mềm" qua cột booking_code (giống dữ liệu chuỗi,
   -- không phải khóa ngoại) — giữ đúng nguyên schema gốc.
   ============================================================ */
INSERT INTO ticket (customer_id, showtime_id, seat_id, ticket_price_id, booking_code, status, paid_at) VALUES
-- Booking Dune: Part Two (Screen 04, ghế J12, J13) — khớp ảnh Thanh toán
((SELECT user_id FROM users WHERE email = 'phong.huynh@gmail.com'),
 (SELECT id FROM show_time WHERE start_time = '2026-05-20 19:30:00'),
 (SELECT id FROM seat WHERE seat_code = 'J12' AND room_id = (SELECT id FROM cinema_room WHERE name = N'Screen 04')),
 (SELECT id FROM ticket_price WHERE seat_type='VIP' AND screen_format='IMAX' AND day_type='WEEKDAY' AND time_slot='EVENING'),
 'CMX20260520D01', 'PENDING', NULL),

((SELECT user_id FROM users WHERE email = 'phong.huynh@gmail.com'),
 (SELECT id FROM show_time WHERE start_time = '2026-05-20 19:30:00'),
 (SELECT id FROM seat WHERE seat_code = 'J13' AND room_id = (SELECT id FROM cinema_room WHERE name = N'Screen 04')),
 (SELECT id FROM ticket_price WHERE seat_type='VIP' AND screen_format='IMAX' AND day_type='WEEKDAY' AND time_slot='EVENING'),
 'CMX20260520D01', 'PENDING', NULL),

-- Booking Joker: Folie à Deux (Cinema 7, ghế A1, A2) — khớp ảnh Chọn ghế
((SELECT user_id FROM users WHERE email = 'khanh.tran@gmail.com'),
 (SELECT id FROM show_time WHERE start_time = '2026-05-20 22:50:00'),
 (SELECT id FROM seat WHERE seat_code = 'A1' AND room_id = (SELECT id FROM cinema_room WHERE name = N'Cinema 7')),
 (SELECT id FROM ticket_price WHERE seat_type='STANDARD' AND screen_format='IMAX' AND day_type='WEEKDAY' AND time_slot='LATE_NIGHT'),
 'CMX20260520J03', 'PAID', '2026-05-20 21:50:00'),

((SELECT user_id FROM users WHERE email = 'khanh.tran@gmail.com'),
 (SELECT id FROM show_time WHERE start_time = '2026-05-20 22:50:00'),
 (SELECT id FROM seat WHERE seat_code = 'A2' AND room_id = (SELECT id FROM cinema_room WHERE name = N'Cinema 7')),
 (SELECT id FROM ticket_price WHERE seat_type='STANDARD' AND screen_format='IMAX' AND day_type='WEEKDAY' AND time_slot='LATE_NIGHT'),
 'CMX20260520J03', 'PAID', '2026-05-20 21:50:00');
GO

/* ============================================================
   20. PAYMENT_METHOD  (khớp đúng 5 phương thức trong ảnh Thanh toán)
   ============================================================ */
INSERT INTO payment_method (method_name, provider, description, is_active) VALUES
('Vi MoMo',           'MoMo',    'Thanh toan nhanh qua ung dung MoMo', 1),
('ZaloPay',           'ZaloPay', 'Giam gia them 10k cho chu the ZaloPay', 1),
('ShopeePay',         'Shopee',  'Su dung Shopee xu de duoc giam gia', 1),
('The Quoc Te',       NULL,      'Visa, Mastercard, JCB, Amex', 1),
('The ATM Noi Dia',   NULL,      'Ho tro 40+ ngan hang tai Viet Nam', 1);
GO

/* ============================================================
   21. PAYMENT
   ============================================================ */
INSERT INTO payment (booking_id, payment_method_id, amount, payment_time, payment_status) VALUES
((SELECT id FROM booking WHERE booking_code = 'CMX20260520D01'), (SELECT id FROM payment_method WHERE method_name = 'The Quoc Te'),     542000, NULL,                  'PENDING'),
((SELECT id FROM booking WHERE booking_code = 'CMX20261020J01'), (SELECT id FROM payment_method WHERE method_name = 'Vi MoMo'),          408000, '2026-10-20 19:50:00', 'SUCCESS'),
((SELECT id FROM booking WHERE booking_code = 'CMX20260520J03'), (SELECT id FROM payment_method WHERE method_name = 'The ATM Noi Dia'),  387000, '2026-05-20 21:50:00', 'SUCCESS'),
((SELECT id FROM booking WHERE booking_code = 'CMX20261020M01'), (SELECT id FROM payment_method WHERE method_name = 'ZaloPay'),          253000, '2026-10-19 10:00:00', 'FAILED'),
((SELECT id FROM booking WHERE booking_code = 'CMX20261021D02'), (SELECT id FROM payment_method WHERE method_name = 'ShopeePay'),        522000, NULL,                  'PENDING');
GO

/* ============================================================
   HẾT FILE — Tổng kết nhanh:
   role(3) users(6) notification(6) user_notification(7) movie(7)
   genre(7) movie_genre(10) movie_review(7) combo(6) promotion(5)
   city(5) cinema(6) cinema_room(8) show_time(10) seat(82+2)
   ticket_price(8) booking(6) booking_combo(5) ticket(4)
   payment_method(5) payment(5)
   ============================================================ */