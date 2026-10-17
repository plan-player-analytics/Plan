import {isNumber} from "../../util/isNumber";
import {useDateFormatter} from "../../util/format/useDateFormatter";

type Props = {
    date: number | string | undefined; // TODO more strict type
}

const FormattedDay = ({date}: Props) => {
    const {formatDate} = useDateFormatter(false, {pattern: "MMMMM d"});

    if (date === undefined || date === null) return null;
    if (!isNumber(date)) return date;
    return date === 0 ? '-' : formatDate(date);
};

export default FormattedDay