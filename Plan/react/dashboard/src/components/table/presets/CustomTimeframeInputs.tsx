import { useTranslation } from 'react-i18next';
import { DatapointTimeframe } from './useDatapointTimeframe';
import CustomColumnInputs from './CustomColumnInputs';
import { EMPTY_CUSTOM_RANGES } from '../../../util/datapointTablePresets';

type Props = {
    id: string;
    timeframe: DatapointTimeframe;
};

const CustomTimeframeInputs = ({ id, timeframe }: Props) => {
    const { t } = useTranslation();
    const ranges = timeframe.customColumns || EMPTY_CUSTOM_RANGES;
    return (
        <div
            className='px-3 py-2'
            style={{ width: '26rem', maxWidth: 'calc(100vw - 2rem)' }}>
            {ranges.map((range, index) => (
                <CustomColumnInputs
                    key={index}
                    id={id}
                    index={index}
                    range={range}
                    timeframe={timeframe}
                />
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
