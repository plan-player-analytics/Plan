import React from 'react';
import { Trend } from '../../dataHooks/model/datapoint/Trend';
import { FontAwesomeIcon as Fa } from '@fortawesome/react-fontawesome';
import {
    faCaretDown,
    faCaretRight,
    faCaretUp,
} from '@fortawesome/free-solid-svg-icons';

type Props = {
    trend?: Trend | null;
    format?: (value: string) => string;
};

type ValueProps = {
    value: string;
};

const TrendUpGood = ({ value }: ValueProps) => (
    <span title={value}>
        <Fa icon={faCaretUp} className='trend col-trend-better' />
    </span>
);
const TrendUpBad = ({ value }: ValueProps) => (
    <span title={value}>
        <Fa icon={faCaretUp} className='trend col-trend-worse' />
    </span>
);
const TrendDownBad = ({ value }: ValueProps) => (
    <span title={value}>
        <Fa icon={faCaretDown} className='trend col-trend-worse' />
    </span>
);
const TrendDownGood = ({ value }: ValueProps) => (
    <span title={value}>
        <Fa icon={faCaretDown} className='trend col-trend-better' />
    </span>
);
const TrendSame = ({ value }: ValueProps) => (
    <span title={value}>
        <Fa icon={faCaretRight} className='trend col-trend-same' />
    </span>
);

const SmallTrend = ({ trend, format }: Props) => {
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

export default SmallTrend;
