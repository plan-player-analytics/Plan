import React, {PropsWithChildren} from "react";
import {useTheme} from "../../hooks/themeHook";

type Props = {
    headers: string[];
} & PropsWithChildren;

const AsNumbersTable = ({headers, children}: Props) => {
    const {nightModeEnabled} = useTheme();

    return <table className={"table table-striped" + (nightModeEnabled ? " table-dark" : '')}>
        <thead>
        <tr>
            <th/>
            {headers.map((header, i) => <th key={header}>{header}</th>)}
        </tr>
        </thead>
        <tbody>
        {children}
        </tbody>
    </table>
}

export default AsNumbersTable