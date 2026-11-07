import { useCallback, useMemo, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useMetadata } from '../../../hooks/metadataHook';
import { usePreferences } from '../../../hooks/preferences/preferencesHook';
import { useNavigation } from '../../../hooks/navigationHook';
import { useI18nFriendlyLanguage } from '../../../service/localeService';
import { useDatapointTimeframeContext } from '../../../hooks/context/datapointTimeframeContext';
import {
    calculateTimeframeColumns,
    CustomRange,
    defaultCustomRanges,
    TimeframeColumn,
    TimeframePreset,
} from '../../../util/datapointTablePresets';
import { Column } from '../QueryDatapointTable';
import { formatPeriodLabel } from './formatPeriodLabel';

export const useDatapointTimeframe = (
    tableId: string,
    defaultPreset: TimeframePreset,
) => {
    const { t } = useTranslation();
    const locale = useI18nFriendlyLanguage();
    const metadata = useMetadata();
    const { firstDay } = usePreferences();
    const { updateRequested } = useNavigation();
    const { timeframes, updateTimeframe } = useDatapointTimeframeContext();
    const stored = timeframes[tableId];
    const preset = stored?.preset ?? defaultPreset;
    const customRanges = stored?.customRanges;
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
        (selected: TimeframePreset) => {
            setInvalidFields([]);
            updateTimeframe(tableId, (previous) =>
                selected === 'custom' && !previous?.customRanges && zone
                    ? {
                          preset: selected,
                          customRanges: defaultCustomRanges(
                              Date.now(),
                              zone,
                              firstDay ?? 1,
                          ),
                      }
                    : { ...previous, preset: selected },
            );
        },
        [updateTimeframe, tableId, zone, firstDay],
    );

    const setCustomDay = useCallback(
        (index: number, part: keyof CustomRange, value?: string) => {
            updateTimeframe(tableId, (previous) => ({
                preset: 'custom',
                customRanges: (previous?.customRanges ?? [{}, {}]).map(
                    (range, i) =>
                        i === index ? { ...range, [part]: value } : range,
                ),
            }));
        },
        [updateTimeframe, tableId],
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

    const calculated = useMemo(
        () =>
            calculateTimeframeColumns(
                preset,
                Date.now(),
                zone,
                firstDay ?? 1,
                customRanges,
            ),
        [preset, customRanges, zone, firstDay, updateRequested],
    );

    const invalidCustom =
        preset === 'custom' && (!calculated || invalidFields.length > 0);
    if (preset === 'custom' && !invalidCustom) {
        lastValidCustom.current = calculated;
    }
    const timeframeColumns = invalidCustom
        ? lastValidCustom.current
        : calculated;

    const columns: Column[] = useMemo(
        () =>
            (timeframeColumns ?? []).map((column) => {
                let label: string | undefined;
                if (column.period) {
                    label = formatPeriodLabel(
                        column.period,
                        locale,
                        t('html.label.now'),
                    );
                } else if (column.label) {
                    label = t(column.label.key) + ' ' + column.label.number;
                    if (column.label.detailKey) {
                        label += ' (' + t(column.label.detailKey) + ')';
                    }
                }
                return { key: column.key, filter: column.filter, label };
            }),
        [timeframeColumns, locale, t],
    );

    return {
        preset,
        selectPreset,
        customRanges,
        setCustomDay,
        setFieldInvalid,
        setFieldValid,
        invalidCustom,
        columns,
    };
};

export type DatapointTimeframe = ReturnType<typeof useDatapointTimeframe>;
