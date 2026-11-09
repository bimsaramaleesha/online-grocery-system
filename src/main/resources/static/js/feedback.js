// Feedback module scripts (Reviews & Customer Complaints).

document.addEventListener('DOMContentLoaded', function () {

    // 1) Confirm before deleting a review or complaint
    document.querySelectorAll('.fb-delete-form').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            const label = form.getAttribute('data-name') || 'this entry';
            if (!confirm('Delete "' + label + '"? This cannot be undone.')) {
                event.preventDefault();
            }
        });
    });

    // 2) On the shared feedback form, show only the fields matching REVIEW or COMPLAINT
    const typeField = document.getElementById('type');
    const reviewFields = document.getElementById('reviewFields');
    const complaintFields = document.getElementById('complaintFields');
    const feedbackForm = document.getElementById('feedbackForm');

    if (typeField && reviewFields && complaintFields) {
        function updateTypeFields() {
            const isReview = typeField.value === 'REVIEW';
            const isComplaint = typeField.value === 'COMPLAINT';
            reviewFields.style.display = isReview ? 'block' : 'none';
            complaintFields.style.display = isComplaint ? 'block' : 'none';

            if (feedbackForm && feedbackForm.getAttribute('data-edit-mode') !== 'true') {
                feedbackForm.action = isComplaint ? '/complaints/new' : '/reviews/new';
            }
        }
        typeField.addEventListener('change', updateTypeFields);
        updateTypeFields();
    }
});
