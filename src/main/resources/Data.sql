/* ============================================================
   CINEMAX — SEED DATA (bản gộp, chạy sau create.sql)
   • 10 người dùng tên thật: 1 ADMIN + 1 NHÂN VIÊN + 8 KHÁCH HÀNG.
   • 13 phòng đa loại (2D/3D/IMAX), ghế: hàng thường (A–D) & VIP (E–H)
     10 ghế/hàng, Sweetbox (I) 8 ghế → 88 ghế/phòng (×13 = 1144).
   • Suất chiếu: 03/07 (chỉ 12h–22h) + 04/07 (cả ngày), 8 phim NOW_SHOWING.
   • 3 phương thức thanh toán: VNPay, Thẻ Quốc Tế, Thẻ ATM Nội Địa.
   QUY ƯỚC:
   • FK resolve bằng khóa tự nhiên (email/title/code/name) — không hard-code ID.
   • Text tiếng Việt dùng N'...'; các cột combo/promotion/payment_method
     PHẢI là NVARCHAR (đã sửa trong create.sql) mới lưu đúng dấu.
   ============================================================ */

USE HSF_PROJECT;
GO

/* ============================================================
    FIX: Đảm bảo cột comment trong movie_review là NVARCHAR
    để lưu được tiếng Việt (idempotent, chạy nhiều lần không sao)
   ============================================================ */
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME='movie_review' AND COLUMN_NAME='comment' AND DATA_TYPE='nvarchar')
ALTER TABLE movie_review ALTER COLUMN comment NVARCHAR(MAX);
GO

/* ============================================================
    FIX: Đảm bảo cột name trong genre là NVARCHAR
    để lưu được tên thể loại tiếng Việt (idempotent)
   ============================================================ */
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME='genre' AND COLUMN_NAME='name' AND DATA_TYPE='nvarchar')
ALTER TABLE genre ALTER COLUMN name NVARCHAR(50) NOT NULL;
GO

/* ============================================================
    1. ROLE
   ============================================================ */
-- role_id: 1 = ADMIN | 2 = STAFF | 3 = CUSTOMER
INSERT INTO role (role_name, is_deleted) VALUES
                                             (N'ADMIN',    0),   -- role_id = 1
                                             (N'MANAGER',    0),   -- role_id = 2
                                             (N'CUSTOMER', 0);   -- role_id = 3
GO

/* ============================================================
   2. USERS  (10 người dùng: 1 ADMIN, 1 NHÂN VIÊN, 8 KHÁCH HÀNG — tên thật)
   role_id: 1=ADMIN  2=STAFF  3=CUSTOMER.  Nhân viên gán chi nhánh ở mục 12.
   ============================================================ */
INSERT INTO users
(role_id, first_name, last_name, email, phone_number, password,
 date_of_birth, gender, status, is_deleted)
VALUES
    (1, N'Minh',  N'Nguyễn Quang', 'admin@cinemax.vn',      '0900000001',
     '$2a$10$2y3m0IZAXxDeLRhSIrtWTuYr7MZf8UXV3OOVNjjEPjPRUHTz/AyO2', '1990-01-15', N'Nam', 'ACTIVE', 0),

    (2, N'Bảo',   N'Hồ Nguyễn',    'staff.bao@cinemax.vn',  '0900000002',
     '$2a$10$2y3m0IZAXxDeLRhSIrtWTuYr7MZf8UXV3OOVNjjEPjPRUHTz/AyO2', '1996-07-25', N'Nam', 'ACTIVE', 0),

    (3, N'Phong', N'Huỳnh Tấn',    'phong.huynh@gmail.com', '0901234567',
     '$2a$10$2y3m0IZAXxDeLRhSIrtWTuYr7MZf8UXV3OOVNjjEPjPRUHTz/AyO2', '1999-07-08', N'Nam', 'ACTIVE', 0),

    (3, N'Mai',   N'Nguyễn Thị',   'mai.nguyen@gmail.com',  '0912345678',
     '$2a$10$2y3m0IZAXxDeLRhSIrtWTuYr7MZf8UXV3OOVNjjEPjPRUHTz/AyO2', '2001-11-02', N'Nữ',  'ACTIVE', 0),

    (3, N'Khánh', N'Trần Gia',     'khanh.tran@gmail.com',  '0923456789',
     '$2a$10$2y3m0IZAXxDeLRhSIrtWTuYr7MZf8UXV3OOVNjjEPjPRUHTz/AyO2', '1998-05-19', N'Nam', 'ACTIVE', 0),

    (3, N'Linh',  N'Phạm Thuỳ',    'linh.pham@gmail.com',   '0934567890',
     '$2a$10$2y3m0IZAXxDeLRhSIrtWTuYr7MZf8UXV3OOVNjjEPjPRUHTz/AyO2', '2000-09-30', N'Nữ',  'ACTIVE', 0),

    (3, N'Nam',   N'Đặng Hoài',    'nam.dang@gmail.com',    '0945678901',
     '$2a$10$2y3m0IZAXxDeLRhSIrtWTuYr7MZf8UXV3OOVNjjEPjPRUHTz/AyO2', '1997-04-12', N'Nam', 'ACTIVE', 0),

    (3, N'Hương', N'Lê Thị',       'huong.le@gmail.com',    '0956789012',
     '$2a$10$2y3m0IZAXxDeLRhSIrtWTuYr7MZf8UXV3OOVNjjEPjPRUHTz/AyO2', '1995-08-23', N'Nữ',  'ACTIVE', 0),

    (3, N'Quân',  N'Vũ Minh',      'quan.vu@gmail.com',     '0967890123',
     '$2a$10$2y3m0IZAXxDeLRhSIrtWTuYr7MZf8UXV3OOVNjjEPjPRUHTz/AyO2', '2002-01-17', N'Nam', 'ACTIVE', 0),

    (3, N'Trâm',  N'Đỗ Bảo',       'tram.do@gmail.com',     '0978901234',
     '$2a$10$2y3m0IZAXxDeLRhSIrtWTuYr7MZf8UXV3OOVNjjEPjPRUHTz/AyO2', '1999-12-05', N'Nữ',  'ACTIVE', 0),
    (2, N'Bình', N'Viết', 'binh@fpt.edu.vn', '0912345678',
     '$2a$10$2y3m0IZAXxDeLRhSIrtWTuYr7MZf8UXV3OOVNjjEPjPRUHTz/AyO2', '2004-01-01', N'Nam', 'ACTIVE', 0);
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
     'T18', 0, 'NOW_SHOWING', 0),

    (N'Dune: Part Two',
     N'Paul Atreides tiếp tục hành trình báo thù, tìm cách ngăn chặn tương lai đen tối và đoàn kết người Fremen để giành lại quyền kiểm soát Arrakis.',
     166, N'Denis Villeneuve',
     N'Timothée Chalamet, Zendaya, Rebecca Ferguson',
     '2026-04-01', '/images/parttwo.jpeg',
     'T13', 0, 'NOW_SHOWING', 0),

    (N'Lằn Ranh Sinh Tử',
     N'Một cựu cảnh sát buộc phải truy đuổi tổ chức buôn người xuyên quốc gia để cứu con gái trước khi quá muộn.',
     124, N'Lê Minh Khoa',
     N'Trấn Thành, Ngọc Lan, Hữu Long',
     '2026-08-01', '/images/lanranhsinhtu.jpeg',
     'T16', 0, 'NOW_SHOWING', 0),

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
     '2026-08-01', '/images/vutrusonghanh.jpeg',
     'T13', 0, 'COMING_SOON', 0),

    (N'Mùa Hè Của Em',
     N'Câu chuyện tình nhẹ nhàng giữa hai người trẻ gặp lại nhau sau 10 năm xa cách tại chính nơi họ từng chia tay.',
     105, N'Nguyễn Hữu Tuấn',
     N'Khả Ngân, Thanh Sơn',
     '2026-09-01', '/images/muahecuaem.jpeg',
     'P', 0, 'COMING_SOON', 0),

    (N'Bóng Ma Quá Khứ',
     N'Một gia đình chuyển đến căn nhà cũ và dần phát hiện những bí mật rùng rợn bị chôn giấu suốt 30 năm.',
     98, N'Trần Bửu Lộc',
     N'Lan Ngọc, Việt Hương',
     '2026-02-14', '/images/loinguyengiatoc.jpeg',
     'T18', 0, 'ENDED', 0),

    -- ============================================================
    -- NOW_SHOWING
    -- ============================================================

    (N'Kẻ Đánh Cắp Ký Ức',
     N'Một nhà khoa học phát minh thiết bị có thể xâm nhập ký ức con người nhưng nhanh chóng bị cuốn vào âm mưu đánh cắp bí mật quốc gia.',
     126, N'Nguyễn Khắc Minh',
     N'Quốc Trường, Kaity Nguyễn, Hứa Vĩ Văn',
     '2026-07-10', '/images/thanhphokhongngu.jpeg',
     'T16', 0, 'NOW_SHOWING', 0),

    (N'Đảo Bão',
     N'Một nhóm du khách mắc kẹt trên hòn đảo sau cơn bão lớn và phát hiện nơi đây đang che giấu một phòng thí nghiệm bí mật.',
     118, N'Bùi Quốc Việt',
     N'Liên Bỉnh Phát, Thu Anh, Bình An',
     '2026-06-28', '/images/chuyentauhoanghon.jpeg',
     'T13', 0, 'NOW_SHOWING', 0),

    (N'Cuộc Gọi Cuối Cùng',
     N'Một nữ tổng đài viên nhận được cuộc gọi từ tương lai, cảnh báo về chuỗi thảm họa sẽ xảy ra trong vòng 24 giờ.',
     112, N'Đặng Hoàng Nam',
     N'Jun Vũ, Anh Dũng, Quang Tuấn',
     '2026-07-02', '/images/thanhphokhongngu.jpeg',
     'T13', 0, 'NOW_SHOWING', 0),

    (N'Biệt Đội Săn Bão',
     N'Một nhóm chuyên gia khí tượng thực hiện nhiệm vụ nguy hiểm nhằm ngăn chặn siêu bão trước khi nó tàn phá miền Trung.',
     130, N'Võ Thành Nhân',
     N'Hồng Ánh, Kiều Minh Tuấn, Song Luân',
     '2026-05-18', '/images/chuyentauhoanghon.jpeg',
     'P', 0, 'NOW_SHOWING', 0),

    (N'Trò Chơi Sinh Tồn',
     N'Tám người xa lạ bị nhốt trong một khu công nghiệp bỏ hoang và buộc phải vượt qua hàng loạt thử thách để sống sót.',
     121, N'Lê Quốc Bảo',
     N'Rima Thanh Vy, Quốc Anh, Võ Điền Gia Huy',
     '2026-06-20', '/images/giaidieutinhyeu.jpeg',
     'T18', 0, 'NOW_SHOWING', 0),

    -- ============================================================
    -- COMING_SOON
    -- ============================================================

    (N'Người Gác Hải Đăng',
     N'Một người gác hải đăng già phát hiện những tín hiệu kỳ lạ ngoài khơi và dần hé lộ bí mật đã bị chôn vùi nhiều thập kỷ.',
     114, N'Nguyễn Minh Đức',
     N'NSƯT Thành Lộc, Lê Phương',
     '2026-11-26', '/images/nguoigachaidang.jpeg',
     'T13', 0, 'COMING_SOON', 0),

    (N'Bản Giao Hưởng Cuối',
     N'Một nhạc trưởng nổi tiếng phải hoàn thành buổi biểu diễn cuối cùng trong khi chống chọi với căn bệnh làm mất dần thính giác.',
     109, N'Vũ Hải Long',
     N'Isaac, Hoàng Hà, Hồng Đào',
     '2026-07-01', '/images/bangiaohuongcuoi.jpeg',
     'P', 0, 'ENDED', 0),

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
                                         (N'Hành động', 0),             -- id = 1
                                         (N'Khoa học viễn tưởng', 0),   -- id = 2
                                         (N'Chính kịch / Tâm lý', 0),   -- id = 3
                                         (N'Tình cảm', 0),              -- id = 4
                                         (N'Kinh dị', 0),               -- id = 5
                                         (N'Hồi hộp', 0),               -- id = 6
                                         (N'Hài', 0),                   -- id = 7
                                         (N'Bí ẩn', 0),                 -- id = 8
                                         (N'Âm nhạc', 0),               -- id = 9
                                         (N'Phiêu lưu', 0),             -- id = 10
                                         (N'Hoạt hình', 0),             -- id = 11
                                         (N'Giả tưởng', 0),             -- id = 12
                                         (N'Tội phạm', 0),              -- id = 13
                                         (N'Gia đình', 0),              -- id = 14
                                         (N'Thiếu nhi', 0),             -- id = 15
                                         (N'Nhạc kịch', 0),             -- id = 16
                                         (N'Tiểu sử', 0),               -- id = 17
                                         (N'Lịch sử', 0),               -- id = 18
                                         (N'Chiến tranh', 0),           -- id = 19
                                         (N'Võ thuật', 0),              -- id = 20
                                         (N'Thể thao', 0),              -- id = 21
                                         (N'Tài liệu', 0),              -- id = 22
                                         (N'Viễn Tây', 0),              -- id = 23
                                         (N'Hoạt hình 3D', 0),          -- id = 24
                                         (N'Siêu anh hùng', 0);         -- id = 25
