document.addEventListener("DOMContentLoaded", function () {

    // ==============================
    // APPROVAL MODAL
    // ==============================

    const approveModal = document.getElementById("approveModal");

    if (approveModal) {

        approveModal.addEventListener("show.bs.modal", function (event) {

            const button = event.relatedTarget;

            if (!button) return;

            const loanId = button.getAttribute("data-loan-id");
            const borrowerName = button.getAttribute("data-borrower-name");

            document.getElementById("approveLoanId").value = loanId;

            document.getElementById("approveBorrowerName")
                .textContent = borrowerName;
        });
    }

    // ==============================
    // REJECTION MODAL
    // ==============================

    const rejectModal = document.getElementById("rejectModal");
    const rejectionRemark = document.getElementById("rejectionRemark");
    const remarkCounter = document.getElementById("remarkCounter");

    if (rejectModal) {

        rejectModal.addEventListener("show.bs.modal", function (event) {

            const button = event.relatedTarget;

            if (!button) return;

            const loanId = button.getAttribute("data-loan-id");
            const borrowerName = button.getAttribute("data-borrower-name");

            document.getElementById("rejectLoanId").value = loanId;

            document.getElementById("rejectBorrowerName")
                .textContent = borrowerName;

            rejectionRemark.value = "";
            rejectionRemark.setCustomValidity("");

            updateRemarkCounter();
        });

        rejectModal.addEventListener("shown.bs.modal", function () {
            rejectionRemark.focus();
        });
    }

    // ==============================
    // LIVE CHARACTER COUNTER
    // ==============================

    function updateRemarkCounter() {

        if (!rejectionRemark || !remarkCounter) return;

        const length = rejectionRemark.value.length;

        remarkCounter.textContent = length + " / 500";

        remarkCounter.classList.toggle("text-danger", length >= 450);
        remarkCounter.classList.toggle("text-muted", length < 450);
    }

    if (rejectionRemark) {

        rejectionRemark.addEventListener("input", function () {

            rejectionRemark.setCustomValidity("");

            updateRemarkCounter();
        });
    }

    // ==============================
    // PREVENT DUPLICATE SUBMISSIONS
    // ==============================

    function disableSubmitButton(form, message) {

        const button = form.querySelector('button[type="submit"]');

        if (button) {

            button.disabled = true;

            button.innerHTML =
                '<span class="spinner-border spinner-border-sm me-2" ' +
                'role="status" aria-hidden="true"></span>' + message;
        }
    }

    // ==============================
    // APPROVAL FORM
    // ==============================

    const approveForm = document.getElementById("approveForm");

    if (approveForm) {

        approveForm.addEventListener("submit", function (event) {

            if (approveForm.dataset.submitting === "true") {

                event.preventDefault();
                return;
            }

            approveForm.dataset.submitting = "true";

            disableSubmitButton(approveForm, "Approving...");
        });
    }

    // ==============================
    // REJECTION FORM VALIDATION
    // ==============================

    const rejectForm = document.getElementById("rejectForm");

    if (rejectForm) {

        rejectForm.addEventListener("submit", function (event) {

            const remark = rejectionRemark.value.trim();

            if (remark.length === 0) {

                event.preventDefault();

                rejectionRemark.setCustomValidity(
                    "Please enter a valid rejection remark."
                );

                rejectionRemark.reportValidity();

                return;
            }

            if (remark.length > 500) {

                event.preventDefault();

                rejectionRemark.setCustomValidity(
                    "Remark cannot exceed 500 characters."
                );

                rejectionRemark.reportValidity();

                return;
            }

            if (rejectForm.dataset.submitting === "true") {

                event.preventDefault();
                return;
            }

            rejectionRemark.value = remark;

            rejectForm.dataset.submitting = "true";

            disableSubmitButton(rejectForm, "Rejecting...");
        });
    }

    // ==============================
    // AUTO DISMISS SUCCESS ALERTS
    // ==============================

    const successAlerts =
        document.querySelectorAll(".alert-success.alert-dismissible");

    successAlerts.forEach(function (alert) {

        setTimeout(function () {

            if (alert.isConnected && typeof bootstrap !== "undefined") {

                bootstrap.Alert.getOrCreateInstance(alert).close();
            }

        }, 5000);
    });

});