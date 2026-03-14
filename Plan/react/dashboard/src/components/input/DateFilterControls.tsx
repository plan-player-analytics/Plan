import {useGenericFilter} from "../../dataHooks/genericFilterContextHook";
import {Col, Row} from "react-bootstrap";
import DateInputField from "./DateInputField";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faArrowRight, faFilterCircleXmark} from "@fortawesome/free-solid-svg-icons";
import OutlineButton from "./button/OutlineButton";
import React from "react";
import {staticSite} from "../../service/backendConfiguration";

export const DateFilterControls = () => {
    const {after, setAfter, before, setBefore, reset} = useGenericFilter();

    if (staticSite) return null;

    return (
        <Row className={"mb-4"}>
            <Col>
                <DateInputField id={"from-date"}
                                value={after}
                                setValue={setAfter}
                                setAsInvalid={() => {
                                }} setAsValid={() => {
                }}
                                disabled={false}
                                rangeEnd={before}
                                type="number"
                />
            </Col>
            <Col md={"auto"} className={"p-0 mt-2"}><FontAwesomeIcon icon={faArrowRight}/></Col>
            <Col>
                <DateInputField id={"to-date"}
                                value={before}
                                setValue={setBefore}
                                setAsInvalid={() => {
                                }} setAsValid={() => {
                }}
                                disabled={false}
                                rangeStart={after}
                                type="number"
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