GO

/* ============================================================
   7. MOVIE_GENRE
   ============================================================ */
INSERT INTO movie_genre (movie_id, genre_id) VALUES
                                                 ((SELECT id FROM movie WHERE title = N'Joker: Folie à Deux'), (SELECT id FROM genre WHERE name = N'Hành động')),
                                                 ((SELECT id FROM movie WHERE title = N'Joker: Folie à Deux'), (SELECT id FROM genre WHERE name = N'Chính kịch / Tâm lý')),
                                                 ((SELECT id FROM movie WHERE title = N'Dune: Part Two'),       (SELECT id FROM genre WHERE name = N'Khoa học viễn tưởng')),
                                                 ((SELECT id FROM movie WHERE title = N'Dune: Part Two'),       (SELECT id FROM genre WHERE name = N'Chính kịch / Tâm lý')),
                                                 ((SELECT id FROM movie WHERE title = N'Lằn Ranh Sinh Tử'),     (SELECT id FROM genre WHERE name = N'Hành động')),
                                                 ((SELECT id FROM movie WHERE title = N'Lằn Ranh Sinh Tử'),     (SELECT id FROM genre WHERE name = N'Hồi hộp')),
                                                 ((SELECT id FROM movie WHERE title = N'Đường Đua Tốc Độ'),     (SELECT id FROM genre WHERE name = N'Hành động')),
                                                 ((SELECT id FROM movie WHERE title = N'Vũ Trụ Song Hành'),     (SELECT id FROM genre WHERE name = N'Khoa học viễn tưởng')),
                                                 ((SELECT id FROM movie WHERE title = N'Mùa Hè Của Em'),        (SELECT id FROM genre WHERE name = N'Tình cảm')),
                                                 ((SELECT id FROM movie WHERE title = N'Bóng Ma Quá Khứ'),      (SELECT id FROM genre WHERE name = N'Kinh dị')),
                                                 ((SELECT id FROM movie WHERE title = N'Kẻ Đánh Cắp Ký Ức'),    (SELECT id FROM genre WHERE name = N'Khoa học viễn tưởng')),
                                                 ((SELECT id FROM movie WHERE title = N'Kẻ Đánh Cắp Ký Ức'),    (SELECT id FROM genre WHERE name = N'Hồi hộp')),
                                                 ((SELECT id FROM movie WHERE title = N'Đảo Bão'),              (SELECT id FROM genre WHERE name = N'Hành động')),
                                                 ((SELECT id FROM movie WHERE title = N'Đảo Bão'),              (SELECT id FROM genre WHERE name = N'Hồi hộp')),
                                                 ((SELECT id FROM movie WHERE title = N'Cuộc Gọi Cuối Cùng'),   (SELECT id FROM genre WHERE name = N'Hồi hộp')),
                                                 ((SELECT id FROM movie WHERE title = N'Biệt Đội Săn Bão'),     (SELECT id FROM genre WHERE name = N'Hành động')),
                                                 ((SELECT id FROM movie WHERE title = N'Trò Chơi Sinh Tồn'),    (SELECT id FROM genre WHERE name = N'Kinh dị')),
                                                 ((SELECT id FROM movie WHERE title = N'Trò Chơi Sinh Tồn'),    (SELECT id FROM genre WHERE name = N'Hồi hộp')),
                                                 ((SELECT id FROM movie WHERE title = N'Người Gác Hải Đăng'),   (SELECT id FROM genre WHERE name = N'Chính kịch / Tâm lý')),
                                                 ((SELECT id FROM movie WHERE title = N'Người Gác Hải Đăng'),   (SELECT id FROM genre WHERE name = N'Bí ẩn')),
                                                 ((SELECT id FROM movie WHERE title = N'Bản Giao Hưởng Cuối'),  (SELECT id FROM genre WHERE name = N'Chính kịch / Tâm lý')),
                                                 ((SELECT id FROM movie WHERE title = N'Bản Giao Hưởng Cuối'),  (SELECT id FROM genre WHERE name = N'Âm nhạc')),
                                                 ((SELECT id FROM movie WHERE title = N'Chiến Dịch Sao Đỏ'),    (SELECT id FROM genre WHERE name = N'Hành động')),
                                                 ((SELECT id FROM movie WHERE title = N'Giấc Mơ Atlantis'),     (SELECT id FROM genre WHERE name = N'Khoa học viễn tưởng')),
                                                 ((SELECT id FROM movie WHERE title = N'Ánh Sáng Cuối Đường Hầm'), (SELECT id FROM genre WHERE name = N'Hồi hộp'));
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
   9. COMBO  (khớp đúng 6 combo trong ảnh Bắp Nước) — mô tả có dấu
   ============================================================ */
