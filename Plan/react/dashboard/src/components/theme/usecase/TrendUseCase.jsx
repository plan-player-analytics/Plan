import React from 'react';
import BigTrend from "../../trend/BigTrend.jsx";
import SmallTrend from "../../trend/SmallTrend.jsx";
import {Card} from "react-bootstrap";

const TrendUseCase = () => {
    return (
        <Card>
            <Card.Body className={"col-text"}>
                <p>
                    <BigTrend trend={{text: "1234", direction: '+'}}/>
                </p>
                <p>
                    <BigTrend trend={{text: "1234", direction: '-'}}/>
                </p>
                <p>
                    <BigTrend trend={{text: "1234", direction: ''}}/>
                </p>
                <hr/>
                <p>
                    1234 <SmallTrend trend={{text: "1234", direction: '+'}}/>
                </p>
                <p>
                    1234 <SmallTrend trend={{text: "1234", direction: '-'}}/>
                </p>
                <p>
                    1234 <SmallTrend trend={{text: "1234", direction: ''}}/>
                </p>
            </Card.Body>
        </Card>
    )
};

export default TrendUseCase