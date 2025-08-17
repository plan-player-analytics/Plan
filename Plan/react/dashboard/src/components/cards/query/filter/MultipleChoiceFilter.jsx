import React, {useEffect, useState} from 'react';
import {useTranslation} from "react-i18next";
import MultiSelect from "../../../input/MultiSelect";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faTrashAlt} from "@fortawesome/free-regular-svg-icons";
import {Col, Row} from "react-bootstrap";
import OutlineButton from "../../../input/button/OutlineButton.jsx";

const MultipleChoiceFilter = ({index, label, filter, removeFilter, setFilterOptions}) => {
    const {t} = useTranslation();
    const select = index === 0 ? t('html.query.filter.generic.start') : t('html.query.filter.generic.and');

    const parameterSelected = filter.parameters?.selected
        ? JSON.parse(filter.parameters.selected)
            .map(option => filter.options.options.indexOf(option))
        : undefined;

    const [selectedIndexes, setSelectedIndexes] = useState(parameterSelected || []);
    useEffect(() => {
        setFilterOptions({
            ...filter,
            parameters: {
                selected: JSON.stringify(selectedIndexes.map(index => filter.options.options[index]))
            }
        })
    }, [setFilterOptions, selectedIndexes, filter]);

    return (
        <div id={'filter-' + index} className="mt-2">
            <label className="form-label" htmlFor={'filter-' + index}>{select} {t(label)}:</label>
            <Row>
                <Col md={11} className={"flex-fill"}>
                    <MultiSelect options={filter.options.options}
                                 setSelectedIndexes={setSelectedIndexes}
                                 selectedIndexes={selectedIndexes}/>
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

export default MultipleChoiceFilter