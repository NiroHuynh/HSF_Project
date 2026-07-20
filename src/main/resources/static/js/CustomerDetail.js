document.addEventListener('DOMContentLoaded', function () {
    var rows = document.querySelectorAll('#transactionBody .tx-row');
    if (rows.length === 0) return;

    var pageSize = 5;
    var currentPage = 0;
    var totalPages = Math.ceil(rows.length / pageSize);

    var prevBtn = document.getElementById('txPrevPage');
    var nextBtn = document.getElementById('txNextPage');
    var pageInfo = document.getElementById('txPageInfo');

    function showPage(page) {
        for (var i = 0; i < rows.length; i++) {
            rows[i].style.display = (i >= page * pageSize && i < (page + 1) * pageSize) ? '' : 'none';
        }
        pageInfo.textContent = 'Trang ' + (page + 1) + ' / ' + totalPages;
        prevBtn.disabled = page <= 0;
        nextBtn.disabled = page >= totalPages - 1;
    }

    prevBtn.addEventListener('click', function () {
        if (currentPage > 0) { currentPage--; showPage(currentPage); }
    });

    nextBtn.addEventListener('click', function () {
        if (currentPage < totalPages - 1) { currentPage++; showPage(currentPage); }
    });

    showPage(0);
});