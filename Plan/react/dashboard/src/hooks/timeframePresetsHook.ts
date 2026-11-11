import { useCallback, useMemo, useState } from 'react';
import { usePreferences } from './preferences/preferencesHook';
import {
    calculatePresetRange,
    DateRange,
    DateRangePreset,
    ServerTimeZone,
} from '../util/queryDatePresets';

export type Preset = {
    name: DateRangePreset;
    label: string;
    compute: () => DateRange | undefined;
};

export type TimeframePresets = {
    currentPreset: Preset;
    presetOptions: Preset[];
    selectPreset: (name: DateRangePreset) => void;
    resetToCustom: () => void;
};

type Props = {
    defaultPreset: DateRangePreset;
    options: { name: DateRangePreset; label: string }[];
    zone?: ServerTimeZone;
    onSelect?: (preset: Preset) => void;
};

export const useTimeframePresets = ({
    defaultPreset,
    options,
    zone,
    onSelect,
}: Props): TimeframePresets => {
    const { firstDay } = usePreferences();
    const [current, setCurrent] = useState<DateRangePreset>(defaultPreset);

    const presetOptions = useMemo(
        () =>
            options.map((option) => ({
                ...option,
                compute: () =>
                    zone
                        ? calculatePresetRange(
                              option.name,
                              Date.now(),
                              zone,
                              firstDay ?? 1,
                          )
                        : undefined,
            })),
        [options, zone, firstDay],
    );

    const selectPreset = useCallback(
        (name: DateRangePreset) => {
            const preset = presetOptions.find((option) => option.name === name);
            if (!preset) return;
            setCurrent(name);
            onSelect?.(preset);
        },
        [presetOptions, onSelect],
    );

    const resetToCustom = useCallback(() => setCurrent('custom'), []);

    return useMemo(() => {
        const currentPreset =
            presetOptions.find((option) => option.name === current) ??
            presetOptions[0];

        return { currentPreset, presetOptions, selectPreset, resetToCustom };
    }, [presetOptions, current, selectPreset, resetToCustom]);
};
