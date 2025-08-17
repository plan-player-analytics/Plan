import React, {useCallback, useState} from 'react';
import GroupTable from "../table/GroupTable";
import GroupPie from "./GroupPie";
import {faBarChart, faChartColumn, faPieChart, faTable} from "@fortawesome/free-solid-svg-icons";
import {Col, Row} from "react-bootstrap";
import GroupBarGraph from "./GroupBarGraph";
import ErrorBoundary from "../ErrorBoundary";
import {ErrorViewText} from "../../views/ErrorView";
import VisualizerSelector from "../input/button/VisualizerSelector.jsx";

const options = {
    BAR: 'bar',
    COLUMN: 'column',
    PIE: 'pie',
    TABLE: 'table'
}

const Visualizer = ({option, groups, colors, name}) => {
    switch (option) {
        case options.TABLE:
            return <GroupTable groups={groups} colors={colors}/>
        case options.PIE:
            return <GroupPie id={'group-pie-' + new Date()} groups={groups} colors={colors} name={name}/>
        case options.BAR:
            return <GroupBarGraph id={'group-bar-' + new Date()} groups={groups} colors={colors} name={name}
                                  horizontal/>;
        case options.COLUMN:
        default:
            return <GroupBarGraph id={'group-bar-' + new Date()} groups={groups} colors={colors} name={name}/>;
    }
}

const GroupVisualizer = ({groups, colors, name, horizontal}) => {
    const [visualization, setVisualization] = useState(groups.length > 1 ? options.COLUMN : options.TABLE);

    const selectorFloatStyle = {
        height: "0",
        zIndex: 100,
        position: "absolute",
        width: "100%",
        right: "0",
        top: "0.5rem"
    };

    const changeViewToPie = useCallback(() => setVisualization(options.PIE), [setVisualization])
    const changeViewToTable = useCallback(() => setVisualization(options.TABLE), [setVisualization])
    const changeViewToBar = useCallback(() => setVisualization(horizontal ? options.BAR : options.COLUMN), [setVisualization, horizontal])

    const fallbackFunction = useCallback((error) => <ErrorViewText error={error}/>, []);

    return <Row>
        <Col md={12} style={selectorFloatStyle}>
            <VisualizerSelector icon={faPieChart} onClick={changeViewToPie}/>
            <VisualizerSelector icon={faTable} onClick={changeViewToTable}/>
            <VisualizerSelector icon={horizontal ? faBarChart : faChartColumn} onClick={changeViewToBar}/>
        </Col>
        <Col md={12}>
            <ErrorBoundary fallbackFunction={fallbackFunction}>
                <Visualizer option={visualization} groups={groups} colors={colors} name={name}/>
            </ErrorBoundary>
        </Col>
    </Row>
};

export default GroupVisualizer