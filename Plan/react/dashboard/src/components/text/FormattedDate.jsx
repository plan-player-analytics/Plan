import React from 'react';
import {useDateFormatter} from "../../util/format/useDateFormatter.js";

const FormattedDate = ({date, react, includeSeconds}) => {
    const {formatDate} = useDateFormatter(includeSeconds);

    if (react) {
        return <span title={formatDate(date)}>
            {formatDate(date)}
        </span>
    }

    return formatDate(date);
};

export default FormattedDate