import React, {useState} from 'react';
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {InputGroup} from "react-bootstrap";

const TextInput = ({
                       id,
                       icon,
                       invalidFeedback,
                       placeholder,
                       value,
                       setValue,
                       isInvalid,
                       disabled,
                       disabledFeedback
                   }) => {
    const [cachedValue, setCachedValue] = useState(value);
    const [invalid, setInvalid] = useState(false);
    const onChange = event => {
        const newValue = event.target.value;
        setCachedValue(newValue);
        setInvalid(isInvalid ? isInvalid(newValue) : false);
    }

    const onBlur = () => {
        setValue(cachedValue);
    }

    const onKeyDown = event => {
        if (event.key === 'Enter') {
            setValue(cachedValue);
        }
    }

    return (
        <InputGroup id={id}>
            <div className={"input-group-text"}>
                <FontAwesomeIcon icon={icon}/>
            </div>
            <input type="text" className={"form-control" + (invalid ? " is-invalid" : '')}
                   placeholder={placeholder}
                   value={cachedValue}
                   onChange={onChange}
                   onBlur={onBlur}
                   onKeyDown={onKeyDown}
                   disabled={disabled}
            />
            {invalid && invalidFeedback && <div className="invalid-feedback">
                {invalidFeedback}
            </div>}
            {disabled && disabledFeedback && <div className="disabled-feedback">
                {disabledFeedback}
            </div>}
        </InputGroup>
    )
};

export default TextInput;