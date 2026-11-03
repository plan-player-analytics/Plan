import { IconProp } from '@fortawesome/fontawesome-svg-core';
import { FontAwesomeIcon as Fa } from '@fortawesome/react-fontawesome';
import React, { ReactNode } from 'react';
import { useTranslation } from 'react-i18next';

type Props = {
    icon: IconProp;
    text: ReactNode;
    color: string;
    values?: ReactNode[] | null;
    bold?: boolean;
    title?: string;
};

export const TableRow = ({ icon, text, color, values, bold, title }: Props) => {
    const { t } = useTranslation();
    if (
        !values ||
        values.filter((value) => value !== undefined).length < values.length
    )
        return <></>;

    const label = (
        <>
            <Fa icon={icon} className={'col-' + color} /> {text}
        </>
    );
    return (
        <tr>
            <td title={title}>{bold ? <b>{label}</b> : label}</td>
            {values.map((value, j) => {
                const isTranslatable =
                    typeof value === 'string' &&
                    (value.startsWith('html') || value.startsWith('plugin'));
                const translatedValue = isTranslatable ? t(value) : value;
                return <td key={j}>{translatedValue}</td>;
            })}
        </tr>
    );
};
