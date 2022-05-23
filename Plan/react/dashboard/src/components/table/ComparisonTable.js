import React from "react";
import {useTheme} from "../../hooks/themeHook";
import ComparingLabel from "../trend/ComparingLabel";

const ComparisonTable = ({headers, children, comparisonHeader}) => {
    const {nightModeEnabled} = useTheme();

    return <table className={"table table-striped" + (nightModeEnabled ? " table-dark" : '')}>
        <thead>
        <tr>
            <th>
                <ComparingLabel>{comparisonHeader}</ComparingLabel>
            </th>
            {headers.map((header, i) => <th key={i}>{header}</th>)}
        </tr>
        </thead>
        <tbody>
        {children}
        </tbody>
    </table>
}

export default ComparisonTable