INSERT INTO combo (name, description, price, quantity, status, is_deleted) VALUES
                                                                               (N'P CGV Combo',
                                                                                N'01 Bắp ngọt lớn, 02 Nước ngọt siêu lớn, 01 Snack',
                                                                                135000, 200, 'ACTIVE', 0),
                                                                               (N'BT21 VN Single',
                                                                                N'01 Ly BT21 Vietnam Edition, 01 Nước ngọt siêu lớn, 01 Bắp ngọt lớn',
                                                                                299000, 80, 'ACTIVE', 0),
                                                                               (N'Hotdog Combo',
                                                                                N'01 Hotdog, 01 Nước ngọt lớn (Tặng +2.000 Upsize nước)',
                                                                                64000, 150, 'ACTIVE', 0),
                                                                               (N'Michael Combo',
                                                                                N'01 Hộp bắp non fedora Michael, 01 Nước ngọt siêu lớn, 01 Bắp ngọt lớn',
                                                                                259000, 60, 'ACTIVE', 0),
                                                                               (N'Topokki Combo',
                                                                                N'01 Topokki phô mai lắc, 01 Nước ngọt lớn',
                                                                                110000, 100, 'ACTIVE', 0),
                                                                               (N'BT21 VN Full Set',
                                                                                N'07 Ly BT21 Vietnam Edition, 02 Nước ngọt siêu lớn, 01 Bắp ngọt lớn',
                                                                                1599000, 20, 'ACTIVE', 0);
GO

/* ============================================================
   10. PROMOTION  (name/description có dấu)
   ============================================================ */
INSERT INTO promotion
(code, name, description, discount_type, discount_value,
 start_date, end_date, usage_limit, used_count, status, is_deleted)
