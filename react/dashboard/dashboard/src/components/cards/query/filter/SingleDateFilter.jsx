import {useTranslation} from "react-i18next";
import React, {useEffect, useState} from "react";
import {Col, Row} from "react-bootstrap";
import DateInputField from "../../../input/DateInputField.jsx";
import TimeInputField from "../../../input/TimeInputField.jsx";
import OutlineButton from "../../../input/button/OutlineButton.jsx";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faTrashAlt} from "@fortawesome/free-regular-svg-icons";

const SingleDateFilter = ({index, label, filter, removeFilter, setFilterOptions, setAsInvalid, setAsValid}) => {
    const {t} = useTranslation();
    const select = index === 0 ? t('html.query.filter.generic.start') : t('html.query.filter.generic.and');

    const options = filter.options;

    const [fromDate, setFromDate] = useState(filter.parameters?.afterDate || options.before[0]);
    const [fromTime, setFromTime] = useState(filter.parameters?.afterTime || options.before[1]);

    useEffect(() => {
        setFilterOptions({
            ...filter,
            parameters: {
                afterDate: fromDate,
                afterTime: fromTime
            }
        })
    }, [setFilterOptions, fromDate, fromTime, filter]);

    return (
        <div id={'filter-' + index} className="mt-2">
            <label>{select} {t(label)}:</label>
            <Row className={"my-2 justify-content-start"}>
                <Col md={3} sm={6}>
                    <DateInputField id={"filter-" + index + "-date"}
                                    value={fromDate}
                                    setValue={setFromDate}
                                    placeholder={options.before[0]}
                                    setAsInvalid={setAsInvalid} setAsValid={setAsValid}
                    />
                </Col>
                <Col md={2} sm={6}>
                    <TimeInputField id={"filter-" + index + "-time"}
                                    value={fromTime}
                                    setValue={setFromTime}
                                    placeholder={options.before[1]}
                                    setAsInvalid={setAsInvalid} setAsValid={setAsValid}
                    />
                </Col>
                <Col md={"auto"} sm={12} className={"my-1 my-md-auto ms-auto"}>
                    <OutlineButton className="filter-remover float-end" onClick={removeFilter}>
                        <FontAwesomeIcon icon={faTrashAlt}/>
                    </OutlineButton>
                </Col>
            </Row>
        </div>
    )
};

export default SingleDateFilter