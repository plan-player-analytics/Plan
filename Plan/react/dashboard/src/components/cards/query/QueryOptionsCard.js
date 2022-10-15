import React, {useState} from 'react';
import {Card, Col, Row} from "react-bootstrap-v5";
import {useTranslation} from "react-i18next";
import {useDataRequest} from "../../../hooks/dataFetchHook";
import {fetchFilters} from "../../../service/queryService";
import {ErrorViewCard} from "../../../views/ErrorView";
import {ChartLoader} from "../../navigation/Loader";
import DateInputField from "../../input/DateInputField";
import TimeInputField from "../../input/TimeInputField";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faSearch} from "@fortawesome/free-solid-svg-icons";

const QueryOptionsCard = () => {
    const {t} = useTranslation();

    const [fromDate, setFromDate] = useState(undefined);
    const [fromTime, setFromTime] = useState(undefined);
    const [toDate, setToDate] = useState(undefined);
    const [toTime, setToTime] = useState(undefined);

    const [invalidFields, setInvalidFields] = useState([]);
    const setAsInvalid = id => setInvalidFields([...invalidFields, id]);
    const setAsValid = id => setInvalidFields(invalidFields.filter(invalid => id !== invalid));

    const {data: options, loadingError} = useDataRequest(fetchFilters, []);

    if (loadingError) return <ErrorViewCard error={loadingError}/>
    if (!options) return (<Card>
        <Card.Body>
            <ChartLoader/>
        </Card.Body>
    </Card>)

    const view = options.view;

    return (
        <Card>
            <Card.Body>
                <label>{t('html.query.label.view')}</label>
                <Row className={"my-2 justify-content-start justify-content-md-center"}>
                    <Col className={"my-2"}>
                        <label>{t('html.query.label.from')
                            .replace('</label>', '')
                            .replace('>', '')}</label>
                    </Col>
                    <Col md={3}>
                        <DateInputField id={"viewFromDateField"}
                                        value={fromDate}
                                        setValue={setFromDate}
                                        placeholder={view.afterDate}
                                        setAsInvalid={setAsInvalid} setAsValid={setAsValid}
                        />
                    </Col>
                    <Col md={2}>
                        <TimeInputField id={"viewFromTimeField"}
                                        value={fromTime}
                                        setValue={setFromTime}
                                        placeholder={view.afterTime}
                                        setAsInvalid={setAsInvalid} setAsValid={setAsValid}
                        />
                    </Col>
                    <Col md={1} className={"my-2 text-center"}>
                        <label>{t('html.query.label.to')
                            .replace('</label>', '')
                            .replace('>', '')}</label>
                    </Col>
                    <Col md={3}>
                        <DateInputField id={"viewToDateField"}
                                        value={toDate}
                                        setValue={setToDate}
                                        placeholder={view.beforeDate}
                                        setAsInvalid={setAsInvalid} setAsValid={setAsValid}
                        />
                    </Col>
                    <Col md={2}>
                        <TimeInputField id={"viewToTimeField"}
                                        value={toTime}
                                        setValue={setToTime}
                                        placeholder={view.beforeTime}
                                        setAsInvalid={setAsInvalid} setAsValid={setAsValid}
                        />
                    </Col>
                </Row>
            </Card.Body>
            <button id={"query-button"} className={"btn bg-plan m-2"} disabled={Boolean(invalidFields.length)}>
                <FontAwesomeIcon icon={faSearch}/> {t('html.query.performQuery')}
            </button>
        </Card>
    )
};

export default QueryOptionsCard