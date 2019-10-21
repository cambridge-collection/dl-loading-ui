$(document).ready(function () {

    $('#dataTable').DataTable({
        paging: true,
        searching: true
    });

    $('#collectioneditform-submit').on('click', function () {
        $('#collectioneditform').submit();
    });
    $('#collectioneditform-reset').on('click', function () {
        $('#collectioneditform')[0].reset();
    });
});



