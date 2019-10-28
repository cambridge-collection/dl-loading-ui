$(document).ready(function () {

    let updateModal = $('#updateModal');
    updateModal.modal({show: false});

    let deleteItemModal = $('#deleteItemModal');
    deleteItemModal.modal({show: false});

    $('#dataTable').DataTable({
        paging: true,
        searching: true
    });

    $('#collectioneditform-submit').on('click', function () {
        updateModal.modal('show');
        $('#collectioneditform').submit();
    });
    $('#collectioneditform-reset').on('click', function () {
        $('#collectioneditform')[0].reset();
    });

    $('#addItemFile').on('change', function () {
        updateModal.modal('show');
        $('#addItemFileForm').submit();
    });

    $('button[id^="deleteItemButton_"]').on('click', function () {
        console.log(this);
        deleteItemModal.modal('show');
        let form = this.form;
        $('#confirmDeleteItemButton').on('click', function () {
            form.submit();
        });
        return false;
    });

    $('#confirmDeleteItemButton').on('click', function () {
        deleteItemModal.modal('hide');
        updateModal.modal('show');
        $('#deleteItemForm').submit();
    });

});



