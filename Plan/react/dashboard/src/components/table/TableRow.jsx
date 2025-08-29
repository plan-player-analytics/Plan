import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import React from "react";
import {useTranslation} from "react-i18next";

export const TableRow = ({icon, text, color, values, bold, title}) => {
    const {t} = useTranslation();
    if (!values || values.filter(value => value !== undefined).length < values.length) return <></>;

    const label = (<><Fa icon={icon} className={'col-' + color}/> {text}</>);
    return (
        <tr>
            <td title={title}>{bold ? <b>{label}</b> : label}</td>
            {values.map((value, j) => {
                const isTranslatable = typeof value === 'string' && (value.startsWith('html') || value.startsWith('plugin'));
                const translatedValue = isTranslatable ? t(value) : value;
                return <td key={j}>{translatedValue}</td>;
            })}
        </tr>
    )
}