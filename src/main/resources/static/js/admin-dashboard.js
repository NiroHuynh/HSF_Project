document.addEventListener('DOMContentLoaded', function () {
    let revenueChart = null;

    const filterFrom = document.getElementById('filterFrom');
    const filterTo = document.getElementById('filterTo');
    const filterBtn = document.getElementById('filterBtn');
    const rankingSearch = document.getElementById('rankingSearch');
    const genreFilter = document.getElementById('genreFilter');
    const rankingBody = document.getElementById('rankingBody');

    // Set default dates
    const today = new Date();
    const firstOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);
    filterFrom.value = formatDate(firstOfMonth);
    filterTo.value = formatDate(today);

    function formatDate(date) {
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, '0');
        const d = String(date.getDate()).padStart(2, '0');
        return y + '-' + m + '-' + d;
    }

    function formatCurrency(amount) {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
    }

    function formatShortCurrency(amount) {
        if (amount >= 1000000000) return (amount / 1000000000).toFixed(1) + 'B';
        if (amount >= 1000000) return (amount / 1000000).toFixed(0) + 'M';
        if (amount >= 1000) return (amount / 1000).toFixed(0) + 'K';
        return amount.toString();
    }

    // ========== STATS ==========
    async function loadStats() {
        try {
            const res = await fetch('/admin/dashboard/movie');
            const data = await res.json();
            if (data.code === 200 && data.result) {
                document.getElementById('totalMovies').textContent = data.result.totalMovies || 0;
                document.getElementById('nowShowingCount').textContent = data.result.nowShowingCount || 0;
                document.getElementById('dailyRevenue').textContent = formatCurrency(data.result.dailyRevenue || 0);
                document.getElementById('averageRating').textContent = (data.result.averageRating || 0).toFixed(1) + '/10';
            }
        } catch (err) {
            console.error('Failed to load stats:', err);
        }
    }

    // ========== REVENUE CHART ==========
    let currentPeriod = 'month';

    async function loadChart(period) {
        currentPeriod = period;
        const from = filterFrom.value || formatDate(firstOfMonth);
        const to = filterTo.value || formatDate(today);

        try {
            const res = await fetch('/admin/dashboard/movie/trend?period=' + period + '&from=' + from + '&to=' + to);
            const data = await res.json();
            if (data.code !== 200) return;

            const trends = data.result || [];
            const labels = trends.map(t => t.period);
            const revenues = trends.map(t => Number(t.revenue) || 0);
            const targets = trends.map(t => {
                const val = t.target;
                return val != null ? Number(val) : null;
            });

            const ctx = document.getElementById('revenueChart').getContext('2d');

            if (revenueChart) revenueChart.destroy();

            revenueChart = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'Doanh thu (VND)',
                        data: revenues,
                        backgroundColor: revenues.map((v, i) => {
                            return (targets[i] != null && v >= targets[i])
                                ? '#E50914' : 'rgba(229, 9, 20, 0.30)';
                        }),
                        borderColor: revenues.map((v, i) => {
                            return (targets[i] != null && v >= targets[i])
                                ? '#E50914' : 'rgba(229, 9, 20, 0.30)';
                        }),
                        borderWidth: 1,
                        borderRadius: 4,
                        borderSkipped: false,
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            callbacks: {
                                label: function (context) {
                                    return formatCurrency(context.raw);
                                }
                            }
                        }
                    },
                    scales: {
                        y: {
                            grid: {
                                color: 'rgba(255, 255, 255, 0.05)',
                                drawBorder: false,
                            },
                            ticks: {
                                color: 'rgba(198, 196, 218, 0.40)',
                                font: { size: 11, weight: 'bold' },
                                callback: function (value) {
                                    return formatShortCurrency(value);
                                }
                            }
                        },
                        x: {
                            grid: { display: false },
                            ticks: {
                                color: 'rgba(198, 196, 218, 0.30)',
                                font: { size: 10, weight: 'bold' }
                            }
                        }
                    }
                }
            });
        } catch (err) {
            console.error('Failed to load chart:', err);
        }
    }

    // ========== RANKING TABLE ==========
    let rankingTimer = null;

    async function loadRanking() {
        const from = filterFrom.value || formatDate(firstOfMonth);
        const to = filterTo.value || formatDate(today);
        const search = rankingSearch.value.trim();
        const genre = genreFilter.value;
        const statusTab = document.querySelector('.status-tab.active');
        const status = statusTab ? statusTab.dataset.status : '';

        try {
            let url = '/admin/dashboard/movie/ranking?from=' + from + '&to=' + to;
            if (status) url += '&status=' + status;
            if (genre) url += '&genre=' + genre;
            if (search) url += '&search=' + encodeURIComponent(search);

            const res = await fetch(url);
            const data = await res.json();
            if (data.code !== 200) return;

            let movies = data.result || [];

            if (movies.length === 0) {
                rankingBody.innerHTML = '<tr><td colspan="6" class="text-muted text-center">Không có dữ liệu</td></tr>';
                return;
            }

            rankingBody.innerHTML = movies.map(m => {
                const rankClass = m.rank <= 3 ? 'rank-highlight' : '';
                return '<tr>' +
                    '<td class="col-rank"><span class="rank-badge ' + rankClass + '">#' + m.rank + '</span></td>' +
                    '<td class="col-movie">' +
                    '  <div class="movie-cell">' +
                    '    <img class="movie-thumb" src="' + (m.posterUrl || 'https://placehold.co/40x56/2A292F/E4E1E9?text=Poster') + '" alt="' + escapeHtml(m.title) + '">' +
                    '    <span class="movie-name">' + escapeHtml(m.title) + '</span>' +
                    '  </div>' +
                    '</td>' +
                    '<td class="col-revenue revenue-value">' + formatCurrency(m.revenue || 0) + '</td>' +
                    '<td class="col-tickets">' + (m.ticketsSold || 0).toLocaleString() + '</td>' +
                    '<td class="col-rating">' + formatRating(m.averageRating) + '</td>' +
                    '<td class="col-actions">' +
                    '  <button class="action-btn" onclick="editMovie(' + m.movieId + ')" title="Sửa"><i class="fa-solid fa-pen"></i></button>' +
                    '  <button class="action-btn" onclick="deleteMovie(' + m.movieId + ')" title="Xóa"><i class="fa-solid fa-trash"></i></button>' +
                    '</td>' +
                    '</tr>';
            }).join('');
        } catch (err) {
            console.error('Failed to load ranking:', err);
        }
    }

    function formatRating(rating) {
        if (rating == null) return '<span class="text-muted">N/A</span>';
        return '<span class="rating-value">' + Number(rating).toFixed(1) + '</span>';
    }

    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function debounceRanking() {
        clearTimeout(rankingTimer);
        rankingTimer = setTimeout(loadRanking, 300);
    }

    // ========== PERIOD TABS ==========
    document.querySelectorAll('.period-tab').forEach(tab => {
        tab.addEventListener('click', function () {
            document.querySelectorAll('.period-tab').forEach(t => t.classList.remove('active'));
            this.classList.add('active');
            loadChart(this.dataset.period);
        });
    });

    // ========== STATUS TABS ==========
    document.querySelectorAll('.status-tab').forEach(tab => {
        tab.addEventListener('click', function () {
            document.querySelectorAll('.status-tab').forEach(t => t.classList.remove('active'));
            this.classList.add('active');
            loadRanking();
        });
    });

    // ========== FILTER ==========
    filterBtn.addEventListener('click', function () {
        loadChart(currentPeriod);
        loadRanking();
    });

    rankingSearch.addEventListener('input', debounceRanking);
    genreFilter.addEventListener('change', loadRanking);

    // ========== GENRE DROPDOWN ==========
    function populateGenres() {
        if (!GENRES || GENRES.length === 0) return;
        genreFilter.innerHTML = '<option value="">Tất cả</option>';
        GENRES.forEach(g => {
            const opt = document.createElement('option');
            opt.value = g.id;
            opt.textContent = g.name;
            genreFilter.appendChild(opt);
        });
    }

    // ========== INIT ==========
    populateGenres();
    loadStats();
    loadChart('month');
    loadRanking();
});
