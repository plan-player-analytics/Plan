import {useGenericFilter} from "../../dataHooks/genericFilterContextHook";
import Highcharts from "highcharts/highstock";
import {Col, Row} from "react-bootstrap";
import DateInputField from "./DateInputField";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faArrowRight, faFilterCircleXmark} from "@fortawesome/free-solid-svg-icons";
import OutlineButton from "./button/OutlineButton";
import React, {useMemo} from "react";
import {staticSite} from "../../service/backendConfiguration";

export const DateFilterControls = () => {
    const {after, setAfter, before, setBefore, reset} = useGenericFilter();

    const start = useMemo(() => after ? Highcharts.dateFormat('%d/%m/%Y', after) : undefined, [after]);
    const end = useMemo(() => before ? Highcharts.dateFormat('%d/%m/%Y', before) : undefined, [before]);

    if (staticSite) return null;

    return (
        <Row className={"mb-4"}>
            <Col>
                <DateInputField id={"from-date"}
                                value={start}
                                setValue={setAfter}
                                placeholder={"yyyy/mm/dd"}
                                setAsInvalid={() => {
                                }} setAsValid={() => {
                }}
                                disabled={false}
                />
            </Col>
            <Col md={"auto"} className={"p-0 mt-2"}><FontAwesomeIcon icon={faArrowRight}/></Col>
            <Col>
                <DateInputField id={"to-date"}
                                value={end}
                                setValue={setBefore}
                                placeholder={"yyyy/mm/dd"}
                                setAsInvalid={() => {
                                }} setAsValid={() => {
                }}
                                disabled={false}
                />
            </Col>
            <Col md={"auto"}>
                <OutlineButton onClick={reset}>
                    <FontAwesomeIcon icon={faFilterCircleXmark}/>
                </OutlineButton>
            </Col>
        </Row>
    );
}