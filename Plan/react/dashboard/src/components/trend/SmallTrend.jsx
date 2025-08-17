import React from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCaretDown, faCaretRight, faCaretUp} from "@fortawesome/free-solid-svg-icons";

const TrendUpGood = ({value}) => <Fa icon={faCaretUp} className="trend col-trend-better" title={value}/>;
const TrendUpBad = ({value}) => <Fa icon={faCaretUp} className="trend col-trend-worse" title={value}/>;
const TrendDownBad = ({value}) => <Fa icon={faCaretDown} className="trend col-trend-worse" title={value}/>;
const TrendDownGood = ({value}) => <Fa icon={faCaretDown} className="trend col-trend-better" title={value}/>;
const TrendSame = ({value}) => <Fa icon={faCaretRight} className="trend col-trend-same" title={value}/>;


const SmallTrend = ({trend}) => {
    if (!trend) {
        return <TrendSame value={'?'}/>;
    }
    switch (trend.direction) {
        case '+':
            return (trend.reversed ? <TrendUpBad value={trend.text}/> : <TrendUpGood value={trend.text}/>);
        case '-':
            return (trend.reversed ? <TrendDownGood value={trend.text}/> : <TrendDownBad value={trend.text}/>);
        default:
            return <TrendSame value={trend.text}/>;
    }
}

export default SmallTrend