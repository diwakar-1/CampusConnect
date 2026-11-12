// CampusConnect Admin Analytics Charts (Chart.js 4.x)
function initAdminCharts(eventChartData, categoryChartData) {
    // 1. Registrations Bar Chart
    const regCanvas = document.getElementById('eventRegistrationsChart');
    if (regCanvas && eventChartData) {
        new Chart(regCanvas.getContext('2d'), {
            type: 'bar',
            data: {
                labels: eventChartData.labels,
                datasets: [{
                    label: 'Confirmed Registrations',
                    data: eventChartData.data,
                    backgroundColor: 'rgba(99, 102, 241, 0.75)',
                    borderColor: '#6366f1',
                    borderWidth: 1,
                    borderRadius: 8,
                    maxBarThickness: 45
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: '#0f172a',
                        titleColor: '#f8fafc',
                        bodyColor: '#cbd5e1',
                        borderColor: 'rgba(255, 255, 255, 0.1)',
                        borderWidth: 1,
                        padding: 10
                    }
                },
                scales: {
                    x: {
                        ticks: { color: '#94a3b8' },
                        grid: { display: false }
                    },
                    y: {
                        ticks: { color: '#94a3b8', stepSize: 1 },
                        grid: { color: 'rgba(255, 255, 255, 0.05)' }
                    }
                }
            }
        });
    }

    // 2. Category Distribution Doughnut Chart
    const catCanvas = document.getElementById('categoryDistributionChart');
    if (catCanvas && categoryChartData) {
        new Chart(catCanvas.getContext('2d'), {
            type: 'doughnut',
            data: {
                labels: categoryChartData.labels,
                datasets: [{
                    data: categoryChartData.data,
                    backgroundColor: [
                        '#6366f1', '#ec4899', '#10b981', '#3b82f6', '#f59e0b', '#8b5cf6'
                    ],
                    borderColor: '#0f172a',
                    borderWidth: 3
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: { color: '#94a3b8', boxWidth: 12, padding: 15 }
                    }
                },
                cutout: '70%'
            }
        });
    }
}

