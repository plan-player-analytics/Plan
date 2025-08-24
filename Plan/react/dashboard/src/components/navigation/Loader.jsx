import React from "react";
import {Card} from "react-bootstrap";

export const CardLoader = () => {
    return (
        <Card className="loading">
            <Card.Header>
                <h6 className="col-text">
                    ...
                </h6>
            </Card.Header>
            <ChartLoader/>
        </Card>
    )
}

export const ChartLoader = ({style}) => {
    return <div className="chart-area loading" style={style}>
        <Loader/>
    </div>
}

const Loader = () => {
    return (
        <span className="loader"/>
    )
}

export default Loader;