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
   • ĐÃ CHUẨN HOÁ PHÒNG CHIẾU: 13 phòng (mỗi chi nhánh 1..3 phòng, đa
     loại 2D/3D/IMAX), layout ghế cố định 72 ghế/phòng (A–D thường,
     E–H VIP, I Sweetbox, 8 ghế/hàng). Suất chiếu seed cho ngày
     02/07 & 03/07/2026 (8 phim NOW_SHOWING).
   • Giá vé (ticket_price) GIỐNG NHAU mọi chi nhánh, khác theo LOẠI
     PHÒNG × LOẠI GHẾ. Đơn demo trang Thanh toán (CMX20260520D01):
     2 ghế VIP IMAX × 155.000 = 310.000 + BT21 Single 299.000
     + phí 3.000 = Tổng 612.000đ. (Giá đã đổi so với mockup gốc.)

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
     '123456', '1990-01-15', N'Nam', 'ACTIVE',   0),

-- 6 STAFF — moi nguoi phu trach 1 rap (gan cinema_id sau khi seed cinema)
    (2, N'Hà',    N'Trần Thị',     'staff.ha@cinemax.vn',     '0900000002',
     '123456', '1996-03-22', N'Nữ',  'ACTIVE',   0),

    (2, N'Tuấn',  N'Lê Văn',       'staff.tuan@cinemax.vn',   '0900000003',
     '123456', '1995-06-10', N'Nam', 'ACTIVE',   0),

    (2, N'Thảo',  N'Phan Thị',     'staff.thao@cinemax.vn',   '0900000004',
     '123456', '1997-02-18', N'Nữ',  'ACTIVE',   0),

    (2, N'Đức',   N'Vũ Quang',     'staff.duc@cinemax.vn',    '0900000005',
     '123456', '1994-09-05', N'Nam', 'ACTIVE',   0),

    (2, N'Trang', N'Đỗ Thị',       'staff.trang@cinemax.vn',  '0900000006',
     '$2a$10$placeholderHashStaff006', '1998-12-01', N'Nữ',  'ACTIVE',   0),

    (2, N'Bảo',   N'Hồ Nguyễn',    'staff.bao@cinemax.vn',    '0900000007',
     '123456', '1996-07-25', N'Nam', 'ACTIVE',   0),

