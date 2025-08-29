import {Card, Col, Row} from "react-bootstrap";
import React from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faBug} from "@fortawesome/free-solid-svg-icons";
import LoadIn from "../components/animation/LoadIn";
import {useTranslation} from "react-i18next";

export const ErrorViewText = ({error}) => {
    console.error(error);
    return (
        <>
            <p>{error.message} {error.url && <a href={error.url}>{error.url}</a>}</p>
            {error.data && <><br/>
                <pre>{JSON.stringify(error.data)}</pre>
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
    const {t} = useTranslation();
    return (
        <LoadIn>
            <Card>
                <Card.Header>
                    <h6 className="col-text">
                        <Fa icon={error.icon ? error.icon : faBug}/> {error.title || t('html.label.errorInformation')}
                    </h6>
                </Card.Header>
                <ErrorViewBody error={error}/>
            </Card>
        </LoadIn>
    )
}

const ErrorView = ({error}) => {
    return (
        <LoadIn>
            <section className="error_view">
                <Row>
                    <Col lg={12}>
                        <ErrorViewCard error={error}/>
                    </Col>
                </Row>
            </section>
        </LoadIn>
    )
}

export default ErrorView;