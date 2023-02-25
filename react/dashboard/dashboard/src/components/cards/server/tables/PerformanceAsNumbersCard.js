import React from 'react';
import PerformanceAsNumbersTable from "../../../table/PerformanceAsNumbersTable";
import CardHeader from "../../CardHeader";
import {faBookOpen} from "@fortawesome/free-solid-svg-icons";
import {Card} from "react-bootstrap";

const PerformanceAsNumbersCard = ({data}) => {
    return (
        <Card>
            <CardHeader icon={faBookOpen} color="blue-grey" label={'html.label.performanceAsNumbers'}/>
            <PerformanceAsNumbersTable data={data}/>
        </Card>
    )
};

export default PerformanceAsNumbersCard