VALUES
    ('SUMMER10',    N'Ưu đãi mùa hè',
     N'Giảm 10% cho mọi đơn đặt vé trong mùa hè',
     'PERCENT', 10,    '2026-06-01', '2026-09-30', 1000, 12, 'ACTIVE',   0),
    ('WELCOME50K',  N'Chào mừng thành viên',
     N'Giảm ngay 50.000đ cho đơn đặt vé đầu tiên',
     'FIXED',   50000, '2026-01-01', '2026-12-31',  500, 45, 'ACTIVE',   0),
    ('VIPMEMBER15', N'Ưu đãi hội viên VIP',
     N'Giảm 15% dành riêng cho hội viên VIP',
     'PERCENT', 15,    '2026-01-01', '2026-12-31', NULL,  8, 'ACTIVE',   0),
    ('FLASHSALE30', N'Flash Sale 24h',
     N'Giảm ngay 30.000đ chỉ áp dụng trong 24 giờ',
     'FIXED',   30000, '2026-07-01', '2026-07-02',  200,  5, 'ACTIVE',   0),
    ('COMBO20',     N'Ưu đãi bắp nước',
     N'Giảm 20% khi mua kèm combo bắp nước',
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

-- Gán chi nhánh làm việc cho NHÂN VIÊN (sau khi cinema đã có dữ liệu).
-- ADMIN và CUSTOMER giữ cinema_id = NULL.
UPDATE users
SET cinema_id = (SELECT id FROM cinema WHERE name = N'CGV Vincom Nguyễn Chí Thanh')
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
                                                                                  (N'Vincom NCT - 2D',   '2D',   88, (SELECT id FROM cinema WHERE name = N'CGV Vincom Nguyễn Chí Thanh'), 0),
                                                                                  (N'Vincom NCT - 3D',   '3D',   88, (SELECT id FROM cinema WHERE name = N'CGV Vincom Nguyễn Chí Thanh'), 0),
                                                                                  (N'Vincom NCT - IMAX', 'IMAX', 88, (SELECT id FROM cinema WHERE name = N'CGV Vincom Nguyễn Chí Thanh'), 0),

                                                                                  (N'Liberty - 2D',      '2D',   88, (SELECT id FROM cinema WHERE name = N'CGV Liberty Citypoint'), 0),
                                                                                  (N'Liberty - 3D',      '3D',   88, (SELECT id FROM cinema WHERE name = N'CGV Liberty Citypoint'), 0),
                                                                                  (N'Liberty - IMAX',    'IMAX', 88, (SELECT id FROM cinema WHERE name = N'CGV Liberty Citypoint'), 0),

                                                                                  (N'Pandora - 2D',      '2D',   88, (SELECT id FROM cinema WHERE name = N'CGV Pandora City'), 0),
                                                                                  (N'Pandora - 3D',      '3D',   88, (SELECT id FROM cinema WHERE name = N'CGV Pandora City'), 0),

                                                                                  (N'Gigamall - 2D',     '2D',   88, (SELECT id FROM cinema WHERE name = N'CGV Gigamall Thủ Đức'), 0),
                                                                                  (N'Gigamall - IMAX',   'IMAX', 88, (SELECT id FROM cinema WHERE name = N'CGV Gigamall Thủ Đức'), 0),

                                                                                  (N'Da Nang - 2D',      '2D',   88, (SELECT id FROM cinema WHERE name = N'CGV Vincom Đà Nẵng'), 0),
                                                                                  (N'Da Nang - 3D',      '3D',   88, (SELECT id FROM cinema WHERE name = N'CGV Vincom Đà Nẵng'), 0),

                                                                                  (N'Can Tho - 2D',      '2D',   88, (SELECT id FROM cinema WHERE name = N'CGV Sense City Cần Thơ'), 0);
GO

/* ============================================================
   14. SEAT  (hàng thường A–D & VIP E–H: 10 ghế/hàng; Sweetbox I: 8 ghế
   → 4×10 + 4×10 + 8 = 88 ghế/phòng × 13 phòng = 1144 ghế)
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
          SELECT 1 UNION ALL SELECT n + 1 FROM nums WHERE n < 10
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
 WHERE cr.total_seats = 88
   AND (rd.seat_type <> 'SWEETBOX' OR nm.n <= 8)   -- Sweetbox chỉ 8 ghế/hàng
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
 WHERE cr.total_seats = 88;
GO

/* ============================================================
   16. SHOW_TIME  (03/07 & 04/07/2026 — 8 phim NOW_SHOWING; bỏ 02/07)
   • 03/07: chỉ khung 12h–22h.   • 04/07: cả ngày.
   plan=(giờ, phim, loại phòng); JOIN theo room_type → mỗi chi nhánh có
   phòng loại đó nhận 1 suất. Mỗi phim có 1 suất 2D (phủ đủ 6 chi nhánh)
   + có thể thêm 1 suất 3D/IMAX (Joker & Dune ưu tiên IMAX).
   Suất cùng phòng cách ≥ 3h nên không đè giờ.  Tổng = 76 suất.
   ============================================================ */
;WITH screening_plan (start_dt, movie_title, room_type) AS (
    -- ===== 03/07/2026 (12h–22h) =====
    SELECT CAST('2026-07-03 12:00:00' AS DATETIME2), N'Joker: Folie à Deux', '2D'
    UNION ALL
    SELECT CAST('2026-07-03 15:00:00' AS DATETIME2), N'Dune: Part Two', '2D'
    UNION ALL
    SELECT CAST('2026-07-03 18:00:00' AS DATETIME2), N'Lằn Ranh Sinh Tử', '2D'
    UNION ALL
    SELECT CAST('2026-07-03 21:00:00' AS DATETIME2), N'Kẻ Đánh Cắp Ký Ức', '2D'
    UNION ALL
    SELECT CAST('2026-07-03 13:30:00' AS DATETIME2), N'Lằn Ranh Sinh Tử', '3D'
    UNION ALL
    SELECT CAST('2026-07-03 19:30:00' AS DATETIME2), N'Kẻ Đánh Cắp Ký Ức', '3D'
    UNION ALL
    SELECT CAST('2026-07-03 12:30:00' AS DATETIME2), N'Joker: Folie à Deux', 'IMAX'
    UNION ALL
    SELECT CAST('2026-07-03 20:00:00' AS DATETIME2), N'Dune: Part Two', 'IMAX'
    UNION ALL
    -- ===== 04/07/2026 (cả ngày) =====
    SELECT CAST('2026-07-04 09:00:00' AS DATETIME2), N'Đảo Bão', '2D'
    UNION ALL
    SELECT CAST('2026-07-04 12:00:00' AS DATETIME2), N'Cuộc Gọi Cuối Cùng', '2D'
    UNION ALL
    SELECT CAST('2026-07-04 15:00:00' AS DATETIME2), N'Biệt Đội Săn Bão', '2D'
    UNION ALL
    SELECT CAST('2026-07-04 18:00:00' AS DATETIME2), N'Trò Chơi Sinh Tồn', '2D'
    UNION ALL
    SELECT CAST('2026-07-04 10:30:00' AS DATETIME2), N'Biệt Đội Săn Bão', '3D'
    UNION ALL
    SELECT CAST('2026-07-04 20:30:00' AS DATETIME2), N'Trò Chơi Sinh Tồn', '3D'
    UNION ALL
    SELECT CAST('2026-07-04 10:00:00' AS DATETIME2), N'Đảo Bão', 'IMAX'
    UNION ALL
    SELECT CAST('2026-07-04 21:00:00' AS DATETIME2), N'Cuộc Gọi Cuối Cùng', 'IMAX'
    UNION ALL
    -- ===== 09/07/2026 =====
    SELECT CAST('2026-07-09 09:00:00' AS DATETIME2), N'Joker: Folie à Deux', '2D'
    UNION ALL
    SELECT CAST('2026-07-09 12:00:00' AS DATETIME2), N'Dune: Part Two', '2D'
    UNION ALL
    SELECT CAST('2026-07-09 15:00:00' AS DATETIME2), N'Lằn Ranh Sinh Tử', '2D'
    UNION ALL
    SELECT CAST('2026-07-09 18:00:00' AS DATETIME2), N'Kẻ Đánh Cắp Ký Ức', '2D'
    UNION ALL
    SELECT CAST('2026-07-09 10:30:00' AS DATETIME2), N'Lằn Ranh Sinh Tử', '3D'
    UNION ALL
    SELECT CAST('2026-07-09 20:30:00' AS DATETIME2), N'Kẻ Đánh Cắp Ký Ức', '3D'
    UNION ALL
    SELECT CAST('2026-07-09 10:00:00' AS DATETIME2), N'Joker: Folie à Deux', 'IMAX'
    UNION ALL
    SELECT CAST('2026-07-09 21:00:00' AS DATETIME2), N'Dune: Part Two', 'IMAX'
    UNION ALL

-- ===== 10/07/2026 =====
    SELECT CAST('2026-07-10 09:00:00' AS DATETIME2), N'Đảo Bão', '2D'
    UNION ALL
    SELECT CAST('2026-07-10 12:00:00' AS DATETIME2), N'Cuộc Gọi Cuối Cùng', '2D'
    UNION ALL
    SELECT CAST('2026-07-10 15:00:00' AS DATETIME2), N'Biệt Đội Săn Bão', '2D'
    UNION ALL
    SELECT CAST('2026-07-10 18:00:00' AS DATETIME2), N'Trò Chơi Sinh Tồn', '2D'
    UNION ALL
    SELECT CAST('2026-07-10 10:30:00' AS DATETIME2), N'Biệt Đội Săn Bão', '3D'
    UNION ALL
    SELECT CAST('2026-07-10 20:30:00' AS DATETIME2), N'Trò Chơi Sinh Tồn', '3D'
    UNION ALL
    SELECT CAST('2026-07-10 10:00:00' AS DATETIME2), N'Đảo Bão', 'IMAX'
    UNION ALL
    SELECT CAST('2026-07-10 21:00:00' AS DATETIME2), N'Cuộc Gọi Cuối Cùng', 'IMAX'
    UNION ALL

-- ===== 11/07/2026 =====
    SELECT CAST('2026-07-11 09:00:00' AS DATETIME2), N'Joker: Folie à Deux', '2D'
    UNION ALL
    SELECT CAST('2026-07-11 12:00:00' AS DATETIME2), N'Dune: Part Two', '2D'
    UNION ALL
    SELECT CAST('2026-07-11 15:00:00' AS DATETIME2), N'Đảo Bão', '2D'
    UNION ALL
    SELECT CAST('2026-07-11 18:00:00' AS DATETIME2), N'Cuộc Gọi Cuối Cùng', '2D'
    UNION ALL
    SELECT CAST('2026-07-11 10:30:00' AS DATETIME2), N'Đảo Bão', '3D'
    UNION ALL
    SELECT CAST('2026-07-11 20:30:00' AS DATETIME2), N'Cuộc Gọi Cuối Cùng', '3D'
    UNION ALL
    SELECT CAST('2026-07-11 10:00:00' AS DATETIME2), N'Joker: Folie à Deux', 'IMAX'
    UNION ALL
    SELECT CAST('2026-07-11 21:00:00' AS DATETIME2), N'Dune: Part Two', 'IMAX'
    UNION ALL

-- ===== 12/07/2026 =====
    SELECT CAST('2026-07-12 09:00:00' AS DATETIME2), N'Biệt Đội Săn Bão', '2D'
    UNION ALL
    SELECT CAST('2026-07-12 12:00:00' AS DATETIME2), N'Trò Chơi Sinh Tồn', '2D'
    UNION ALL
    SELECT CAST('2026-07-12 15:00:00' AS DATETIME2), N'Lằn Ranh Sinh Tử', '2D'
    UNION ALL
    SELECT CAST('2026-07-12 18:00:00' AS DATETIME2), N'Kẻ Đánh Cắp Ký Ức', '2D'
    UNION ALL
    SELECT CAST('2026-07-12 10:30:00' AS DATETIME2), N'Lằn Ranh Sinh Tử', '3D'
    UNION ALL
    SELECT CAST('2026-07-12 20:30:00' AS DATETIME2), N'Kẻ Đánh Cắp Ký Ức', '3D'
    UNION ALL
    SELECT CAST('2026-07-12 10:00:00' AS DATETIME2), N'Biệt Đội Săn Bão', 'IMAX'
    UNION ALL
    SELECT CAST('2026-07-12 21:00:00' AS DATETIME2), N'Trò Chơi Sinh Tồn', 'IMAX'
    UNION ALL

-- ===== 13/07/2026 =====
    SELECT CAST('2026-07-13 09:00:00' AS DATETIME2), N'Joker: Folie à Deux', '2D'
    UNION ALL
    SELECT CAST('2026-07-13 12:00:00' AS DATETIME2), N'Dune: Part Two', '2D'
    UNION ALL
    SELECT CAST('2026-07-13 15:00:00' AS DATETIME2), N'Biệt Đội Săn Bão', '2D'
    UNION ALL
    SELECT CAST('2026-07-13 18:00:00' AS DATETIME2), N'Trò Chơi Sinh Tồn', '2D'
    UNION ALL
    SELECT CAST('2026-07-13 10:30:00' AS DATETIME2), N'Biệt Đội Săn Bão', '3D'
    UNION ALL
    SELECT CAST('2026-07-13 20:30:00' AS DATETIME2), N'Trò Chơi Sinh Tồn', '3D'
    UNION ALL
    SELECT CAST('2026-07-13 10:00:00' AS DATETIME2), N'Joker: Folie à Deux', 'IMAX'
    UNION ALL
    SELECT CAST('2026-07-13 21:00:00' AS DATETIME2), N'Dune: Part Two', 'IMAX'
    UNION ALL

-- ===== 14/07/2026 =====
    SELECT CAST('2026-07-14 09:00:00' AS DATETIME2), N'Đảo Bão', '2D'
    UNION ALL
    SELECT CAST('2026-07-14 12:00:00' AS DATETIME2), N'Cuộc Gọi Cuối Cùng', '2D'
    UNION ALL
    SELECT CAST('2026-07-14 15:00:00' AS DATETIME2), N'Lằn Ranh Sinh Tử', '2D'
    UNION ALL
    SELECT CAST('2026-07-14 18:00:00' AS DATETIME2), N'Kẻ Đánh Cắp Ký Ức', '2D'
    UNION ALL
    SELECT CAST('2026-07-14 10:30:00' AS DATETIME2), N'Lằn Ranh Sinh Tử', '3D'
    UNION ALL
    SELECT CAST('2026-07-14 20:30:00' AS DATETIME2), N'Kẻ Đánh Cắp Ký Ức', '3D'
    UNION ALL
    SELECT CAST('2026-07-14 10:00:00' AS DATETIME2), N'Đảo Bão', 'IMAX'
    UNION ALL
    SELECT CAST('2026-07-14 21:00:00' AS DATETIME2), N'Cuộc Gọi Cuối Cùng', 'IMAX')

 INSERT INTO show_time (start_time, end_time, room_id, movie_id)
 SELECT p.start_dt,
        DATEADD(MINUTE, mv.duration_minutes, p.start_dt),
        cr.id,
        mv.id
 FROM screening_plan p
          JOIN movie       mv ON mv.title     = p.movie_title
          JOIN cinema_room cr ON cr.room_type = p.room_type
 WHERE cr.total_seats = 88;
GO

;WITH screening_plan (start_dt, movie_title, room_type) AS (
    -- Giả lập 2 suất chiếu test cho ngày 15/07/2026 trong kế hoạch của em
    SELECT CAST('2026-07-15 12:00:00' AS DATETIME2), N'Joker: Folie à Deux', '2D'
    UNION ALL
    SELECT CAST('2026-07-15 15:00:00' AS DATETIME2), N'Dune: Part Two', '2D'
)
 INSERT INTO show_time (start_time, end_time, room_id, movie_id)
 SELECT p.start_dt,
        DATEADD(MINUTE, mv.duration_minutes, p.start_dt),
        cr.id,
        mv.id
 FROM screening_plan p
          JOIN movie mv       ON mv.title = p.movie_title
          JOIN cinema_room cr ON cr.room_type = p.room_type
          JOIN cinema c       ON cr.cinema_id = c.id --JOIN thêm bảng rạp/chi nhánh
 WHERE cr.total_seats = 88
   AND c.name LIKE N'%CGV Vincom Nguyễn Chí Thanh%'; --Đổi tên chi nhánh em muốn test ở đây!
GO

/* ============================================================
   17. BOOKING  (6 đơn demo — dựng lại trên phòng & suất mới 03/07)
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
-- 1. Phong — Dune, Vincom NCT - IMAX 03/07 20:00, ghế E1+E2 (VIP), BT21 VN Single
((SELECT user_id FROM users WHERE email = 'phong.huynh@gmail.com'),
 NULL, 'CMX20260520D01',
 609000, 0, 612000,
 'PENDING', N'Đơn hàng đang chờ thanh toán (khớp ảnh trang Thanh toán)'),

-- 2. Mai — Joker, Pandora - 2D 03/07 09:00, ghế B1+B2, P CGV Combo ×2, mã SUMMER10 −10%
((SELECT user_id FROM users WHERE email = 'mai.nguyen@gmail.com'),
 (SELECT id FROM promotion WHERE code = 'SUMMER10'),
 'CMX20261020J01',
 420000, 42000, 381000,
 'CONFIRMED', NULL),

-- 3. Khánh — Joker, Vincom NCT - IMAX 03/07 10:30, ghế A1+A2, Hotdog Combo
((SELECT user_id FROM users WHERE email = 'khanh.tran@gmail.com'),
 NULL, 'CMX20260520J03',
 254000, 0, 257000,
 'CONFIRMED', NULL),

-- 4. Linh — Joker, Pandora - 2D, đã hủy (không có ghế/ticket), mã WELCOME50K
((SELECT user_id FROM users WHERE email = 'linh.pham@gmail.com'),
 (SELECT id FROM promotion WHERE code = 'WELCOME50K'),
 'CMX20261020M01',
 150000, 50000, 103000,
 'CANCELED', N'Khách hủy do đổi lịch'),

-- 5. Phong — Lằn Ranh Sinh Tử, Gigamall - 2D 03/07 15:00, ghế A3+A4, Topokki Combo
((SELECT user_id FROM users WHERE email = 'phong.huynh@gmail.com'),
 NULL, 'CMX20261021L01',
 260000, 0, 263000,
 'CONFIRMED', NULL),

-- 6. Mai — Dune, Liberty - IMAX 03/07 20:00, ghế D1+D2, Michael Combo
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
   19. TICKET  (10 vé — 5 đơn có ghế; đơn CANCELLED không vé)
   showtime resolve theo start_time + room_id; seat & ticket_price theo room_id.
   ============================================================ */
INSERT INTO ticket
(booking_id, showtime_id, seat_id, ticket_price_id, status, paid_at)
VALUES
    ((SELECT id FROM booking WHERE booking_code = 'CMX20260520D01'),
     (SELECT id FROM show_time WHERE start_time = '2026-07-03 20:00:00'
                                 AND room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')),
     (SELECT id FROM seat WHERE seat_code = 'E1'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')
        AND seat_type = 'VIP'),
     'PENDING', NULL),
    ((SELECT id FROM booking WHERE booking_code = 'CMX20260520D01'),
     (SELECT id FROM show_time WHERE start_time = '2026-07-03 20:00:00'
                                 AND room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')),
     (SELECT id FROM seat WHERE seat_code = 'E2'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')
        AND seat_type = 'VIP'),
     'PENDING', NULL),
    ((SELECT id FROM booking WHERE booking_code = 'CMX20261020J01'),
     (SELECT id FROM show_time WHERE start_time = '2026-07-03 12:00:00'
                                 AND room_id = (SELECT id FROM cinema_room WHERE name = N'Pandora - 2D')),
     (SELECT id FROM seat WHERE seat_code = 'B1'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Pandora - 2D')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Pandora - 2D')
        AND seat_type = 'STANDARD'),
     'PAID', '2026-07-02 20:00:00'),
    ((SELECT id FROM booking WHERE booking_code = 'CMX20261020J01'),
     (SELECT id FROM show_time WHERE start_time = '2026-07-03 12:00:00'
                                 AND room_id = (SELECT id FROM cinema_room WHERE name = N'Pandora - 2D')),
     (SELECT id FROM seat WHERE seat_code = 'B2'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Pandora - 2D')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Pandora - 2D')
        AND seat_type = 'STANDARD'),
     'PAID', '2026-07-02 20:00:00'),
    ((SELECT id FROM booking WHERE booking_code = 'CMX20260520J03'),
     (SELECT id FROM show_time WHERE start_time = '2026-07-03 12:30:00'
                                 AND room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')),
     (SELECT id FROM seat WHERE seat_code = 'A1'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')
        AND seat_type = 'STANDARD'),
     'PAID', '2026-07-02 21:00:00'),
    ((SELECT id FROM booking WHERE booking_code = 'CMX20260520J03'),
     (SELECT id FROM show_time WHERE start_time = '2026-07-03 12:30:00'
                                 AND room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')),
     (SELECT id FROM seat WHERE seat_code = 'A2'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')
        AND seat_type = 'STANDARD'),
     'PAID', '2026-07-02 21:00:00'),
    ((SELECT id FROM booking WHERE booking_code = 'CMX20261021L01'),
     (SELECT id FROM show_time WHERE start_time = '2026-07-03 18:00:00'
                                 AND room_id = (SELECT id FROM cinema_room WHERE name = N'Gigamall - 2D')),
     (SELECT id FROM seat WHERE seat_code = 'A3'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Gigamall - 2D')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Gigamall - 2D')
        AND seat_type = 'STANDARD'),
     'PAID', '2026-07-02 22:00:00'),
    ((SELECT id FROM booking WHERE booking_code = 'CMX20261021L01'),
     (SELECT id FROM show_time WHERE start_time = '2026-07-03 18:00:00'
                                 AND room_id = (SELECT id FROM cinema_room WHERE name = N'Gigamall - 2D')),
     (SELECT id FROM seat WHERE seat_code = 'A4'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Gigamall - 2D')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Gigamall - 2D')
        AND seat_type = 'STANDARD'),
     'PAID', '2026-07-02 22:00:00'),
    ((SELECT id FROM booking WHERE booking_code = 'CMX20261021D02'),
     (SELECT id FROM show_time WHERE start_time = '2026-07-03 20:00:00'
                                 AND room_id = (SELECT id FROM cinema_room WHERE name = N'Liberty - IMAX')),
     (SELECT id FROM seat WHERE seat_code = 'D1'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Liberty - IMAX')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Liberty - IMAX')
        AND seat_type = 'STANDARD'),
     'PENDING', NULL),
    ((SELECT id FROM booking WHERE booking_code = 'CMX20261021D02'),
     (SELECT id FROM show_time WHERE start_time = '2026-07-03 20:00:00'
                                 AND room_id = (SELECT id FROM cinema_room WHERE name = N'Liberty - IMAX')),
     (SELECT id FROM seat WHERE seat_code = 'D2'
                            AND room_id = (SELECT id FROM cinema_room WHERE name = N'Liberty - IMAX')),
     (SELECT id FROM ticket_price
      WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Liberty - IMAX')
        AND seat_type = 'STANDARD'),
     'PENDING', NULL);
