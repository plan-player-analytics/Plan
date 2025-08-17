import React from 'react';

const OutlineButton = ({id, className, disabled, onClick, children, style}) => {
    return (
        <button id={id}
                className={"btn btn-outline-secondary " + className}
                onClick={onClick}
                disabled={disabled}
                style={style}>
            {children}
        </button>
    )
};

export default OutlineButton