import React, {useCallback, useEffect} from 'react';
import LoadIn from "../../components/animation/LoadIn";
import {Col, Row} from "react-bootstrap-v5";
import QueryPath from "../../components/alert/QueryPath";
import {useQueryResultContext} from "../../hooks/queryResultContext";
import {useNavigate} from "react-router-dom";
import PlayerListCard from "../../components/cards/common/PlayerListCard";
import {fetchExistingResults} from "../../service/queryService";

const QueryResultView = () => {
    const navigate = useNavigate();
    const {result, setResult} = useQueryResultContext();

    const getResult = useCallback(async () => {
        const urlParams = new URLSearchParams(window.location.search);
        const timestamp = urlParams.get('timestamp');
        if (!timestamp) return {};

        const {data: result} = await fetchExistingResults(timestamp);
        if (result) {
            return result;
        } else {
            return {};
        }
    }, [])

    useEffect(() => {
        if (!result.data) {
            getResult().then(data => {
                if (data.data) {
                    setResult(data);
                } else {
                    navigate('../new');
                }
            });
        }
    }, [result, navigate, getResult, setResult])

    if (!result.data) {
        return <></>
    }

    const getViewTitle = () => {
        return 'View: ' + result.view.afterDate + " - " + result.view.beforeDate + ', ' +
            (result.view.servers.len ? 'using data of servers: ' + result.view.servers.map(server => server.name).join(', ') : "using data of all servers")
    }

    return (
        <LoadIn>
            <section className={"query-results-view"}>
                <Row>
                    <Col md={12}>
                        <QueryPath/>
                        <PlayerListCard
                            data={result.data.players}
                            title={getViewTitle()}
                        />
                    </Col>
                </Row>
                <Row>
                    <Col lg={8}>
                        {/*<PlayerbaseDevelopmentCard/>*/}
                    </Col>
                    <Col lg={4}>
                        {/*<CurrentPlayerbaseCard/>*/}
                    </Col>
                </Row>
                <Row>
                    <Col lg={3}>
                        {/*<SessionsWithinViewCard/>*/}
                    </Col>
                    <Col lg={9}>
                        {/*<GeolocationsCard/>*/}
                    </Col>
                </Row>
            </section>
        </LoadIn>
    )
};

export default QueryResultView