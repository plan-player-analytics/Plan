import React, {useState} from 'react';

const Checkbox = ({children, className, indeterminate, checked, onChange}) => {
    const [renderTime] = useState(new Date().getTime());
    const id = 'checkbox-' + renderTime;

    return (
        <>
            <input id={id} className={"form-check-input " + className} type={"checkbox"}
                   value={indeterminate ? "" : checked}
                   checked={checked}
                   ref={input => {
                       if (input) input.indeterminate = indeterminate
                   }}
                   onChange={onChange}
            />
            <label className="form-check-label ms-1" htmlFor={id}>{children}</label>
        </>
    )
};

export default Checkbox