-- 4 CUSTOMER
    (3, N'Phong', N'Huỳnh Tấn',    'phong.huynh@gmail.com', '0901234567',
     '123456', '1999-07-08',  N'Nam', 'ACTIVE',   0),

    (3, N'Mai',   N'Nguyễn Thị',   'mai.nguyen@gmail.com',  '0912345678',
     '123456', '2001-11-02',  N'Nữ',  'ACTIVE',   0),

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
     '2026-09-15', '/images/joker.jpeg',
     'T18', 9.3, 'NOW_SHOWING', 0),

    (N'Dune: Part Two',
     N'Paul Atreides tiếp tục hành trình báo thù, tìm cách ngăn chặn tương lai đen tối và đoàn kết người Fremen để giành lại quyền kiểm soát Arrakis.',
     166, N'Denis Villeneuve',
     N'Timothée Chalamet, Zendaya, Rebecca Ferguson',
     '2026-04-01', '/images/parttwo.jpeg',
     'T13', 9.0, 'NOW_SHOWING', 0),

    (N'Lằn Ranh Sinh Tử',
     N'Một cựu cảnh sát buộc phải truy đuổi tổ chức buôn người xuyên quốc gia để cứu con gái trước khi quá muộn.',
     124, N'Lê Minh Khoa',
     N'Trấn Thành, Ngọc Lan, Hữu Long',
     '2026-08-01', '/images/lanranhsinhtu.jpeg',
     'T16', 8.0, 'NOW_SHOWING', 0),

    (N'Đường Đua Tốc Độ',
     N'Một tay đua trẻ phải vượt qua quá khứ để giành chiến thắng trong giải đua xuyên Việt lớn nhất từ trước đến nay.',
     110, N'Phan Đăng Vũ',
     N'Quốc Anh, Thuý Diễm, Bảo Long',
     '2026-09-01', '/images/duongduatocdo.jpeg',
     'P', 0, 'COMING_SOON', 0),

    (N'Vũ Trụ Song Hành',
     N'Hai nhà vật lý phát hiện cánh cổng dẫn đến một vũ trụ song song, nơi mọi lựa chọn của họ đã rẽ theo hướng hoàn toàn khác.',
     132, N'Đỗ Gia Linh',
     N'Mai Tài Phến, Nhã Phương',
     '2026-07-01', '/images/vutrusonghanh.jpeg',
     'T13', 0, 'COMING_SOON', 0),

    (N'Mùa Hè Của Em',
     N'Câu chuyện tình nhẹ nhàng giữa hai người trẻ gặp lại nhau sau 10 năm xa cách tại chính nơi họ từng chia tay.',
     105, N'Nguyễn Hữu Tuấn',
     N'Khả Ngân, Thanh Sơn',
     '2026-07-01', '/images/muahecuaem.jpeg',
     'P', 0, 'COMING_SOON', 0),

    (N'Bóng Ma Quá Khứ',
     N'Một gia đình chuyển đến căn nhà cũ và dần phát hiện những bí mật rùng rợn bị chôn giấu suốt 30 năm.',
     98, N'Trần Bửu Lộc',
     N'Lan Ngọc, Việt Hương',
     '2026-02-14', '/images/loinguyengiatoc.jpeg',
     'T18', 6.9, 'ENDED', 0),

    -- ============================================================
    -- NOW_SHOWING
    -- ============================================================

    (N'Kẻ Đánh Cắp Ký Ức',
     N'Một nhà khoa học phát minh thiết bị có thể xâm nhập ký ức con người nhưng nhanh chóng bị cuốn vào âm mưu đánh cắp bí mật quốc gia.',
     126, N'Nguyễn Khắc Minh',
     N'Quốc Trường, Kaity Nguyễn, Hứa Vĩ Văn',
     '2026-07-10', '/images/thanhphokhongngu.jpeg',
     'T16', 8.4, 'NOW_SHOWING', 0),

    (N'Đảo Bão',
     N'Một nhóm du khách mắc kẹt trên hòn đảo sau cơn bão lớn và phát hiện nơi đây đang che giấu một phòng thí nghiệm bí mật.',
     118, N'Bùi Quốc Việt',
     N'Liên Bỉnh Phát, Thu Anh, Bình An',
     '2026-06-28', '/images/chuyentauhoanghon.jpeg',
     'T13', 7.9, 'NOW_SHOWING', 0),

    (N'Cuộc Gọi Cuối Cùng',
     N'Một nữ tổng đài viên nhận được cuộc gọi từ tương lai, cảnh báo về chuỗi thảm họa sẽ xảy ra trong vòng 24 giờ.',
     112, N'Đặng Hoàng Nam',
     N'Jun Vũ, Anh Dũng, Quang Tuấn',
     '2026-07-02', '/images/thanhphokhongngu.jpeg',
     'T13', 8.1, 'NOW_SHOWING', 0),

    (N'Biệt Đội Săn Bão',
     N'Một nhóm chuyên gia khí tượng thực hiện nhiệm vụ nguy hiểm nhằm ngăn chặn siêu bão trước khi nó tàn phá miền Trung.',
     130, N'Võ Thành Nhân',
     N'Hồng Ánh, Kiều Minh Tuấn, Song Luân',
     '2026-05-18', '/images/chuyentauhoanghon.jpeg',
     'P', 7.7, 'NOW_SHOWING', 0),

    (N'Trò Chơi Sinh Tồn',
     N'Tám người xa lạ bị nhốt trong một khu công nghiệp bỏ hoang và buộc phải vượt qua hàng loạt thử thách để sống sót.',
     121, N'Lê Quốc Bảo',
     N'Rima Thanh Vy, Quốc Anh, Võ Điền Gia Huy',
     '2026-06-20', '/images/giaidieutinhyeu.jpeg',
     'T18', 8.5, 'NOW_SHOWING', 0),

    -- ============================================================
    -- COMING_SOON
    -- ============================================================

    (N'Người Gác Hải Đăng',
     N'Một người gác hải đăng già phát hiện những tín hiệu kỳ lạ ngoài khơi và dần hé lộ bí mật đã bị chôn vùi nhiều thập kỷ.',
     114, N'Nguyễn Minh Đức',
     N'NSƯT Thành Lộc, Lê Phương',
     '2026-07-01', '/images/nguoigachaidang.jpeg',
     'T13', 0, 'COMING_SOON', 0),

    (N'Bản Giao Hưởng Cuối',
     N'Một nhạc trưởng nổi tiếng phải hoàn thành buổi biểu diễn cuối cùng trong khi chống chọi với căn bệnh làm mất dần thính giác.',
     109, N'Vũ Hải Long',
     N'Isaac, Hoàng Hà, Hồng Đào',
     '2026-07-01', '/images/bangiaohuongcuoi.jpeg',
     'P', 0, 'COMING_SOON', 0),

    (N'Chiến Dịch Sao Đỏ',
     N'Lực lượng đặc nhiệm Việt Nam thực hiện nhiệm vụ giải cứu con tin tại một căn cứ bí mật nằm sâu trong rừng nhiệt đới.',
     137, N'Trần Quốc Hưng',
     N'Nhan Phúc Vinh, Hà Việt Dũng, Oanh Kiều',
     '2026-11-08', '/images/duongduatocdo.jpeg',
     'T16', 0, 'COMING_SOON', 0),
    (N'Giấc Mơ Atlantis',
     N'Một đoàn thám hiểm dưới đáy đại dương phát hiện tàn tích của nền văn minh Atlantis cùng sức mạnh vượt ngoài trí tưởng tượng.',
     145, N'Phạm Tuấn Anh',
     N'Khương Ngọc, Diễm My 9X, Huỳnh Anh',
     '2026-12-05', '/images/vutrusonghanh.jpeg',
     'T13', 0, 'COMING_SOON', 0),

    (N'Ánh Sáng Cuối Đường Hầm',
     N'Một kỹ sư đường sắt và đội cứu hộ chạy đua với thời gian để giải cứu hàng trăm hành khách mắc kẹt trong vụ sập hầm.',
     119, N'Đỗ Minh Quân',
     N'Thanh Sơn, Lương Thu Trang, Doãn Quốc Đam',
     '2026-10-30', '/images/thanhphokhongngu.jpeg',
     'T13', 0, 'COMING_SOON', 0);
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
                                         (N'Comedy',   0),
                                         (N'Mystery',  0),
                                         (N'Music',    0);
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
                                                 ((SELECT id FROM movie WHERE title = N'Bóng Ma Quá Khứ'),      (SELECT id FROM genre WHERE name = N'Horror')),
                                                 ((SELECT id FROM movie WHERE title = N'Kẻ Đánh Cắp Ký Ức'),    (SELECT id FROM genre WHERE name = N'Sci-Fi')),
                                                 ((SELECT id FROM movie WHERE title = N'Kẻ Đánh Cắp Ký Ức'),    (SELECT id FROM genre WHERE name = N'Thriller')),
                                                 ((SELECT id FROM movie WHERE title = N'Đảo Bão'),              (SELECT id FROM genre WHERE name = N'Action')),
                                                 ((SELECT id FROM movie WHERE title = N'Đảo Bão'),              (SELECT id FROM genre WHERE name = N'Thriller')),
                                                 ((SELECT id FROM movie WHERE title = N'Cuộc Gọi Cuối Cùng'),   (SELECT id FROM genre WHERE name = N'Thriller')),
                                                 ((SELECT id FROM movie WHERE title = N'Biệt Đội Săn Bão'),     (SELECT id FROM genre WHERE name = N'Action')),
                                                 ((SELECT id FROM movie WHERE title = N'Trò Chơi Sinh Tồn'),    (SELECT id FROM genre WHERE name = N'Horror')),
                                                 ((SELECT id FROM movie WHERE title = N'Trò Chơi Sinh Tồn'),    (SELECT id FROM genre WHERE name = N'Thriller')),
                                                 ((SELECT id FROM movie WHERE title = N'Người Gác Hải Đăng'),   (SELECT id FROM genre WHERE name = N'Drama')),
                                                 ((SELECT id FROM movie WHERE title = N'Người Gác Hải Đăng'),   (SELECT id FROM genre WHERE name = N'Mystery')),
                                                 ((SELECT id FROM movie WHERE title = N'Bản Giao Hưởng Cuối'),  (SELECT id FROM genre WHERE name = N'Drama')),
                                                 ((SELECT id FROM movie WHERE title = N'Bản Giao Hưởng Cuối'),  (SELECT id FROM genre WHERE name = N'Music')),
                                                 ((SELECT id FROM movie WHERE title = N'Chiến Dịch Sao Đỏ'),    (SELECT id FROM genre WHERE name = N'Action')),
                                                 ((SELECT id FROM movie WHERE title = N'Giấc Mơ Atlantis'),     (SELECT id FROM genre WHERE name = N'Sci-Fi')),
                                                 ((SELECT id FROM movie WHERE title = N'Ánh Sáng Cuối Đường Hầm'), (SELECT id FROM genre WHERE name = N'Thriller'));
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
   13. CINEMA_ROOM  (13 phòng — mỗi chi nhánh 1..3 phòng, đa loại)
   room_type nhận '2D' | '3D' | 'IMAX'.
     Vincom NCT : 2D + 3D + IMAX      Gigamall : 2D + IMAX
     Liberty    : 2D + 3D + IMAX      Đà Nẵng  : 2D + 3D
     Pandora    : 2D + 3D             Cần Thơ  : 2D
   ============================================================ */
