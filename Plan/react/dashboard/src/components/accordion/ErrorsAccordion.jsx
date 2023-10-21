import React from 'react';
import Accordion from "./Accordion";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faBug} from "@fortawesome/free-solid-svg-icons";
import {faClock} from "@fortawesome/free-regular-svg-icons";

const ErrorBody = ({error}) => {
    return (<pre className="pre-scrollable" style={{overflowX: "scroll"}}>
                {error.contents.map((line) => <>{line}<br/></>)}
            </pre>)
}

const ErrorHeader = ({error}) => {
    return <>
        <td>{error.fileName}</td>
        <td>{error.contents.length ? error.contents[0] : '{Empty file}'}</td>
    </>
}

const ErrorsAccordion = ({errors}) => {
    const headers = [
        <><FontAwesomeIcon icon={faBug}/> Logfile</>,
        <><FontAwesomeIcon icon={faClock}/> Occurrences</>
    ];
    const slices = errors ? errors.map(error => {
        return {
            body: <ErrorBody error={error}/>,
            header: <ErrorHeader error={error}/>,
            color: 'orange',
            outline: true
        }
    }) : [];

    return (
        <Accordion headers={headers} slices={slices} open={false} style={{tableLayout: "fixed"}}/>
    )
};

export default ErrorsAccordion