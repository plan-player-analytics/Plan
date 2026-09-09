import React from 'react';
import LoadIn from "../../components/animation/LoadIn.tsx";
import {Col, Row} from "react-bootstrap";
import QueryOptionsCard from "../../components/cards/query/QueryOptionsCard";
import QueryPath from "../../components/alert/QueryPath";
import {useAuth} from "../../hooks/authenticationHook.tsx";

const NewQueryView = () => {
    const {hasPermission} = useAuth();
    return (
        <LoadIn>
            {hasPermission('access.query') && <section className={"query-options-view"}>
                <Row>
                    <Col md={12}>
                        <QueryPath newQuery/>
                        <QueryOptionsCard/>
                    </Col>
                </Row>
            </section>}
        </LoadIn>
    )
};

export default NewQueryView