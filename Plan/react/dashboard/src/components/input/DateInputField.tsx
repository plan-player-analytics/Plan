import React, {useMemo, useState} from 'react';
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faCalendar} from "@fortawesome/free-regular-svg-icons";
import "react-datepicker/dist/react-datepicker.css";
import "./DateInputField.css";
import DatePicker from "react-datepicker";
import {InputGroup} from "react-bootstrap";
import {useDateFormatter} from "../../util/format/useDateFormatter";
import {InlinedRow} from "../layout/InlinedRow";
import OutlineButton from "./button/OutlineButton";
import {faChevronDown, faTimes} from "@fortawesome/free-solid-svg-icons";
import {useMetadata} from "../../hooks/metadataHook";

const isValidDate = (value: string) => {
    if (!value) return true;
    const d = new RegExp(/^(0\d|\d{2})[/|-]?(0\d|\d{2})[/|-]?(\d{4,5})$/).exec(value);
    if (!d) return false;
    // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/Date
    const parsedDay = Number(d[1]);
    const parsedMonth = Number(d[2]) - 1; // 0=January, 11=December
    const parsedYear = Number(d[3]);
    return new Date(parsedYear, parsedMonth, parsedDay);
};

const correctDate = (value: string) => {
    const ddmmyyyy = new RegExp(/^(0\d|\d{2})[/|-]?(0\d|\d{2})[/|-]?(\d{4,5})$/).exec(value);
    const yyyymmdd = new RegExp(/^(\d{4,5})[/|-](0\d|\d{2})[/|-]?(0\d|\d{2})$/).exec(value);
    if (!ddmmyyyy && !yyyymmdd) return value;

    // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/Date
    let date = null;
    if (ddmmyyyy) {
        const day = Number(ddmmyyyy[1]);
        const month = Number(ddmmyyyy[2]) - 1; // 0=January, 11=December
        const year = Number(ddmmyyyy[3]);
        date = new Date(year, month, day);
    }
    if (yyyymmdd) {
        const year = Number(yyyymmdd[1]);
        const month = Number(yyyymmdd[2]) - 1; // 0=January, 11=December
        const day = Number(yyyymmdd[3]);
        date = new Date(year, month, day);
    }
    if (!date) return value;

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

const parseAsDate = (value: string | undefined) => {
    if (!value) return undefined;
    const d = new RegExp(/^(0\d|\d{2})[/|-]?(0\d|\d{2})[/|-]?(\d{4,5})$/).exec(value);
    if (!d) return undefined;
    const year = d[3];
    const month = d[2];
    const day = d[1];
    const dateString = `${year}-${month}-${day}T00:00:00+0000`;
    return new Date(dateString);
}

const dateToString = (value: Date | undefined) => {
    if (!value) return undefined;
    const year = value.getFullYear();
    const month = String(value.getMonth() + 1).padStart(2, "0");
    const day = String(value.getDate()).padStart(2, "0");
    return `${day}/${month}/${year}`;
}

type ValueSetFunction = | {
    value?: number;
    setValue: (value: number | undefined) => void;
    type: 'number';
    rangeEnd?: number;
    rangeStart?: number;
} | {
    value?: string;
    setValue: (value: string | undefined) => void;
    type: 'string';
    rangeEnd?: string;
    rangeStart?: string;
}

type Props = {
    id: string,
    placeholder?: string,
    setAsInvalid: (id: string) => void,
    setAsValid: (id: string) => void,
    disabled?: boolean,
} & ValueSetFunction

type DatePickerHeaderProps = {
    date: Date;
    decreaseMonth: () => void;
    increaseMonth: () => void;
    prevMonthButtonDisabled: boolean;
    nextMonthButtonDisabled: boolean;
}

const DateInputField = (
    {
        id,
        setValue,
        value,
        placeholder,
        setAsInvalid,
        setAsValid,
        disabled,
        rangeStart,
        rangeEnd,
        type
    }: Props
) => {
    const [invalid, setInvalid] = useState(false);
    const [picker, setPicker] = useState<string | undefined>(undefined);
    const {formatDate: formatAsMonth} = useDateFormatter(false, {pattern: "MMMM"});
    const {formatDate: formatAsYear} = useDateFormatter(false, {pattern: "yyyy"});
    const {timeZoneOffsetMinutes} = useMetadata() as { timeZoneOffsetMinutes: number };

    const onChange = (newValue: Date | null) => {
        if (picker) {
            setPicker(undefined);
            return;
        }
        if (newValue) {
            if (type === 'string') {
                setValue(dateToString(newValue));
            } else {
                setValue(newValue.getTime());
            }
            setAsValid(id);
        } else {
            setValue(undefined);
            setAsValid(id);
        }
    }

    const onRawChange = (event: any) => {
        if (!event.target.value) return;
        const corrected = correctDate(event.target.value);
        const valid = isValidDate(corrected);

        if (valid && valid instanceof Date) {
            if (type === 'string') {
                setValue(corrected);
            } else {
                setValue(valid.getTime());
            }
            setAsValid(id);
            setInvalid(false);
        } else if (!valid) {
            setAsInvalid(id);
            setInvalid(true);
        }
    }

    const asDate = useMemo(() => {
        if (!value) return undefined;
        if (type === 'string') return parseAsDate(value);
        return new Date(value)
    }, [value])

    const getDayClassName = (date: Date): string => {
        if ((!rangeEnd && !rangeStart) || (rangeStart && rangeEnd) || !asDate) return ''
        const rangeEndAsNumber = type === 'string' ? parseAsDate(rangeEnd)?.getTime() : rangeEnd;
        if (rangeEndAsNumber) {
            return date.getTime() > asDate.getTime() && date.getTime() < rangeEndAsNumber ? "in-range" : "";
        }
        const rangeStartAsNumber = type === 'string' ? parseAsDate(rangeStart)?.getTime() : rangeStart;
        if (rangeStartAsNumber) {
            return date.getTime() < asDate.getTime() && date.getTime() > rangeStartAsNumber ? "in-range" : "";
        }
        return value && date.getTime() > asDate.getTime() ? "in-range" : "";
    };
    return (
        <InputGroup>
            <div className={"input-group-text"}>
                <FontAwesomeIcon icon={faCalendar}/>
            </div>
            <DatePicker dateFormat="dd/MM/yyyy"
                        selected={asDate}
                        onChange={onChange}
                        onChangeRaw={onRawChange}
                        disabled={disabled}
                        placeholderText={placeholder || "dd/mm/yyyy"}
                        className={"form-control" + (invalid ? " is-invalid" : '')}
                        shouldCloseOnSelect={false}
                        showMonthYearPicker={picker === 'month'}
                        showYearPicker={picker === 'year'}
                        timeZone='UTC'
                        allowSameDay
                        dayClassName={getDayClassName}
                        renderCustomHeader={({
                                                 date,
                                                 decreaseMonth,
                                                 increaseMonth,
                                                 prevMonthButtonDisabled,
                                                 nextMonthButtonDisabled
                                             }: DatePickerHeaderProps) => {
                            return (
                                <InlinedRow justifyContent={"space-between"}>
                                    <OutlineButton onClick={decreaseMonth} disabled={prevMonthButtonDisabled}>
                                        <FontAwesomeIcon icon={"chevron-left"}/>
                                    </OutlineButton>
                                    <OutlineButton
                                        onClick={() => {
                                            setPicker(picker === 'month' ? undefined : 'month');
                                        }}>
                                        {formatAsMonth(date.getTime())}
                                        <FontAwesomeIcon icon={picker === 'month' ? faTimes : faChevronDown}
                                                         className={"ms-1"}/>
                                    </OutlineButton>
                                    <OutlineButton onClick={increaseMonth} disabled={nextMonthButtonDisabled}>
                                        <FontAwesomeIcon icon={"chevron-right"}/>
                                    </OutlineButton>
                                    <OutlineButton
                                        onClick={() => {
                                            setPicker(picker === 'year' ? undefined : 'year');
                                        }}>
                                        {formatAsYear(date.getTime())}
                                        <FontAwesomeIcon icon={picker === 'year' ? faTimes : faChevronDown}
                                                         className={"ms-1"}/>
                                    </OutlineButton>
                                </InlinedRow>
                            )
                        }}
                        isClearable
            />
            {/*<input type="text" className={"form-control" + (invalid ? " is-invalid" : '')}*/}
            {/*       id={id}*/}
            {/*       placeholder={placeholder}*/}
            {/*       value={value}*/}
            {/*       onChange={onChange}*/}
            {/*       disabled={disabled}*/}
            {/*/>*/}
        </InputGroup>
    )
};

export default DateInputField