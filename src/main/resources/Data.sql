/* ============================================================
   CINEMAX — DATA SEED SCRIPT  (khớp chính xác với create.sql)
   File: data.sql

   GHI CHÚ THÊM:
   • Bảng users đã thêm cột cinema_id (chi nhánh làm việc của STAFF).
     Vì bảng cinema được tạo SAU bảng users trong create.sql, FK
     được gắn bằng ALTER TABLE ngay sau khi cinema được CREATE.
     Trong data.sql, users insert trước với cinema_id = NULL, sau
     đó UPDATE gán cinema_id cho 6 staff khi cinema đã có dữ liệu
     (xem mục 12. CINEMA) — mỗi rạp có đúng 1 staff phụ trách.
   • Bảng movie có cột age_rating VARCHAR(10) với CHECK ('P','T13','T16','T18').
     Khớp với AgeRating enum trong Movie entity.
   • Bảng ticket_price trong create.sql đơn giản hơn file data.sql
     cũ: chỉ có room_id + seat_type + price (không có screen_format,
     day_type, time_slot). File này seed theo đúng schema thật.
   • Bảng ticket dùng booking_id (FK) thay vì customer_id/booking_code
     như file data.sql cũ đã sai.
   • Cột combo.name, combo.description, promotion.code/name/description
     là VARCHAR → dùng chuỗi không dấu để tránh lỗi collation.

   QUY ƯỚC:
   • Mọi FK resolve bằng subquery theo khóa tự nhiên (email, code…)
     thay vì hard-code ID, giúp script chạy đúng dù IDENTITY
     không bắt đầu từ 1.
   • Giá vé (ticket_price) tính theo phòng + loại ghế, khớp đúng
     ảnh mockup Thanh toán: 2 ghế VIP Screen 04 × 120.000đ =
     240.000đ (Tạm tính) + BT21 Single 299.000đ + phí 3.000đ
     = Tổng 542.000đ ✓
   
   ============================================================ */

USE HSF_PROJECT;
GO

/* ============================================================
   1. ROLE
   ============================================================ */
-- role_id: 1 = ADMIN | 2 = STAFF | 3 = CUSTOMER
INSERT INTO role (role_name, is_deleted) VALUES
                                             (N'ADMIN',    0),   -- role_id = 1
                                             (N'STAFF',    0),   -- role_id = 2
                                             (N'CUSTOMER', 0);   -- role_id = 3
GO

/* ============================================================
   2. USERS  (11 nguoi dung: 1 admin, 6 staff, 4 khach hang)
   role_id hard-code: 1=ADMIN  2=STAFF  3=CUSTOMER
   6 staff tuong ung 6 rap — gan cinema_id o muc 12. CINEMA.
   ============================================================ */
INSERT INTO users
(role_id, first_name, last_name, email, phone_number, password,
 date_of_birth, gender, status, is_deleted)
VALUES
    (1, N'Minh',  N'Nguyễn Quang', 'admin@cinemax.vn',      '0900000001',
     '$2a$10$placeholderHashAdmin001', '1990-01-15', N'Nam', 'ACTIVE',   0),

-- 6 STAFF — moi nguoi phu trach 1 rap (gan cinema_id sau khi seed cinema)
    (2, N'Hà',    N'Trần Thị',     'staff.ha@cinemax.vn',     '0900000002',
     '$2a$10$placeholderHashStaff002', '1996-03-22', N'Nữ',  'ACTIVE',   0),

    (2, N'Tuấn',  N'Lê Văn',       'staff.tuan@cinemax.vn',   '0900000003',
     '$2a$10$placeholderHashStaff003', '1995-06-10', N'Nam', 'ACTIVE',   0),

    (2, N'Thảo',  N'Phan Thị',     'staff.thao@cinemax.vn',   '0900000004',
     '$2a$10$placeholderHashStaff004', '1997-02-18', N'Nữ',  'ACTIVE',   0),

    (2, N'Đức',   N'Vũ Quang',     'staff.duc@cinemax.vn',    '0900000005',
     '$2a$10$placeholderHashStaff005', '1994-09-05', N'Nam', 'ACTIVE',   0),

    (2, N'Trang', N'Đỗ Thị',       'staff.trang@cinemax.vn',  '0900000006',
     '$2a$10$placeholderHashStaff006', '1998-12-01', N'Nữ',  'ACTIVE',   0),

    (2, N'Bảo',   N'Hồ Nguyễn',    'staff.bao@cinemax.vn',    '0900000007',
     '$2a$10$placeholderHashStaff007', '1996-07-25', N'Nam', 'ACTIVE',   0),

-- 4 CUSTOMER
    (3, N'Phong', N'Huỳnh Tấn',    'phong.huynh@gmail.com', '0901234567',
     '$2a$10$placeholderHashCust003', '1999-07-08',  N'Nam', 'ACTIVE',   0),

    (3, N'Mai',   N'Nguyễn Thị',   'mai.nguyen@gmail.com',  '0912345678',
     '$2a$10$placeholderHashCust004', '2001-11-02',  N'Nữ',  'ACTIVE',   0),

    (3, N'Khánh', N'Trần Gia',     'khanh.tran@gmail.com',  '0923456789',
     '$2a$10$placeholderHashCust005', '1998-05-19',  N'Nam', 'ACTIVE',   0),

    (3, N'Linh',  N'Phạm Thuỳ',    'linh.pham@gmail.com',   '0934567890',
     '$2a$10$placeholderHashCust006', '2000-09-30',  N'Nữ',  'INACTIVE', 0);
GO

/* ============================================================
   3. NOTIFICATION
   ============================================================ */
