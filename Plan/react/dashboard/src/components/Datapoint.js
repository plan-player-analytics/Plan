import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import React from "react";

const End = ({children}) => (
    <span className="float-end">{children}</span>
)

const Datapoint = ({icon, color, name, value, valueLabel, bold, boldTitle}) => (
    <p>
        <Fa icon={icon} className={"col-" + color}/> {boldTitle ? <b>{name}</b> : name}
        {value ? <End>{bold ? <b>{value}</b> : value} {valueLabel ? ` (${valueLabel})` : ''}</End> : ''}
    </p>
)

export default Datapoint