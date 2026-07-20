function showAppToast(message, type) {
    type = type === 'error' ? 'error' : 'success';
    let wrap = document.querySelector('.app-toast-wrap');
    if (!wrap) {
        wrap = document.createElement('div');
        wrap.className = 'app-toast-wrap';
        document.body.appendChild(wrap);
    }

    const toast = document.createElement('div');
    toast.className = 'app-toast app-toast-' + type;
    const icon = type === 'success' ? 'fa-circle-check' : 'fa-circle-exclamation';
    toast.innerHTML = '<i class="fa-solid ' + icon + '"></i><span></span>';
    toast.querySelector('span').textContent = message;
    wrap.appendChild(toast);

    requestAnimationFrame(() => toast.classList.add('show'));

    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 250);
    }, 3500);
}