INSERT INTO cinema_room (name, room_type, total_seats, cinema_id, is_deleted) VALUES
                                                                                  (N'Vincom NCT - 2D',   '2D',   72, (SELECT id FROM cinema WHERE name = N'CGV Vincom Nguyễn Chí Thanh'), 0),
                                                                                  (N'Vincom NCT - 3D',   '3D',   72, (SELECT id FROM cinema WHERE name = N'CGV Vincom Nguyễn Chí Thanh'), 0),
                                                                                  (N'Vincom NCT - IMAX', 'IMAX', 72, (SELECT id FROM cinema WHERE name = N'CGV Vincom Nguyễn Chí Thanh'), 0),

                                                                                  (N'Liberty - 2D',      '2D',   72, (SELECT id FROM cinema WHERE name = N'CGV Liberty Citypoint'), 0),
                                                                                  (N'Liberty - 3D',      '3D',   72, (SELECT id FROM cinema WHERE name = N'CGV Liberty Citypoint'), 0),
                                                                                  (N'Liberty - IMAX',    'IMAX', 72, (SELECT id FROM cinema WHERE name = N'CGV Liberty Citypoint'), 0),

                                                                                  (N'Pandora - 2D',      '2D',   72, (SELECT id FROM cinema WHERE name = N'CGV Pandora City'), 0),
                                                                                  (N'Pandora - 3D',      '3D',   72, (SELECT id FROM cinema WHERE name = N'CGV Pandora City'), 0),

                                                                                  (N'Gigamall - 2D',     '2D',   72, (SELECT id FROM cinema WHERE name = N'CGV Gigamall Thủ Đức'), 0),
                                                                                  (N'Gigamall - IMAX',   'IMAX', 72, (SELECT id FROM cinema WHERE name = N'CGV Gigamall Thủ Đức'), 0),

                                                                                  (N'Da Nang - 2D',      '2D',   72, (SELECT id FROM cinema WHERE name = N'CGV Vincom Đà Nẵng'), 0),
                                                                                  (N'Da Nang - 3D',      '3D',   72, (SELECT id FROM cinema WHERE name = N'CGV Vincom Đà Nẵng'), 0),

                                                                                  (N'Can Tho - 2D',      '2D',   72, (SELECT id FROM cinema WHERE name = N'CGV Sense City Cần Thơ'), 0);
