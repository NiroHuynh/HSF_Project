document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('movieForm');
    const mode = form.dataset.mode;
    const movieId = form.dataset.movieId;
    const genreTagList = document.getElementById('genreTagList');
    const genreIdsValue = document.getElementById('genreIdsValue');

    // ========== DISABLE FORM WHEN ENDED / CANCELLED ==========
    if (mode === 'edit' && MOVIE_STATUS && (MOVIE_STATUS === 'ENDED' || MOVIE_STATUS === 'CANCELLED')) {
        form.querySelectorAll('input, select, textarea, button[type="submit"]').forEach(el => {
            if (el.name !== 'status') el.disabled = true;
        });
        document.querySelector('.btn-top-submit').textContent = 'KHÔNG THỂ SỬA';
        document.getElementById('posterUpload').style.pointerEvents = 'none';
    }

    // ========== GENRE TAG CHIPS (dropdown) ==========
    const allGenres = typeof GENRES !== 'undefined' ? GENRES : [
        { id: 1, name: 'Hành động' },
        { id: 2, name: 'Hài' },
        { id: 3, name: 'Kinh dị' },
        { id: 4, name: 'Tâm lý' },
        { id: 5, name: 'Tình cảm' },
        { id: 6, name: 'Hoạt hình' },
        { id: 7, name: 'Phiêu lưu' },
        { id: 8, name: 'Khoa học viễn tưởng' }
    ];

    intializeGenreComponent(allGenres);

    function intializeGenreComponent(allGenres) {
        const searchInput = document.getElementById('genreSearchInput');
        const dropdown = document.getElementById('genreDropdown');
        const tagsWrapper = document.getElementById('activeTagsWrapper');
        const hiddenInput = document.getElementById('genreIdsValue');

        const initialSelectedIds = hiddenInput.value ? hiddenInput.value.split(',').map(Number) : [];
        const selectedGenreIds = new Set(initialSelectedIds);

        function renderDropdown(filterText = '') {
            dropdown.innerHTML = '';
            const filtered = allGenres.filter(g => g.name.toLowerCase().includes(filterText.toLowerCase()));

            if (filtered.length === 0) {
                dropdown.innerHTML = '<div style="padding: 10px 16px; color: #6c728e; font-size: 13px;">Không tìm thấy thể loại...</div>';
                return;
            }

            filtered.forEach(genre => {
                const isSelected = selectedGenreIds.has(genre.id);
                const row = document.createElement('div');
                row.className = 'dropdown-item-row' + (isSelected ? ' already-selected' : '');
                row.innerHTML = '<span>' + genre.name + '</span>' + (isSelected ? '<i class="fa-solid fa-check" style="color: #e50914; font-size: 12px;"></i>' : '');

                if (!isSelected) {
                    row.addEventListener('click', () => {
                        addGenreTag(genre);
                        searchInput.value = '';
                        searchInput.focus();
                        renderDropdown('');
                    });
                }
                dropdown.appendChild(row);
            });
        }

        function renderChips() {
            tagsWrapper.innerHTML = '';
            allGenres.forEach(genre => {
                if (selectedGenreIds.has(genre.id)) {
                    const chip = document.createElement('div');
                    chip.className = 'sample-chip';
                    chip.style.margin = '2px 0';
                    chip.innerHTML = genre.name + ' <i class="fa-solid fa-xmark" data-id="' + genre.id + '"></i>';

                    chip.querySelector('i').addEventListener('click', (e) => {
                        e.stopPropagation();
                        selectedGenreIds.delete(genre.id);
                        updateHiddenInputValue();
                        renderChips();
                        renderDropdown(searchInput.value);
                    });
                    tagsWrapper.appendChild(chip);
                }
            });
        }

        function updateHiddenInputValue() {
            hiddenInput.value = Array.from(selectedGenreIds).join(',');
        }

        function addGenreTag(genre) {
            selectedGenreIds.add(genre.id);
            updateHiddenInputValue();
            renderChips();
        }

        searchInput.addEventListener('focus', () => {
            renderDropdown(searchInput.value);
            dropdown.style.display = 'block';
        });

        searchInput.addEventListener('input', (e) => {
            renderDropdown(e.target.value);
        });

        document.addEventListener('click', (e) => {
            if (!document.getElementById('genreTagList').contains(e.target)) {
                dropdown.style.display = 'none';
            }
        });

        document.getElementById('genreTagList').addEventListener('click', (e) => {
            if (e.target === searchInput || e.target === tagsWrapper) {
                searchInput.focus();
            }
        });

        renderChips();
    }

    // ========== DIRECTOR TAG CHIPS ==========
    const directorTagList = document.getElementById('directorTagList');
    const directorInput = document.getElementById('directorInput');
    const directorValue = document.getElementById('directorValue');
    let directors = [];

    function loadDirectors() {
        const val = directorValue.value;
        if (val) {
            directors = val.split(',').map(s => s.trim()).filter(Boolean);
        }
    }
    loadDirectors();

    function renderDirectorTags() {
        const existing = directorTagList.querySelectorAll('.tag');
        existing.forEach(t => t.remove());

        const directorWrapper = directorTagList.querySelector('.tag-input-wrapper');
        directors.forEach(name => {
            const tag = document.createElement('span');
            tag.className = 'tag';
            tag.innerHTML = name + ' <span class="tag-remove">&#10005;</span>';
            directorTagList.insertBefore(tag, directorWrapper);
        });

        directorValue.value = directors.join(', ');
    }

    directorTagList.addEventListener('click', function (e) {
        if (e.target.classList.contains('tag-remove')) {
            const tag = e.target.closest('.tag');
            const name = tag.textContent.replace('✕', '').trim();
            directors = directors.filter(d => d !== name);
            renderDirectorTags();
            directorInput.focus();
        } else if (e.target.tagName !== 'INPUT') {
            directorInput.focus();
        }
    });

    directorInput.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            const name = this.value.trim();
            if (name && !directors.includes(name)) {
                directors.push(name);
                renderDirectorTags();
            }
            this.value = '';
        }
        if (e.key === 'Backspace' && this.value === '' && directors.length > 0) {
            directors.pop();
            renderDirectorTags();
        }
    });

    renderDirectorTags();

    // ========== CAST TAG CHIPS ==========
    const castTagList = document.getElementById('castTagList');
    const castInput = document.getElementById('castInput');
    const castValue = document.getElementById('castValue');
    let castList = [];

    function loadCast() {
        const val = castValue.value;
        if (val) {
            castList = val.split(',').map(s => s.trim()).filter(Boolean);
        }
    }
    loadCast();

    function renderCastTags() {
        const existing = castTagList.querySelectorAll('.tag');
        existing.forEach(t => t.remove());

        const castWrapper = castTagList.querySelector('.tag-input-wrapper');
        castList.forEach(name => {
            const tag = document.createElement('span');
            tag.className = 'tag';
            tag.innerHTML = name + ' <span class="tag-remove">&#10005;</span>';
            castTagList.insertBefore(tag, castWrapper);
        });

        castValue.value = castList.join(', ');
    }

    castTagList.addEventListener('click', function (e) {
        if (e.target.classList.contains('tag-remove')) {
            const tag = e.target.closest('.tag');
            const name = tag.textContent.replace('✕', '').trim();
            const idx = castList.indexOf(name);
            if (idx !== -1) castList.splice(idx, 1);
            renderCastTags();
            castInput.focus();
        } else if (e.target.tagName !== 'INPUT') {
            castInput.focus();
        }
    });

    castInput.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            const name = this.value.trim();
            if (name && !castList.includes(name)) {
                castList.push(name);
                renderCastTags();
            }
            this.value = '';
        }
        if (e.key === 'Backspace' && this.value === '' && castList.length > 0) {
            castList.pop();
            renderCastTags();
        }
    });

    renderCastTags();

    // ========== POSTER UPLOAD ==========
    const posterUpload = document.getElementById('posterUpload');
    const posterFile = document.getElementById('posterFile');
    const posterPreview = document.getElementById('posterPreview');
    const posterOverlay = document.getElementById('posterOverlay');
    const posterUrlValue = document.getElementById('posterUrlValue');

    // Hiển thị poster cũ nếu có
    if (posterUrlValue.value) {
        posterPreview.src = posterUrlValue.value;
        posterPreview.style.display = 'block';
        posterOverlay.classList.add('has-preview');
    }

    posterUpload.addEventListener('click', function () {
        posterFile.click();
    });

    posterUpload.addEventListener('dragover', function (e) {
        e.preventDefault();
        this.style.outline = '2px solid var(--red)';
    });

    posterUpload.addEventListener('dragleave', function () {
        this.style.outline = '';
    });

    posterUpload.addEventListener('drop', function (e) {
        e.preventDefault();
        this.style.outline = '';
        const files = e.dataTransfer.files;
        if (files.length > 0) {
            handlePosterFile(files[0]);
        }
    });

    posterFile.addEventListener('change', function () {
        if (this.files.length > 0) {
            handlePosterFile(this.files[0]);
        }
    });

    function handlePosterFile(file) {
        const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
        if (!validTypes.includes(file.type)) {
            alert('Chỉ chấp nhận định dạng: JPG, JPEG, PNG, WEBP');
            return;
        }
        if (file.size > 5 * 1024 * 1024) {
            alert('File không được vượt quá 5MB');
            return;
        }

        const reader = new FileReader();
        reader.onload = function (e) {
            posterPreview.src = e.target.result;
            posterPreview.style.display = 'block';
            posterOverlay.classList.add('has-preview');
            posterUpload.dataset.hasFile = 'true';
        };
        reader.readAsDataURL(file);
    }

    // ========== FORM VALIDATION ==========
    function isDisabled(el) {
        return el.disabled === true;
    }

    function validateForm() {
        const errors = [];
        const errorBox = document.getElementById('formErrorBox');
        form.querySelectorAll('.form-item-input, .form-item-select, .tag-list-box-line, .tag-combo-container')
            .forEach(el => el.classList.remove('error'));

        if (mode === 'edit' && MOVIE_STATUS && (MOVIE_STATUS === 'ENDED' || MOVIE_STATUS === 'CANCELLED')) {
            errors.push('Phim đã ' + (MOVIE_STATUS === 'ENDED' ? 'kết thúc' : 'hủy') + ', không thể chỉnh sửa thông tin');
            const errorBox = document.getElementById('formErrorBox');
            let html = '';
            errors.forEach(msg => {
                html += '<div class="error-item"><i class="fa-solid fa-circle-exclamation"></i> ' + msg + '</div>';
            });
            errorBox.innerHTML = html;
            errorBox.style.display = 'block';
            errorBox.scrollIntoView({ behavior: 'smooth', block: 'center' });
            return false;
        }

        // Tên phim
        const title = form.querySelector('[name="title"]');
        if (!isDisabled(title) && !title.value.trim()) {
            title.classList.add('error');
            errors.push('Tên phim không được để trống');
        }

        // Mô tả
        const desc = form.querySelector('[name="description"]');
        if (!isDisabled(desc) && !desc.value.trim()) {
            desc.classList.add('error');
            errors.push('Mô tả phim không được để trống');
        }

        // Thể loại
        const genreCount = genreIdsValue.value ? genreIdsValue.value.split(',').filter(Boolean).length : 0;
        if (genreCount === 0) {
            genreTagList.classList.add('error');
            errors.push('Vui lòng chọn ít nhất một thể loại');
        }

        // Phân loại độ tuổi
        const ageRating = form.querySelector('[name="ageRating"]');
        if (!isDisabled(ageRating) && !ageRating.value) {
            ageRating.classList.add('error');
            errors.push('Vui lòng chọn phân loại độ tuổi');
        }

        // Thời lượng
        const duration = form.querySelector('[name="durationMinutes"]');
        if (!isDisabled(duration)) {
            if (!duration.value) {
                duration.classList.add('error');
                errors.push('Thời lượng không được để trống');
            } else {
                const durVal = parseInt(duration.value);
                if (durVal < 30 || durVal > 300) {
                    duration.classList.add('error');
                    errors.push('Thời lượng phải từ 30 đến 300 phút');
                }
            }
        }

        // Quốc gia
        const lang = form.querySelector('[name="language"]');
        if (!isDisabled(lang) && !lang.value) {
            lang.classList.add('error');
            errors.push('Vui lòng chọn quốc gia');
        }

        // Đạo diễn
        const directorVal = directorValue.value || directorInput.value.trim();
        if (!directorVal) {
            directorTagList.classList.add('error');
            errors.push('Vui lòng nhập ít nhất một đạo diễn');
        }

        // Diễn viên
        const castVal = castValue.value;
        if (!castVal) {
            castTagList.classList.add('error');
            errors.push('Vui lòng nhập ít nhất một diễn viên');
        }

        // Ngày khởi chiếu
        const releaseDate = form.querySelector('[name="releaseDate"]');
        if (!isDisabled(releaseDate) && !releaseDate.value) {
            releaseDate.classList.add('error');
            errors.push('Vui lòng chọn ngày khởi chiếu');
        }

        // Ngày kết thúc
        const endDate = form.querySelector('[name="endDate"]');
        if (!endDate.value) {
            endDate.classList.add('error');
            errors.push('Vui lòng chọn ngày kết thúc dự kiến');
        }

        // Ngày kết thúc phải sau ngày khởi chiếu
        const refRelease = form.querySelector('[name="releaseDate"]');
        if (endDate.value && refRelease.value && endDate.value < refRelease.value) {
            endDate.classList.add('error');
            errors.push('Ngày kết thúc phải sau ngày khởi chiếu');
        }

        // Poster (chỉ check khi thêm phim)
        if (mode === 'add') {
            const posterHasFile = posterUpload.dataset.hasFile === 'true';
            const posterExisting = posterUrlValue.value;
            if (!posterHasFile && !posterExisting) {
                posterUpload.classList.add('error');
                errors.push('Vui lòng chọn ảnh poster cho phim');
            }
        }

        // Hiển thị tất cả lỗi trong formErrorBox
        if (errors.length > 0) {
            let html = '';
            errors.forEach(msg => {
                html += '<div class="error-item"><i class="fa-solid fa-circle-exclamation"></i> ' + msg + '</div>';
            });
            errorBox.innerHTML = html;
            errorBox.style.display = 'block';
            errorBox.scrollIntoView({ behavior: 'smooth', block: 'center' });
            return false;
        }

        errorBox.style.display = 'none';
        return true;
    }

    // ========== ERROR DISPLAY ==========
    const ERROR_FIELD_MAP = {
        1006: 'title',
        1009: 'durationMinutes',
        1011: 'releaseDate'
    };

    function clearApiErrors() {
        const errorBox = document.getElementById('formErrorBox');
        if (errorBox) {
            errorBox.style.display = 'none';
            errorBox.innerHTML = '';
        }
        form.querySelectorAll('.form-item-input.error, .form-item-select.error, .tag-list-box.error, .tag-list-box-line.error, .tag-combo-container.error, .poster-preview-container.error')
            .forEach(el => el.classList.remove('error'));
    }

    function showFormError(message, code) {
        clearApiErrors();

        // Highlight field nếu có mapping
        if (code) {
            const fieldName = ERROR_FIELD_MAP[code];
            if (fieldName) {
                const input = form.querySelector(`[name="${fieldName}"]`);
                if (input) input.classList.add('error');
            }
            if (code === 1012) {
                genreTagList.classList.add('error');
            }
        }

        // Tách message nếu là validation tổng hợp (code 400)
        let items = [];
        if (code === 400 && message.includes('; ')) {
            items = message.split('; ').filter(Boolean);
        } else {
            items = [message];
        }

        // Hiển thị tất cả trong formErrorBox
        const errorBox = document.getElementById('formErrorBox');
        if (errorBox) {
            let html = '';
            items.forEach(msg => {
                html += '<div class="error-item"><i class="fa-solid fa-circle-exclamation"></i> ' + msg + '</div>';
            });
            errorBox.innerHTML = html;
            errorBox.style.display = 'block';
            errorBox.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
    }

    function resetSubmitBtn() {
        const btn = form.querySelector('.btn-top-submit');
        btn.disabled = false;
        btn.textContent = 'LƯU THAY ĐỔI';
    }

    // ========== FORM SUBMIT ==========
    form.addEventListener('submit', async function (e) {
        e.preventDefault();
        clearApiErrors();

        if (!validateForm()) return;

        const formData = new FormData(form);
        formData.set('director', directorValue.value || directorInput.value.trim());
        formData.set('cast', castValue.value);

        const posterHasFile = posterUpload.dataset.hasFile === 'true';
        const posterFileEl = posterFile.files[0];

        const durationEl = form.querySelector('[name="durationMinutes"]');
        const releaseDateEl = form.querySelector('[name="releaseDate"]');

        const body = {
            title: formData.get('title'),
            durationMinutes: !durationEl.disabled && formData.get('durationMinutes')
                    ? parseInt(formData.get('durationMinutes')) : undefined,
            releaseDate: !releaseDateEl.disabled ? formData.get('releaseDate') : undefined,
            director: formData.get('director') || null,
            cast: formData.get('cast') || null,
            description: formData.get('description') || null,
            ageRating: formData.get('ageRating') || null,
            genreIds: genreIdsValue.value ? genreIdsValue.value.split(',').map(Number) : [],
            language: formData.get('language') || null,
            endDate: formData.get('endDate') || null,
            ...(mode === 'edit' ? { status: formData.get('status') } : {})
        };

        const submitBtn = form.querySelector('.btn-top-submit');
        submitBtn.disabled = true;
        submitBtn.textContent = 'ĐANG LƯU...';

        try {
            let movieResultId = movieId;

            if (mode === 'add') {
                const createRes = await fetch('/admin/movies', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(body)
                });
                const createData = await createRes.json();
                if (createData.code !== 200 && createData.code !== 201) {
                    showFormError(createData.message, createData.code);
                    resetSubmitBtn();
                    return;
                }
                movieResultId = createData.result.id;
            } else {
                const updateRes = await fetch('/admin/movies/' + movieId, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(body)
                });
                const updateData = await updateRes.json();
                if (updateData.code !== 200) {
                    showFormError(updateData.message, updateData.code);
                    resetSubmitBtn();
                    return;
                }
                movieResultId = movieId;
            }

            if (posterHasFile && posterFileEl) {
                const posterFormData = new FormData();
                posterFormData.append('file', posterFileEl);

                const posterRes = await fetch('/admin/movies/' + movieResultId + '/poster', {
                    method: 'POST',
                    body: posterFormData
                });
                const posterData = await posterRes.json();
                if (posterData.code !== 200) {
                    showFormError('Lỗi upload poster: ' + posterData.message);
                } else {
                    posterUrlValue.value = posterData.result;
                }
            }

            alert(mode === 'add' ? 'Tạo phim thành công!' : 'Cập nhật phim thành công!');
            window.location.href = '/admin/movies/dashboard';

        } catch (err) {
            showFormError('Lỗi kết nối máy chủ: ' + err.message);
            resetSubmitBtn();
        }
    });

    // ========== REMOVE ERROR ON INPUT ==========
    form.querySelectorAll('.form-item-input, .form-item-select, .tag-list-box, .tag-list-box-line, .tag-combo-container, .poster-preview-container').forEach(el => {
        el.addEventListener('input', function () {
            this.classList.remove('error');
        });
        el.addEventListener('change', function () {
            this.classList.remove('error');
        });
    });

    // Clear error box on any interaction
    document.getElementById('formErrorBox')?.addEventListener('click', function () {
        this.style.display = 'none';
    });
});
