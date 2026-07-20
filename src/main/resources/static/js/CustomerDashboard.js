document.addEventListener('DOMContentLoaded', function () {
    let customerChart = null;
    let currentPage = 0;
    const pageSize = 6;
    let currentSort = 'bookingCount';
    let currentDir = 'desc';
    let loadTimer = null;

    const filterFrom = document.getElementById('fromDate');
    const filterTo = document.getElementById('toDate');
    const filterBtn = document.getElementById('filterBtn');
    const searchInput = document.getElementById('searchInput');
    const sortSelect = document.getElementById('sortSelect');
    const customerTableBody = document.getElementById('customerTableBody');
    const prevPage = document.getElementById('prevPage');
    const nextPage = document.getElementById('nextPage');
    const pageInfo = document.getElementById('pageInfo');

    const today = new Date();
    const firstOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);
    const defaultFrom = new Date(today.getFullYear(), today.getMonth() - 1, today.getDate());
    filterFrom.value = formatDate(defaultFrom);
    filterTo.value = formatDate(today);

    function formatDate(date) {
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, '0');
        const d = String(date.getDate()).padStart(2, '0');
        return y + '-' + m + '-' + d;
    }

    function formatNumber(n) {
        if (n >= 1000000000) return (n / 1000000000).toFixed(1) + 'B';
        if (n >= 1000000) return (n / 1000000).toFixed(1) + 'M';
        if (n >= 1000) return (n / 1000).toFixed(1) + 'k';
        return n.toString();
    }

    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    async function loadStats() {
        const from = filterFrom.value || formatDate(firstOfMonth);
        const to = filterTo.value || formatDate(today);
        try {
            const res = await fetch('/admin/dashboard/customers/summary?from=' + from + '&to=' + to);
            const json = await res.json();
            if (json.code === 200 && json.result) {
                const d = json.result;
                document.getElementById('totalCustomers').textContent = formatNumber(d.totalCustomers || 0);
                document.getElementById('newCustomers').textContent = formatNumber(d.newCustomers || 0);
                document.getElementById('monthlyVisits').textContent = formatNumber(d.monthlyVisits || 0);
                const avg = d.averageSpending || 0;
                if (avg >= 1000000000) document.getElementById('avgSpending').textContent = (avg / 1000000000).toFixed(2) + 'B';
                else if (avg >= 1000000) document.getElementById('avgSpending').textContent = (avg / 1000000).toFixed(1) + 'M';
                else if (avg >= 1000) document.getElementById('avgSpending').textContent = (avg / 1000).toFixed(1) + 'k';
                else document.getElementById('avgSpending').textContent = avg.toString();
            }
        } catch (err) {
            console.error('Failed to load stats:', err);
        }
    }

    let currentPeriod = 'month';

    async function loadChart(period) {
        currentPeriod = period;
        const from = filterFrom.value;
        const to = filterTo.value;

        try {
            const res = await fetch('/admin/dashboard/customers/growth?type=' + period + '&from=' + from + '&to=' + to);
            const json = await res.json();
            if (json.code !== 200) return;
            const data = json.result || [];

            const labels = data.map(function (d) { return d.label; });
            const values = data.map(function (d) { return d.value; });

            const ctx = document.getElementById('customerChart').getContext('2d');
            if (customerChart) customerChart.destroy();

            const maxVal = values.length > 0 ? Math.max.apply(null, values) : 100;
            const step = Math.ceil(maxVal / 4 / 100) * 100 || 100;

            customerChart = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: [{
                        data: values,
                        backgroundColor: '#861822',
                        hoverBackgroundColor: '#FF2C3B',
                        borderRadius: 4,
                        borderSkipped: false,
                        barPercentage: 0.45
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { display: false } },
                    scales: {
                        x: {
                            grid: { display: false },
                            ticks: {
                                color: '#8E8D9C',
                                font: { family: 'Be Vietnam Pro', size: 10, weight: '600' }
                            }
                        },
                        y: {
                            max: Math.ceil(maxVal * 1.15 / step) * step,
                            min: 0,
                            border: { dash: [4, 4] },
                            grid: { color: 'rgba(255, 255, 255, 0.03)' },
                            ticks: {
                                stepSize: step,
                                color: '#8E8D9C',
                                font: { family: 'Be Vietnam Pro', size: 10 }
                            }
                        }
                    }
                }
            });
        } catch (err) {
            console.error('Failed to load chart:', err);
        }
    }

    async function loadCustomers() {
        const from = filterFrom.value || formatDate(firstOfMonth);
        const to = filterTo.value || formatDate(today);
        const search = searchInput.value.trim();
        try {
            let url = '/admin/customers/active?page=' + currentPage + '&size=' + pageSize + '&sort=' + currentSort + '&direction=' + currentDir;
            if (search) url += '&search=' + encodeURIComponent(search);

            const res = await fetch(url);
            const json = await res.json();
            if (json.code !== 200) return;

            const page = json.result || { content: [], totalElements: 0, number: 0, totalPages: 0 };
            const customers = page.content || [];

            if (customers.length === 0) {
                customerTableBody.innerHTML = '<tr><td colspan="5" style="text-align:center;padding:24px;color:var(--text-muted);">Không tìm thấy khách hàng</td></tr>';
            } else {
                customerTableBody.innerHTML = customers.map(function (c) {
                    return '<tr>' +
                        '<td>' +
                        '<div class="user-cell">' +
                        '<div class="avatar-letter">' + escapeHtml(c.avatar || '?') + '</div>' +
                        '<div>' +
                        '<div class="user-name">' + escapeHtml(c.fullName) + '</div>' +
                        '<div class="user-email">' + escapeHtml(c.email) + '</div>' +
                        '</div>' +
                        '</div>' +
                        '</td>' +
                        '<td><span class="highlight-text">' + (c.bookingCount || 0) + ' vé</span></td>' +
                        '<td class="movie-name-td">' + escapeHtml(c.latestMovie || '---') + '</td>' +
                        '<td><span class="status-pill ' + (c.status === 'ACTIVE' ? 'active' : 'locked') + '">' + (c.status === 'ACTIVE' ? 'Hoạt động' : 'Bị khóa') + '</span></td>' +
                        '<td class="text-right">' +
                        '<a href="/admin/customers/' + (c.customerId || '') + '/detail" class="action-btn" title="Xem chi tiết"><i class="fa-solid fa-eye"></i></a>' +
                        '<button class="action-btn" onclick="lockAccount(' + (c.customerId || '') + ')" title="' + (c.status === 'LOCKED' ? 'Mở khóa tài khoản' : 'Khóa tài khoản') + '">' +
                        '<i class="fa-solid ' + (c.status === 'LOCKED' ? 'fa-unlock' : 'fa-lock') + '"></i></button>' +
                        '</td>' +
                        '</tr>';
                }).join('');
            }

            const total = page.totalElements || 0;
            const number = page.number || 0;
            const totalPages = page.totalPages || 0;
            const fromNum = total === 0 ? 0 : (number * pageSize + 1);
            const toNum = Math.min((number + 1) * pageSize, total);
            pageInfo.textContent = 'Hiển thị ' + fromNum + ' - ' + toNum + ' của ' + total.toLocaleString('en-US') + ' khách hàng';
            prevPage.disabled = number <= 0;
            nextPage.disabled = number >= totalPages - 1;
        } catch (err) {
            console.error('Failed to load customers:', err);
        }
    }

    function debounceLoad() {
        clearTimeout(loadTimer);
        loadTimer = setTimeout(loadCustomers, 300);
    }

    filterBtn.addEventListener('click', function () {
        currentPage = 0;
        loadChart(currentPeriod);
        loadCustomers();
        loadStats();
    });

    document.querySelectorAll('.period-tab').forEach(function (tab) {
        tab.addEventListener('click', function () {
            document.querySelectorAll('.period-tab').forEach(function (t) { t.classList.remove('active'); });
            this.classList.add('active');
            loadChart(this.dataset.period);
        });
    });

    searchInput.addEventListener('input', function () {
        currentPage = 0;
        debounceLoad();
    });

    sortSelect.addEventListener('change', function () {
        currentSort = this.value;
        currentDir = 'desc';
        currentPage = 0;
        loadCustomers();
    });

    prevPage.addEventListener('click', function () {
        if (currentPage > 0) {
            currentPage--;
            loadCustomers();
        }
    });

    nextPage.addEventListener('click', function () {
        currentPage++;
        loadCustomers();
    });

    loadStats();
    loadChart('month');
    loadCustomers();
});