GO

/* ============================================================
   14. SEAT  (layout CỐ ĐỊNH mọi phòng: A–D STANDARD, E–H VIP, I SWEETBOX;
   mỗi hàng 8 ghế → 72 ghế/phòng × 13 = 936 ghế)
   ============================================================ */
;WITH rows_def (row_label, seat_type) AS (
    SELECT 'A','STANDARD' UNION ALL
    SELECT 'B','STANDARD' UNION ALL
    SELECT 'C','STANDARD' UNION ALL
    SELECT 'D','STANDARD' UNION ALL
    SELECT 'E','VIP'      UNION ALL
    SELECT 'F','VIP'      UNION ALL
    SELECT 'G','VIP'      UNION ALL
    SELECT 'H','VIP'      UNION ALL
    SELECT 'I','SWEETBOX'
),
      nums (n) AS (
          SELECT 1 UNION ALL SELECT n + 1 FROM nums WHERE n < 8
      )
 INSERT INTO seat (room_id, row_label, seat_number, seat_code, type, is_active, is_deleted)
 SELECT cr.id,
        rd.row_label,
        nm.n,
        rd.row_label + CAST(nm.n AS VARCHAR(2)),
        rd.seat_type,
        1, 0
 FROM cinema_room cr
          CROSS JOIN rows_def rd
          CROSS JOIN nums nm
 WHERE cr.total_seats = 72
 OPTION (MAXRECURSION 100);
