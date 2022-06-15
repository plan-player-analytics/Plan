import React from "react";
import {Card} from "react-bootstrap-v5";

export const CardLoader = () => {
    return (
        <Card className="loading">
            <Card.Header>
                <h6 className="col-black">
                    ...
                </h6>
            </Card.Header>
            <ChartLoader/>
        </Card>
    )
}

export const ChartLoader = () => {
    return <div className="chart-area loading">
        <Loader/>
    </div>
}

const Loader = () => {
    return (
        <span className="loader"/>
    )
}

export default Loader;