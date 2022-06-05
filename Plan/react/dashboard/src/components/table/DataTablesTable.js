import React, {useEffect, useState} from 'react';
import DataTable from 'datatables.net'
import 'datatables.net-bs5'
import 'datatables.net-responsive-bs5'
import 'datatables.net-bs5/css/dataTables.bootstrap5.min.css';
import 'datatables.net-responsive-bs5/css/responsive.bootstrap5.min.css';

const DataTablesTable = ({id, options}) => {
    const [dataTable, setDataTable] = useState(null);

    useEffect(() => {
        if (dataTable) {
            dataTable.destroy();
            setDataTable(null);
        }

        const createdDataTable = new DataTable(`#${id}`, options);
        setDataTable(createdDataTable);

        return () => {
            if (dataTable) {
                dataTable.clear();
                dataTable.destroy();
            }
        };
    }, [id, options, dataTable]);

    return (
        <table id={id} className="table table-bordered table-striped" style={{width: "100%"}}/>
    )
};

export default DataTablesTable