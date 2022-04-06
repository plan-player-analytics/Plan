import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import React from "react";

const End = ({children}) => (
    <span className="float-end">{children}</span>
)

const Datapoint = ({icon, color, name, value, valueLabel, bold, boldTitle, title}) => {
    const displayedValue = bold ? <b>{value}</b> : value;
    const extraLabel = valueLabel ? ` (${valueLabel})` : '';
    const colorClass = color && color.startsWith("col-") ? color : "col-" + color;
    return (
        <p title={title ? title : name + " is " + value}>
            <Fa icon={icon} className={colorClass}/> {boldTitle ? <b>{name}</b> : name}
            {value !== undefined ? <End>{displayedValue} {extraLabel}</End> : ''}
        </p>
    );
}

export default Datapoint