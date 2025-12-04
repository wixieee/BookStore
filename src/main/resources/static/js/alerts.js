/**
 * Auto-dismiss alerts after a specified duration
 */
document.addEventListener('DOMContentLoaded', function () {
    const alerts = document.querySelectorAll('.alert');

    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.3s ease-out, transform 0.3s ease-out';
            alert.style.opacity = '0';
            alert.style.transform = 'translateX(100%)';

            setTimeout(() => {
                alert.remove();
            }, 300);
        }, 4000);
    });
});