GO

/* ============================================================
   15. TICKET_PRICE  (giống nhau mọi chi nhánh, khác theo LOẠI PHÒNG × LOẠI GHẾ)
     2D   : 75.000 / 110.000 / 195.000
     3D   : 85.000 / 130.000 / 215.000
     IMAX : 95.000 / 155.000 / 270.000   (STANDARD / VIP / SWEETBOX)
   ============================================================ */
;WITH price_def (room_type, seat_type, price) AS (
    SELECT '2D',  'STANDARD',  75000 UNION ALL
    SELECT '2D',  'VIP',      110000 UNION ALL
    SELECT '2D',  'SWEETBOX', 195000 UNION ALL
    SELECT '3D',  'STANDARD',  85000 UNION ALL
    SELECT '3D',  'VIP',      130000 UNION ALL
    SELECT '3D',  'SWEETBOX', 215000 UNION ALL
    SELECT 'IMAX','STANDARD',  95000 UNION ALL
    SELECT 'IMAX','VIP',      155000 UNION ALL
    SELECT 'IMAX','SWEETBOX', 270000
)
 INSERT INTO ticket_price (room_id, seat_type, price, is_deleted)
 SELECT cr.id, pd.seat_type, pd.price, 0
 FROM cinema_room cr
          JOIN price_def pd ON pd.room_type = cr.room_type
 WHERE cr.total_seats = 72;
GO

/* ============================================================
   16. SHOW_TIME  (02/07 & 03/07/2026 — 8 phim NOW_SHOWING)
   screening_plan = (giờ, phim, loại phòng); JOIN cinema_room theo room_type
   → mỗi chi nhánh có phòng loại đó nhận đúng 1 suất.
     • 2D  : mọi phim → phủ đủ 6 chi nhánh (≥1 suất/phim).
     • 3D / IMAX : suất thứ 2 cho một số phim (Joker/Dune ưu tiên IMAX).
   end_time = DATEADD(MINUTE, duration_minutes, start_time).
   Các suất trong CÙNG phòng cách ≥ 3 giờ nên không đè giờ.
   Tổng: 2D 8×6 + 3D 4×4 + IMAX 4×3 = 76 suất.
   ============================================================ */
;WITH screening_plan (start_dt, movie_title, room_type) AS (
    -- 2D (mọi chi nhánh)
    SELECT CAST('2026-07-02 09:00:00' AS DATETIME2), N'Joker: Folie à Deux', '2D' UNION ALL
    SELECT CAST('2026-07-02 12:00:00' AS DATETIME2), N'Dune: Part Two',      '2D' UNION ALL
    SELECT CAST('2026-07-02 15:00:00' AS DATETIME2), N'Lằn Ranh Sinh Tử',    '2D' UNION ALL
    SELECT CAST('2026-07-02 18:00:00' AS DATETIME2), N'Kẻ Đánh Cắp Ký Ức',   '2D' UNION ALL
    SELECT CAST('2026-07-03 09:00:00' AS DATETIME2), N'Đảo Bão',             '2D' UNION ALL
    SELECT CAST('2026-07-03 12:00:00' AS DATETIME2), N'Cuộc Gọi Cuối Cùng',  '2D' UNION ALL
    SELECT CAST('2026-07-03 15:00:00' AS DATETIME2), N'Biệt Đội Săn Bão',    '2D' UNION ALL
    SELECT CAST('2026-07-03 18:00:00' AS DATETIME2), N'Trò Chơi Sinh Tồn',   '2D' UNION ALL
    -- 3D (Vincom NCT, Liberty, Pandora, Đà Nẵng)
    SELECT CAST('2026-07-02 13:30:00' AS DATETIME2), N'Lằn Ranh Sinh Tử',    '3D' UNION ALL
    SELECT CAST('2026-07-02 19:00:00' AS DATETIME2), N'Kẻ Đánh Cắp Ký Ức',   '3D' UNION ALL
    SELECT CAST('2026-07-03 13:30:00' AS DATETIME2), N'Biệt Đội Săn Bão',    '3D' UNION ALL
    SELECT CAST('2026-07-03 19:00:00' AS DATETIME2), N'Trò Chơi Sinh Tồn',   '3D' UNION ALL
    -- IMAX (Vincom NCT, Liberty, Gigamall)
    SELECT CAST('2026-07-02 10:30:00' AS DATETIME2), N'Joker: Folie à Deux', 'IMAX' UNION ALL
    SELECT CAST('2026-07-02 20:00:00' AS DATETIME2), N'Dune: Part Two',      'IMAX' UNION ALL
    SELECT CAST('2026-07-03 10:30:00' AS DATETIME2), N'Đảo Bão',             'IMAX' UNION ALL
    SELECT CAST('2026-07-03 20:00:00' AS DATETIME2), N'Cuộc Gọi Cuối Cùng',  'IMAX'
)
 INSERT INTO show_time (start_time, end_time, room_id, movie_id)
 SELECT p.start_dt,
        DATEADD(MINUTE, mv.duration_minutes, p.start_dt),
        cr.id,
        mv.id
 FROM screening_plan p
          JOIN movie       mv ON mv.title     = p.movie_title
          JOIN cinema_room cr ON cr.room_type = p.room_type
 WHERE cr.total_seats = 72;
