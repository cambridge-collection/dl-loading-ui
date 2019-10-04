$('#confirmChangeModal').modal({ show: false})

$('#dataTable').DataTable( {
    paging: false,
    searching: false
} );

let $previousSelectValue;

function beforeVersionChange(sel) {
   $previousSelectValue = sel.value;
}

function onVersionChange(sel) {

    console.log(sel.value);
    //let $prevSelection = ob.originalEvent.originalTarget.selectedIndex;
    //let $newSelection = ob.target.selectedIndex;

    console.log($previousSelectValue);
    console.log(sel.value);
    console.log(sel.name);


    $('#confirmChangePackageName').text(sel.value);
    $('#confirmChangeInstanceId').text(sel.name);
    $('#confirmDeployForm input[name="version"]').val(sel.value);
    $('#confirmDeployForm').attr('action', '/deploy/'+sel.name);
    $('#confirmChangeModal').modal('show');

    sel.value =  $previousSelectValue;
}




