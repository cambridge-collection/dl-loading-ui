$(document).ready(function () {
    $('#confirmChangeModal').modal({show: false})

    $('#dataTable').DataTable({
        paging: false,
        searching: false
    });

    let $previousSelectValue;

    function beforeVersionChange(sel) {
        $previousSelectValue = sel.value;
    }

    function onVersionChange(sel) {

        $('#confirmChangePackageName').text(sel.value);
        $('#confirmChangeInstanceId').text(sel.name);
        $('#confirmDeployForm input[name="version"]').val(sel.value);
        $('#confirmDeployForm').attr('action', '/deploy/' + sel.name);
        $('#confirmChangeModal').modal('show');

        sel.value = $previousSelectValue;
    }
});



