import React, {useState} from 'react';
import {InputGroup} from "react-bootstrap";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faSearch} from "@fortawesome/free-solid-svg-icons";

const SearchField = ({id, className, setValue, value, placeholder, setAsInvalid, setAsValid, caseSensitive}) => {
    const [invalid] = useState(false);

    const onChange = (event) => {
        const invalid = false;
        // Value has to change before invalidity events
        // because all-valid fields triggers graph refresh with the current value
        setValue(caseSensitive ? event.target.value : event.target.value.toLowerCase());
        if (invalid && setAsInvalid) {
            setAsInvalid(id);
        } else if (setAsValid) {
            setAsValid(id);
        }
    }

    return (
        <InputGroup className={className}>
            <div className={"input-group-text"}>
                <FontAwesomeIcon icon={faSearch}/>
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

export default SearchField