/*
 * INT BANK — utilitas antarmuka.
 * Hanya perilaku umum: konfirmasi submit, pencegahan dobel submit,
 * format nominal Rupiah opsional, dan tombol cetak bukti transaksi.
 * Tidak ada data model yang dibaca dari JavaScript ini.
 */
(function () {
    'use strict';

    function ready(callback) {
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', callback, { once: true });
        } else {
            callback();
        }
    }

    /* ---------- Nominal Rupiah (opt-in lewat atribut data-rupiah) ---------- */
    function digitsOnly(value) {
        return String(value == null ? '' : value).replace(/\D/g, '');
    }

    function formatRupiah(value) {
        var digits = digitsOnly(value).replace(/^0+(?=\d)/, '');
        if (!digits) {
            return '';
        }
        return digits.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
    }

    function initRupiahFields() {
        var fields = document.querySelectorAll('[data-rupiah]');

        Array.prototype.forEach.call(fields, function (input) {
            if (input.dataset.rupiahReady === 'true') {
                return;
            }
            input.dataset.rupiahReady = 'true';

            var initial = digitsOnly(input.value);
            input.dataset.raw = initial;
            if (initial) {
                input.value = formatRupiah(initial);
            }

            input.addEventListener('input', function () {
                var raw = digitsOnly(input.value);
                input.dataset.raw = raw;
                input.value = formatRupiah(raw);
            });

            input.addEventListener('focus', function () {
                input.value = input.dataset.raw || '';
            });

            input.addEventListener('blur', function () {
                var raw = digitsOnly(input.value);
                input.dataset.raw = raw;
                input.value = formatRupiah(raw);
            });
        });
    }

    /* ---------- Konfirmasi & pencegahan dobel submit ---------- */
    function initSubmitGuards() {
        document.addEventListener('submit', function (event) {
            var form = event.target;
            if (!form || form.nodeName !== 'FORM') {
                return;
            }

            var message = form.getAttribute('data-confirm');
            if (message && !window.confirm(message)) {
                event.preventDefault();
                return;
            }

            // Kirim angka mentah ke server, bukan string yang sudah diformat.
            Array.prototype.forEach.call(form.querySelectorAll('[data-rupiah]'), function (input) {
                var raw = input.dataset.raw;
                if (raw !== undefined && raw !== null) {
                    input.value = raw;
                }
            });

            if (form.dataset.submitting === 'true') {
                event.preventDefault();
                return;
            }
            form.dataset.submitting = 'true';

            var submitters = form.querySelectorAll('button[type="submit"], input[type="submit"]');
            Array.prototype.forEach.call(submitters, function (button) {
                if (button.disabled) {
                    return;
                }
                button.disabled = true;
                button.setAttribute('aria-busy', 'true');
            });
        });
    }

    /* ---------- Cetak bukti transaksi ---------- */
    function initPrintButtons() {
        var buttons = document.querySelectorAll('[data-print]');
        Array.prototype.forEach.call(buttons, function (button) {
            button.addEventListener('click', function () {
                window.print();
            });
        });
    }

    ready(function () {
        initRupiahFields();
        initSubmitGuards();
        initPrintButtons();
    });
})();
