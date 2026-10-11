// Inventory module JavaScript (small UI helpers only; the real logic is in Java).

document.addEventListener('DOMContentLoaded', function () {

    // 1) Ask "are you sure?" before deleting
    document.querySelectorAll('.delete-form').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            const name = form.getAttribute('data-name');
            if (!confirm('Delete "' + name + '"? This cannot be undone.')) {
                event.preventDefault();   // stop the form from being sent
            }
        });
    });

    // 2) On the add/edit form, show only the fields that match the chosen type
    const typeField = document.getElementById('type');
    const perishable = document.getElementById('perishableFields');
    const nonPerishable = document.getElementById('nonPerishableFields');

    if (typeField && perishable && nonPerishable) {
        function updateFields() {
            perishable.style.display = typeField.value === 'PERISHABLE' ? 'block' : 'none';
            nonPerishable.style.display = typeField.value === 'NON_PERISHABLE' ? 'block' : 'none';
        }
        typeField.addEventListener('change', updateFields);
        updateFields();   // run once when the page opens (needed for Edit)
    }
});
