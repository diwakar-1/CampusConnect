// CampusConnect Main JavaScript
document.addEventListener('DOMContentLoaded', function () {
    // Auto-dismiss alerts
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) {
                bsAlert.close();
            }
        }, 6000);
    });

    // Tooltips initialization
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Dynamic Team Member Field Addition in Team Registration
    const addMemberBtn = document.getElementById('addTeamMemberBtn');
    const membersContainer = document.getElementById('teamMembersContainer');

    if (addMemberBtn && membersContainer) {
        addMemberBtn.addEventListener('click', function () {
            const currentCount = membersContainer.querySelectorAll('.team-member-row').length;
            const maxMembers = parseInt(membersContainer.getAttribute('data-max') || '4', 10);

            if (currentCount + 1 >= maxMembers) { // +1 for team leader
                alert('Maximum team size reached (' + maxMembers + ' members total including team leader).');
                return;
            }

            const index = currentCount;
            const memberRow = document.createElement('div');
            memberRow.className = 'team-member-row p-3 mb-3 rounded-3 glass-panel';
            memberRow.innerHTML = `
                <div class="d-flex justify-content-between align-items-center mb-2">
                    <span class="badge bg-secondary">Team Member #${index + 2}</span>
                    <button type="button" class="btn btn-sm btn-outline-danger remove-member-btn">
                        <i class="bi bi-trash"></i> Remove
                    </button>
                </div>
                <div class="row g-2">
                    <div class="col-md-6">
                        <label class="form-label">Full Name *</label>
                        <input type="text" name="teamMembers[${index}].fullName" required class="form-control" placeholder="Member full name">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">Email Address *</label>
                        <input type="email" name="teamMembers[${index}].email" required class="form-control" placeholder="member@college.edu">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label">Roll Number / Student ID</label>
                        <input type="text" name="teamMembers[${index}].rollNumber" class="form-control" placeholder="Roll No.">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label">College / University</label>
                        <input type="text" name="teamMembers[${index}].collegeName" class="form-control" placeholder="College name">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label">Contact Number</label>
                        <input type="tel" name="teamMembers[${index}].phone" class="form-control" placeholder="Phone">
                    </div>
                </div>
            `;
            membersContainer.appendChild(memberRow);

            // Bind remove button
            memberRow.querySelector('.remove-member-btn').addEventListener('click', function () {
                memberRow.remove();
            });
        });
    }
});

function copyToClipboard(text, feedbackElementId) {
    navigator.clipboard.writeText(text).then(function () {
        const fb = document.getElementById(feedbackElementId);
        if (fb) {
            const originalText = fb.innerHTML;
            fb.innerHTML = '<i class="bi bi-check2"></i> Copied!';
            setTimeout(() => fb.innerHTML = originalText, 2000);
        }
    });
}

