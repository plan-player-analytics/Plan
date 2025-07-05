import React from 'react';

const Checkbox = ({className, indeterminate, checked, onChange}) => {
    return (
        <input className={"form-check-input " + className} type={"checkbox"} value={indeterminate ? "" : checked}
               checked={checked}
               ref={input => {
                   if (input) input.indeterminate = indeterminate
               }}
               onChange={onChange}
        />
    )
};

export default Checkbox