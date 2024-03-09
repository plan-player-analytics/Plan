import React from "react";
import {useTheme} from "../../hooks/themeHook";

const AsNumbersTable = ({headers, children}) => {
    const {nightModeEnabled} = useTheme();

    return <table className={"table table-striped" + (nightModeEnabled ? " table-dark" : '')}>
        <thead>
        <tr>
            <th/>
            {headers.map((header, i) => <th key={i}>{header}</th>)}
        </tr>
        </thead>
        <tbody>
        {children}
        </tbody>
    </table>
}

export default AsNumbersTable