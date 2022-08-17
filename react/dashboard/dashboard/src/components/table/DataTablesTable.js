import React, {useEffect, useRef} from 'react';
import DataTable from 'datatables.net'
import 'datatables.net-bs5'
import 'datatables.net-responsive-bs5'
import 'datatables.net-bs5/css/dataTables.bootstrap5.min.css';
import 'datatables.net-responsive-bs5/css/responsive.bootstrap5.min.css';

const DataTablesTable = ({id, options}) => {
    const dataTableRef = useRef(null);

    useEffect(() => {
        const idSelector = `#${id}`;
        if (dataTableRef.current && DataTable.isDataTable(idSelector)) {
            dataTableRef.current.destroy();
        }

        dataTableRef.current = new DataTable(idSelector, options);

        return () => {
            if (dataTableRef.current) {
                dataTableRef.current.destroy();
            }
        };
    }, [id, options, dataTableRef]);

    return (
        <table id={id} className="table table-bordered table-striped" style={{width: "100%"}}/>
    )
};

export default DataTablesTable