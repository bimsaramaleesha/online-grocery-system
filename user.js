document.addEventListener('DOMContentLoaded', function () {


    document.querySelectorAll('[data-toggle-password]').forEach(function (box) {
        box.addEventListener('change', function () {
            box.getAttribute('data-toggle-password').split(',').forEach(function (id) {
                const input = document.getElementById(id.trim());
                if (input) {
                    input.type = box.checked ? 'text' : 'password';
                }
            });
        });
    });

    document.querySelectorAll('form[data-match-form]').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            const first = form.querySelector('#password, #newPassword');
            const second = form.querySelector('#confirmPassword');
            if (first && second && first.value !== second.value) {
                event.preventDefault();
                alert('The two passwords do not match.');
                second.focus();
            }
        });
    });

    document.querySelectorAll('.usr-delete-form').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            const name = form.getAttribute('data-name');
            if (!confirm('Delete the account of "' + name + '"? This cannot be undone.')) {
                event.preventDefault();
            }
        });
    });

    const typeField = document.getElementById('type');
    const customerFields = document.getElementById('customerFields');
    const adminFields = document.getElementById('adminFields');

    if (typeField && customerFields && adminFields) {
        function updateFields() {
            customerFields.style.display = typeField.value === 'CUSTOMER' ? 'block' : 'none';
            adminFields.style.display = typeField.value === 'ADMIN' ? 'block' : 'none';
        }
        typeField.addEventListener('change', updateFields);
        updateFields();
    }
});
