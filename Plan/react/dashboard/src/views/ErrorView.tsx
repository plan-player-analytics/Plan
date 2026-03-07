import {Card, Col, Row} from "react-bootstrap";
import React from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faBug} from "@fortawesome/free-solid-svg-icons";
import LoadIn from "../components/animation/LoadIn";
import {useTranslation} from "react-i18next";
import {IconProp} from "@fortawesome/fontawesome-svg-core";

export type PlanError = {
    status?: number;
    title?: string;
    message: string;
    url?: string;
    data?: any
    icon?: IconProp
}

type ErrorViewProps = {
    error: PlanError;
}

export const ErrorViewText = ({error}: ErrorViewProps) => {
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

export const ErrorViewBody = ({error}: ErrorViewProps) => {
    return (
        <Card.Body>
            <ErrorViewText error={error}/>
        </Card.Body>
    )
}

export const ErrorViewCard = ({error}: ErrorViewProps) => {
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

const ErrorView = ({error}: ErrorViewProps) => {
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