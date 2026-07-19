/* ============================================================
   Validation phía client dùng chung cho các form không submit thẳng
   (modal gọi fetch) hoặc cần báo lỗi rõ hơn tooltip mặc định của trình duyệt.

   Dùng:
     validateForm(formEl, {
       firstName: [required('Vui lòng nhập tên'), personName()],
       price:     [required(), min(1000, 'Giá tối thiểu 1.000đ')]
     })
   Trả về true nếu hợp lệ; nếu sai thì gắn thông báo ngay dưới ô nhập.
   ============================================================ */

/* \p{L} bắt được cả chữ có dấu tiếng Việt; chặn số và ký tự đặc biệt trong tên người. */
const NAME_PATTERN = /^[\p{L}\s'.-]+$/u;
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
const PHONE_PATTERN = /^0\d{9}$/;

function required(message = 'Không được để trống') {
    return v => (v.trim() === '' ? message : null);
}

function personName(message = 'Chỉ được nhập chữ, không chứa số hay ký tự đặc biệt') {
    return v => (v.trim() !== '' && !NAME_PATTERN.test(v.trim()) ? message : null);
}

function email(message = 'Email không hợp lệ') {
    return v => (v.trim() !== '' && !EMAIL_PATTERN.test(v.trim()) ? message : null);
}

/** Số điện thoại để trống được coi là hợp lệ; nơi nào bắt buộc thì thêm required(). */
function phone(message = 'Số điện thoại phải gồm 10 số và bắt đầu bằng 0') {
    return v => (v.trim() !== '' && !PHONE_PATTERN.test(v.trim()) ? message : null);
}

function minLength(n, message) {
    return v => (v.length > 0 && v.length < n ? (message || `Tối thiểu ${n} ký tự`) : null);
}

function maxLength(n, message) {
    return v => (v.length > n ? (message || `Tối đa ${n} ký tự`) : null);
}

function min(n, message) {
    return v => {
        if (v.trim() === '') return null;
        const num = Number(v);
        if (Number.isNaN(num)) return 'Vui lòng nhập số hợp lệ';
        return num < n ? (message || `Giá trị tối thiểu là ${n}`) : null;
    };
}

function max(n, message) {
    return v => {
        if (v.trim() === '') return null;
        const num = Number(v);
        if (Number.isNaN(num)) return 'Vui lòng nhập số hợp lệ';
        return num > n ? (message || `Giá trị tối đa là ${n}`) : null;
    };
}

function integer(message = 'Vui lòng nhập số nguyên') {
    return v => (v.trim() !== '' && !Number.isInteger(Number(v)) ? message : null);
}

/** So khớp với một ô khác trong cùng form, dùng cho "nhập lại mật khẩu". */
function matches(otherFieldName, message = 'Giá trị không khớp') {
    return (v, form) => (v !== form.elements[otherFieldName].value ? message : null);
}

function showFieldError(field, message) {
    clearFieldError(field);
    field.classList.add('is-invalid');
    const el = document.createElement('small');
    el.className = 'field-error';
    el.textContent = message;
    // Ô nhập thường nằm trong <label> hoặc .form-group; gắn lỗi ngay sau khối đó.
    const anchor = field.closest('label, .form-group, .input-wrapper') || field;
    anchor.parentNode.insertBefore(el, anchor.nextSibling);
}

function clearFieldError(field) {
    field.classList.remove('is-invalid');
    const anchor = field.closest('label, .form-group, .input-wrapper') || field;
    const next = anchor.nextElementSibling;
    if (next && next.classList.contains('field-error')) next.remove();
}

function validateForm(form, rules) {
    form.querySelectorAll('.field-error').forEach(e => e.remove());
    form.querySelectorAll('.is-invalid').forEach(e => e.classList.remove('is-invalid'));

    let firstInvalid = null;
    for (const [name, checks] of Object.entries(rules)) {
        const field = form.elements[name];
        if (!field) continue;
        for (const check of checks) {
            const message = check(field.value, form);
            if (message) {
                showFieldError(field, message);
                if (!firstInvalid) firstInvalid = field;
                break;                       // mỗi ô chỉ hiện lỗi đầu tiên
            }
        }
    }

    if (firstInvalid) {
        firstInvalid.focus();
        return false;
    }
    return true;
}

/** Xoá lỗi ngay khi người dùng bắt đầu sửa, đỡ để chữ đỏ nằm lì trên màn hình. */
function bindClearOnInput(form) {
    form.addEventListener('input', e => {
        if (e.target.classList.contains('is-invalid')) clearFieldError(e.target);
    });
}
