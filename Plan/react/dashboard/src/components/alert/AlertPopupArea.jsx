import React from 'react';
import {useAlertPopupContext} from "../../hooks/context/alertPopupContext";
import LoadIn from "../animation/LoadIn.tsx";

const Alert = ({alert}) => {
    const {dismissAlert} = useAlertPopupContext();
    return (
        <LoadIn>
            <button className={"alert shadow alert-" + alert.color} onClick={() => dismissAlert(alert)}>
                {alert.content}
                <span aria-hidden="true" style={{marginLeft: "0.5rem"}}>&times;</span>
                <div className={"alert-timer"}
                     style={{animationDuration: (alert.timeout - (Date.now() - alert.time)) + "ms"}}/>
            </button>
        </LoadIn>
    )
}

const AlertPopupArea = () => {
    const {alerts} = useAlertPopupContext();

    return (
        <div className={"alert-popup-area"}>
            {alerts.map(alert => <Alert key={alert.time} alert={alert}/>)}
        </div>
    )
};

export default AlertPopupArea