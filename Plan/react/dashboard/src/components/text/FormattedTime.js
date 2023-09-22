import React from 'react';
import {usePreferences} from "../../hooks/preferencesHook";
import {formatTimeAmount} from "../../util/format/TimeAmountFormat";

const FormattedTime = ({timeMs}) => {
    const {} = usePreferences();

    const options = {
        YEAR: '1 year, ',
        YEARS: '%years% years, ',
        MONTH: '1 month, ',
        MONTHS: '%months% months, ',
        DAY: '1d, ',
        DAYS: '%days%d, ',
        HOURS: '%zero%%hours%:',
        MINUTES: '%zero%%minutes%:',
        SECONDS: '%zero%%seconds%',
        ZERO: '0s'
    }
    const formatted = formatTimeAmount(options, timeMs);

    return (
        <>{formatted}</>
    )
};

export default FormattedTime