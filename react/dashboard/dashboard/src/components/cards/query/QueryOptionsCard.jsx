import React, {useCallback, useEffect, useState} from 'react';
import {Card, Col, Row} from "react-bootstrap";
import {useTranslation} from "react-i18next";
import {useDataRequest} from "../../../hooks/dataFetchHook";
import {fetchFilters, postQuery} from "../../../service/queryService";
import {ErrorViewCard} from "../../../views/ErrorView";
import {ChartLoader} from "../../navigation/Loader";
import DateInputField from "../../input/DateInputField";
import TimeInputField from "../../input/TimeInputField";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faGear, faSearch} from "@fortawesome/free-solid-svg-icons";
import PlayersOnlineGraph from "../../graphs/PlayersOnlineGraph";
import Highcharts from "highcharts/highstock";
import "highcharts/modules/no-data-to-display"
import "highcharts/modules/accessibility";
import MultiSelect from "../../input/MultiSelect";
import CollapseWithButton from "../../layout/CollapseWithButton";
import FilterDropdown from "./FilterDropdown";
import FilterList from "./FilterList";
import {useQueryResultContext} from "../../../hooks/queryResultContext";
import {useNavigate} from "react-router-dom";
import {useNavigation} from "../../../hooks/navigationHook.jsx";
import ActionButton from "../../input/button/ActionButton.jsx";

const parseTime = (dateString, timeString) => {
    const d = dateString.match(
        /^(0\d|\d{2})[/|-]?(0\d|\d{2})[/|-]?(\d{4,5})$/
    );
    const t = timeString.match(/^(0\d|\d{2}):?(0\d|\d{2})$/);

    // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/Date
    const parsedDay = Number(d[1]);
    const parsedMonth = Number(d[2]) - 1; // 0=January, 11=December
    const parsedYear = Number(d[3]);
    let hour = Number(t[1]);
    let minute = Number(t[2]);
    const date = new Date(parsedYear, parsedMonth, parsedDay, hour, minute);
    return date.getTime() - (date.getTimezoneOffset() * 60000);
};

