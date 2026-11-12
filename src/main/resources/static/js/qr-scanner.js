// CampusConnect QR Scanner Helper
document.addEventListener('DOMContentLoaded', function () {
    const qrInput = document.getElementById('scannerQrInput');
    const resultBox = document.getElementById('scanResultAlert');
    const eventSelect = document.getElementById('scannerEventSelect');

    if (qrInput) {
        // Auto-focus scanner input
        qrInput.focus();

        // Handle enter key from USB / Bluetooth handheld barcode/QR scanners
        qrInput.addEventListener('keypress', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                processLiveScan(qrInput.value);
            }
        });
    }

    window.triggerManualScan = function () {
        if (qrInput && qrInput.value.trim() !== '') {
            processLiveScan(qrInput.value.trim());
        }
    };

    function processLiveScan(code) {
        if (!code || !eventSelect) return;
        const eventId = eventSelect.value;
        if (!eventId) {
            showScanResult(false, 'Please select an event before scanning.');
            return;
        }

        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

        const headers = { 'Content-Type': 'application/json' };
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        fetch('/api/attendance/scan', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify({ qrCode: code, eventId: eventId })
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showScanResult(true, `Verified: ${data.studentName} (${data.rollNumber || 'ID'}) - Pass: ${data.registrationNumber}`);
                qrInput.value = '';
                // Reload list or play chime
                setTimeout(() => window.location.reload(), 1200);
            } else {
                showScanResult(false, data.message || 'Verification failed');
                qrInput.select();
            }
        })
        .catch(err => {
            showScanResult(false, 'Network or server error during scan verification');
        });
    }

    function showScanResult(isSuccess, message) {
        if (!resultBox) return;
        resultBox.className = isSuccess ? 'alert alert-success d-flex align-items-center' : 'alert alert-danger d-flex align-items-center';
        resultBox.innerHTML = `
            <i class="bi ${isSuccess ? 'bi-check-circle-fill text-success' : 'bi-exclamation-triangle-fill text-danger'} fs-4 me-2"></i>
            <div><strong>${isSuccess ? 'SUCCESS' : 'SCAN ERROR'}:</strong> ${message}</div>
        `;
        resultBox.classList.remove('d-none');
    }
});

