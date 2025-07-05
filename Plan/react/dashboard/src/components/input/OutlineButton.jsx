import React from 'react';

const OutlineButton = ({id, className, disabled, onClick, children}) => {
    return (
        <button id={id}
                className={"btn btn-outline-secondary " + className}
                onClick={onClick}
                disabled={disabled}>
            {children}
        </button>
    )
};

export default OutlineButton