INSERT INTO notification (title, content, type, is_deleted) VALUES
                                                                (N'Đặt vé thành công',
                                                                 N'Bạn đã đặt vé thành công cho phim Dune: Part Two. Vui lòng đến rạp trước giờ chiếu 15 phút.',
                                                                 'BOOKING', 0),
                                                                (N'Ưu đãi mới',
                                                                 N'Nhập mã SUMMER10 để được giảm 10% cho mọi đơn đặt vé trong tháng này.',
                                                                 'PROMOTION', 0),
                                                                (N'Bảo trì hệ thống',
                                                                 N'Hệ thống sẽ bảo trì từ 00:00 đến 02:00 ngày 01/07/2026. Mong bạn thông cảm.',
                                                                 'SYSTEM', 0),
                                                                (N'Nhắc lịch chiếu',
                                                                 N'Suất chiếu Dune: Part Two của bạn sẽ bắt đầu trong 1 giờ nữa.',
                                                                 'REMINDER', 0),
                                                                (N'Vé đã được xác nhận',
                                                                 N'Đơn hàng CMX20261020J01 của bạn đã được xác nhận thanh toán.',
                                                                 'BOOKING', 0),
                                                                (N'Flash Sale 24h',
                                                                 N'Săn mã FLASHSALE30 giảm ngay 30.000đ, chỉ áp dụng trong 24 giờ.',
                                                                 'PROMOTION', 0);
GO

/* ============================================================
   4. USER_NOTIFICATION
   ============================================================ */
INSERT INTO user_notification (user_id, notification_id) VALUES
                                                             ((SELECT user_id FROM users WHERE email = 'phong.huynh@gmail.com'),
                                                              (SELECT notification_id FROM notification WHERE title = N'Đặt vé thành công')),
                                                             ((SELECT user_id FROM users WHERE email = 'phong.huynh@gmail.com'),
                                                              (SELECT notification_id FROM notification WHERE title = N'Nhắc lịch chiếu')),
                                                             ((SELECT user_id FROM users WHERE email = 'phong.huynh@gmail.com'),
                                                              (SELECT notification_id FROM notification WHERE title = N'Flash Sale 24h')),
                                                             ((SELECT user_id FROM users WHERE email = 'mai.nguyen@gmail.com'),
                                                              (SELECT notification_id FROM notification WHERE title = N'Vé đã được xác nhận')),
                                                             ((SELECT user_id FROM users WHERE email = 'mai.nguyen@gmail.com'),
                                                              (SELECT notification_id FROM notification WHERE title = N'Ưu đãi mới')),
                                                             ((SELECT user_id FROM users WHERE email = 'khanh.tran@gmail.com'),
                                                              (SELECT notification_id FROM notification WHERE title = N'Flash Sale 24h')),
                                                             ((SELECT user_id FROM users WHERE email = 'linh.pham@gmail.com'),
                                                              (SELECT notification_id FROM notification WHERE title = N'Bảo trì hệ thống'));
GO

/* ============================================================
   5. MOVIE
   age_rating dùng đúng giá trị của enum AgeRating:
     P   = Phổ biến (mọi đối tượng)
     T13 = Từ 13 tuổi trở lên
     T16 = Từ 16 tuổi trở lên
     T18 = Từ 18 tuổi trở lên
   ============================================================ */
INSERT INTO movie
(title, description, duration_minutes, director, [cast],
 release_date, poster_url, age_rating, average_rating, status, is_deleted)
VALUES
    (N'Joker: Folie à Deux',
     N'Arthur Fleck đối diện thế giới nội tâm hỗn loạn khi mối quan hệ với Harley Quinn ngày càng sâu đậm, giữa bối cảnh phiên tòa xét xử căng thẳng tại Gotham.',
     138, N'Todd Phillips',
     N'Joaquin Phoenix, Lady Gaga, Brendan Gleeson',
     '2026-09-15', '/assets/posters/joker-folie-a-deux.jpg',
     'T18', 8.9, 'NOW_SHOWING', 0),

    (N'Dune: Part Two',
     N'Paul Atreides tiếp tục hành trình báo thù, tìm cách ngăn chặn tương lai đen tối và đoàn kết người Fremen để giành lại quyền kiểm soát Arrakis.',
     166, N'Denis Villeneuve',
     N'Timothée Chalamet, Zendaya, Rebecca Ferguson',
     '2026-04-01', '/assets/posters/dune-part-two.jpg',
     'T13', 8.7, 'NOW_SHOWING', 0),

    (N'Lằn Ranh Sinh Tử',
     N'Một cựu cảnh sát buộc phải truy đuổi tổ chức buôn người xuyên quốc gia để cứu con gái trước khi quá muộn.',
     124, N'Lê Minh Khoa',
     N'Trấn Thành, Ngọc Lan, Hữu Long',
     '2026-08-01', '/assets/posters/lan-ranh-sinh-tu.jpg',
     'T16', 7.8, 'NOW_SHOWING', 0),

    (N'Đường Đua Tốc Độ',
     N'Một tay đua trẻ phải vượt qua quá khứ để giành chiến thắng trong giải đua xuyên Việt lớn nhất từ trước đến nay.',
     110, N'Phan Đăng Vũ',
     N'Quốc Anh, Thuý Diễm, Bảo Long',
     '2026-09-01', '/assets/posters/duong-dua-toc-do.jpg',
     'P', 7.5, 'NOW_SHOWING', 0),

    (N'Vũ Trụ Song Hành',
     N'Hai nhà vật lý phát hiện cánh cổng dẫn đến một vũ trụ song song, nơi mọi lựa chọn của họ đã rẽ theo hướng hoàn toàn khác.',
     132, N'Đỗ Gia Linh',
     N'Mai Tài Phến, Nhã Phương',
     '2026-12-10', '/assets/posters/vu-tru-song-hanh.jpg',
     'T13', 0, 'COMING_SOON', 0),

    (N'Mùa Hè Của Em',
     N'Câu chuyện tình nhẹ nhàng giữa hai người trẻ gặp lại nhau sau 10 năm xa cách tại chính nơi họ từng chia tay.',
     105, N'Nguyễn Hữu Tuấn',
     N'Khả Ngân, Thanh Sơn',
     '2026-11-20', '/assets/posters/mua-he-cua-em.jpg',
     'P', 0, 'COMING_SOON', 0),

    (N'Bóng Ma Quá Khứ',
     N'Một gia đình chuyển đến căn nhà cũ và dần phát hiện những bí mật rùng rợn bị chôn giấu suốt 30 năm.',
     98, N'Trần Bửu Lộc',
     N'Lan Ngọc, Việt Hương',
     '2026-02-14', '/assets/posters/bong-ma-qua-khu.jpg',
     'T18', 6.9, 'ENDED', 0);
GO

/* ============================================================
   6. GENRE
   ============================================================ */
