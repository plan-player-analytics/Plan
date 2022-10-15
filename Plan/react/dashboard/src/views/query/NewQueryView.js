import React from 'react';
import LoadIn from "../../components/animation/LoadIn";
import {Col, Row} from "react-bootstrap-v5";
import QueryOptionsCard from "../../components/cards/query/QueryOptionsCard";

const NewQueryView = () => {
    return (
        <LoadIn>
            <section className={"query-options-view"}>
                <Row>
                    <Col md={12}>
                        <QueryOptionsCard/>
                    </Col>
                </Row>
            </section>
        </LoadIn>
    )
};

export default NewQueryView