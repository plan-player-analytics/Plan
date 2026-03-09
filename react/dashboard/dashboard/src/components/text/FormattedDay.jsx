import {isNumber} from "../../util/isNumber.js";
import {useDateFormatter} from "../../util/format/useDateFormatter.js";

const FormattedDay = ({date}) => {
    const {formatDate} = useDateFormatter(false, {pattern: "MMMMM d"});

    if (date === undefined || date === null) return null;
    if (!isNumber(date)) return date;
    return date === 0 ? '-' : formatDate(date);
};

export default FormattedDay