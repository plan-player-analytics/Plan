import React, { PropsWithChildren, useState } from 'react';

type Props = {
    value: boolean;
    onValueChange: (value: boolean) => void;
    color?: string;
    inline?: boolean;
    readOnly?: boolean;
    className?: string;
} & PropsWithChildren;

const Toggle = ({
    children,
    value,
    onValueChange,
    color,
    inline,
    className,
    readOnly,
}: Props) => {
    const [renderTime] = useState(Date.now());
    const id = 'checkbox-' + renderTime;

    const handleChange = () => {
        onValueChange(!value);
    };

    return (
        <div
            className={`form-check form-switch ${className}`}
            style={inline ? { display: 'inline-block' } : {}}>
            <input
                id={id}
                type={'checkbox'}
                className={'form-check-input bg-' + color}
                role='switch'
                onChange={handleChange}
                checked={value}
                readOnly={readOnly}
            />
            <label className='form-check-label' htmlFor={id}>
                {children}
            </label>
        </div>
    );
};

export default Toggle;
