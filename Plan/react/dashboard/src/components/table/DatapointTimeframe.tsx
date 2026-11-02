import React, { useCallback, useMemo, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Col, Row } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faArrowRight } from '@fortawesome/free-solid-svg-icons';
import DateInputField from '../input/DateInputField';
import { useMetadata } from '../../hooks/metadataHook';
import { usePreferences } from '../../hooks/preferences/preferencesHook';
import { useNavigation } from '../../hooks/navigationHook';
import { useI18nFriendlyLanguage } from '../../service/localeService';
import { staticSite } from '../../service/backendConfiguration';
import {
    calculateTimeframeColumns,
    CustomRange,
    defaultCustomRanges,
    PeriodUnit,
    TIMEFRAME_PRESETS,
    TimeframeColumn,
    TimeframePreset,
} from '../../util/datapointTablePresets';
import { Column } from './QueryDatapointTable';

const DAY_MS = 24 * 60 * 60 * 1000;

const FORMAT_OPTIONS: Record<PeriodUnit, Intl.DateTimeFormatOptions> = {
    hour: { hour: '2-digit', minute: '2-digit', hourCycle: 'h23' },
    day: { weekday: 'short', day: 'numeric', month: 'short' },
    week: { day: 'numeric', month: 'short' },
    month: { month: 'long', year: 'numeric' },
    range: { day: 'numeric', month: 'short', year: 'numeric' },
};

const createFormatter = (locale: string, unit: PeriodUnit) => {
    const options = { ...FORMAT_OPTIONS[unit], timeZone: 'UTC' };
    try {
        return new Intl.DateTimeFormat(locale, options);
    } catch {
        return new Intl.DateTimeFormat('en', options);
    }
};

export const PeriodLabel = ({ period }: Pick<TimeframeColumn, 'period'>) => {
    const locale = useI18nFriendlyLanguage();
    const formatter = useMemo(
        () => createFormatter(locale, period.unit),
        [locale, period.unit],
    );
    if (period.unit === 'week' || period.unit === 'range') {
        const lastDay = period.end - DAY_MS;
        if (lastDay > period.start) {
            return formatter.formatRange(period.start, lastDay);
        }
    }
    return formatter.format(period.start);
};

type StoredTimeframe = {
    preset: TimeframePreset;
    customRanges?: CustomRange[];
};

const storageKey = (tableId: string) => 'datapointTimeframe.' + tableId;

const isDayString = (value: unknown) =>
    value === undefined || typeof value === 'string';

const loadStored = (
    tableId: string,
    presets: TimeframePreset[],
): StoredTimeframe => {
    if (staticSite) return { preset: 'default' };
    try {
        const stored = JSON.parse(
            globalThis.localStorage.getItem(storageKey(tableId)) || '{}',
        );
        if (!presets.includes(stored.preset)) return { preset: 'default' };
        const customRanges =
            Array.isArray(stored.customRanges) &&
            stored.customRanges.length === 2 &&
            stored.customRanges.every(
                (range: CustomRange) =>
                    range && isDayString(range.from) && isDayString(range.to),
            )
                ? stored.customRanges
                : undefined;
        return { preset: stored.preset, customRanges };
    } catch {
        return { preset: 'default' };
    }
};

const store = (tableId: string, timeframe: StoredTimeframe) => {
    try {
        globalThis.localStorage.setItem(
            storageKey(tableId),
            JSON.stringify(timeframe),
        );
    } catch {
        return;
    }
};

