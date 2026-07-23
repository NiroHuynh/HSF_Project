/* ============================================================
   Fix Vietnamese encoding — CineMax
   Chạy 1 lần sau khi pull code để sửa DB local
   
   Cách chạy:
     Cách 1: Double-click fix_vietnamese_db.bat
     Cách 2: sqlcmd -S localhost -d HSF_PROJECT -U sa -P 12345 -i "%~dp0fix_vietnamese_db.sql" -f 65001
   ============================================================ */
USE HSF_PROJECT;
GO

PRINT N'=== 1. Fix movie_review.comment sang NVARCHAR ===';
GO
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME='movie_review' AND COLUMN_NAME='comment' AND DATA_TYPE='nvarchar')
BEGIN
    ALTER TABLE movie_review ALTER COLUMN comment NVARCHAR(MAX);
    PRINT N'  ✅ Altered movie_review.comment to NVARCHAR';
END
ELSE
    PRINT N'  ℹ️  movie_review.comment already NVARCHAR';
GO

PRINT N'=== 2. Fix review data ===';
GO
UPDATE movie_review SET comment = N'Diễn xuất của Joaquin Phoenix quá đỉnh, xem ở IMAX cực kỳ đáng tiền.' WHERE id = 1;
UPDATE movie_review SET comment = N'Phim hay nhưng nhịp phim hơi chậm ở giữa.' WHERE id = 2;
UPDATE movie_review SET comment = N'Âm thanh Dolby Atmos rất sống động, xứng đáng 5 sao.' WHERE id = 3;
UPDATE movie_review SET comment = N'Hình ảnh hoành tráng, đáng xem trên màn IMAX.' WHERE id = 4;
UPDATE movie_review SET comment = N'Cốt truyện sâu nhưng cần xem phần 1 trước.' WHERE id = 5;
UPDATE movie_review SET comment = N'Hành động mãn nhãn, kịch bản ổn.' WHERE id = 6;
UPDATE movie_review SET comment = N'Xem giải trí cuối tuần thì ổn, không cần suy nghĩ nhiều.' WHERE id = 7;
PRINT N'  ✅ Review data fixed';
GO

PRINT N'=== 3. Fix movie data ===';
GO
UPDATE movie SET title = N'Lằn Ranh Sinh Tử',     description = N'Một cựu cảnh sát buộc phải truy đuổi tổ chức buôn người xuyên quốc gia để cứu con gái trước khi quá muộn.',       director = N'Lê Minh Khoa',     [cast] = N'Trấn Thành, Ngọc Lan, Hữu Long'                  WHERE id = 3;
UPDATE movie SET title = N'Đường Đua Tốc Độ',     description = N'Một tay đua trẻ phải vượt qua quá khứ để giành chiến thắng trong giải đua xuyên Việt lớn nhất từ trước đến nay.', director = N'Phan Đăng Vũ',     [cast] = N'Quốc Anh, Thuý Diễm, Bảo Long'                WHERE id = 4;
UPDATE movie SET title = N'Vũ Trụ Song Hành',     description = N'Hai nhà vật lý phát hiện cánh cổng dẫn đến một vũ trụ song song, nơi mọi lựa chọn của họ đã rẽ theo hướng hoàn toàn khác.', director = N'Đỗ Gia Linh',     [cast] = N'Mai Tài Phến, Nhã Phương'                       WHERE id = 5;
UPDATE movie SET title = N'Mùa Hè Của Em',        description = N'Câu chuyện tình nhẹ nhàng giữa hai người trẻ gặp lại nhau sau 10 năm xa cách tại chính nơi họ từng chia tay.',      director = N'Nguyễn Hữu Tuấn',  [cast] = N'Khả Ngân, Thanh Sơn'                            WHERE id = 6;
UPDATE movie SET title = N'Bóng Ma Quá Khứ',      description = N'Một gia đình chuyển đến căn nhà cũ và dần phát hiện những bí mật rùng rợn bị chôn giấu suốt 30 năm.',               director = N'Trần Bửu Lộc',    [cast] = N'Lan Ngọc, Việt Hương'                           WHERE id = 7;
UPDATE movie SET title = N'Kẻ Đánh Cắp Ký Ức',   description = N'Một nhà khoa học phát minh thiết bị có thể xâm nhập ký ức con người nhưng nhanh chóng bị cuốn vào âm mưu đánh cắp bí mật quốc gia.', director = N'Nguyễn Khắc Minh', [cast] = N'Quốc Trường, Kaity Nguyễn, Hứa Vĩ Văn'         WHERE id = 8;
UPDATE movie SET title = N'Đảo Bão',              description = N'Một nhóm du khách mắc kẹt trên hòn đảo sau cơn bão lớn và phát hiện nơi đây đang che giấu một phòng thí nghiệm bí mật.',    director = N'Bùi Quốc Việt',   [cast] = N'Liên Bỉnh Phát, Thu Anh, Bình An'               WHERE id = 9;
UPDATE movie SET title = N'Trò Chơi Sinh Tồn',    description = N'Tám người xa lạ bị nhốt trong một khu công nghiệp bỏ hoang và buộc phải vượt qua hàng loạt thử thách để sống sót.',     director = N'Lê Quốc Bảo',     [cast] = N'Rima Thanh Vy, Quốc Anh, Võ Điền Gia Huy'       WHERE id = 12;
UPDATE movie SET title = N'Người Gác Hải Đăng',   description = N'Một người gác hải đăng già phát hiện những tín hiệu kỳ lạ ngoài khơi và dần hé lộ bí mật đã bị chôn vùi nhiều thập kỷ.',  director = N'Nguyễn Minh Đức', [cast] = N'NSƯT Thành Lộc, Lê Phương'                    WHERE id = 13;
UPDATE movie SET title = N'Bản Giao Hưởng Cuối',  description = N'Một nhạc trưởng nổi tiếng phải hoàn thành buổi biểu diễn cuối cùng trong khi chống chọi với căn bệnh làm mất dần thính giác.', director = N'Vũ Hải Long',    [cast] = N'Isaac, Hoàng Hà, Hồng Đào'                     WHERE id = 14;
-- Fix description cho movie 1,2 (titles là tiếng Anh OK)
UPDATE movie SET description = N'Arthur Fleck đối diện thế giới nội tâm hỗn loạn khi mối quan hệ với Harley Quinn ngày càng sâu đậm, giữa bối cảnh phiên tòa xét xử căng thẳng tại Gotham.' WHERE id = 1;
UPDATE movie SET description = N'Paul Atreides tiếp tục hành trình báo thù, tìm cách ngăn chặn tương lai đen tối và đoàn kết người Fremen để giành lại quyền kiểm soát Arrakis.' WHERE id = 2;
PRINT N'  ✅ Movie data fixed';
GO

