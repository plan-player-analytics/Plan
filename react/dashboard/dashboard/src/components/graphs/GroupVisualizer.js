import React, {useState} from 'react';
import GroupTable from "../table/GroupTable";
import GroupPie from "./GroupPie";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faBarChart, faChartColumn, faPieChart, faTable} from "@fortawesome/free-solid-svg-icons";
import {Col, Row} from "react-bootstrap-v5";
import GroupBarGraph from "./GroupBarGraph";

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

const VisualizerSelector = ({onClick, icon}) => {
    return (
        <button className="btn float-end" onClick={onClick}>
            <FontAwesomeIcon icon={icon} className="col-gray"/>
        </button>
    )
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
    return <Row>
        <Col md={12} style={selectorFloatStyle}>
            <VisualizerSelector icon={faPieChart} onClick={() => setVisualization(options.PIE)}/>
            <VisualizerSelector icon={faTable} onClick={() => setVisualization(options.TABLE)}/>
            <VisualizerSelector icon={horizontal ? faBarChart : faChartColumn}
                                onClick={() => setVisualization(horizontal ? options.BAR : options.COLUMN)}/>
        </Col>
        <Col md={12}>
            <Visualizer option={visualization} groups={groups} colors={colors} name={name}/>
        </Col>
    </Row>
};

export default GroupVisualizer