GO

/* ============================================================
   20. PAYMENT_METHOD  (3 phương thức: VNPay, Thẻ Quốc Tế, Thẻ ATM Nội Địa)
   ============================================================ */
INSERT INTO payment_method (method_name, provider, description, is_active) VALUES
                                                                               (N'VNPay',           N'VNPAY', N'Thanh toán qua ví điện tử & ứng dụng ngân hàng VNPay', 1),
                                                                               (N'Thẻ Quốc Tế',     N'INTERNATIONAL_CARD', N'Visa, Mastercard, JCB, Amex',             1),
                                                                               (N'Thẻ ATM Nội Địa', N'DOMESTIC_ATM',       N'Hỗ trợ hơn 40 ngân hàng nội địa tại Việt Nam', 1);
GO

/* ============================================================
   21. PAYMENT
   ============================================================ */
INSERT INTO payment
(booking_id, payment_method_id, amount, payment_time, payment_status)
VALUES
    ((SELECT id FROM booking WHERE booking_code = 'CMX20260520D01'),
     (SELECT id FROM payment_method WHERE provider = 'INTERNATIONAL_CARD'),
     612000, NULL, 'PENDING'),

    ((SELECT id FROM booking WHERE booking_code = 'CMX20261020J01'),
     (SELECT id FROM payment_method WHERE provider = 'VNPAY'),
     381000, '2026-07-02 20:00:00', 'SUCCESS'),

    ((SELECT id FROM booking WHERE booking_code = 'CMX20260520J03'),
     (SELECT id FROM payment_method WHERE provider = 'DOMESTIC_ATM'),
     257000, '2026-07-02 21:00:00', 'SUCCESS'),

    ((SELECT id FROM booking WHERE booking_code = 'CMX20261020M01'),
     (SELECT id FROM payment_method WHERE provider = 'VNPAY'),
     103000, '2026-07-02 18:00:00', 'FAILED'),

    ((SELECT id FROM booking WHERE booking_code = 'CMX20261021L01'),
     (SELECT id FROM payment_method WHERE provider = 'VNPAY'),
     263000, '2026-07-02 22:00:00', 'SUCCESS'),

    ((SELECT id FROM booking WHERE booking_code = 'CMX20261021D02'),
     (SELECT id FROM payment_method WHERE provider = 'INTERNATIONAL_CARD'),
     452000, NULL, 'PENDING');