INSERT INTO genre (name, is_deleted) VALUES
                                         (N'Action',   0),
                                         (N'Sci-Fi',   0),
                                         (N'Drama',    0),
                                         (N'Romance',  0),
                                         (N'Horror',   0),
                                         (N'Thriller', 0),
                                         (N'Comedy',   0);
GO

/* ============================================================
   7. MOVIE_GENRE
   ============================================================ */
INSERT INTO movie_genre (movie_id, genre_id) VALUES
                                                 ((SELECT id FROM movie WHERE title = N'Joker: Folie à Deux'), (SELECT id FROM genre WHERE name = N'Action')),
                                                 ((SELECT id FROM movie WHERE title = N'Joker: Folie à Deux'), (SELECT id FROM genre WHERE name = N'Drama')),
                                                 ((SELECT id FROM movie WHERE title = N'Dune: Part Two'),       (SELECT id FROM genre WHERE name = N'Sci-Fi')),
                                                 ((SELECT id FROM movie WHERE title = N'Dune: Part Two'),       (SELECT id FROM genre WHERE name = N'Drama')),
                                                 ((SELECT id FROM movie WHERE title = N'Lằn Ranh Sinh Tử'),     (SELECT id FROM genre WHERE name = N'Action')),
                                                 ((SELECT id FROM movie WHERE title = N'Lằn Ranh Sinh Tử'),     (SELECT id FROM genre WHERE name = N'Thriller')),
                                                 ((SELECT id FROM movie WHERE title = N'Đường Đua Tốc Độ'),     (SELECT id FROM genre WHERE name = N'Action')),
                                                 ((SELECT id FROM movie WHERE title = N'Vũ Trụ Song Hành'),     (SELECT id FROM genre WHERE name = N'Sci-Fi')),
                                                 ((SELECT id FROM movie WHERE title = N'Mùa Hè Của Em'),        (SELECT id FROM genre WHERE name = N'Romance')),
                                                 ((SELECT id FROM movie WHERE title = N'Bóng Ma Quá Khứ'),      (SELECT id FROM genre WHERE name = N'Horror'));
GO

/* ============================================================
   8. MOVIE_REVIEW
   ============================================================ */
INSERT INTO movie_review (movie_id, user_id, rating_star, comment) VALUES
                                                                       ((SELECT id FROM movie WHERE title = N'Joker: Folie à Deux'),
                                                                        (SELECT user_id FROM users WHERE email = 'phong.huynh@gmail.com'),
                                                                        5, N'Diễn xuất của Joaquin Phoenix quá đỉnh, xem ở IMAX cực kỳ đáng tiền.'),

                                                                       ((SELECT id FROM movie WHERE title = N'Joker: Folie à Deux'),
                                                                        (SELECT user_id FROM users WHERE email = 'mai.nguyen@gmail.com'),
                                                                        4, N'Phim hay nhưng nhịp phim hơi chậm ở giữa.'),

                                                                       ((SELECT id FROM movie WHERE title = N'Joker: Folie à Deux'),
                                                                        (SELECT user_id FROM users WHERE email = 'khanh.tran@gmail.com'),
                                                                        5, N'Âm thanh Dolby Atmos rất sống động, xứng đáng 5 sao.'),

                                                                       ((SELECT id FROM movie WHERE title = N'Dune: Part Two'),
                                                                        (SELECT user_id FROM users WHERE email = 'phong.huynh@gmail.com'),
                                                                        5, N'Hình ảnh hoành tráng, đáng xem trên màn IMAX.'),

                                                                       ((SELECT id FROM movie WHERE title = N'Dune: Part Two'),
                                                                        (SELECT user_id FROM users WHERE email = 'linh.pham@gmail.com'),
                                                                        4, N'Cốt truyện sâu nhưng cần xem phần 1 trước.'),

                                                                       ((SELECT id FROM movie WHERE title = N'Lằn Ranh Sinh Tử'),
                                                                        (SELECT user_id FROM users WHERE email = 'mai.nguyen@gmail.com'),
                                                                        4, N'Hành động mãn nhãn, kịch bản ổn.'),

                                                                       ((SELECT id FROM movie WHERE title = N'Đường Đua Tốc Độ'),
                                                                        (SELECT user_id FROM users WHERE email = 'khanh.tran@gmail.com'),
                                                                        3, N'Xem giải trí cuối tuần thì ổn, không cần suy nghĩ nhiều.');
GO

/* ============================================================
   9. COMBO  (khớp đúng 6 combo trong ảnh Bắp Nước)
   Tên/mô tả viết không dấu vì cột là VARCHAR (xem ghi chú đầu file).
   ============================================================ */
INSERT INTO combo (name, description, price, quantity, status, is_deleted) VALUES
                                                                               ('P CGV Combo',
                                                                                '01 Bap ngot lon, 02 Nuoc ngot sieu lon, 01 Snack',
                                                                                135000, 200, 'ACTIVE', 0),

                                                                               ('BT21 VN Single',
                                                                                '01 Ly BT21 Vietnam Edition, 01 Nuoc ngot sieu lon, 01 Bap ngot lon',
                                                                                299000, 80, 'ACTIVE', 0),

                                                                               ('Hotdog Combo',
                                                                                '01 Hotdog, 01 Nuoc ngot lon (Tang +2.000 Upsize nuoc)',
                                                                                64000, 150, 'ACTIVE', 0),

                                                                               ('Michael Combo',
                                                                                '01 Hop bap non fedora Michael, 01 Nuoc ngot sieu lon, 01 Bap ngot lon',
                                                                                259000, 60, 'ACTIVE', 0),

                                                                               ('Topokki Combo',
                                                                                '01 Topokki pho mai lac, 01 Nuoc ngot lon',
                                                                                110000, 100, 'ACTIVE', 0),

                                                                               ('BT21 VN Full Set',
                                                                                '07 Ly BT21 Vietnam Edition, 02 Nuoc ngot sieu lon, 01 Bap ngot lon',
                                                                                1599000, 20, 'ACTIVE', 0);
GO

/* ============================================================
   10. PROMOTION
   Code/name/description viết không dấu vì cột là VARCHAR.
   ============================================================ */
INSERT INTO promotion
(code, name, description, discount_type, discount_value,
 start_date, end_date, usage_limit, used_count, status, is_deleted)
