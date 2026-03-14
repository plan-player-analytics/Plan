import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import React, {ReactNode} from "react";
import End from "../layout/End.jsx";
import {useTranslation} from "react-i18next";
import {Trend} from "../../dataHooks/model/datapoint/Trend";
import {IconProp} from "@fortawesome/fontawesome-svg-core";

const ExtraLabel = ({valueLabel}: { valueLabel: string | ReactNode }) => {
    const {t} = useTranslation();

    if (typeof valueLabel === 'string') {
        return ` (${t(valueLabel)})`;
    } else if (valueLabel) {
        return valueLabel;
    } else {
        return '';
    }
}

export type DatapointProps = {
    icon: IconProp;
    color: string;
    name: string;
    value: any;
    valueLabel?: string | ReactNode;
    prefix?: string;
    bold?: boolean;
    boldTitle?: boolean;
    title?: string;
    trend?: Trend;
}

export const Datapoint = ({
                              icon,
                              color,
                              name,
                              value,
                              valueLabel,
                              prefix,
                              bold,
                              boldTitle,
                              title,
                              trend
                          }: DatapointProps) => {
    const {t} = useTranslation();
    if (value === undefined && valueLabel === undefined) return <></>;

    const isTranslatable = typeof value === 'string' && (value.startsWith('html') || value.startsWith('plugin') || value.startsWith('generic'));
    const translatedValue = isTranslatable ? t(value) : value;
    const displayedValue = bold ? <b>{translatedValue}</b> : translatedValue;
    const extraLabel = <ExtraLabel valueLabel={valueLabel}/>;
    const colorClass = color?.startsWith("col-") ? color : "col-" + color;
    return (
        <p className={"col-text datapoint"} title={title || (name + ": " + translatedValue)}>
            {icon && <Fa icon={icon} className={colorClass}/>} {boldTitle ? <b>{name}</b> : name}
            {value !== undefined && <End>{prefix}{displayedValue} {extraLabel}{trend}</End>}
        </p>
    );
}

export default Datapoint