GO

/* ============================================================
   TỔNG KẾT (bản gộp)
   role(3)  users(10: 1 admin + 1 nhân viên + 8 khách)
   notification(6)  movie(17)  genre(9)  combo(6)  promotion(5)
   city(5)  cinema(6)
   cinema_room(13)  seat(1144)  ticket_price(39)  show_time(76)
   booking(6)  booking_combo(5)  ticket(10)
   payment_method(3)  payment(6)
   ============================================================ */

/* ================================================================
   FILE GỘP — CHẠY SAU create.sql + Data.sql (DB GỐC)
   Thứ tự xử lý:
     BƯỚC 1: Xóa phim trùng poster_url (giữ 1 đại diện/nhóm) + dọn cascade
             FK liên quan (payment -> booking_combo -> ticket -> booking
             -> show_time -> movie).
     BƯỚC 2: Insert 5 tài khoản manager còn thiếu (1 station = 1 manager).
     BƯỚC 3: Insert show_time test (động theo GETDATE()+30p) cho 8 phim.
             2 phim đã bị xóa ở BƯỚC 1 (Cuộc Gọi Cuối Cùng, Biệt Đội Săn Bão)
             được THAY bằng Người Gác Hải Đăng / Bản Giao Hưởng Cuối (dùng lại
             đúng phòng Gigamall - 2D / Gigamall - IMAX).
     BƯỚC 4: Insert booking + ticket (PAID) để test in vé tại CGV Vincom NCT
             (Joker, Dune — không bị ảnh hưởng bởi BƯỚC 1).
   ================================================================ */

USE HSF_PROJECT;
GO


/* ================================================================
   BƯỚC 1: XÓA PHIM TRÙNG poster_url — GIỮ 1 ĐẠI DIỆN/NHÓM
   Giữ lại: Kẻ Đánh Cắp Ký Ức, Đường Đua Tốc Độ, Vũ Trụ Song Hành, Đảo Bão
   Xóa:     Cuộc Gọi Cuối Cùng, Ánh Sáng Cuối Đường Hầm,
            Chiến Dịch Sao Đỏ, Giấc Mơ Atlantis, Biệt Đội Săn Bão
   ================================================================ */

DECLARE @MoviesToDelete TABLE (title NVARCHAR(255));
INSERT INTO @MoviesToDelete (title) VALUES
                                        (N'Cuộc Gọi Cuối Cùng'),
                                        (N'Ánh Sáng Cuối Đường Hầm'),
                                        (N'Chiến Dịch Sao Đỏ'),
                                        (N'Giấc Mơ Atlantis'),
                                        (N'Biệt Đội Săn Bão');

-- Toàn bộ show_time thuộc các phim sắp xóa (bao gồm show_time gốc trong Data.sql)
DECLARE @ShowtimeIds TABLE (id BIGINT);
INSERT INTO @ShowtimeIds (id)
SELECT st.id
FROM show_time st
         JOIN movie m ON m.id = st.movie_id
WHERE m.title IN (SELECT title FROM @MoviesToDelete);

-- Các booking có ít nhất 1 vé thuộc những show_time này
DECLARE @AffectedBookingIds TABLE (id BIGINT);
INSERT INTO @AffectedBookingIds (id)
SELECT DISTINCT booking_id
FROM ticket
WHERE showtime_id IN (SELECT id FROM @ShowtimeIds);

-- Xóa các vé thuộc show_time sắp xóa (chỉ đúng những vé đó, không đụng vé khác của cùng booking)
DELETE FROM ticket
WHERE showtime_id IN (SELECT id FROM @ShowtimeIds);

-- Trong số booking bị ảnh hưởng, chỉ những booking KHÔNG còn vé nào mới bị xóa hẳn
DECLARE @BookingsToDelete TABLE (id BIGINT);
INSERT INTO @BookingsToDelete (id)
SELECT ab.id
FROM @AffectedBookingIds ab
WHERE NOT EXISTS (SELECT 1 FROM ticket t WHERE t.booking_id = ab.id);

DELETE FROM payment WHERE booking_id IN (SELECT id FROM @BookingsToDelete);
DELETE FROM booking_combo WHERE booking_id IN (SELECT id FROM @BookingsToDelete);
DELETE FROM booking WHERE id IN (SELECT id FROM @BookingsToDelete);

DELETE FROM show_time WHERE id IN (SELECT id FROM @ShowtimeIds);

-- Dọn bảng trung gian movie_genre (many-to-many movie <-> genre) trước khi xóa movie
DELETE FROM movie_genre
WHERE movie_id IN (
    SELECT id FROM movie WHERE title IN (SELECT title FROM @MoviesToDelete)
);

