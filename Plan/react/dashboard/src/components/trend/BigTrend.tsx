import React, { ReactNode } from 'react';
import { FontAwesomeIcon as Fa } from '@fortawesome/react-fontawesome';
import {
    faCaretDown,
    faCaretRight,
    faCaretUp,
} from '@fortawesome/free-solid-svg-icons';

type Props<T extends string | number> = {
    trend?: {
        text: T;
        direction?: '+' | '-' | null;
        reversed?: boolean;
    } | null;
    format?: (value: T) => ReactNode;
};

type ValueProps = {
    value: ReactNode;
};

const TrendUpGood = ({ value }: ValueProps) => (
    <span className='badge bg-trend-better'>
        <Fa icon={faCaretUp} />
        {value}
    </span>
);
const TrendUpBad = ({ value }: ValueProps) => (
    <span className='badge bg-trend-worse'>
        <Fa icon={faCaretUp} />
        {value}
    </span>
);
const TrendDownBad = ({ value }: ValueProps) => (
    <span className='badge bg-trend-worse'>
        <Fa icon={faCaretDown} />
        {value}
    </span>
);
const TrendDownGood = ({ value }: ValueProps) => (
    <span className='badge bg-trend-better'>
        <Fa icon={faCaretDown} />
        {value}
    </span>
);
const TrendSame = ({ value }: ValueProps) => (
    <span className='badge bg-trend-same'>
        <Fa icon={faCaretRight} />
        {value}
    </span>
);

const BigTrend = <T extends string | number>({ trend, format }: Props<T>) => {
    if (!trend) {
        return <TrendSame value={'?'} />;
    }

    const value = format ? format(trend.text) : trend.text;

    switch (trend.direction) {
        case '+':
            return trend.reversed ? (
                <TrendUpBad value={value} />
            ) : (
                <TrendUpGood value={value} />
            );
        case '-':
            return trend.reversed ? (
                <TrendDownGood value={value} />
            ) : (
                <TrendDownBad value={value} />
            );
        default:
            return <TrendSame value={value} />;
    }
};

export default BigTrend;
