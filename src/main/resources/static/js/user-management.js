
$(document).ready(function () {
    $('#confirmChangeModal').modal({show: false})

    $('#usersDataTable').DataTable({
        paging: false,
        searching: false
    });

    $('#workspacesDataTable').DataTable({
        paging: false,
        searching: false
    });
});
