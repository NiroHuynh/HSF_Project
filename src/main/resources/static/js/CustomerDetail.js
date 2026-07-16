document.addEventListener("DOMContentLoaded", function () {

    const editBtn = document.getElementById('editBtn');
    if (editBtn) {
        editBtn.addEventListener('click', function () {
            alert('Chức năng chỉnh sửa đang được phát triển.');
        });
    }

    const exportBtn = document.getElementById('exportBtn');
    if (exportBtn) {
        exportBtn.addEventListener('click', function () {
            const pathParts = window.location.pathname.split('/');
            const id = pathParts[pathParts.length - 2];
            if (id && !isNaN(id)) {
                window.open('/admin/customers/' + id + '/export', '_blank');
            } else {
                alert('Không thể xuất báo cáo.');
            }
        });
    }
});
