document.addEventListener('DOMContentLoaded', function () {
    updateCartBadge();

    document.querySelectorAll('.btn-add-cart').forEach(btn => {
        btn.addEventListener('click', function () {
            const bookId = this.dataset.id;
            if (!bookId) return;

            const csrfToken = document.querySelector('meta[name="_csrf"]')?.content || '';
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';

            fetch('/cart/add/' + bookId, {
                method: 'POST',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest',
                    [csrfHeader]: csrfToken
                }
            })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        showToast('Đã thêm vào giỏ hàng!', 'success');
                        updateCartBadge(data.cartCount);
                    } else if (data.redirect) {
                        window.location.href = '/login';
                    } else {
                        showToast(data.message || 'Có lỗi xảy ra', 'danger');
                    }
                })
                .catch(() => {
                    window.location.href = '/cart/add/' + bookId;
                });
        });
    });
});

function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    if (!container) return;
    const id = 'toast-' + Date.now();
    const icon = type === 'success' ? 'bi-check-circle-fill' : 'bi-exclamation-circle-fill';
    container.insertAdjacentHTML('beforeend', `
        <div id="${id}" class="toast align-items-center text-bg-${type} border-0 show" role="alert">
            <div class="d-flex">
                <div class="toast-body">
                    <i class="bi ${icon} me-2"></i>${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        </div>
    `);
    setTimeout(() => document.getElementById(id)?.remove(), 3000);
}

function updateCartBadge(count) {
    const badge = document.getElementById('cartCount');
    if (!badge) return;
    if (count !== undefined) { badge.textContent = count; return; }
    fetch('/cart/count')
        .then(r => r.json())
        .then(data => { badge.textContent = data.count || 0; })
        .catch(() => {});
}