VALUES
    ('SUMMER10',    'Uu dai mua he',
     'Giam 10% cho moi don dat ve trong mua he',
     'PERCENT', 10,    '2026-06-01', '2026-09-30', 1000, 12, 'ACTIVE',   0),

    ('WELCOME50K',  'Chao mung thanh vien',
     'Giam ngay 50.000d cho don dat ve dau tien',
     'FIXED',   50000, '2026-01-01', '2026-12-31',  500, 45, 'ACTIVE',   0),

    ('VIPMEMBER15', 'Uu dai hoi vien VIP',
     'Giam 15% danh rieng cho hoi vien VIP',
     'PERCENT', 15,    '2026-01-01', '2026-12-31', NULL,  8, 'ACTIVE',   0),

    ('FLASHSALE30', 'Flash Sale 24h',
     'Giam ngay 30.000d chi ap dung trong 24 gio',
     'FIXED',   30000, '2026-07-01', '2026-07-02',  200,  5, 'ACTIVE',   0),

    ('COMBO20',     'Uu dai bap nuoc',
     'Giam 20% khi mua kem combo bap nuoc',
     'PERCENT', 20,    '2026-03-01', '2026-12-31', NULL, 30, 'INACTIVE', 0);
GO

/* ============================================================
   11. CITY
   ============================================================ */
INSERT INTO city (name, is_deleted) VALUES
                                        (N'Hồ Chí Minh', 0),
                                        (N'Hà Nội',      0),
                                        (N'Đà Nẵng',     0),
                                        (N'Cần Thơ',     0),
                                        (N'Hải Phòng',   0);
GO

/* ============================================================
   12. CINEMA  (khớp đúng tên rạp trong ảnh Chi tiết phim)
   ============================================================ */
INSERT INTO cinema (name, address, city_id, is_deleted) VALUES
                                                            (N'CGV Vincom Nguyễn Chí Thanh',
                                                             N'Vincom Nguyễn Chí Thanh, Đống Đa',
                                                             (SELECT id FROM city WHERE name = N'Hà Nội'), 0),

                                                            (N'CGV Pandora City',
                                                             N'1/1 Trường Chinh, P. Tây Thạnh, Q. Tân Phú',
                                                             (SELECT id FROM city WHERE name = N'Hồ Chí Minh'), 0),

                                                            (N'CGV Liberty Citypoint',
                                                             N'Tầng M-1, 59-61 Pasteur, Quận 1',
                                                             (SELECT id FROM city WHERE name = N'Hồ Chí Minh'), 0),

                                                            (N'CGV Gigamall Thủ Đức',
                                                             N'Tầng 6, Gigamall, 240-242 Phạm Văn Đồng, Thủ Đức',
                                                             (SELECT id FROM city WHERE name = N'Hồ Chí Minh'), 0),

                                                            (N'CGV Vincom Đà Nẵng',
                                                             N'910A Ngô Quyền, Sơn Trà',
                                                             (SELECT id FROM city WHERE name = N'Đà Nẵng'), 0),

                                                            (N'CGV Sense City Cần Thơ',
                                                             N'1 Đại lộ Hòa Bình, Ninh Kiều',
                                                             (SELECT id FROM city WHERE name = N'Cần Thơ'), 0);
GO

-- Gán chi nhánh làm việc cho 6 STAFF — phải làm SAU khi cinema đã có dữ liệu.
-- Mỗi rạp có ít nhất 1 STAFF phụ trách. ADMIN và CUSTOMER giữ cinema_id = NULL.
UPDATE users
SET cinema_id = (SELECT id FROM cinema WHERE name = N'CGV Vincom Nguyễn Chí Thanh')
WHERE email = 'staff.ha@cinemax.vn';

UPDATE users
SET cinema_id = (SELECT id FROM cinema WHERE name = N'CGV Pandora City')
WHERE email = 'staff.tuan@cinemax.vn';

UPDATE users
SET cinema_id = (SELECT id FROM cinema WHERE name = N'CGV Liberty Citypoint')
WHERE email = 'staff.thao@cinemax.vn';

UPDATE users
SET cinema_id = (SELECT id FROM cinema WHERE name = N'CGV Gigamall Thủ Đức')
WHERE email = 'staff.duc@cinemax.vn';

UPDATE users
SET cinema_id = (SELECT id FROM cinema WHERE name = N'CGV Vincom Đà Nẵng')
WHERE email = 'staff.trang@cinemax.vn';

UPDATE users
SET cinema_id = (SELECT id FROM cinema WHERE name = N'CGV Sense City Cần Thơ')
WHERE email = 'staff.bao@cinemax.vn';
GO

/* ============================================================
   13. CINEMA_ROOM
   room_type nhận giá trị '2D', '3D', 'IMAX'
   (sau khi sửa CHECK constraint trong create.sql theo LỖI 1 ở trên).
   ============================================================ */
INSERT INTO cinema_room (name, room_type, total_seats, cinema_id, is_deleted) VALUES
                                                                                  (N'Cinema 7',
                                                                                   'IMAX', 82,
                                                                                   (SELECT id FROM cinema WHERE name = N'CGV Vincom Nguyễn Chí Thanh'), 0),

                                                                                  (N'Screen 04',
                                                                                   'IMAX', 144,
                                                                                   (SELECT id FROM cinema WHERE name = N'CGV Vincom Nguyễn Chí Thanh'), 0),

                                                                                  (N'Pandora - Room 1',
                                                                                   '2D', 120,
                                                                                   (SELECT id FROM cinema WHERE name = N'CGV Pandora City'), 0),

                                                                                  (N'Liberty - Room 2',
                                                                                   '3D', 100,
                                                                                   (SELECT id FROM cinema WHERE name = N'CGV Liberty Citypoint'), 0),

                                                                                  (N'Liberty - Room IMAX',
                                                                                   'IMAX', 150,
                                                                                   (SELECT id FROM cinema WHERE name = N'CGV Liberty Citypoint'), 0),

                                                                                  (N'Gigamall - Room 1',
                                                                                   '2D', 110,
                                                                                   (SELECT id FROM cinema WHERE name = N'CGV Gigamall Thủ Đức'), 0),

                                                                                  (N'Da Nang - Room 1',
                                                                                   '2D', 100,
                                                                                   (SELECT id FROM cinema WHERE name = N'CGV Vincom Đà Nẵng'), 0),

                                                                                  (N'Can Tho - Room 1',
                                                                                   '2D', 90,
                                                                                   (SELECT id FROM cinema WHERE name = N'CGV Sense City Cần Thơ'), 0);
