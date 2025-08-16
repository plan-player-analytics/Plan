import React from 'react';

const DangerButton = ({id, className, disabled, onClick, children}) => {
    return (
        <button id={id}
                className={"btn btn-danger " + className}
                onClick={onClick}
                disabled={disabled}>
            {children}
        </button>
    )
};

export default DangerButton