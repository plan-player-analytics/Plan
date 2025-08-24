import React from 'react';
import {SimpleDateFormat} from "../../util/format/SimpleDateFormat";
import {useMetadata} from "../../hooks/metadataHook";
import {isNumber} from "../../util/isNumber.js";

const FormattedDay = ({date}) => {
    const {timeZoneOffsetHours} = useMetadata();

    if (date === undefined || date === null) return <></>;
    if (!isNumber(date)) return date;

    const pattern = "MMMMM d";

    const offset = timeZoneOffsetHours * 60 * 60 * 1000;
    const timestamp = date - offset;

    const formatted = date !== 0 ? new SimpleDateFormat(pattern).format(timestamp) : '-';

    return (
        <>{formatted}</>
    )
};

export default FormattedDay