GO

/* ============================================================
   14. SHOW_TIME
   Lưu ý: movie_id phải đổi thành INT trong create.sql (LỖI 3).
   ============================================================ */
INSERT INTO show_time (start_time, end_time, room_id, movie_id) VALUES
-- Joker @ Cinema 7 — khớp ảnh Chọn ghế (22:50 → 00:42)
('2026-05-20 22:50:00', '2026-05-21 00:42:00',
 (SELECT id FROM cinema_room WHERE name = N'Cinema 7'),
 (SELECT id FROM movie WHERE title = N'Joker: Folie à Deux')),

-- Dune @ Screen 04 — khớp ảnh Thanh toán (19:30 → 22:16)
('2026-05-20 19:30:00', '2026-05-20 22:16:00',
 (SELECT id FROM cinema_room WHERE name = N'Screen 04'),
 (SELECT id FROM movie WHERE title = N'Dune: Part Two')),

-- Joker @ Pandora Room 1 — khớp ảnh Chi tiết phim (10:00, 19:45)
('2026-10-20 10:00:00', '2026-10-20 12:18:00',
 (SELECT id FROM cinema_room WHERE name = N'Pandora - Room 1'),
 (SELECT id FROM movie WHERE title = N'Joker: Folie à Deux')),

('2026-10-20 19:45:00', '2026-10-20 22:03:00',
 (SELECT id FROM cinema_room WHERE name = N'Pandora - Room 1'),
 (SELECT id FROM movie WHERE title = N'Joker: Folie à Deux')),

-- Joker @ Liberty Room 2 (3D) — khớp ảnh Chi tiết phim (18:30, 22:00)
('2026-10-20 18:30:00', '2026-10-20 20:48:00',
 (SELECT id FROM cinema_room WHERE name = N'Liberty - Room 2'),
 (SELECT id FROM movie WHERE title = N'Joker: Folie à Deux')),

('2026-10-20 22:00:00', '2026-10-21 00:18:00',
 (SELECT id FROM cinema_room WHERE name = N'Liberty - Room 2'),
 (SELECT id FROM movie WHERE title = N'Joker: Folie à Deux')),

-- Joker @ Liberty Room IMAX — khớp ảnh Chi tiết phim (20:30)
('2026-10-20 20:30:00', '2026-10-20 22:48:00',
 (SELECT id FROM cinema_room WHERE name = N'Liberty - Room IMAX'),
 (SELECT id FROM movie WHERE title = N'Joker: Folie à Deux')),

-- Joker @ Gigamall — khớp ảnh Chi tiết phim (17:45)
('2026-10-20 17:45:00', '2026-10-20 20:03:00',
 (SELECT id FROM cinema_room WHERE name = N'Gigamall - Room 1'),
 (SELECT id FROM movie WHERE title = N'Joker: Folie à Deux')),

-- Lằn Ranh @ Gigamall
('2026-10-21 20:00:00', '2026-10-21 22:04:00',
 (SELECT id FROM cinema_room WHERE name = N'Gigamall - Room 1'),
 (SELECT id FROM movie WHERE title = N'Lằn Ranh Sinh Tử')),

-- Đường Đua @ Pandora
('2026-10-21 21:00:00', '2026-10-21 22:50:00',
 (SELECT id FROM cinema_room WHERE name = N'Pandora - Room 1'),
 (SELECT id FROM movie WHERE title = N'Đường Đua Tốc Độ')),

-- Dune @ Liberty IMAX
('2026-10-22 20:00:00', '2026-10-22 22:46:00',
 (SELECT id FROM cinema_room WHERE name = N'Liberty - Room IMAX'),
 (SELECT id FROM movie WHERE title = N'Dune: Part Two'));
GO

/* ============================================================
   15. SEAT
   ── Cinema 7 (IMAX, 82 ghế) — layout A–G khớp ảnh Chọn ghế:
        A, B, C, F = STANDARD × 12 mỗi hàng
        D, E       = VIP      × 12 mỗi hàng
        G          = SWEETBOX × 10
        Tổng = 12×6 + 10 = 82 ✓

   ── Screen 04 (IMAX) — chỉ seed 2 ghế J12, J13
        khớp đúng ảnh trang Thanh toán.

   ── Pandora Room 1 (2D, 120 ghế) — A–K × 10 + L × 10
        D, E = VIP; L = SWEETBOX; còn lại STANDARD.

   ── Gigamall Room 1 (2D, 110 ghế) — A–J × 10 + K × 10
        D, E = VIP; còn lại STANDARD.
   ============================================================ */

/* --- 15a. Cinema 7 (82 ghế) --- */
DECLARE @roomC7 INT = (SELECT id FROM cinema_room WHERE name = N'Cinema 7');

;WITH rows7 (row_label, max_num, seat_type) AS (
    SELECT 'A', 12, 'STANDARD' UNION ALL
    SELECT 'B', 12, 'STANDARD' UNION ALL
    SELECT 'C', 12, 'STANDARD' UNION ALL
    SELECT 'D', 12, 'VIP'      UNION ALL
    SELECT 'E', 12, 'VIP'      UNION ALL
    SELECT 'F', 12, 'STANDARD' UNION ALL
    SELECT 'G', 10, 'SWEETBOX'
),
      nums7 (n) AS (
          SELECT 1
          UNION ALL
          SELECT n + 1 FROM nums7 WHERE n < 12
      )
 INSERT INTO seat (room_id, row_label, seat_number, seat_code, type, is_active, is_deleted)
 SELECT @roomC7, r.row_label, num.n,
        r.row_label + CAST(num.n AS VARCHAR(2)),
        r.seat_type, 1, 0
 FROM rows7 r
          JOIN nums7 num ON num.n <= r.max_num
 OPTION (MAXRECURSION 100);
GO

