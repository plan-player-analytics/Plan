import React, { useCallback, useMemo, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Dropdown } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faArrowRight, faGear } from '@fortawesome/free-solid-svg-icons';
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
import {
    DEFAULT_TIMEFRAME,
    StoredTimeframe,
    useStoredTimeframe,
} from '../../hooks/context/datapointTimeframeContext';

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
    const { t } = useTranslation();
    const locale = useI18nFriendlyLanguage();
    const formatter = useMemo(
        () => createFormatter(locale, period.unit),
        [locale, period.unit],
    );
    if (period.unit === 'hour') {
        return (
            formatter.format(period.start) +
            '–' +
            (period.untilNow
                ? t('html.label.now')
                : formatter.format(period.end))
        );
    }
    if (period.unit === 'week' || period.unit === 'range') {
        const lastDay = period.end - DAY_MS;
        if (lastDay > period.start) {
            return formatter.formatRange(period.start, lastDay);
        }
    }
    return formatter.format(period.start);
};

const isDayString = (value: unknown) =>
    value === undefined || typeof value === 'string';

const validCustomRanges = (ranges?: CustomRange[]) =>
    Array.isArray(ranges) &&
    ranges.length === 2 &&
    ranges.every(
        (range) => range && isDayString(range.from) && isDayString(range.to),
    );

export const useDatapointTimeframe = (
    tableId: string,
    presets: TimeframePreset[],
) => {
    const metadata = useMetadata();
    const { firstDay } = usePreferences();
    const { updateRequested } = useNavigation() as {
        updateRequested: number;
    };
    const [stored, setTimeframe] = useStoredTimeframe(tableId);
    const timeframe: StoredTimeframe = useMemo(
        () =>
            staticSite || !presets.includes(stored.preset)
                ? DEFAULT_TIMEFRAME
                : {
                      preset: stored.preset,
                      customRanges: validCustomRanges(stored.customRanges)
                          ? stored.customRanges
                          : undefined,
                  },
        [stored, presets],
    );
    const [invalidFields, setInvalidFields] = useState<string[]>([]);
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

    const selectPreset = useCallback(
        (preset: TimeframePreset) => {
            setInvalidFields([]);
            setTimeframe((previous) =>
                preset === 'custom' && !previous.customRanges && zone
                    ? {
                          preset,
                          customRanges: defaultCustomRanges(
                              Date.now(),
                              zone,
                              firstDay ?? 1,
                          ),
                      }
                    : { ...previous, preset },
            );
        },
        [setTimeframe, zone, firstDay],
    );

    const setCustomDay = useCallback(
        (index: number, part: keyof CustomRange, value?: string) => {
            setTimeframe((previous) => ({
                ...previous,
                customRanges: (previous.customRanges || [{}, {}]).map(
                    (range, i) =>
                        i === index ? { ...range, [part]: value } : range,
                ),
            }));
        },
        [setTimeframe],
    );

    const setFieldInvalid = useCallback((fieldId: string) => {
        setInvalidFields((previous) =>
            previous.includes(fieldId) ? previous : [...previous, fieldId],
        );
    }, []);

    const setFieldValid = useCallback((fieldId: string) => {
        setInvalidFields((previous) =>
            previous.includes(fieldId)
                ? previous.filter((id) => id !== fieldId)
                : previous,
        );
    }, []);

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

    const invalidCustom =
        timeframe.preset === 'custom' &&
        (!calculated || invalidFields.length > 0);
    if (!invalidCustom && timeframe.preset === 'custom' && calculated) {
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
        setFieldInvalid,
        setFieldValid,
        invalidCustom,
        columns,
    };
};

type Timeframe = ReturnType<typeof useDatapointTimeframe>;

const CustomTimeframeInputs = ({
    id,
    timeframe,
}: {
    id: string;
    timeframe: Timeframe;
}) => {
    const { t } = useTranslation();
    const ranges = timeframe.customRanges || [{}, {}];
    return (
        <div
            className='px-3 py-2'
            style={{ width: '26rem', maxWidth: 'calc(100vw - 2rem)' }}>
            {ranges.map((range, index) => (
                <div key={index} className='mb-2'>
                    <label
                        className='form-label mb-1'
                        htmlFor={`${id}-custom-${index}-from`}>
                        {t('html.label.timeframe.column', {
                            number: index + 1,
                        })}
                    </label>
                    <div className='d-flex align-items-center gap-2'>
                        <DateInputField
                            id={`${id}-custom-${index}-from`}
                            type='string'
                            value={range.from}
                            setValue={(value) =>
                                timeframe.setCustomDay(index, 'from', value)
                            }
                            rangeEnd={range.to}
                            setAsInvalid={timeframe.setFieldInvalid}
                            setAsValid={timeframe.setFieldValid}
                        />
                        <FontAwesomeIcon icon={faArrowRight} />
                        <DateInputField
                            id={`${id}-custom-${index}-to`}
                            type='string'
                            value={range.to}
                            setValue={(value) =>
                                timeframe.setCustomDay(index, 'to', value)
                            }
                            rangeStart={range.from}
                            setAsInvalid={timeframe.setFieldInvalid}
                            setAsValid={timeframe.setFieldValid}
                        />
                    </div>
                </div>
            ))}
            {timeframe.invalidCustom && (
                <p className='alert alert-warning mb-0'>
                    {t('html.label.timeframe.invalidRange')}
                </p>
            )}
        </div>
    );
};

export const TimeframeControl = ({
    id,
    timeframe,
}: {
    id: string;
    timeframe: Timeframe;
}) => {
    const { t } = useTranslation();
    const [customOpen, setCustomOpen] = useState(false);
    if (staticSite) return null;
    return (
        <div className='d-flex align-items-center gap-1'>
            <select
                id={id + '-timeframe'}
                className='form-select form-select-sm'
                style={{
                    width: 0,
                    flexGrow: 1,
                    minWidth: '8rem',
                    maxWidth: '16rem',
                }}
                aria-label={t('html.label.timeframe.label')}
                value={timeframe.preset}
                onChange={(event) => {
                    const preset = event.target.value as TimeframePreset;
                    timeframe.selectPreset(preset);
                    setCustomOpen(preset === 'custom');
                }}>
                {TIMEFRAME_PRESETS.filter((option) =>
                    timeframe.presets.includes(option.name),
                ).map((option) => (
                    <option key={option.name} value={option.name}>
                        {t(option.label)}
                    </option>
                ))}
            </select>
            {timeframe.preset === 'custom' && (
                <Dropdown
                    id={id + '-custom'}
                    show={customOpen}
                    onToggle={(next) => setCustomOpen(next)}
                    autoClose='outside'>
                    <Dropdown.Toggle
                        variant={''}
                        size='sm'
                        className={
                            timeframe.invalidCustom
                                ? 'text-warning'
                                : 'col-text'
                        }
                        style={
                            {
                                '--bs-btn-color':
                                    'var(--color-forms-input-text)',
                            } as React.CSSProperties
                        }
                        title={t('html.label.timeframe.custom')}
                        aria-label={t('html.label.timeframe.custom')}>
                        <FontAwesomeIcon icon={faGear} />
                    </Dropdown.Toggle>
                    <Dropdown.Menu>
                        <CustomTimeframeInputs id={id} timeframe={timeframe} />
                    </Dropdown.Menu>
                </Dropdown>
            )}
        </div>
    );
};
