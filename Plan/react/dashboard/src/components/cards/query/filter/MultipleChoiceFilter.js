import React, {useEffect, useState} from 'react';
import {useTranslation} from "react-i18next";
import MultiSelect from "../../../input/MultiSelect";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faTrashAlt} from "@fortawesome/free-regular-svg-icons";
import {Col, Row} from "react-bootstrap";

const MultipleChoiceFilter = ({index, label, filter, removeFilter, setFilterOptions}) => {
    const {t} = useTranslation();
    const select = index === 0 ? t('html.query.filter.generic.start') : t('html.query.filter.generic.and');

    const [selectedIndexes, setSelectedIndexes] = useState([]);
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
            <label className="form-label" htmlFor={'filter-' + index}>{select}{t(label)}:</label>
            <Row>
                <Col md={11} className={"flex-fill"}>
                    <MultiSelect options={filter.options.options}
                                 setSelectedIndexes={setSelectedIndexes}
                                 selectedIndexes={selectedIndexes}/>
                </Col>
                {removeFilter && <Col md={"auto"}>
                    <button className="filter-remover btn btn-outline-secondary float-end"
                            onClick={removeFilter}><FontAwesomeIcon icon={faTrashAlt}/>
                    </button>
                </Col>}
            </Row>
        </div>
    )
};

export default MultipleChoiceFilter