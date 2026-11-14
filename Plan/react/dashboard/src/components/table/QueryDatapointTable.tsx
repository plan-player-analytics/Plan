import React from 'react';
import { GenericFilter } from '../../dataHooks/model/GenericFilter';
import {
    DatapointType,
    NumericDatapointType,
} from '../../dataHooks/model/datapoint/Datapoint';
import { FontAwesomeIcon as Fa } from '@fortawesome/react-fontawesome';
import { useTheme } from '../../hooks/themeHook';
import { IconProp } from '@fortawesome/fontawesome-svg-core';
import {
    calculatePermission,
    QueryDatapointTrend,
    QueryDatapointValue,
} from '../datapoint/QueryDatapoint';
import FormattedDay from '../text/FormattedDay';
import { useAuth } from '../../hooks/authenticationHook';
import { useTranslation } from 'react-i18next';
import { MS_24H, MS_MONTH, MS_WEEK } from '../../util/format/useDateFormatter';
import {
    ALL_TIMEFRAME_PRESETS,
    TimeframePreset,
} from '../../util/datapointTablePresets';
import { useDatapointTimeframe } from './presets/useDatapointTimeframe';
import TimeframeControl from './presets/TimeframeControl';
import { SEPARATOR } from '../../util/presetSeparator';

export type Column = {
    filter: GenericFilter;
    label?: string;
    key: string;
};

type Row = {
    dataType: DatapointType;
    permission?: string;
    icon: IconProp | IconProp[];
    key?: string;
    text: string | React.ReactNode;
    color: string | string[];
    filter?: GenericFilter;
    bold?: boolean;
    title?: string;
    hidden?: boolean;
    indent?: boolean;
    boldBottom?: boolean;
    trendDownGood?: boolean;
};

type Props = {
    id: string;
    defaultPreset: TimeframePreset;
    presets?: (TimeframePreset | typeof SEPARATOR)[];
    filter: GenericFilter;
    rows: Row[];
    showTrend?: boolean;
};

const FilterHeader = ({ filter, label }: Column) => {
    const { t } = useTranslation();
    if (label) {
        return <th>{label}</th>;
    }
    if (filter.afterMillisAgo) {
        if (filter.beforeMillisAgo === undefined) {
            if (filter.afterMillisAgo === MS_MONTH)
                return <th>{t('html.label.last30days')}</th>;
            if (filter.afterMillisAgo === MS_WEEK)
                return <th>{t('html.label.last7days')}</th>;
            if (filter.afterMillisAgo === MS_24H)
                return <th>{t('html.label.last24hours')}</th>;
        }

        return (
            <th>
                <FormattedDay date={Date.now() - filter.afterMillisAgo} /> -{' '}
                <FormattedDay
                    date={Date.now() - (filter.beforeMillisAgo || 0)}
                />
            </th>
        );
    }
    if (filter.after) {
        return (
            <th>
                <FormattedDay date={filter.after} /> -{' '}
                <FormattedDay date={filter.before || Date.now()} />
            </th>
        );
    }
    return null;
};

export const QueryDatapointTable = ({
    id,
    defaultPreset,
    presets = ALL_TIMEFRAME_PRESETS,
    filter,
    rows,
    showTrend,
}: Props) => {
    const { t } = useTranslation();
    const timeframe = useDatapointTimeframe(id, defaultPreset);
    const { columns } = timeframe;
    const { nightModeEnabled } = useTheme();
    const { hasPermission } = useAuth();
    const trendShown = showTrend && columns.length === 2;

    return (
        <table
            className={
                'table table-striped' + (nightModeEnabled ? ' table-dark' : '')
            }>
            <thead className={'sticky-top'}>
                <tr>
                    <th>
                        <TimeframeControl
                            id={id}
                            presets={presets}
                            timeframe={timeframe}
                        />
                    </th>
                    {columns.map((column) => (
                        <FilterHeader
                            key={column.key}
                            filter={column.filter}
                            label={column.label}
                        />
                    ))}
                    {trendShown && <th>{t('html.label.trend')}</th>}
                </tr>
            </thead>
            <tbody>
                {rows
                    .filter((row) => !row.hidden)
                    .filter((row) =>
                        hasPermission(
                            calculatePermission(row.dataType, filter),
                        ),
                    )
                    .map((row) => (
                        <tr
                            key={row.key || String(row.text)}
                            title={row.title}
                            style={
                                row.boldBottom
                                    ? { borderBottomWidth: '3px' }
                                    : undefined
                            }>
                            <td>
                                {(() => {
                                    const icons = Array.isArray(row.icon)
                                        ? row.icon
                                        : [row.icon];
                                    const colors = Array.isArray(row.color)
                                        ? row.color
                                        : [row.color];
                                    return icons.map((icon, idx) => (
                                        <Fa
                                            key={idx}
                                            icon={icon as IconProp}
                                            className={
                                                'col-' +
                                                (colors[idx] || colors[0])
                                            }
                                            style={
                                                idx === 0 && row.indent
                                                    ? { marginLeft: '1rem' }
                                                    : idx > 0
                                                      ? {
                                                            marginLeft:
                                                                '0.25rem',
                                                        }
                                                      : undefined
                                            }
                                        />
                                    ));
                                })()}{' '}
                                {row.text}
                            </td>
                            {columns.map((column) => (
                                <td key={column.key + row.text}>
                                    <QueryDatapointValue
                                        dataType={row.dataType}
                                        filter={{
                                            ...filter,
                                            ...column.filter,
                                            ...row.filter,
                                        }}
                                        noDataFallback={'-'}
                                    />
                                </td>
                            ))}
                            {trendShown && (
                                <td>
                                    <QueryDatapointTrend
                                        filter={{
                                            ...filter,
                                            ...columns[0].filter,
                                        }}
                                        filter2={{
                                            ...filter,
                                            ...columns[1].filter,
                                        }}
                                        dataType={
                                            row.dataType as NumericDatapointType
                                        }
                                        downGood={row.trendDownGood}
                                    />
                                </td>
                            )}
                        </tr>
                    ))}
            </tbody>
        </table>
    );
};
