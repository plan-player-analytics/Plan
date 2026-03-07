import React, {useEffect, useState} from 'react';
import {useTranslation} from "react-i18next";
import DateInputField from "../../../input/DateInputField";
import TimeInputField from "../../../input/TimeInputField";
import {Col, Row} from "react-bootstrap";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faTrashAlt} from "@fortawesome/free-regular-svg-icons";
import OutlineButton from "../../../input/button/OutlineButton.jsx";
import Checkbox from "../../../input/Checkbox.jsx";
import {InlinedRow} from "../../../layout/InlinedRow.jsx";

const BetweenDatesFilter = ({index, label, filter, removeFilter, setFilterOptions, setAsInvalid, setAsValid}) => {
    const {t} = useTranslation();
    const select = index === 0 ? t('html.query.filter.generic.start') : t('html.query.filter.generic.and');

    const options = filter.options;

    const [fromDateEnabled, setFromDateEnabled] = useState(true);
    const [fromDate, setFromDate] = useState(filter.parameters?.afterDate || options.after[0]);
    const [fromTime, setFromTime] = useState(filter.parameters?.afterTime || options.after[1]);
    const [toDateEnabled, setToDateEnabled] = useState(true);
    const [toDate, setToDate] = useState(filter.parameters?.beforeDate || options.before[0]);
    const [toTime, setToTime] = useState(filter.parameters?.beforeTime || options.before[1]);

    const toggleFromDate = () => {
        if (!toDateEnabled && fromDateEnabled) {
            setToDateEnabled(true);
        }
        setFromDateEnabled(!fromDateEnabled);
    }
    const toggleToDate = () => {
        if (!fromDateEnabled && toDateEnabled) {
            setFromDateEnabled(true);
        }
        setToDateEnabled(!toDateEnabled);
    }

    useEffect(() => {
        const after = fromDateEnabled ? {
            afterDate: fromDate,
            afterTime: fromTime
        } : {};
        const before = toDateEnabled ? {
            beforeDate: toDate,
            beforeTime: toTime
        } : {}
        setFilterOptions({
            ...filter,
            parameters: {...after, ...before}
        })
    }, [setFilterOptions, fromDateEnabled, fromDate, fromTime, toDateEnabled, toDate, toTime, filter]);

    let actualLabel = label;
    if (!toDateEnabled) actualLabel = label.replace('Between', 'After');
    if (!fromDateEnabled) actualLabel = label.replace('Between', 'Before');

    return (
        <div id={'filter-' + index} className="mt-2">
            <label>{select} {t(actualLabel)}:</label>
            <Row className={"my-2 justify-content-start"}>
                <Col md={3} sm={6}>
                    <InlinedRow>
                        <Checkbox checked={fromDateEnabled} onChange={toggleFromDate}/>
                        <DateInputField id={"filter-" + index + "-from-date"}
                                        value={fromDate}
                                        setValue={setFromDate}
                                        placeholder={options.after[0]}
                                        setAsInvalid={setAsInvalid} setAsValid={setAsValid}
                                        disabled={!fromDateEnabled}
                        />
                    </InlinedRow>
                </Col>
                <Col md={2} sm={6}>
                    <TimeInputField id={"filter-" + index + "-from-time"}
                                    value={fromTime}
                                    setValue={setFromTime}
                                    placeholder={options.after[1]}
                                    setAsInvalid={setAsInvalid} setAsValid={setAsValid}
                                    disabled={!fromDateEnabled}
                    />
                </Col>
                <Col md={1} sm={12} className={"text-center my-1 my-md-2 flex-fill"}>
                    <label htmlFor="inlineFormCustomSelectPref">&</label>
                </Col>
                <Col md={3} sm={6}>
                    <InlinedRow>
                        <Checkbox checked={toDateEnabled} onChange={toggleToDate}/>
                        <DateInputField id={"filter-" + index + "-to-date"}
                                        value={toDate}
                                        setValue={setToDate}
                                        placeholder={options.before[0]}
                                        setAsInvalid={setAsInvalid} setAsValid={setAsValid}
                                        disabled={!toDateEnabled}
                        />
                    </InlinedRow>
                </Col>
                <Col md={2} sm={6}>
                    <TimeInputField id={"filter-" + index + "-to-time"}
                                    value={toTime}
                                    setValue={setToTime}
                                    placeholder={options.before[1]}
                                    setAsInvalid={setAsInvalid} setAsValid={setAsValid}
                                    disabled={!toDateEnabled}
                    />
                </Col>
                <Col md={"auto"} sm={12} className={"my-1 my-md-auto"}>
                    <OutlineButton className="filter-remover float-end" onClick={removeFilter}>
                        <FontAwesomeIcon icon={faTrashAlt}/>
                    </OutlineButton>
                </Col>
            </Row>
        </div>
    )
};

export default BetweenDatesFilter