import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faArrowRight } from '@fortawesome/free-solid-svg-icons';
import DateInputField from '../../input/DateInputField';
import { DatapointTimeframe } from './useDatapointTimeframe';
import { CUSTOM_RANGE_PRESETS } from '../../../util/datapointTablePresets';
import { DateRangePreset } from '../../../util/queryDatePresets';

type Props = {
    id: string;
    timeframe: DatapointTimeframe;
};

const CustomTimeframeInputs = ({ id, timeframe }: Props) => {
    const { t } = useTranslation();
    const ranges = timeframe.customRanges || [{}, {}];
    const [resetCounts, setResetCounts] = useState<Record<number, number>>({});

    const selectPreset = (index: number, preset: DateRangePreset) => {
        timeframe.setCustomPreset(index, preset);
        timeframe.setFieldValid(`${id}-custom-${index}-from`);
        timeframe.setFieldValid(`${id}-custom-${index}-to`);
        setResetCounts((previous) => ({
            ...previous,
            [index]: (previous[index] ?? 0) + 1,
        }));
    };

    return (
        <div
            className='px-3 py-2'
            style={{ width: '26rem', maxWidth: 'calc(100vw - 2rem)' }}>
            {ranges.map((range, index) => (
                <div key={index} className='mb-2'>
                    <div className='d-flex align-items-center gap-2 mb-1'>
                        <label
                            className='form-label mb-0'
                            htmlFor={`${id}-custom-${index}-preset`}>
                            {t('html.label.timeframe.column', {
                                number: index + 1,
                            })}
                        </label>
                        <select
                            id={`${id}-custom-${index}-preset`}
                            className='form-select form-select-sm w-auto ms-auto'
                            value={range.preset ?? 'custom'}
                            onChange={(event) =>
                                selectPreset(
                                    index,
                                    event.target.value as DateRangePreset,
                                )
                            }>
                            {CUSTOM_RANGE_PRESETS.map((option) => (
                                <option key={option.name} value={option.name}>
                                    {t(option.label)}
                                </option>
                            ))}
                        </select>
                    </div>
                    <div className='d-flex align-items-center gap-2'>
                        <DateInputField
                            key={`from-${resetCounts[index] ?? 0}`}
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
                            key={`to-${resetCounts[index] ?? 0}`}
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

export default CustomTimeframeInputs;
