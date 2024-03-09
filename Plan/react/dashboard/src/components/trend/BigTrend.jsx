import React from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCaretDown, faCaretRight, faCaretUp} from "@fortawesome/free-solid-svg-icons";

const TrendUpGood = ({value}) => <span className="badge bg-success"><Fa icon={faCaretUp}/>{value}</span>;
const TrendUpBad = ({value}) => <span className="badge bg-danger"><Fa icon={faCaretUp}/>{value}</span>;
const TrendDownBad = ({value}) => <span className="badge bg-danger"><Fa icon={faCaretDown}/>{value}</span>;
const TrendDownGood = ({value}) => <span className="badge bg-success"><Fa icon={faCaretDown}/>{value}</span>;
const TrendSame = ({value}) => <span className="badge bg-warning"><Fa icon={faCaretRight}/>{value}</span>;


const BigTrend = ({trend, format}) => {
    if (!trend) {
        return <TrendSame value={'?'}/>;
    }

    const value = format ? format(trend.text) : trend.text;

    switch (trend.direction) {
        case '+':
            return (trend.reversed ? <TrendUpBad value={trend.text}/> : <TrendUpGood value={value}/>);
        case '-':
            return (trend.reversed ? <TrendDownGood value={trend.text}/> : <TrendDownBad value={value}/>);
        default:
            return <TrendSame value={value}/>;
    }
}

export default BigTrend