GO

/* ============================================================
   17. BOOKING  (6 đơn demo — dựng lại trên phòng & suất mới 02/07)
   Phí dịch vụ 3.000đ:  final_amount = total_amount − discount_amount + 3000.
   Giá theo bảng mới, nên số tiền KHÁC ảnh mockup cũ (VD IMAX VIP = 155k).
   Phân tích CMX20260520D01 (trang Thanh toán):
     2 ghế VIP IMAX × 155.000       = 310.000đ
     Combo BT21 VN Single           = 299.000đ
     total_amount                   = 609.000đ
     Phí dịch vụ                    =   3.000đ
     final_amount                   = 612.000đ
   ============================================================ */
INSERT INTO booking
(user_id, promotion_id, booking_code,
 total_amount, discount_amount, final_amount,
 status, note)
VALUES
-- 1. Phong — Dune, Vincom NCT - IMAX 02/07 20:00, ghế E1+E2 (VIP), BT21 VN Single
((SELECT user_id FROM users WHERE email = 'phong.huynh@gmail.com'),
 NULL, 'CMX20260520D01',
 609000, 0, 612000,
 'PENDING', N'Đơn hàng đang chờ thanh toán (khớp ảnh trang Thanh toán)'),

-- 2. Mai — Joker, Pandora - 2D 02/07 09:00, ghế B1+B2, P CGV Combo ×2, mã SUMMER10 −10%
((SELECT user_id FROM users WHERE email = 'mai.nguyen@gmail.com'),
 (SELECT id FROM promotion WHERE code = 'SUMMER10'),
 'CMX20261020J01',
 420000, 42000, 381000,
 'PAID', NULL),

-- 3. Khánh — Joker, Vincom NCT - IMAX 02/07 10:30, ghế A1+A2, Hotdog Combo
((SELECT user_id FROM users WHERE email = 'khanh.tran@gmail.com'),
 NULL, 'CMX20260520J03',
 254000, 0, 257000,
 'PAID', NULL),

-- 4. Linh — Joker, Pandora - 2D, đã hủy (không có ghế/ticket), mã WELCOME50K
((SELECT user_id FROM users WHERE email = 'linh.pham@gmail.com'),
 (SELECT id FROM promotion WHERE code = 'WELCOME50K'),
 'CMX20261020M01',
 150000, 50000, 103000,
 'CANCELLED', N'Khách hủy do đổi lịch'),

-- 5. Phong — Lằn Ranh Sinh Tử, Gigamall - 2D 02/07 15:00, ghế A3+A4, Topokki Combo
((SELECT user_id FROM users WHERE email = 'phong.huynh@gmail.com'),
 NULL, 'CMX20261021L01',
 260000, 0, 263000,
 'PAID', NULL),

-- 6. Mai — Dune, Liberty - IMAX 02/07 20:00, ghế D1+D2, Michael Combo
((SELECT user_id FROM users WHERE email = 'mai.nguyen@gmail.com'),
 NULL, 'CMX20261021D02',
 449000, 0, 452000,
 'PENDING', NULL);
GO