PRINT N'=== 4. Fix genre (ALTER + đổi tên + thêm mới) ===';
GO
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME='genre' AND COLUMN_NAME='name' AND DATA_TYPE='nvarchar')
BEGIN
    ALTER TABLE genre ALTER COLUMN name NVARCHAR(50) NOT NULL;
    PRINT N'  ✅ Altered genre.name to NVARCHAR';
END
ELSE
    PRINT N'  ℹ️  genre.name already NVARCHAR';
GO

-- Chỉ UPDATE nếu genre còn tên cũ (tiếng Anh)
IF EXISTS (SELECT 1 FROM genre WHERE id = 1 AND name = 'Action')
BEGIN
    UPDATE genre SET name = N'Hành động' WHERE id = 1;
    UPDATE genre SET name = N'Khoa học viễn tưởng' WHERE id = 2;
    UPDATE genre SET name = N'Chính kịch / Tâm lý' WHERE id = 3;
    UPDATE genre SET name = N'Tình cảm' WHERE id = 4;
    UPDATE genre SET name = N'Kinh dị' WHERE id = 5;
    UPDATE genre SET name = N'Hồi hộp' WHERE id = 6;
    UPDATE genre SET name = N'Hài' WHERE id = 7;
    UPDATE genre SET name = N'Bí ẩn' WHERE id = 8;
    UPDATE genre SET name = N'Âm nhạc' WHERE id = 9;
    PRINT N'  ✅ Updated 9 genre names to Vietnamese';
END
ELSE
    PRINT N'  ℹ️  Genre names already Vietnamese';
GO

-- Thêm 16 thể loại mới nếu chưa tồn tại
IF NOT EXISTS (SELECT 1 FROM genre WHERE id = 10)
BEGIN
    INSERT INTO genre (name, is_deleted) VALUES
    (N'Phiêu lưu', 0), (N'Hoạt hình', 0), (N'Giả tưởng', 0), (N'Tội phạm', 0),
    (N'Gia đình', 0), (N'Thiếu nhi', 0), (N'Nhạc kịch', 0), (N'Tiểu sử', 0),
    (N'Lịch sử', 0), (N'Chiến tranh', 0), (N'Võ thuật', 0), (N'Thể thao', 0),
    (N'Tài liệu', 0), (N'Viễn Tây', 0), (N'Hoạt hình 3D', 0), (N'Siêu anh hùng', 0);
    PRINT N'  ✅ Inserted 16 new genres';
END
ELSE
    PRINT N'  ℹ️  New genres already exist';
GO

PRINT N'';
PRINT N'============================================';
PRINT N'  ✅ Fix hoàn tất!';
PRINT N'  - Movie + review data: fixed';
PRINT N'  - Thể loại: đã đổi sang tiếng Việt + thêm mới';
PRINT N'============================================';
GO
