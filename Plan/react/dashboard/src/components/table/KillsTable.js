import React from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faAngleRight, faSkullCrossbones} from "@fortawesome/free-solid-svg-icons";

const KillRow = ({kill}) => {
    const killSeparator = <Fa
        icon={kill.killerUUID === kill.victimUUID ? faSkullCrossbones : faAngleRight}
        className={"col-red"}/>;
    return (
        <tr>
            <td>{kill.date}</td>
            <td>{kill.killerName} {killSeparator} {kill.victimName}</td>
            <td>{kill.weapon}</td>
            <td>{kill.serverName}</td>
        </tr>
    );
}

const KillsTable = ({kills}) => (
    <table className="table mb-0">
        <tbody>
        {kills.length ? kills.map((kill, i) => <KillRow key={i} kill={kill}/>) : <tr>
            <td>None</td>
            <td>-</td>
            <td>-</td>
            <td>-</td>
        </tr>}
        </tbody>
    </table>
);

export default KillsTable;