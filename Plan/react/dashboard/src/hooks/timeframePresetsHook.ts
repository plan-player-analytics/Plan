import { useCallback, useMemo, useState } from 'react';
import { usePreferences } from './preferences/preferencesHook';
import {
    calculatePresetRange,
    DateRange,
    DateRangePreset,
    ServerTimeZone,
} from '../util/queryDatePresets';
import { SEPARATOR } from '../util/datapointTablePresets';

export type Preset = {
    name: DateRangePreset | typeof SEPARATOR;
    label: string;
    compute: () => DateRange | undefined;
};

export type TimeframePresets = {
    currentPreset: Preset;
    presetOptions: Preset[];
    selectPreset: (name: DateRangePreset | typeof SEPARATOR) => void;
    resetToCustom: () => void;
};

type OptionEntry =
    | { name: DateRangePreset; label: string }
    | { name: typeof SEPARATOR; label: string };

type Props = {
    defaultPreset: DateRangePreset;
    options: OptionEntry[];
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
                    zone && option.name !== SEPARATOR
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
        (name: DateRangePreset | typeof SEPARATOR) => {
            if (name === SEPARATOR) return;
            const preset = presetOptions.find((option) => option.name === name);
            if (!preset) return;
            setCurrent(name as DateRangePreset);
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