/* --- 15b. Screen 04 — chỉ 2 ghế khớp ảnh Thanh toán --- */
INSERT INTO seat (room_id, row_label, seat_number, seat_code, type, is_active, is_deleted) VALUES
                                                                                               ((SELECT id FROM cinema_room WHERE name = N'Screen 04'), 'J', 12, 'J12', 'VIP', 1, 0),
                                                                                               ((SELECT id FROM cinema_room WHERE name = N'Screen 04'), 'J', 13, 'J13', 'VIP', 1, 0);
GO

/* --- 15c. Pandora Room 1 (120 ghế) --- */
DECLARE @roomP1 INT = (SELECT id FROM cinema_room WHERE name = N'Pandora - Room 1');

;WITH rowsP (row_label, max_num, seat_type) AS (
    SELECT 'A', 10, 'STANDARD' UNION ALL
    SELECT 'B', 10, 'STANDARD' UNION ALL
    SELECT 'C', 10, 'STANDARD' UNION ALL
    SELECT 'D', 10, 'VIP'      UNION ALL
    SELECT 'E', 10, 'VIP'      UNION ALL
    SELECT 'F', 10, 'STANDARD' UNION ALL
    SELECT 'G', 10, 'STANDARD' UNION ALL
    SELECT 'H', 10, 'STANDARD' UNION ALL
    SELECT 'I', 10, 'STANDARD' UNION ALL
    SELECT 'J', 10, 'STANDARD' UNION ALL
    SELECT 'K', 10, 'STANDARD' UNION ALL
    SELECT 'L', 10, 'SWEETBOX'
),
      numsP (n) AS (
          SELECT 1
          UNION ALL
          SELECT n + 1 FROM numsP WHERE n < 10
      )
 INSERT INTO seat (room_id, row_label, seat_number, seat_code, type, is_active, is_deleted)
 SELECT @roomP1, r.row_label, num.n,
        r.row_label + CAST(num.n AS VARCHAR(2)),
        r.seat_type, 1, 0
 FROM rowsP r
          JOIN numsP num ON num.n <= r.max_num
 OPTION (MAXRECURSION 100);
GO

/* --- 15d. Gigamall Room 1 (110 ghế) --- */
DECLARE @roomG1 INT = (SELECT id FROM cinema_room WHERE name = N'Gigamall - Room 1');

;WITH rowsG (row_label, max_num, seat_type) AS (
    SELECT 'A', 10, 'STANDARD' UNION ALL
    SELECT 'B', 10, 'STANDARD' UNION ALL
    SELECT 'C', 10, 'STANDARD' UNION ALL
    SELECT 'D', 10, 'VIP'      UNION ALL
    SELECT 'E', 10, 'VIP'      UNION ALL
    SELECT 'F', 10, 'STANDARD' UNION ALL
    SELECT 'G', 10, 'STANDARD' UNION ALL
    SELECT 'H', 10, 'STANDARD' UNION ALL
    SELECT 'I', 10, 'STANDARD' UNION ALL
    SELECT 'J', 10, 'STANDARD' UNION ALL
    SELECT 'K', 10, 'STANDARD'
),
      numsG (n) AS (
          SELECT 1
          UNION ALL
          SELECT n + 1 FROM numsG WHERE n < 10
      )
 INSERT INTO seat (room_id, row_label, seat_number, seat_code, type, is_active, is_deleted)
 SELECT @roomG1, r.row_label, num.n,
        r.row_label + CAST(num.n AS VARCHAR(2)),
        r.seat_type, 1, 0
 FROM rowsG r
          JOIN numsG num ON num.n <= r.max_num
 OPTION (MAXRECURSION 100);
GO

/* ============================================================
   16. TICKET_PRICE
   Schema thực tế trong create.sql: (room_id, seat_type, price).
   Không có screen_format / day_type / time_slot như file cũ.

   Giá vé Screen 04 VIP = 120.000đ → 2 ghế J12+J13 = 240.000đ
   khớp đúng "Tạm tính: 240.000đ" trong ảnh Thanh toán ✓
   ============================================================ */
INSERT INTO ticket_price (room_id, seat_type, price, is_deleted) VALUES
-- Cinema 7 (IMAX)
((SELECT id FROM cinema_room WHERE name = N'Cinema 7'), 'STANDARD',  90000, 0),
((SELECT id FROM cinema_room WHERE name = N'Cinema 7'), 'VIP',       150000, 0),
((SELECT id FROM cinema_room WHERE name = N'Cinema 7'), 'SWEETBOX',  260000, 0),

-- Screen 04 (IMAX) — VIP 120.000đ khớp ảnh Thanh toán
((SELECT id FROM cinema_room WHERE name = N'Screen 04'), 'STANDARD', 90000,  0),
((SELECT id FROM cinema_room WHERE name = N'Screen 04'), 'VIP',      120000, 0),
((SELECT id FROM cinema_room WHERE name = N'Screen 04'), 'SWEETBOX', 230000, 0),

-- Pandora Room 1 (2D)
((SELECT id FROM cinema_room WHERE name = N'Pandora - Room 1'), 'STANDARD', 75000,  0),
((SELECT id FROM cinema_room WHERE name = N'Pandora - Room 1'), 'VIP',      110000, 0),
((SELECT id FROM cinema_room WHERE name = N'Pandora - Room 1'), 'SWEETBOX', 195000, 0),

-- Liberty Room 2 (3D)
((SELECT id FROM cinema_room WHERE name = N'Liberty - Room 2'), 'STANDARD', 85000,  0),
((SELECT id FROM cinema_room WHERE name = N'Liberty - Room 2'), 'VIP',      130000, 0),
((SELECT id FROM cinema_room WHERE name = N'Liberty - Room 2'), 'SWEETBOX', 215000, 0),

-- Liberty Room IMAX
((SELECT id FROM cinema_room WHERE name = N'Liberty - Room IMAX'), 'STANDARD',  95000, 0),
((SELECT id FROM cinema_room WHERE name = N'Liberty - Room IMAX'), 'VIP',       155000, 0),
((SELECT id FROM cinema_room WHERE name = N'Liberty - Room IMAX'), 'SWEETBOX',  270000, 0),

