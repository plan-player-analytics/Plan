import React from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
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

export const TableRow = ({icon, text, color, values, bold}) => {
    const label = (<><Fa icon={icon} className={'col-' + color}/> {text}</>);
    return (
        <tr>
            <td>{bold ? <b>{label}</b> : label}</td>
            {values.map((value, j) => <td key={j}>{value}</td>)}
        </tr>
    )
}

export default AsNumbersTable