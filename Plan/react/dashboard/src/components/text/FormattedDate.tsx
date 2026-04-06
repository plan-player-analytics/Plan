import React from 'react';
import {useDateFormatter} from "../../util/format/useDateFormatter.js";

type Props = {
    date: number;
    react?: boolean;
    includeSeconds?: boolean;
}

const FormattedDate = ({date, react, includeSeconds}: Props) => {
    const {formatDate} = useDateFormatter(includeSeconds);

    if (react) {
        return <span title={formatDate(date)}>
            {formatDate(date)}
        </span>
    }

    return formatDate(date);
};

export default FormattedDate