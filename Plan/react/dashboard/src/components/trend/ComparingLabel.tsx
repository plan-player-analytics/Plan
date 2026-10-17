import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCaretDown, faCaretUp} from "@fortawesome/free-solid-svg-icons";
import React, {PropsWithChildren} from "react";

const ComparingLabel = ({children}: PropsWithChildren) => {
    return (<>
        <Fa icon={faCaretUp} className="comparing col-trend-better"/>
        <Fa icon={faCaretDown} className="comparing col-trend-worse" style={{marginLeft: "-0.5rem"}}/>
        {' '}<small>{children}</small>
    </>);
}

export default ComparingLabel;