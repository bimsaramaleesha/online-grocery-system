// order module scripts. Only the owner of the order module edits this file.

function increaseQty(button) {
    const input = button.parentElement.querySelector('.order-qty-input');
    if (input) {
        let val = parseInt(input.value, 10) || 1;
        input.value = val + 1;
        input.form.submit();
    }
}

function decreaseQty(button) {
    const input = button.parentElement.querySelector('.order-qty-input');
    if (input) {
        let val = parseInt(input.value, 10) || 1;
        if (val > 1) {
            input.value = val - 1;
            input.form.submit();
        } else {
            if (confirm('Remove this item from your cart?')) {
                input.value = 0;
                input.form.submit();
            }
        }
    }
}
