import React, {useState} from 'react';
import {InputGroup} from "react-bootstrap";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faClock} from "@fortawesome/free-regular-svg-icons";

const isValidTime = value => {
    if (!value) return true;
    const regex = /^[0-2]\d:[0-5]\d$/;
    return regex.test(value);
};

const correctTime = value => {
    const d = value.match(/^(0\d|\d{2}):?(0\d|\d{2})$/);
    if (!d) return value;
    let hour = Number(d[1]);
    while (hour > 23) hour--;
    let minute = Number(d[2]);
    while (minute > 59) minute--;
    return (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute);
};

const TimeInputField = ({id, setValue, value, placeholder, setAsInvalid, setAsValid}) => {
    const [invalid, setInvalid] = useState(false);

    const onChange = (event) => {
        const value = correctTime(event.target.value);
        const invalid = !isValidTime(value);
        setInvalid(invalid);

        // Value has to change before invalidity events
        // because all-valid fields triggers graph refresh with the current value
        setValue(value);
        if (invalid) {
            setAsInvalid(id);
        } else {
            setAsValid(id);
        }
    }

    return (
        <InputGroup>
            <div className={"input-group-text"}>
                <FontAwesomeIcon icon={faClock}/>
            </div>
            <input type="text" className={"form-control" + (invalid ? " is-invalid" : '')}
                   id={id}
                   placeholder={placeholder}
                   value={value}
                   onChange={onChange}
            />
        </InputGroup>
    )
};

export default TimeInputField