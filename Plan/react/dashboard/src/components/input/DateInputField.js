import React, {useState} from 'react';
import {InputGroup} from "react-bootstrap";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faCalendar} from "@fortawesome/free-regular-svg-icons";

const isValidDate = value => {
    if (!value) return true;
    const d = value.match(
        /^(0\d|\d{2})[/|-]?(0\d|\d{2})[/|-]?(\d{4,5})$/
    );
    if (!d) return false;
    // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/Date
    const parsedDay = Number(d[1]);
    const parsedMonth = Number(d[2]) - 1; // 0=January, 11=December
    const parsedYear = Number(d[3]);
    return new Date(parsedYear, parsedMonth, parsedDay);
};

const correctDate = value => {
    const d = value.match(
        /^(0\d|\d{2})[/|-]?(0\d|\d{2})[/|-]?(\d{4,5})$/
    );
    if (!d) return value;

    // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/Date
    const parsedDay = Number(d[1]);
    const parsedMonth = Number(d[2]) - 1; // 0=January, 11=December
    const parsedYear = Number(d[3]);
    const date = d ? new Date(parsedYear, parsedMonth, parsedDay) : null;

    const day = `${date.getDate()}`;
    const month = `${date.getMonth() + 1}`;
    const year = `${date.getFullYear()}`;
    return (
        (day.length === 1 ? `0${day}` : day) +
        "/" +
        (month.length === 1 ? `0${month}` : month) +
        "/" +
        year
    );
};

const DateInputField = ({id, setValue, value, placeholder, setAsInvalid, setAsValid}) => {
    const [invalid, setInvalid] = useState(false);

    const onChange = (event) => {
        const value = correctDate(event.target.value);
        const invalid = !isValidDate(value);
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
                <FontAwesomeIcon icon={faCalendar}/>
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

export default DateInputField