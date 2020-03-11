loading_ui_um_user = {};

$(document).ready(function () {

    let updateModal = $('#updateModal');
    updateModal.modal({show: false});

    let deleteModal = $('#deleteModal');
    deleteModal.modal({show: false});

    $('#umform-submit').on('click', function () {
        updateModal.modal('show');
        $('#updateuserform').submit();
    });
    $('#umform-reset').on('click', function () {
        $('#updateuserform')[0].reset();
    });
    $('#umform-delete').on('click', function () {
        return loading_ui_um_user.showDeleteModal(this);
    });

    loading_ui_um_user.showDeleteModal = function (button) {
        deleteModal.modal('show');
        let form = button.form;
        $('#confirmDeleteButton').on('click', function () {
            deleteModal.modal('hide');
            updateModal.modal('show');
            form.submit();
        });
        return false;
    };

});



