import {useTranslation} from "react-i18next";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import End from "../layout/End";
import ColoredText from "../text/ColoredText";
import {Link} from "react-router";
import {MinecraftChat} from "react-mcjsonchat";
import FormattedTime from "../text/FormattedTime";
import FormattedDate from "../text/FormattedDate";
import React, {PropsWithChildren} from "react";
import {ExtensionValue as DataType} from "../../dataHooks/model/extension/ExtensionData";
import {IconProp} from "@fortawesome/fontawesome-svg-core";

type Props = {
    data: DataType;
}

const valueOrUndefined = (value?: any) => {
    return typeof value === "undefined" ? undefined : value;
}
const sanitizeComponent = (component: any) => {
    if (!component) return [];
    return {
        extra: component.extra ? component.extra.filter(Boolean).map(sanitizeComponent) : [],
        color: valueOrUndefined(component.color),
        bold: valueOrUndefined(component.bold),
        italic: valueOrUndefined(component.italic),
        underlined: valueOrUndefined(component.underlined),
        strikethrough: valueOrUndefined(component.strikethrough),
        obfuscation: valueOrUndefined(component.obfuscation),
        text: valueOrUndefined(component.text)
    };
}

const ValueWrapper = ({data, children}: Props & PropsWithChildren) => {
    const color = data.description.icon.colorClass;
    const colorClass = color?.startsWith("col-") ? color : "col-" + color;
    const icon: IconProp = [data.description.icon.familyClass, data.description.icon.iconName];
    const name = data.description.text;
    const title = data.description.description;

    return (
        <p title={title}>
            {icon && <Fa icon={icon} className={colorClass}/>} {name}
            <End>{children}</End>
        </p>
    );
}

const Value = ({data}: Props) => {
    const {t} = useTranslation();

    switch (data.type) {
        case 'STRING':
            return <ColoredText text={data.value}/>;
        case 'LINK':
            return <Link to={data.value?.link}><ColoredText text={data.value?.text}/></Link>;
        case 'COMPONENT':
            return <MinecraftChat component={sanitizeComponent(JSON.parse(data.value))}/>;
        case 'BOOLEAN':
            return t(data.value ? 'plugin.generic.yes' : 'plugin.generic.no');
        case 'TIME_MILLISECONDS':
            return <FormattedTime timeMs={data.value}/>;
        case 'DATE_YEAR':
            return <FormattedDate date={data.value}/>;
        case 'DATE_SECOND':
            return <FormattedDate date={data.value} includeSeconds/>;
        default:
            return data.value;
    }
};

export const ExtensionValue = ({data}: Props) => {
    return (
        <ValueWrapper data={data}>
            <Value data={data}/>
        </ValueWrapper>
    );
}

export const ExtensionValueTableCell = ({data}: Props) => {
    if (!data) return '-';

    const title = data.description.description;
    return <span title={title}><Value data={data}/></span>
}