export const useDatapointTimeframe = (
    tableId: string,
    presets: TimeframePreset[],
) => {
    const metadata = useMetadata();
    const { firstDay } = usePreferences();
    const { updateRequested } = useNavigation() as {
        updateRequested: number;
    };
    const [timeframe, setTimeframe] = useState<StoredTimeframe>(() =>
        loadStored(tableId, presets),
    );
    const lastValidCustom = useRef<TimeframeColumn[] | undefined>(undefined);

    const zone = useMemo(
        () =>
            metadata.loaded
                ? {
                      timeZone: metadata.timeZoneId,
                      timeZoneOffsetMs:
                          -metadata.timeZoneOffsetMinutes * 60 * 1000,
                  }
                : undefined,
        [metadata],
    );

    const update = useCallback(
        (next: StoredTimeframe) => {
            setTimeframe(next);
            store(tableId, next);
        },
        [tableId],
    );

    const selectPreset = useCallback(
        (preset: TimeframePreset) => {
            if (preset === 'custom' && !timeframe.customRanges && zone) {
                update({
                    preset,
                    customRanges: defaultCustomRanges(
                        Date.now(),
                        zone,
                        firstDay ?? 1,
                    ),
                });
            } else {
                update({ ...timeframe, preset });
            }
        },
        [timeframe, zone, firstDay, update],
    );

    const setCustomDay = useCallback(
        (index: number, part: keyof CustomRange, value?: string) => {
            const customRanges = (timeframe.customRanges || [{}, {}]).map(
                (range, i) =>
                    i === index ? { ...range, [part]: value } : range,
            );
            update({ ...timeframe, customRanges });
        },
        [timeframe, update],
    );

    const calculated = useMemo(() => {
        if (timeframe.preset === 'default' || !zone) return undefined;
        return calculateTimeframeColumns(
            timeframe.preset,
            Date.now(),
            zone,
            firstDay ?? 1,
            timeframe.customRanges,
        );
    }, [timeframe, zone, firstDay, updateRequested]);

    const invalidCustom = timeframe.preset === 'custom' && !calculated;
    if (timeframe.preset === 'custom' && calculated) {
        lastValidCustom.current = calculated;
    }
    const timeframeColumns = invalidCustom
        ? lastValidCustom.current
        : calculated;

    const columns: Column[] | undefined = useMemo(() => {
        if (timeframe.preset === 'default') return undefined;
        if (!timeframeColumns) return [];
        return timeframeColumns.map((column) => ({
            key: column.key,
            filter: column.filter,
            label: <PeriodLabel period={column.period} />,
        }));
    }, [timeframe.preset, timeframeColumns]);

    return {
        preset: timeframe.preset,
        presets,
        selectPreset,
        customRanges: timeframe.customRanges,
        setCustomDay,
        invalidCustom,
        columns,
    };
};

type Timeframe = ReturnType<typeof useDatapointTimeframe>;

export const TimeframeSelect = ({
    id,
    timeframe,
}: {
    id: string;
    timeframe: Timeframe;
}) => {
    const { t } = useTranslation();
    if (staticSite) return null;
    return (
        <select
            id={id + '-timeframe'}
            className='form-select form-select-sm w-auto d-inline-block float-end'
            aria-label={t('html.label.timeframe.label')}
            value={timeframe.preset}
            onChange={(event) =>
                timeframe.selectPreset(event.target.value as TimeframePreset)
            }>
            {TIMEFRAME_PRESETS.filter((option) =>
                timeframe.presets.includes(option.name),
            ).map((option) => (
                <option key={option.name} value={option.name}>
                    {t(option.label)}
                </option>
            ))}
        </select>
    );
};

export const CustomTimeframeInputs = ({
    id,
    timeframe,
}: {
    id: string;
    timeframe: Timeframe;
}) => {
    const { t } = useTranslation();
    if (staticSite || timeframe.preset !== 'custom') return null;
    const ranges = timeframe.customRanges || [{}, {}];
    return (
        <div className='px-3 pt-2'>
            {ranges.map((range, index) => (
                <Row key={index} className='mb-2 align-items-center'>
                    <Col md={2}>
                        <label htmlFor={`${id}-custom-${index}-from`}>
                            {t('html.label.timeframe.column', {
                                number: index + 1,
                            })}
                        </label>
                    </Col>
                    <Col md={4}>
                        <DateInputField
                            id={`${id}-custom-${index}-from`}
                            type='string'
                            value={range.from}
                            setValue={(value) =>
                                timeframe.setCustomDay(index, 'from', value)
                            }
                            rangeEnd={range.to}
                            setAsInvalid={() => {}}
                            setAsValid={() => {}}
                        />
                    </Col>
                    <Col md={'auto'} className='p-0'>
                        <FontAwesomeIcon icon={faArrowRight} />
                    </Col>
                    <Col md={4}>
                        <DateInputField
                            id={`${id}-custom-${index}-to`}
                            type='string'
                            value={range.to}
                            setValue={(value) =>
                                timeframe.setCustomDay(index, 'to', value)
                            }
                            rangeStart={range.from}
                            setAsInvalid={() => {}}
                            setAsValid={() => {}}
                        />
                    </Col>
                </Row>
            ))}
            {timeframe.invalidCustom && (
                <p className='alert alert-warning mb-2'>
                    {t('html.label.timeframe.invalidRange')}
                </p>
            )}
        </div>
    );
};
