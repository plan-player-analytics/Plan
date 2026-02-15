import React, {useEffect, useState} from 'react';
import {useTranslation} from "react-i18next";
import MultiSelect from "../../../input/MultiSelect";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faTrashAlt} from "@fortawesome/free-regular-svg-icons";
import {Col, Row} from "react-bootstrap";
import OutlineButton from "../../../input/button/OutlineButton.jsx";
import DateInputField from "../../../input/DateInputField.jsx";
import TimeInputField from "../../../input/TimeInputField.jsx";

const MultipleChoiceOnDateFilter = ({
                                        index,
                                        label,
                                        filter,
                                        removeFilter,
                                        setFilterOptions,
                                        setAsInvalid,
                                        setAsValid
                                    }) => {
    const {t} = useTranslation();
    const select = index === 0 ? t('html.query.filter.generic.start') : t('html.query.filter.generic.and');

    const parameterSelected = filter.parameters?.selected
        ? JSON.parse(filter.parameters.selected)
            .map(option => filter.options.options.indexOf(option))
        : undefined;
    const options = filter.options;

    const [selectedIndexes, setSelectedIndexes] = useState(parameterSelected || []);
    const [date, setDate] = useState(filter.parameters?.date || options.before[0]);
    const [time, setTime] = useState(filter.parameters?.time || options.before[1]);
    useEffect(() => {
        setFilterOptions({
            ...filter,
            parameters: {
                selected: JSON.stringify(selectedIndexes.map(index => filter.options.options[index])),
                date,
                time
            }
        })
    }, [setFilterOptions, selectedIndexes, date, time, filter]);

    return (
        <div id={'filter-' + index} className="mt-2">
            <label className="form-label" htmlFor={'filter-' + index}>{select} {t(label)}:</label>
            <Row>
                <Col md={5} className={"flex-fill"}>
                    <MultiSelect options={filter.options.options}
                                 setSelectedIndexes={setSelectedIndexes}
                                 selectedIndexes={selectedIndexes}/>
                </Col>
                <Col md={1} sm={12}>{t('html.query.filter.onDate')}</Col>
                <Col md={3} sm={6}>
                    <DateInputField id={"filter-" + index + "-date"}
                                    value={date}
                                    setValue={setDate}
                                    placeholder={options.before[0]}
                                    setAsInvalid={setAsInvalid} setAsValid={setAsValid}
                    />
                </Col>
                <Col md={2} sm={6}>
                    <TimeInputField id={"filter-" + index + "-time"}
                                    value={time}
                                    setValue={setTime}
                                    placeholder={options.before[1]}
                                    setAsInvalid={setAsInvalid} setAsValid={setAsValid}
                    />
                </Col>
                <Col md={"auto"}>
                    <OutlineButton className={"filter-remover float-end"} onClick={removeFilter}>
                        <FontAwesomeIcon icon={faTrashAlt}/>
                    </OutlineButton>
                </Col>
            </Row>
        </div>
    )
};

export default MultipleChoiceOnDateFilter