import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import React from "react";

const End = ({children}) => (
    <span className="float-end">{children}</span>
)

const Datapoint = ({icon, color, name, value, valueLabel, bold, boldTitle, title, trend}) => {
    const displayedValue = bold ? <b>{value}</b> : value;
    const extraLabel = valueLabel instanceof String ? ` (${valueLabel})` : '';
    const colorClass = color && color.startsWith("col-") ? color : "col-" + color;
    return (
        <p title={title ? title : name + " is " + value}>
            {icon && <Fa icon={icon} className={colorClass}/>} {boldTitle ? <b>{name}</b> : name}
            {value !== undefined ? <End>{displayedValue} {extraLabel}{trend}</End> : ''}
        </p>
    );
}

export default Datapoint