/* ============================================================
   18. BOOKING_COMBO
   ============================================================ */
INSERT INTO booking_combo (booking_id, combo_id, quantity, unit_price, total_price) VALUES
-- CMX20260520D01 — BT21 VN Single ×1 = 299.000đ
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
   19. TICKET  (10 vé — 5 đơn có ghế; đơn CANCELLED không có vé)
   showtime resolve theo start_time + room_id (vì nhiều chi nhánh
   trùng giờ), seat & ticket_price resolve theo room_id.
   ============================================================ */
INSERT INTO ticket
(booking_id, showtime_id, seat_id, ticket_price_id, status, paid_at)
VALUES
/* ── CMX20260520D01 (PENDING) — Dune, Vincom NCT - IMAX 20:00, E1 + E2 (VIP) ── */
    ((SELECT id FROM booking WHERE booking_code = 'CMX20260520D01'),
     (SELECT id FROM show_time WHERE start_time = '2026-07-02 20:00:00'
                                 AND room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')),
     (SELECT id FROM seat WHERE seat_code = 'E1'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')
        AND seat_type = 'VIP'),
     'PENDING', NULL),
    ((SELECT id FROM booking WHERE booking_code = 'CMX20260520D01'),
     (SELECT id FROM show_time WHERE start_time = '2026-07-02 20:00:00'
                                 AND room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')),
     (SELECT id FROM seat WHERE seat_code = 'E2'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')
        AND seat_type = 'VIP'),
     'PENDING', NULL),

/* ── CMX20261020J01 (PAID) — Joker, Pandora - 2D 09:00, B1 + B2 (STANDARD) ── */
    ((SELECT id FROM booking WHERE booking_code = 'CMX20261020J01'),
     (SELECT id FROM show_time WHERE start_time = '2026-07-02 09:00:00'
                                 AND room_id = (SELECT id FROM cinema_room WHERE name = N'Pandora - 2D')),
     (SELECT id FROM seat WHERE seat_code = 'B1'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Pandora - 2D')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Pandora - 2D')
        AND seat_type = 'STANDARD'),
     'PAID', '2026-07-01 20:00:00'),
    ((SELECT id FROM booking WHERE booking_code = 'CMX20261020J01'),
     (SELECT id FROM show_time WHERE start_time = '2026-07-02 09:00:00'
                                 AND room_id = (SELECT id FROM cinema_room WHERE name = N'Pandora - 2D')),
     (SELECT id FROM seat WHERE seat_code = 'B2'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Pandora - 2D')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Pandora - 2D')
        AND seat_type = 'STANDARD'),
     'PAID', '2026-07-01 20:00:00'),

/* ── CMX20260520J03 (PAID) — Joker, Vincom NCT - IMAX 10:30, A1 + A2 (STANDARD) ── */
    ((SELECT id FROM booking WHERE booking_code = 'CMX20260520J03'),
     (SELECT id FROM show_time WHERE start_time = '2026-07-02 10:30:00'
                                 AND room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')),
     (SELECT id FROM seat WHERE seat_code = 'A1'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')
        AND seat_type = 'STANDARD'),
     'PAID', '2026-07-01 21:00:00'),
    ((SELECT id FROM booking WHERE booking_code = 'CMX20260520J03'),
     (SELECT id FROM show_time WHERE start_time = '2026-07-02 10:30:00'
                                 AND room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')),
     (SELECT id FROM seat WHERE seat_code = 'A2'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')
        AND seat_type = 'STANDARD'),
     'PAID', '2026-07-01 21:00:00'),

/* ── CMX20261021L01 (PAID) — Lằn Ranh, Gigamall - 2D 15:00, A3 + A4 (STANDARD) ── */
    ((SELECT id FROM booking WHERE booking_code = 'CMX20261021L01'),
     (SELECT id FROM show_time WHERE start_time = '2026-07-02 15:00:00'
                                 AND room_id = (SELECT id FROM cinema_room WHERE name = N'Gigamall - 2D')),
     (SELECT id FROM seat WHERE seat_code = 'A3'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Gigamall - 2D')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Gigamall - 2D')
        AND seat_type = 'STANDARD'),
     'PAID', '2026-07-01 22:00:00'),
    ((SELECT id FROM booking WHERE booking_code = 'CMX20261021L01'),
     (SELECT id FROM show_time WHERE start_time = '2026-07-02 15:00:00'
                                 AND room_id = (SELECT id FROM cinema_room WHERE name = N'Gigamall - 2D')),
     (SELECT id FROM seat WHERE seat_code = 'A4'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Gigamall - 2D')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Gigamall - 2D')
        AND seat_type = 'STANDARD'),
     'PAID', '2026-07-01 22:00:00'),

