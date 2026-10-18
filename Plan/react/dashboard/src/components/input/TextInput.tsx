import React, { ChangeEvent, KeyboardEvent, useEffect, useState } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { InputGroup } from 'react-bootstrap';
import { IconProp } from '@fortawesome/fontawesome-svg-core';

type Props = {
    value: string;
    setValue: (value: string) => void;
    icon: IconProp;
    placeholder?: string;
    id?: string;
    isInvalid?: (value: string) => boolean;
    invalidFeedback?: string;
    disabled?: boolean;
    disabledFeedback?: string;
};

const TextInput = ({
    value,
    setValue,
    icon,
    placeholder,
    id,
    isInvalid,
    invalidFeedback,
    disabled,
    disabledFeedback,
}: Props) => {
    const [cachedValue, setCachedValue] = useState(value);
    const [invalid, setInvalid] = useState(false);
    const onChange = (event: ChangeEvent<HTMLInputElement>) => {
        const newValue = event.target.value;
        setCachedValue(newValue);
        setInvalid(isInvalid ? isInvalid(newValue) : false);
    };

    const onBlur = () => {
        setValue(cachedValue);
    };

    const onKeyDown = (event: KeyboardEvent<HTMLInputElement>) => {
        if (event.key === 'Enter') {
            setValue(cachedValue);
        }
    };

    useEffect(() => {
        if (value !== cachedValue) setCachedValue(value);
    }, [value]);

    return (
        <InputGroup id={id}>
            <div className={'input-group-text'}>
                <FontAwesomeIcon icon={icon} />
            </div>
            <input
                type='text'
                className={'form-control' + (invalid ? ' is-invalid' : '')}
                placeholder={placeholder}
                value={cachedValue}
                onChange={onChange}
                onBlur={onBlur}
                onKeyDown={onKeyDown}
                disabled={disabled}
            />
            {invalid && invalidFeedback && (
                <div className='invalid-feedback'>{invalidFeedback}</div>
            )}
            {disabled && disabledFeedback && (
                <div className='disabled-feedback'>{disabledFeedback}</div>
            )}
        </InputGroup>
    );
};

export default TextInput;