-- Gigamall Room 1 (2D)
((SELECT id FROM cinema_room WHERE name = N'Gigamall - Room 1'), 'STANDARD', 75000,  0),
((SELECT id FROM cinema_room WHERE name = N'Gigamall - Room 1'), 'VIP',      110000, 0),
((SELECT id FROM cinema_room WHERE name = N'Gigamall - Room 1'), 'SWEETBOX', 195000, 0);
GO

/* ============================================================
   17. BOOKING
   (Giả sử đã sửa LỖI 2: FK_booking_user → REFERENCES users(user_id))

   Phân tích tiền CMX20260520D01 khớp ảnh Thanh toán:
     Tạm tính (2 VIP × 120.000đ)   =  240.000đ
     Combo BT21 VN Single           =  299.000đ
     total_amount                   =  539.000đ
     Khuyến mãi                     =       0đ
     Phí dịch vụ (baked into final) =    3.000đ
     final_amount                   =  542.000đ ✓
   ============================================================ */
INSERT INTO booking
(user_id, promotion_id, booking_code,
 total_amount, discount_amount, final_amount,
 status, note)
VALUES
-- 1. Phong — Dune Part Two, Screen 04, ghế J12+J13, BT21 VN Single
((SELECT user_id FROM users WHERE email = 'phong.huynh@gmail.com'),
 NULL, 'CMX20260520D01',
 539000, 0, 542000,
 'PENDING', N'Đơn hàng đang chờ thanh toán (khớp ảnh trang Thanh toán)'),

-- 2. Mai — Joker @ Pandora 19:45, ghế B1+B2, P CGV Combo ×2, mã SUMMER10 −10%
((SELECT user_id FROM users WHERE email = 'mai.nguyen@gmail.com'),
 (SELECT id FROM promotion WHERE code = 'SUMMER10'),
 'CMX20261020J01',
 420000, 42000, 381000,
 'PAID', NULL),

-- 3. Khánh — Joker @ Cinema 7, ghế A1+A2, Hotdog Combo
((SELECT user_id FROM users WHERE email = 'khanh.tran@gmail.com'),
 NULL, 'CMX20260520J03',
 244000, 0, 247000,
 'PAID', NULL),

-- 4. Linh — Joker @ Pandora 10:00, ghế C3+C4, mã WELCOME50K, đã hủy
((SELECT user_id FROM users WHERE email = 'linh.pham@gmail.com'),
 (SELECT id FROM promotion WHERE code = 'WELCOME50K'),
 'CMX20261020M01',
 300000, 50000, 253000,
 'CANCELLED', N'Khách hủy do đổi lịch'),

-- 5. Phong — Lằn Ranh Sinh Tử @ Gigamall, ghế A3+A4, Topokki Combo
((SELECT user_id FROM users WHERE email = 'phong.huynh@gmail.com'),
 NULL, 'CMX20261021L01',
 260000, 0, 263000,
 'PAID', NULL),

-- 6. Mai — Dune @ Liberty IMAX, ghế D1+D2, Michael Combo
((SELECT user_id FROM users WHERE email = 'mai.nguyen@gmail.com'),
 NULL, 'CMX20261021D02',
 569000, 0, 572000,
 'PENDING', NULL);
GO

/* ============================================================
   18. BOOKING_COMBO
   ============================================================ */
INSERT INTO booking_combo (booking_id, combo_id, quantity, unit_price, total_price) VALUES
-- CMX20260520D01 — BT21 VN Single ×1 = 299.000đ (khớp ảnh Thanh toán)
((SELECT id FROM booking WHERE booking_code = 'CMX20260520D01'),
 (SELECT id FROM combo WHERE name = 'BT21 VN Single'),
 1, 299000, 299000),

-- CMX20261020J01 — P CGV Combo ×2 = 270.000đ
((SELECT id FROM booking WHERE booking_code = 'CMX20261020J01'),
 (SELECT id FROM combo WHERE name = 'P CGV Combo'),
 2, 135000, 270000),

-- CMX20260520J03 — Hotdog Combo ×1
((SELECT id FROM booking WHERE booking_code = 'CMX20260520J03'),
 (SELECT id FROM combo WHERE name = 'Hotdog Combo'),
 1, 64000, 64000),

-- CMX20261021L01 — Topokki Combo ×1
((SELECT id FROM booking WHERE booking_code = 'CMX20261021L01'),
 (SELECT id FROM combo WHERE name = 'Topokki Combo'),
 1, 110000, 110000),

-- CMX20261021D02 — Michael Combo ×1
((SELECT id FROM booking WHERE booking_code = 'CMX20261021D02'),
 (SELECT id FROM combo WHERE name = 'Michael Combo'),
 1, 259000, 259000);
GO

/* ============================================================
   19. TICKET
   Schema thực tế: (booking_id, showtime_id, seat_id,
                    ticket_price_id, status, paid_at)
   Không có cột customer_id hay booking_code riêng
   như file data.sql cũ đã sai.

   Seed ticket cho 4 booking có đủ dữ liệu ghế:
     - CMX20260520D01 : Dune, Screen 04, J12 + J13  (PENDING)
     - CMX20260520J03 : Joker, Cinema 7, A1 + A2    (PAID)
     - CMX20261020J01 : Joker, Pandora 19:45, B1+B2 (PAID)
     - CMX20261021L01 : Lằn Ranh, Gigamall, A3+A4   (PAID)

   CMX20261020M01 (CANCELLED) và CMX20261021D02 (Liberty IMAX,
   chưa seed ghế) không có ticket record.
   ============================================================ */
INSERT INTO ticket
(booking_id, showtime_id, seat_id, ticket_price_id, status, paid_at)
VALUES
/* ── Booking CMX20260520D01 (PENDING) ── Dune, Screen 04, J12 + J13 */
    ((SELECT id FROM booking WHERE booking_code = 'CMX20260520D01'),
     (SELECT id FROM show_time WHERE start_time = '2026-05-20 19:30:00'),
     (SELECT id FROM seat WHERE seat_code = 'J12'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Screen 04')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Screen 04')
        AND seat_type = 'VIP'),
     'PENDING', NULL),

    ((SELECT id FROM booking WHERE booking_code = 'CMX20260520D01'),
     (SELECT id FROM show_time WHERE start_time = '2026-05-20 19:30:00'),
     (SELECT id FROM seat WHERE seat_code = 'J13'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Screen 04')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Screen 04')
        AND seat_type = 'VIP'),
     'PENDING', NULL),