/* ── CMX20261021D02 (PENDING) — Dune, Liberty - IMAX 20:00, D1 + D2 (STANDARD) ── */
    ((SELECT id FROM booking WHERE booking_code = 'CMX20261021D02'),
     (SELECT id FROM show_time WHERE start_time = '2026-07-02 20:00:00'
                                 AND room_id = (SELECT id FROM cinema_room WHERE name = N'Liberty - IMAX')),
     (SELECT id FROM seat WHERE seat_code = 'D1'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Liberty - IMAX')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Liberty - IMAX')
        AND seat_type = 'STANDARD'),
     'PENDING', NULL),
    ((SELECT id FROM booking WHERE booking_code = 'CMX20261021D02'),
     (SELECT id FROM show_time WHERE start_time = '2026-07-02 20:00:00'
                                 AND room_id = (SELECT id FROM cinema_room WHERE name = N'Liberty - IMAX')),
     (SELECT id FROM seat WHERE seat_code = 'D2'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Liberty - IMAX')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Liberty - IMAX')
        AND seat_type = 'STANDARD'),
     'PENDING', NULL);
GO

/* ============================================================
   20. PAYMENT_METHOD  (khớp 5 phương thức trong ảnh Thanh toán)
   ============================================================ */
INSERT INTO payment_method (method_name, provider, description, is_active) VALUES
                                                                               ('Vi MoMo',         'MoMo',    'Thanh toan nhanh qua ung dung MoMo',    1),
                                                                               ('ZaloPay',         'ZaloPay', 'Giam gia them 10k cho chu the ZaloPay', 1),
                                                                               ('ShopeePay',       'Shopee',  'Su dung Shopee xu de duoc giam gia',    1),
                                                                               ('The Quoc Te',     NULL,      'Visa, Mastercard, JCB, Amex',           1),
                                                                               ('The ATM Noi Dia', NULL,      'Ho tro 40+ ngan hang tai Viet Nam',     1);
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
 612000, NULL, 'PENDING'),

-- CMX20261020J01 — đã thanh toán bằng VNPay
((SELECT id FROM booking WHERE booking_code = 'CMX20261020J01'),
 (SELECT id FROM payment_method WHERE method_name = 'Vi MoMo'),
 381000, '2026-07-01 20:00:00', 'SUCCESS'),

-- CMX20260520J03 — đã thanh toán bằng Thẻ ATM Nội Địa
((SELECT id FROM booking WHERE booking_code = 'CMX20260520J03'),
 (SELECT id FROM payment_method WHERE method_name = 'The ATM Noi Dia'),
 257000, '2026-07-01 21:00:00', 'SUCCESS'),

-- CMX20261020M01 — ZaloPay thất bại → khách hủy đơn
((SELECT id FROM booking WHERE booking_code = 'CMX20261020M01'),
 (SELECT id FROM payment_method WHERE method_name = 'ZaloPay'),
 103000, '2026-07-01 18:00:00', 'FAILED'),

-- CMX20261021L01 — đã thanh toán bằng ShopeePay
((SELECT id FROM booking WHERE booking_code = 'CMX20261021L01'),
 (SELECT id FROM payment_method WHERE method_name = 'ShopeePay'),
 263000, '2026-07-01 22:00:00', 'SUCCESS'),

-- CMX20261021D02 — chờ thanh toán
((SELECT id FROM booking WHERE booking_code = 'CMX20261021D02'),
 (SELECT id FROM payment_method WHERE method_name = 'Vi MoMo'),
 452000, NULL, 'PENDING');
GO

/* ============================================================
   TỔNG KẾT (bản gộp)
   role(3)  users(11)  notification(6)  user_notification(7)
   movie(17)  genre(9)  movie_genre  movie_review
   combo(6)  promotion  city(5)  cinema(6)
   cinema_room(13)  seat(936)  ticket_price(39)  show_time(76)
   booking(6)  booking_combo(5)  ticket(10)
   payment_method(5)  payment(6)
   ============================================================ */