const QueryOptionsCard = () => {
    const {t} = useTranslation();
    const navigate = useNavigate()
    const {result, setResult} = useQueryResultContext();
    const {setCurrentTab} = useNavigation();

    const [loadingResults, setLoadingResults] = useState(false);

    // View state
    const [fromDate, setFromDate] = useState(undefined);
    const [fromTime, setFromTime] = useState(undefined);
    const [toDate, setToDate] = useState(undefined);
    const [toTime, setToTime] = useState(undefined);

    const [selectedServers, setSelectedServers] = useState([]);
    const [filters, setFilters] = useState([]);

    // View & filter data
    const {data: options, loadingError} = useDataRequest(fetchFilters, []);

    const [graphData, setGraphData] = useState(undefined);
    useEffect(() => {
        if (options) {
            setGraphData({playersOnline: options.viewPoints, color: '#9E9E9E'})
        }
    }, [options, setGraphData]);

    // View state handling
    const [invalidFields, setInvalidFields] = useState([]);
    const setAsInvalid = useCallback(id => setInvalidFields([...invalidFields, id]), [setInvalidFields, invalidFields]);
    const setAsValid = useCallback(id => setInvalidFields(invalidFields.filter(invalid => id !== invalid)), [setInvalidFields, invalidFields]);

    const [extremes, setExtremes] = useState(undefined);
    /*eslint-disable react-hooks/exhaustive-deps */
    // Because: Don't update when any of the date/time fields change because that would lead to infinite loop
    const updateExtremes = useCallback(() => {
        if (invalidFields.length || !options) return;
        if (!fromDate && !fromTime && !toDate && !toTime) return;

        setExtremes({
            min: parseTime(
                fromDate || options.view.afterDate,
                fromTime || options.view.afterTime
            ),
            max: parseTime(
                toDate || options.view.beforeDate,
                toTime || options.view.beforeTime
            )
        });
    }, [invalidFields, options]);
    /* eslint-enable react-hooks/exhaustive-deps */
    useEffect(updateExtremes, [updateExtremes]);

    const onSetExtremes = useCallback((event) => {
        if (event?.trigger) {
            const afterDate = Highcharts.dateFormat('%d/%m/%Y', event.min);
            const afterTime = Highcharts.dateFormat('%H:%M', event.min);
            const beforeDate = Highcharts.dateFormat('%d/%m/%Y', event.max);
            const beforeTime = Highcharts.dateFormat('%H:%M', event.max);
            setFromDate(afterDate);
            setFromTime(afterTime);
            setToDate(beforeDate);
            setToTime(beforeTime);
        }
    }, [setFromTime, setFromDate, setToTime, setToDate]);

    useEffect(() => {
        if (!options?.view || !result?.view) return;

        setSelectedServers(result.view.servers?.map(server => options.view.servers?.findIndex(s => s.serverUUID === server.serverUUID))?.filter(i => i !== -1) || [])

        setFromDate(result.view.afterDate);
        setFromTime(result.view.afterTime);
        setToDate(result.view.beforeDate);
        setToTime(result.view.beforeTime);

        const existingFilters = result.filters;
        existingFilters.forEach(f => f.options = options.filters.find(fo => fo.kind === f.kind).options);
        setFilters(existingFilters);

        setExtremes({
            min: parseTime(
                result.view.afterDate || options.view.afterDate,
                result.view.afterTime || options.view.afterTime
            ),
            max: parseTime(
                result.view.beforeDate || options.view.beforeDate,
                result.view.beforeTime || options.view.beforeTime
            )
        });
        setCurrentTab('html.query.label.editQuery')
    }, [options, result, setFromDate, setFromTime, setToDate, setToTime, setFilters, setSelectedServers, setExtremes]);

    const getServerSelectorMessage = () => {
        const selected = selectedServers.length;
        const available = options.view.servers.length;
        if (selected === 0 || selected === available) {
            return t('html.query.label.servers.all');
        } else if (selected === 1) {
            return t('html.query.label.servers.single');
        } else if (selected === 2) {
            return t('html.query.label.servers.two');
        } else {
            return t('html.query.label.servers.many').replace('{number}', selected);
        }
    }

    const performQuery = async () => {
        const inputDto = {
            view: {
                afterDate: fromDate || options.view.afterDate,
                afterTime: fromTime || options.view.afterTime,
                beforeDate: toDate || options.view.beforeDate,
                beforeTime: toTime || options.view.beforeTime,
                servers: selectedServers.map(index => options.view.servers[index])
            },
            filters
        }

        // TODO handle error
        setLoadingResults(true);
        const {data} = await postQuery(inputDto);
        setLoadingResults(false);
        setResult(data);
        window.scrollTo(0, 0);
        if (data?.data) {
            navigate('../result?timestamp=' + data.timestamp);
        }
    }

    if (loadingError) return <ErrorViewCard error={loadingError}/>
    if (!options) return (<Card>
        <Card.Body>
            <ChartLoader/>
        </Card.Body>
    </Card>)

    const view = options?.view;

    return (
        <Card>
            <Card.Body>
                <label>{t('html.query.label.view')}</label>
                <Row className={"my-2 justify-content-start justify-content-md-center"}>
                    <Col className={"my-2"}>
                        <label>{t('html.query.label.from')}</label>
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
                        <label>{t('html.query.label.to')}</label>
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
                <Row>
                    <Col md={12}>
                        <PlayersOnlineGraph
                            data={graphData}
                            selectedRange={3}
                            extremes={extremes}
                            onSetExtremes={onSetExtremes}
                        />
                    </Col>
                </Row>
                <Row>
                    <Col md={12}>
                        <CollapseWithButton title={getServerSelectorMessage()}>
                            <MultiSelect options={view.servers.map(server => server.serverName)}
                                         selectedIndexes={selectedServers}
                                         setSelectedIndexes={setSelectedServers}/>
                        </CollapseWithButton>
                    </Col>
                </Row>
                <hr style={{marginBottom: 0}}/>
                <Row>
                    <Col md={12}>
                        <FilterList filters={filters} setFilters={setFilters}
                                    setAsInvalid={setAsInvalid} setAsValid={setAsValid}/>
                    </Col>
                </Row>
                <Row>
                    <Col md={12}>
                        <FilterDropdown filterOptions={options.filters} filters={filters} setFilters={setFilters}/>
                    </Col>
                </Row>
            </Card.Body>
            <ActionButton id={"query-button"} className={"m-2"}
                          disabled={Boolean(invalidFields.length) || loadingResults}
                          onClick={performQuery}>
                <FontAwesomeIcon icon={loadingResults ? faGear : faSearch}
                                 spin={loadingResults}/> {t('html.query.performQuery')}
            </ActionButton>
        </Card>
    )
};

export default QueryOptionsCard