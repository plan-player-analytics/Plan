import { isNumber } from '../../util/isNumber';
import { useTimeAmountFormatter } from '../../util/format/useTimeAmountFormatter';

type Props = {
    timeMs: number;
};

const FormattedTime = ({ timeMs }: Props) => {
    const { formatTime } = useTimeAmountFormatter();
    if (!isNumber(timeMs)) return timeMs;
    return formatTime(timeMs);
};

export default FormattedTime;
