import { useTranslation } from 'react-i18next';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faArrowRight } from '@fortawesome/free-solid-svg-icons';
import DateInputField from '../../input/DateInputField';
import { DatapointTimeframe } from './useDatapointTimeframe';

type Props = {
    id: string;
    timeframe: DatapointTimeframe;
};

const CustomTimeframeInputs = ({ id, timeframe }: Props) => {
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

export default CustomTimeframeInputs;
