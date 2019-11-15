let deploy = {};

$(document).ready(function () {
    $('#confirmChangeModal').modal({show: false})

    $('#dataTable').DataTable({
        paging: false,
        searching: false
    });
});

deploy.previousSelectValue = '';
deploy.beforeVersionChange = function (sel) {

    deploy.previousSelectValue = sel.value;
};

deploy.onVersionChange = function (sel) {

    $('#confirmChangePackageName').text(sel.value);
    $('#confirmChangeInstanceId').text(sel.name);
    $('#confirmDeployForm input[name="version"]').val(sel.value);
    $('#confirmDeployForm').attr('action', '/deploy/' + sel.name);
    $('#confirmChangeModal').modal('show');

    sel.value = deploy.previousSelectValue;
};


