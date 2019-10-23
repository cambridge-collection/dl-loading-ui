$(document).ready(function () {

    let updateModel = $('#updateModal');
    updateModel.modal({show: false});

    $('#dataTable').DataTable({
        paging: true,
        searching: true
    });

    $('#collectioneditform-submit').on('click', function () {
        updateModel.modal('show');
        $('#collectioneditform').submit();
    });
    $('#collectioneditform-reset').on('click', function () {
        $('#collectioneditform')[0].reset();
    });
});



