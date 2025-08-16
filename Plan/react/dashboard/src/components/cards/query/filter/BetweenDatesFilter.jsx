import React, {useEffect, useState} from 'react';
import {useTranslation} from "react-i18next";
import DateInputField from "../../../input/DateInputField";
import TimeInputField from "../../../input/TimeInputField";
import {Col, Row} from "react-bootstrap";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faTrashAlt} from "@fortawesome/free-regular-svg-icons";
import OutlineButton from "../../../input/OutlineButton.jsx";

const BetweenDatesFilter = ({index, label, filter, removeFilter, setFilterOptions, setAsInvalid, setAsValid}) => {
    const {t} = useTranslation();
    const select = index === 0 ? t('html.query.filter.generic.start') : t('html.query.filter.generic.and');

    const options = filter.options;

    const [fromDate, setFromDate] = useState(filter.parameters?.afterDate || options.after[0]);
    const [fromTime, setFromTime] = useState(filter.parameters?.afterTime || options.after[1]);
    const [toDate, setToDate] = useState(filter.parameters?.beforeDate || options.before[0]);
    const [toTime, setToTime] = useState(filter.parameters?.beforeTime || options.before[1]);
    useEffect(() => {
        setFilterOptions({
            ...filter,
            parameters: {
                afterDate: fromDate,
                afterTime: fromTime,
                beforeDate: toDate,
                beforeTime: toTime
            }
        })
    }, [setFilterOptions, fromDate, fromTime, toDate, toTime, filter]);

    return (
        <div id={'filter-' + index} className="mt-2">
            <label>{select} {label}:</label>
            <Row className={"my-2 justify-content-start"}>
                <Col md={3} sm={6}>
                    <DateInputField id={"filter-" + index + "-from-date"}
                                    value={fromDate}
                                    setValue={setFromDate}
                                    placeholder={options.after[0]}
                                    setAsInvalid={setAsInvalid} setAsValid={setAsValid}
                    />
                </Col>
                <Col md={2} sm={6}>
                    <TimeInputField id={"filter-" + index + "-from-time"}
                                    value={fromTime}
                                    setValue={setFromTime}
                                    placeholder={options.after[1]}
                                    setAsInvalid={setAsInvalid} setAsValid={setAsValid}
                    />
                </Col>
                <Col md={1} sm={12} className={"text-center my-1 my-md-2 flex-fill"}>
                    <label htmlFor="inlineFormCustomSelectPref">&</label>
                </Col>
                <Col md={3} sm={6}>
                    <DateInputField id={"filter-" + index + "-to-date"}
                                    value={toDate}
                                    setValue={setToDate}
                                    placeholder={options.before[0]}
                                    setAsInvalid={setAsInvalid} setAsValid={setAsValid}
                    />
                </Col>
                <Col md={2} sm={6}>
                    <TimeInputField id={"filter-" + index + "-to-time"}
                                    value={toTime}
                                    setValue={setToTime}
                                    placeholder={options.before[1]}
                                    setAsInvalid={setAsInvalid} setAsValid={setAsValid}
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