DELETE FROM movie WHERE title IN (SELECT title FROM @MoviesToDelete);
GO

-- Kiểm tra lại số phim còn lại (kỳ vọng 17 - 5 = 12)
SELECT COUNT(*) AS total_movies_remaining FROM movie;
GO


/* ================================================================
   BƯỚC 2: TÀI KHOẢN MANAGER CHO TỪNG STATION (CINEMA)
   1 station = 1 manager, status = ACTIVE, role_id = 2 (MANAGER).
   Đã có sẵn: staff.bao@cinemax.vn -> CGV Vincom Nguyễn Chí Thanh
   ================================================================ */

INSERT INTO users
(role_id, first_name, last_name, email, phone_number, password,
 date_of_birth, gender, status, is_deleted)
VALUES
    (2, N'Hải',   N'Trịnh Quốc', 'manager.pandora@cinemax.vn',   '0900000003',
     '$2a$10$2y3m0IZAXxDeLRhSIrtWTuYr7MZf8UXV3OOVNjjEPjPRUHTz/AyO2', '1992-03-11', N'Nam', 'ACTIVE', 0),

    (2, N'Thảo',  N'Nguyễn Anh', 'manager.liberty@cinemax.vn',   '0900000004',
     '$2a$10$2y3m0IZAXxDeLRhSIrtWTuYr7MZf8UXV3OOVNjjEPjPRUHTz/AyO2', '1993-06-22', N'Nữ',  'ACTIVE', 0),

    (2, N'Đức',   N'Lê Minh',    'manager.gigamall@cinemax.vn',  '0900000005',
     '$2a$10$2y3m0IZAXxDeLRhSIrtWTuYr7MZf8UXV3OOVNjjEPjPRUHTz/AyO2', '1991-10-05', N'Nam', 'ACTIVE', 0),

    (2, N'Ngọc',  N'Phan Bảo',   'manager.danang@cinemax.vn',    '0900000006',
     '$2a$10$2y3m0IZAXxDeLRhSIrtWTuYr7MZf8UXV3OOVNjjEPjPRUHTz/AyO2', '1994-02-18', N'Nữ',  'ACTIVE', 0),

    (2, N'Tuấn',  N'Võ Anh',     'manager.cantho@cinemax.vn',    '0900000007',
     '$2a$10$2y3m0IZAXxDeLRhSIrtWTuYr7MZf8UXV3OOVNjjEPjPRUHTz/AyO2', '1990-12-29', N'Nam', 'ACTIVE', 0);
GO

UPDATE users
SET cinema_id = (SELECT id FROM cinema WHERE name = N'CGV Pandora City')
WHERE email = 'manager.pandora@cinemax.vn';
GO

UPDATE users
SET cinema_id = (SELECT id FROM cinema WHERE name = N'CGV Liberty Citypoint')
WHERE email = 'manager.liberty@cinemax.vn';
GO

UPDATE users
SET cinema_id = (SELECT id FROM cinema WHERE name = N'CGV Gigamall Thủ Đức')
WHERE email = 'manager.gigamall@cinemax.vn';
GO

UPDATE users
SET cinema_id = (SELECT id FROM cinema WHERE name = N'CGV Vincom Đà Nẵng')
WHERE email = 'manager.danang@cinemax.vn';
GO

UPDATE users
SET cinema_id = (SELECT id FROM cinema WHERE name = N'CGV Sense City Cần Thơ')
WHERE email = 'manager.cantho@cinemax.vn';
GO


/* ================================================================
   BƯỚC 3: SHOW_TIME ĐỘNG ĐỂ TEST (không hard-code ngày)
   - Mỗi phim 2 suất: base = GETDATE() + 30 phút, suất 2 = base + 3 giờ.
   - 2 phim đã bị xóa ở BƯỚC 1 được thay bằng phim khác, GIỮ NGUYÊN
     phòng cũ (Gigamall - 2D / Gigamall - IMAX) để không đổi cấu trúc rải phòng:
       Cuộc Gọi Cuối Cùng  -> Người Gác Hải Đăng     (Gigamall - 2D)
       Biệt Đội Săn Bão    -> Bản Giao Hưởng Cuối    (Gigamall - IMAX)
   ================================================================ */

DECLARE @base DATETIME2 = DATEADD(MINUTE, 30, GETDATE());
DECLARE @slot2 DATETIME2 = DATEADD(HOUR, 3, @base);

