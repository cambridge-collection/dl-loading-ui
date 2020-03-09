
$(document).ready(function () {

    let updateModal = $('#updateModal');
    updateModal.modal({show: false});

    $('#umform-submit').on('click', function () {
        updateModal.modal('show');
        $('#updateworkspaceform').submit();
    });
    $('#umform-reset').on('click', function () {
        $('#updateworkspaceform')[0].reset();
    });

});



