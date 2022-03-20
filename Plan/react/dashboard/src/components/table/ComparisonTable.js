import React from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCaretDown, faCaretUp} from "@fortawesome/free-solid-svg-icons";
import {useTheme} from "../../hooks/themeHook";

const ComparisonTable = ({headers, children, comparisonHeader}) => {
    const {nightModeEnabled} = useTheme();

    const zeroWidthIcon = {width: "0.7rem"}

    return <table className={"table table-striped" + (nightModeEnabled ? " table-dark" : '')}>
        <thead>
        <tr>
            <th>
                <Fa style={zeroWidthIcon} icon={faCaretUp} className="text-success"/><Fa style={zeroWidthIcon}
                                                                                         icon={faCaretDown}
                                                                                         className="text-danger"/>
                {' '}<small>{comparisonHeader}</small>
            </th>
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

export default ComparisonTable