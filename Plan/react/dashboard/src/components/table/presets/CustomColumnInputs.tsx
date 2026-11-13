import { useCallback, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faArrowRight } from '@fortawesome/free-solid-svg-icons';
import DateInputField from '../../input/DateInputField';
import PresetSelect from '../../input/PresetSelect';
import {
    Preset,
    useTimeframePresets,
} from '../../../hooks/timeframePresetsHook';
import { CustomRange, SEPARATOR } from '../../../util/datapointTablePresets';
import {
    BOUNDED_DATE_RANGE_PRESETS,
    DateRangePreset,
} from '../../../util/queryDatePresets';
import { DatapointTimeframe } from './useDatapointTimeframe';

type Props = {
    id: string;
    index: number;
    range: CustomRange;
    timeframe: DatapointTimeframe;
};

const CustomColumnInputs = ({ id, index, range, timeframe }: Props) => {
    const { t } = useTranslation();
    const [resetCount, setResetCount] = useState(0);
    const fieldId = `${id}-custom-${index}`;

    const { setCustomPreset, setFieldValid } = timeframe;

    const applyPreset = useCallback(
        (preset: Preset) => {
            if (preset.name === SEPARATOR) return;
            setCustomPreset(index, preset.name as DateRangePreset);
            setFieldValid(`${fieldId}-from`);
            setFieldValid(`${fieldId}-to`);
            setResetCount((count) => count + 1);
        },
        [setCustomPreset, setFieldValid, index, fieldId],
    );

    const presets = useTimeframePresets({
        defaultPreset: range.preset,
        options: BOUNDED_DATE_RANGE_PRESETS,
        onSelect: applyPreset,
    });

    const setDay = (part: 'from' | 'to') => (value?: string) => {
        presets.resetToCustom();
        timeframe.setCustomDay(index, part, value);
    };

    return (
        <div className='mb-2'>
            <div className='d-flex align-items-center gap-2 mb-1'>
                <label
                    className='form-label mb-0'
                    htmlFor={`${fieldId}-preset`}>
                    {t('html.label.timeframe.column', { number: index + 1 })}
                </label>
                <PresetSelect
                    id={`${fieldId}-preset`}
                    className='ms-auto'
                    presets={presets}
                />
            </div>
            <div className='d-flex align-items-center gap-2'>
                <DateInputField
                    key={`from-${resetCount}`}
                    id={`${fieldId}-from`}
                    type='string'
                    value={range.from}
                    setValue={setDay('from')}
                    rangeEnd={range.to}
                    setAsInvalid={timeframe.setFieldInvalid}
                    setAsValid={timeframe.setFieldValid}
                />
                <FontAwesomeIcon icon={faArrowRight} />
                <DateInputField
                    key={`to-${resetCount}`}
                    id={`${fieldId}-to`}
                    type='string'
                    value={range.to}
                    setValue={setDay('to')}
                    rangeStart={range.from}
                    setAsInvalid={timeframe.setFieldInvalid}
                    setAsValid={timeframe.setFieldValid}
                />
            </div>
        </div>
    );
};

export default CustomColumnInputs;
