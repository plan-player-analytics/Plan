import React from 'react';

const OpaqueText = ({children, inline, floatEnd}) => {
    if (inline) {
        return <span className={"opaque-text" + (floatEnd ? " float-end" : "")}>{children}</span>
    }

    return (
        <p className={"opaque-text" + (floatEnd ? " float-end" : "")}>{children}</p>
    )
};

export default OpaqueText