INSERT INTO show_time (start_time, end_time, room_id, movie_id, is_deleted)
VALUES
-- 1. Joker: Folie à Deux -> Vincom NCT - 2D
(@base,
 DATEADD(MINUTE, (SELECT duration_minutes FROM movie WHERE title = N'Joker: Folie à Deux'), @base),
 (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - 2D'),
 (SELECT id FROM movie WHERE title = N'Joker: Folie à Deux'), 0),

(@slot2,
 DATEADD(MINUTE, (SELECT duration_minutes FROM movie WHERE title = N'Joker: Folie à Deux'), @slot2),
 (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - 2D'),
 (SELECT id FROM movie WHERE title = N'Joker: Folie à Deux'), 0),

-- 2. Dune: Part Two -> Vincom NCT - IMAX
(@base,
 DATEADD(MINUTE, (SELECT duration_minutes FROM movie WHERE title = N'Dune: Part Two'), @base),
 (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX'),
 (SELECT id FROM movie WHERE title = N'Dune: Part Two'), 0),

(@slot2,
 DATEADD(MINUTE, (SELECT duration_minutes FROM movie WHERE title = N'Dune: Part Two'), @slot2),
 (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX'),
 (SELECT id FROM movie WHERE title = N'Dune: Part Two'), 0),

-- 3. Lằn Ranh Sinh Tử -> Liberty - 2D
(@base,
 DATEADD(MINUTE, (SELECT duration_minutes FROM movie WHERE title = N'Lằn Ranh Sinh Tử'), @base),
 (SELECT id FROM cinema_room WHERE name = N'Liberty - 2D'),
 (SELECT id FROM movie WHERE title = N'Lằn Ranh Sinh Tử'), 0),

(@slot2,
 DATEADD(MINUTE, (SELECT duration_minutes FROM movie WHERE title = N'Lằn Ranh Sinh Tử'), @slot2),
 (SELECT id FROM cinema_room WHERE name = N'Liberty - 2D'),
 (SELECT id FROM movie WHERE title = N'Lằn Ranh Sinh Tử'), 0),

-- 4. Kẻ Đánh Cắp Ký Ức -> Liberty - IMAX
(@base,
 DATEADD(MINUTE, (SELECT duration_minutes FROM movie WHERE title = N'Kẻ Đánh Cắp Ký Ức'), @base),
 (SELECT id FROM cinema_room WHERE name = N'Liberty - IMAX'),
 (SELECT id FROM movie WHERE title = N'Kẻ Đánh Cắp Ký Ức'), 0),

(@slot2,
 DATEADD(MINUTE, (SELECT duration_minutes FROM movie WHERE title = N'Kẻ Đánh Cắp Ký Ức'), @slot2),
 (SELECT id FROM cinema_room WHERE name = N'Liberty - IMAX'),
 (SELECT id FROM movie WHERE title = N'Kẻ Đánh Cắp Ký Ức'), 0),

-- 5. Đảo Bão -> Pandora - 2D
(@base,
 DATEADD(MINUTE, (SELECT duration_minutes FROM movie WHERE title = N'Đảo Bão'), @base),
 (SELECT id FROM cinema_room WHERE name = N'Pandora - 2D'),
 (SELECT id FROM movie WHERE title = N'Đảo Bão'), 0),

(@slot2,
 DATEADD(MINUTE, (SELECT duration_minutes FROM movie WHERE title = N'Đảo Bão'), @slot2),
 (SELECT id FROM cinema_room WHERE name = N'Pandora - 2D'),
 (SELECT id FROM movie WHERE title = N'Đảo Bão'), 0),

-- 6. Người Gác Hải Đăng (thay Cuộc Gọi Cuối Cùng - đã xóa) -> Gigamall - 2D
(@base,
 DATEADD(MINUTE, (SELECT duration_minutes FROM movie WHERE title = N'Người Gác Hải Đăng'), @base),
 (SELECT id FROM cinema_room WHERE name = N'Gigamall - 2D'),
 (SELECT id FROM movie WHERE title = N'Người Gác Hải Đăng'), 0),

(@slot2,
 DATEADD(MINUTE, (SELECT duration_minutes FROM movie WHERE title = N'Người Gác Hải Đăng'), @slot2),
 (SELECT id FROM cinema_room WHERE name = N'Gigamall - 2D'),
 (SELECT id FROM movie WHERE title = N'Người Gác Hải Đăng'), 0),

-- 7. Bản Giao Hưởng Cuối (thay Biệt Đội Săn Bão - đã xóa) -> Gigamall - IMAX
(@base,
 DATEADD(MINUTE, (SELECT duration_minutes FROM movie WHERE title = N'Bản Giao Hưởng Cuối'), @base),
 (SELECT id FROM cinema_room WHERE name = N'Gigamall - IMAX'),
 (SELECT id FROM movie WHERE title = N'Bản Giao Hưởng Cuối'), 0),

(@slot2,
 DATEADD(MINUTE, (SELECT duration_minutes FROM movie WHERE title = N'Bản Giao Hưởng Cuối'), @slot2),
 (SELECT id FROM cinema_room WHERE name = N'Gigamall - IMAX'),
 (SELECT id FROM movie WHERE title = N'Bản Giao Hưởng Cuối'), 0),

-- 8. Trò Chơi Sinh Tồn -> Da Nang - 2D
(@base,
 DATEADD(MINUTE, (SELECT duration_minutes FROM movie WHERE title = N'Trò Chơi Sinh Tồn'), @base),
 (SELECT id FROM cinema_room WHERE name = N'Da Nang - 2D'),
 (SELECT id FROM movie WHERE title = N'Trò Chơi Sinh Tồn'), 0),

(@slot2,
 DATEADD(MINUTE, (SELECT duration_minutes FROM movie WHERE title = N'Trò Chơi Sinh Tồn'), @slot2),
 (SELECT id FROM cinema_room WHERE name = N'Da Nang - 2D'),
 (SELECT id FROM movie WHERE title = N'Trò Chơi Sinh Tồn'), 0);
GO


/* ================================================================
   BƯỚC 4: BOOKING + TICKET (CONFIRMED/PAID) ĐỂ TEST IN VÉ TẠI CGV VINCOM NCT
   - Joker: Folie à Deux -> Vincom NCT - 2D  (2 suất: sớm & muộn)
   - Dune: Part Two      -> Vincom NCT - IMAX (2 suất: sớm & muộn)
   - booking.status = 'CONFIRMED' để khớp TicketRepository.existsBookedSeat()
   ================================================================ */

/* ---------- BOOKING A: Joker (suất sớm) - phong.huynh@gmail.com - 2 ghế ---------- */
INSERT INTO booking (user_id, promotion_id, booking_code, total_amount, discount_amount, final_amount, status, note, is_deleted)
VALUES (
           (SELECT user_id FROM users WHERE email = 'phong.huynh@gmail.com'),
           NULL,
           'CMXTESTNCT01',
           150000, 0, 150000,
           'CONFIRMED',
           N'Booking test in vé - Joker suất sớm - NCT',
           0
       );
GO

INSERT INTO ticket (booking_id, showtime_id, seat_id, ticket_price_id, status, paid_at, is_deleted)
VALUES
    ((SELECT id FROM booking WHERE booking_code = 'CMXTESTNCT01'),
     (SELECT TOP 1 id FROM show_time
      WHERE movie_id = (SELECT id FROM movie WHERE title = N'Joker: Folie à Deux')
        AND room_id  = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - 2D')
      ORDER BY start_time ASC),
     (SELECT id FROM seat WHERE seat_code = 'A1' AND room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - 2D')),
     (SELECT id FROM ticket_price WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - 2D') AND seat_type = 'STANDARD'),
     'PAID', GETDATE(), 0),

    ((SELECT id FROM booking WHERE booking_code = 'CMXTESTNCT01'),
     (SELECT TOP 1 id FROM show_time
      WHERE movie_id = (SELECT id FROM movie WHERE title = N'Joker: Folie à Deux')
        AND room_id  = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - 2D')
      ORDER BY start_time ASC),
     (SELECT id FROM seat WHERE seat_code = 'A2' AND room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - 2D')),
     (SELECT id FROM ticket_price WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - 2D') AND seat_type = 'STANDARD'),
     'PAID', GETDATE(), 0);
GO

INSERT INTO payment (booking_id, payment_method_id, amount, payment_time, payment_status)
VALUES (
           (SELECT id FROM booking WHERE booking_code = 'CMXTESTNCT01'),
           (SELECT id FROM payment_method WHERE provider = 'VNPAY'),
           150000, GETDATE(), 'SUCCESS'
       );
GO

/* ---------- BOOKING B: Dune: Part Two (suất sớm) - mai.nguyen@gmail.com - 2 ghế ---------- */
INSERT INTO booking (user_id, promotion_id, booking_code, total_amount, discount_amount, final_amount, status, note, is_deleted)
VALUES (
           (SELECT user_id FROM users WHERE email = 'mai.nguyen@gmail.com'),
           NULL,
           'CMXTESTNCT02',
           190000, 0, 190000,
           'CONFIRMED',
           N'Booking test in vé - Dune Part Two suất sớm - NCT',
           0
       );
GO

INSERT INTO ticket (booking_id, showtime_id, seat_id, ticket_price_id, status, paid_at, is_deleted)
VALUES
    ((SELECT id FROM booking WHERE booking_code = 'CMXTESTNCT02'),
     (SELECT TOP 1 id FROM show_time
      WHERE movie_id = (SELECT id FROM movie WHERE title = N'Dune: Part Two')
        AND room_id  = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')
      ORDER BY start_time ASC),
     (SELECT id FROM seat WHERE seat_code = 'A1' AND room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')),
     (SELECT id FROM ticket_price WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX') AND seat_type = 'STANDARD'),
     'PAID', GETDATE(), 0),

    ((SELECT id FROM booking WHERE booking_code = 'CMXTESTNCT02'),
     (SELECT TOP 1 id FROM show_time
      WHERE movie_id = (SELECT id FROM movie WHERE title = N'Dune: Part Two')
        AND room_id  = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')
      ORDER BY start_time ASC),
     (SELECT id FROM seat WHERE seat_code = 'A2' AND room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX')),
     (SELECT id FROM ticket_price WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - IMAX') AND seat_type = 'STANDARD'),
     'PAID', GETDATE(), 0);
GO

INSERT INTO payment (booking_id, payment_method_id, amount, payment_time, payment_status)
VALUES (
           (SELECT id FROM booking WHERE booking_code = 'CMXTESTNCT02'),
           (SELECT id FROM payment_method WHERE provider = 'DOMESTIC_ATM'),
           190000, GETDATE(), 'SUCCESS'
       );
GO

/* ---------- BOOKING C: Joker (suất muộn) - khanh.tran@gmail.com - 1 ghế ---------- */
INSERT INTO booking (user_id, promotion_id, booking_code, total_amount, discount_amount, final_amount, status, note, is_deleted)
VALUES (
           (SELECT user_id FROM users WHERE email = 'khanh.tran@gmail.com'),
           NULL,
           'CMXTESTNCT03',
           75000, 0, 75000,
           'CONFIRMED',
           N'Booking test in vé - Joker suất muộn - NCT',
           0
       );
GO

INSERT INTO ticket (booking_id, showtime_id, seat_id, ticket_price_id, status, paid_at, is_deleted)
VALUES
    ((SELECT id FROM booking WHERE booking_code = 'CMXTESTNCT03'),
     (SELECT TOP 1 id FROM show_time
      WHERE movie_id = (SELECT id FROM movie WHERE title = N'Joker: Folie à Deux')
        AND room_id  = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - 2D')
      ORDER BY start_time DESC),
     (SELECT id FROM seat WHERE seat_code = 'B1' AND room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - 2D')),
     (SELECT id FROM ticket_price WHERE room_id = (SELECT id FROM cinema_room WHERE name = N'Vincom NCT - 2D') AND seat_type = 'STANDARD'),
     'PAID', GETDATE(), 0);
GO

INSERT INTO payment (booking_id, payment_method_id, amount, payment_time, payment_status)
VALUES (
           (SELECT id FROM booking WHERE booking_code = 'CMXTESTNCT03'),
           (SELECT id FROM payment_method WHERE provider = 'VNPAY'),
           75000, GETDATE(), 'SUCCESS'
       );
GO