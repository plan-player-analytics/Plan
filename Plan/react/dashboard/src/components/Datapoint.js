import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import React from "react";
import End from "./layout/End";
import {useTranslation} from "react-i18next";

const Datapoint = ({icon, color, name, value, valueLabel, bold, boldTitle, title, trend}) => {
    const {t} = useTranslation();
    if (value === undefined && valueLabel === undefined) return <></>;

    const isTranslatable = typeof value === 'string' && (value.startsWith('html') || value.startsWith('plugin'));
    const translatedValue = isTranslatable ? t(value) : value;
    const displayedValue = bold ? <b>{translatedValue}</b> : translatedValue;
    const extraLabel = typeof valueLabel === 'string' ? ` (${t(valueLabel)})` : '';
    const colorClass = color?.startsWith("col-") ? color : "col-" + color;
    return (
        <p title={title ? title : name + ": " + translatedValue}>
            {icon && <Fa icon={icon} className={colorClass}/>} {boldTitle ? <b>{name}</b> : name}
            {value !== undefined ? <End>{displayedValue} {extraLabel}{trend}</End> : ''}
        </p>
    );
}

export default Datapoint