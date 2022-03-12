import {Card, Col, Row} from "react-bootstrap-v5";
import React from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faBug} from "@fortawesome/free-solid-svg-icons";

const Header = ({error}) => (
    <div className="d-sm-flex align-items-center justify-content-between mb-4">
        <h1 className="h3 mb-0 text-gray-800">
            {error.title ? error.title : "Ran into an Unexpected Error"}
        </h1>
    </div>
);

const ErrorView = ({error}) => {
    console.error(error);
    return (
        <section className="player_overview">
            <Header error={error}/>
            <Row>
                <Col lg={12}>
                    <Card>
                        <Card.Header>
                            <h6 className="col-black">
                                <Fa icon={error.icon ? error.icon : faBug}/> Error information
                            </h6>
                        </Card.Header>
                        <Card.Body>
                            <p>{error.message} {error.url ? (<a href={error.url}>{error.url}</a>) : ''}</p>
                            {error.data ? <><br/>
                                <pre>{error.data}</pre>
                            </> : ''}
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </section>
    )
}

export default ErrorView;