/* ── Booking CMX20260520J03 (PAID) ── Joker, Cinema 7, A1 + A2 */
    ((SELECT id FROM booking WHERE booking_code = 'CMX20260520J03'),
     (SELECT id FROM show_time WHERE start_time = '2026-05-20 22:50:00'),
     (SELECT id FROM seat WHERE seat_code = 'A1'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Cinema 7')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Cinema 7')
        AND seat_type = 'STANDARD'),
     'PAID', '2026-05-20 21:50:00'),

    ((SELECT id FROM booking WHERE booking_code = 'CMX20260520J03'),
     (SELECT id FROM show_time WHERE start_time = '2026-05-20 22:50:00'),
     (SELECT id FROM seat WHERE seat_code = 'A2'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Cinema 7')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Cinema 7')
        AND seat_type = 'STANDARD'),
     'PAID', '2026-05-20 21:50:00'),

/* ── Booking CMX20261020J01 (PAID) ── Joker, Pandora 19:45, B1 + B2 */
    ((SELECT id FROM booking WHERE booking_code = 'CMX20261020J01'),
     (SELECT id FROM show_time WHERE start_time = '2026-10-20 19:45:00'),
     (SELECT id FROM seat WHERE seat_code = 'B1'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Pandora - Room 1')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Pandora - Room 1')
        AND seat_type = 'STANDARD'),
     'PAID', '2026-10-20 19:00:00'),

    ((SELECT id FROM booking WHERE booking_code = 'CMX20261020J01'),
     (SELECT id FROM show_time WHERE start_time = '2026-10-20 19:45:00'),
     (SELECT id FROM seat WHERE seat_code = 'B2'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Pandora - Room 1')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Pandora - Room 1')
        AND seat_type = 'STANDARD'),
     'PAID', '2026-10-20 19:00:00'),

/* ── Booking CMX20261021L01 (PAID) ── Lằn Ranh, Gigamall 20:00, A3 + A4 */
    ((SELECT id FROM booking WHERE booking_code = 'CMX20261021L01'),
     (SELECT id FROM show_time WHERE start_time = '2026-10-21 20:00:00'),
     (SELECT id FROM seat WHERE seat_code = 'A3'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Gigamall - Room 1')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Gigamall - Room 1')
        AND seat_type = 'STANDARD'),
     'PAID', '2026-10-21 19:30:00'),

    ((SELECT id FROM booking WHERE booking_code = 'CMX20261021L01'),
     (SELECT id FROM show_time WHERE start_time = '2026-10-21 20:00:00'),
     (SELECT id FROM seat WHERE seat_code = 'A4'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Gigamall - Room 1')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Gigamall - Room 1')
        AND seat_type = 'STANDARD'),
     'PAID', '2026-10-21 19:30:00');
GO

/* ============================================================
   20. PAYMENT_METHOD  (khớp 5 phương thức trong ảnh Thanh toán)
   description là VARCHAR → viết không dấu.
   ============================================================ */
INSERT INTO payment_method (method_name, provider, description, is_active) VALUES
                                                                               ('Vi MoMo',         'MoMo',    'Thanh toan nhanh qua ung dung MoMo',         1),
                                                                               ('ZaloPay',         'ZaloPay', 'Giam gia them 10k cho chu the ZaloPay',       1),
                                                                               ('ShopeePay',       'Shopee',  'Su dung Shopee xu de duoc giam gia',          1),
                                                                               ('The Quoc Te',     NULL,      'Visa, Mastercard, JCB, Amex',                 1),
                                                                               ('The ATM Noi Dia', NULL,      'Ho tro 40+ ngan hang tai Viet Nam',           1);
GO

/* ============================================================
   21. PAYMENT
   ============================================================ */
INSERT INTO payment
(booking_id, payment_method_id, amount, payment_time, payment_status)
VALUES
-- CMX20260520D01 — chờ thanh toán bằng Thẻ Quốc Tế (khớp ảnh Thanh toán)
((SELECT id FROM booking WHERE booking_code = 'CMX20260520D01'),
 (SELECT id FROM payment_method WHERE method_name = 'The Quoc Te'),
 542000, NULL, 'PENDING'),

-- CMX20261020J01 — đã thanh toán bằng Ví MoMo
((SELECT id FROM booking WHERE booking_code = 'CMX20261020J01'),
 (SELECT id FROM payment_method WHERE method_name = 'Vi MoMo'),
 381000, '2026-10-20 19:00:00', 'SUCCESS'),

-- CMX20260520J03 — đã thanh toán bằng Thẻ ATM Nội Địa
((SELECT id FROM booking WHERE booking_code = 'CMX20260520J03'),
 (SELECT id FROM payment_method WHERE method_name = 'The ATM Noi Dia'),
 247000, '2026-05-20 21:50:00', 'SUCCESS'),

-- CMX20261020M01 — ZaloPay thất bại → khách hủy đơn
((SELECT id FROM booking WHERE booking_code = 'CMX20261020M01'),
 (SELECT id FROM payment_method WHERE method_name = 'ZaloPay'),
 253000, '2026-10-19 10:00:00', 'FAILED'),

-- CMX20261021L01 — đã thanh toán bằng ShopeePay
((SELECT id FROM booking WHERE booking_code = 'CMX20261021L01'),
 (SELECT id FROM payment_method WHERE method_name = 'ShopeePay'),
 263000, '2026-10-21 19:30:00', 'SUCCESS'),

-- CMX20261021D02 — chờ thanh toán
((SELECT id FROM booking WHERE booking_code = 'CMX20261021D02'),
 (SELECT id FROM payment_method WHERE method_name = 'Vi MoMo'),
 572000, NULL, 'PENDING');
GO

/* ============================================================
   TỔNG KẾT
   role(3)  users(11)  notification(6)  user_notification(7)
   movie(7)  genre(7)  movie_genre(10)  movie_review(7)
   combo(6)  promotion(5)
   city(5)  cinema(6)  cinema_room(8)  show_time(11)
   seat: Cinema7(82) + Screen04(2) + Pandora(120) + Gigamall(110) = 314
   ticket_price(18)
   booking(6)  booking_combo(5)  ticket(8)
   payment_method(5)  payment(6)
   ============================================================ */