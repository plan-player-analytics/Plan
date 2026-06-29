import {useGenericFilter} from "../../dataHooks/genericFilterContextHook";
import {Col, Row} from "react-bootstrap";
import DateInputField from "./DateInputField";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faArrowRight, faChevronLeft, faChevronRight, faFilterCircleXmark} from "@fortawesome/free-solid-svg-icons";
import OutlineButton from "./button/OutlineButton";
import React from "react";
import {staticSite} from "../../service/backendConfiguration";

export const DateFilterControls = () => {
    const {after, setAfter, before, setBefore, reset} = useGenericFilter();

    const diff = 86400000; // 24h

    const add = () => {
        if (after) {
            setAfter(after + diff);
        }
        if (before) {
            setBefore(before + diff);
        }
    }
    const sub = () => {
        if (before) {
            setBefore(before - diff);
        }
        if (after) {
            setAfter(after - diff);
        }
    }

    if (staticSite) return null;

    return (
        <Row className={"mb-4"}>
            <Col md={"auto"}>
                <OutlineButton onClick={sub} variant="input">
                    <FontAwesomeIcon icon={faChevronLeft}/>
                </OutlineButton>
            </Col>
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
                <OutlineButton onClick={add} variant="input">
                    <FontAwesomeIcon icon={faChevronRight}/>
                </OutlineButton>
            </Col>
            <Col md={"auto"}>
                <OutlineButton onClick={reset}>
                    <FontAwesomeIcon icon={faFilterCircleXmark}/>
                </OutlineButton>
            </Col>
        </Row>
    );
}