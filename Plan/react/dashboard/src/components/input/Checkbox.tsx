import React, { ChangeEventHandler, PropsWithChildren, useState } from 'react';

type Props = {
    checked: boolean;
    onChange: ChangeEventHandler<HTMLInputElement, HTMLInputElement>;
    indeterminate?: boolean;
    readOnly?: boolean;
    center?: boolean;
    className?: string;
} & PropsWithChildren;

const Checkbox = ({
    children,
    className,
    indeterminate,
    checked,
    onChange,
    center,
    readOnly,
}: Props) => {
    const [renderTime] = useState(Date.now());
    const id = 'checkbox-' + renderTime;

    return (
        <span
            style={
                center
                    ? {
                          display: 'flex',
                          alignItems: 'center',
                          marginTop: '-5px',
                      }
                    : undefined
            }>
            <input
                id={id}
                className={'form-check-input ' + className}
                type={'checkbox'}
                value={indeterminate ? '' : String(checked)}
                checked={checked}
                ref={(input) => {
                    if (input && indeterminate)
                        input.indeterminate = indeterminate;
                }}
                onChange={onChange}
                readOnly={readOnly}
            />
            <label className='form-check-label ms-1' htmlFor={id}>
                {children}
            </label>
        </span>
    );
};

export default Checkbox;
