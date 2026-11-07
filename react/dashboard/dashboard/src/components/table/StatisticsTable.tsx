import { ServerStatistics } from '../../dataHooks/model/ServerStatistics';
import { useTranslation } from 'react-i18next';
import { useTheme } from '../../hooks/themeHook';
import React, { ReactElement, useState } from 'react';
import { useMetadata } from '../../hooks/metadataHook';
import {
    formatStatisticName,
    getStatisticsCategory,
    useStatisticFormatter,
} from '../../util/format/useStatisticFormatter';
import { Card, InputGroup } from 'react-bootstrap';
import CardHeader from '../cards/CardHeader';
import MultiSelect from '../input/MultiSelect';
import {
    faArrowUpRightDots,
    faServer,
} from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { CardLoader } from '../navigation/Loader';
import { classNames } from '../../util/classNames';
import { Server } from '../../hooks/model/Metadata';

type Props = {
    statistics: ServerStatistics[];
    showAllByDefault?: boolean;
    hideFilter?: boolean;
    tableColor?: string;
    icon?: ReactElement;
    splitter?: string;
};

export const StatisticsTableCard = ({
    statistics,
    hideFilter,
    ...props
}: Props) => {
    const { t } = useTranslation();

    const metadata = useMetadata();
    if (!metadata.loaded) return null;
    if (!statistics) return <CardLoader />;

    const servers = metadata.networkMetadata?.servers;

    const serverUUIDs = statistics.map((s) => s.serverUUID);
    const serversWithData = serverUUIDs
        .map((serverUUID) =>
            serverUUID === null
                ? {
                      serverUUID: 'network',
                      serverName: t('html.label.networkOverview'),
                      proxy: true,
                  }
                : servers?.find((ser) => ser.serverUUID === serverUUID),
        )
        .filter((server) => server !== undefined);

    const [selectedOptions, setSelectedOptions] = useState<number[]>([]);
    return (
        <Card>
            <CardHeader
                icon={faArrowUpRightDots}
                color={''}
                label={'html.label.statistics'}>
                {!hideFilter && (
                    <InputGroup
                        className={'float-end'}
                        style={{
                            margin: '-0.7rem',
                            width: 'unset',
                            maxWidth: '80%',
                        }}>
                        <div className={'input-group-text'}>
                            <FontAwesomeIcon icon={faServer} />
                        </div>
                        <MultiSelect
                            options={serversWithData.map(
                                (server) => server.serverName,
                            )}
                            selectedIndexes={selectedOptions}
                            setSelectedIndexes={setSelectedOptions}
                        />
                    </InputGroup>
                )}
            </CardHeader>
            <StatisticsTable
                {...props}
                statistics={statistics}
                selectedOptions={selectedOptions}
                serversWithData={serversWithData}
                serverUUIDs={serverUUIDs}
            />
        </Card>
    );
};

type TableProps = {
    selectedOptions: number[];
    serversWithData: Server[];
    serverUUIDs: string[];
} & Props;

export const StatisticsTable = ({
    statistics,
    showAllByDefault,
    tableColor,
    icon,
    splitter,
    selectedOptions,
    serversWithData,
    serverUUIDs,
}: TableProps) => {
    const { t } = useTranslation();
    const { nightModeEnabled } = useTheme();
    const formatStatistic = useStatisticFormatter();

    const statisticsKeySet = new Set<string>();
    statistics.forEach((s) =>
        Object.keys(s.statistics).forEach((key) => statisticsKeySet.add(key)),
    );
    const statisticsKeys = [...statisticsKeySet].sort((a, b) =>
        a.localeCompare(b),
    );

    const grouped = Object.entries(
        Object.groupBy(statisticsKeys, getStatisticsCategory),
    );
    const filterFunction = (_: any, index: number) =>
        (showAllByDefault && !selectedOptions.length) ||
        selectedOptions.includes(index);
    const serverCount = serverUUIDs.filter((_, index) =>
        filterFunction(_, index),
    ).length;

    return (
        <table
            className={
                'table table-striped' + (nightModeEnabled ? ' table-dark' : '')
            }>
            <thead
                className={classNames('sticky-top', {
                    [`${tableColor}`]: !!tableColor,
                })}>
                <tr>
                    <th style={{ width: '12rem' }}>
                        {icon} {t('html.label.statistic')}
                    </th>
                    {serversWithData
                        .filter((_, index) => filterFunction(_, index))
                        .map((server) => (
                            <th key={server.serverUUID}>{server.serverName}</th>
                        ))}
                </tr>
            </thead>
            <tbody>
                {grouped
                    .filter(() => serverCount)
                    .map((group) => (
                        <React.Fragment key={group[0]}>
                            <tr>
                                <td className={'sticky-top'}>
                                    <b>{formatStatisticName(group[0])}</b>
                                </td>
                                <td
                                    colSpan={
                                        serverUUIDs.filter((_, index) =>
                                            filterFunction(_, index),
                                        ).length
                                    }
                                />
                            </tr>
                            {group[1]?.map((key) => (
                                <tr key={key} title={key}>
                                    <td>
                                        <div style={{ marginLeft: '1rem' }}>
                                            {formatStatisticName(key, splitter)}
                                        </div>
                                    </td>
                                    {serverUUIDs
                                        .filter((_, index) =>
                                            filterFunction(_, index),
                                        )
                                        .map((serverUUID, index) => (
                                            <td key={key + serverUUID}>
                                                {formatStatistic(
                                                    key,
                                                    statistics[index]
                                                        .statistics[key],
                                                )}
                                            </td>
                                        ))}
                                </tr>
                            ))}
                        </React.Fragment>
                    ))}
                {!!(statisticsKeys.length && serverCount > 1) && (
                    <tr key='server-select'>
                        <td>{t('html.label.selectSomeServer')}</td>
                    </tr>
                )}
                {!statisticsKeys.length && (
                    <tr key='no-data'>
                        <td>{t('generic.noData')}</td>
                    </tr>
                )}
            </tbody>
        </table>
    );
};
