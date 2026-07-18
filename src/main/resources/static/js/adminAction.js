// Nhận cả phần tử nút: Thymeleaf không cho nhúng chuỗi vào th:onclick nên
// id và trạng thái được truyền qua data-* rồi đọc lại ở đây.
function toggleAccountStatus(btn) {
    const accountId = btn.dataset.id;
    const targetStatus = btn.dataset.status === 'ACTIVE' ? 'LOCKED' : 'ACTIVE';

    fetch(`/admin/accounts/toggle-status?id=${accountId}&status=${targetStatus}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200) {
                location.reload();
            } else {
                alert('Cập nhật trạng thái thất bại: ' + (data.message || 'Lỗi không xác định'));
            }
        })
        .catch(() => alert('Lỗi kết nối mạng, vui lòng thử lại!'));
}

// Chỉ Manager mới cần rạp phụ trách; Admin không gắn với rạp nào.
function toggleCinemaField(mode) {
    const form = document.getElementById(mode + 'AccountForm');
    const field = document.getElementById(mode + 'CinemaField');
    const isManager = form.roleId.value === '2';
    field.style.display = isManager ? '' : 'none';
    if (!isManager) form.cinemaId.value = '';
}

// ============== ADD MODAL ==============

function openAddAccountModal() {
    document.getElementById('addAccountModal').classList.add('open');
    toggleCinemaField('add');
}

function closeAddAccountModal() {
    document.getElementById('addAccountModal').classList.remove('open');
    document.getElementById('addAccountForm').reset();
    toggleCinemaField('add');
}

function submitAddAccount() {
    const form = document.getElementById('addAccountForm');
    const data = {
        email: form.email.value.trim(),
        password: form.password.value.trim(),
        firstName: form.firstName.value.trim(),
        lastName: form.lastName.value.trim(),
        phoneNumber: form.phoneNumber.value.trim(),
        roleId: form.roleId.value,
        cinemaId: form.cinemaId.value
    };

    if (!data.email || !data.password || !data.firstName || !data.lastName) {
        alert('Vui lòng nhập đầy đủ thông tin bắt buộc (Email, Mật khẩu, Họ, Tên)');
        return;
    }

    if (data.roleId === '2' && !data.cinemaId) {
        alert('Vui lòng chọn rạp phụ trách cho tài khoản Manager');
        return;
    }

    fetch('/admin/accounts/create', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
        .then(res => res.json())
        .then(res => {
            if (res.code === 200) {
                closeAddAccountModal();
                location.reload();
            } else {
                alert('Thêm tài khoản thất bại: ' + (res.message || 'Lỗi không xác định'));
            }
        })
        .catch(() => alert('Lỗi kết nối mạng, vui lòng thử lại!'));
}

// ============== EDIT MODAL ==============

function editAccount(accountId) {
    fetch(`/admin/accounts/${accountId}`)
        .then(res => res.json())
        .then(res => {
            if (res.code !== 200 || !res.result) {
                alert('Không thể lấy thông tin tài khoản');
                return;
            }
            const acc = res.result;
            const form = document.getElementById('editAccountForm');
            form.dataset.accountId = acc.id;
            form.email.value = acc.email;
            form.firstName.value = acc.fullName.split(' ').slice(1).join(' ');
            form.lastName.value = acc.fullName.split(' ')[0];
            form.phoneNumber.value = '';
            form.roleId.value = acc.role === 'ADMIN' ? '1' : '2';
            form.cinemaId.value = acc.cinemaId != null ? acc.cinemaId : '';
            toggleCinemaField('edit');
            document.getElementById('editAccountModal').classList.add('open');
        })
        .catch(() => alert('Lỗi kết nối mạng, vui lòng thử lại!'));
}

function closeEditAccountModal() {
    document.getElementById('editAccountModal').classList.remove('open');
    document.getElementById('editAccountForm').reset();
}

function submitEditAccount() {
    const form = document.getElementById('editAccountForm');
    const id = form.dataset.accountId;
    const data = {
        email: form.email.value.trim(),
        firstName: form.firstName.value.trim(),
        lastName: form.lastName.value.trim(),
        phoneNumber: form.phoneNumber.value.trim(),
        roleId: form.roleId.value,
        cinemaId: form.cinemaId.value
    };

    if (!data.email || !data.firstName || !data.lastName) {
        alert('Vui lòng nhập đầy đủ Email, Họ và Tên');
        return;
    }

    if (data.roleId === '2' && !data.cinemaId) {
        alert('Vui lòng chọn rạp phụ trách cho tài khoản Manager');
        return;
    }

    fetch(`/admin/accounts/update/${id}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
        .then(res => res.json())
        .then(res => {
            if (res.code === 200) {
                closeEditAccountModal();
                location.reload();
            } else {
                alert('Cập nhật thất bại: ' + (res.message || 'Lỗi không xác định'));
            }
        })
        .catch(() => alert('Lỗi kết nối mạng, vui lòng thử lại!'));
}

// ============== DELETE MODAL ==============

let deleteTargetId = null;

function deleteAccount(accountId) {
    deleteTargetId = accountId;
    document.getElementById('deleteConfirmModal').classList.add('open');
}

function closeDeleteModal() {
    document.getElementById('deleteConfirmModal').classList.remove('open');
    deleteTargetId = null;
}

function confirmDelete() {
    if (!deleteTargetId) return;

    fetch(`/admin/accounts/delete/${deleteTargetId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(res => res.json())
        .then(data => {
            closeDeleteModal();
            if (data.code === 200) {
                location.reload();
            } else {
                alert('Xóa thất bại: ' + (data.message || 'Lỗi không xác định'));
            }
        })
        .catch(() => {
            closeDeleteModal();
            alert('Lỗi kết nối mạng, vui lòng thử lại!');
        });
}

// ============== CLICK OUTSIDE TO CLOSE ==============

window.addEventListener('click', function (e) {
    if (e.target === document.getElementById('addAccountModal')) closeAddAccountModal();
    if (e.target === document.getElementById('editAccountModal')) closeEditAccountModal();
    if (e.target === document.getElementById('deleteConfirmModal')) closeDeleteModal();
});
