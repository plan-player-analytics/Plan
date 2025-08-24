import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import React from "react";
import End from "./layout/End";
import {useTranslation} from "react-i18next";

const getExtraLabel = (t, valueLabel) => {
    if (typeof valueLabel === 'string') {
        return ` (${t(valueLabel)})`;
    } else if (valueLabel?.props) {
        return valueLabel;
    } else {
        return '';
    }
}

const Datapoint = ({icon, color, name, value, valueLabel, bold, boldTitle, title, trend}) => {
    const {t} = useTranslation();
    if (value === undefined && valueLabel === undefined) return <></>;

    const isTranslatable = typeof value === 'string' && (value.startsWith('html') || value.startsWith('plugin') || value.startsWith('generic'));
    const translatedValue = isTranslatable ? t(value) : value;
    const displayedValue = bold ? <b>{translatedValue}</b> : translatedValue;
    const extraLabel = getExtraLabel(t, valueLabel);
    const colorClass = color?.startsWith("col-") ? color : "col-" + color;
    return (
        <p className={"col-text"} title={title ? title : name + ": " + translatedValue}>
            {icon && <Fa icon={icon} className={colorClass}/>} {boldTitle ? <b>{name}</b> : name}
            {value !== undefined ? <End>{displayedValue} {extraLabel}{trend}</End> : ''}
        </p>
    );
}

export default Datapoint