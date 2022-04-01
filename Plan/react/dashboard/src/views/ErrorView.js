import {Card, Col, Row} from "react-bootstrap-v5";
import React from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faBug} from "@fortawesome/free-solid-svg-icons";

export const ErrorViewText = ({error}) => {
    return (
        <>
            <p>{error.message} {error.url && <a href={error.url}>{error.url}</a>}</p>
            {error.data && <><br/>
                <pre>{error.data}</pre>
            </>}
        </>
    )
}

export const ErrorViewBody = ({error}) => {
    return (
        <Card.Body>
            <ErrorViewText error={error}/>
        </Card.Body>
    )
}

export const ErrorViewCard = ({error}) => {
    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa icon={error.icon ? error.icon : faBug}/> Error information
                </h6>
            </Card.Header>
            <ErrorViewBody error={error}/>
        </Card>
    )
}

const ErrorView = ({error}) => {
    return (
        <section className="error_view">
            <Row>
                <Col lg={12}>
                    <ErrorViewCard error={error}/>
                </Col>
            </Row>
        </section>
    )
}

export default ErrorView;