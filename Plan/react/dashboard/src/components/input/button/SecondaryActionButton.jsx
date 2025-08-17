import React from 'react';

const SecondaryActionButton = ({className, onClick, disabled, children}) => {
    return (
        <button className={`btn btn-secondary-action ${className}`} onClick={onClick} disabled={disabled}>
            {children}
        </button>
    )
};

export default SecondaryActionButton