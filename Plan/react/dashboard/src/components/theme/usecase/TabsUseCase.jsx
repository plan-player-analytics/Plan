import React from 'react';
import {faBraille, faChartArea, faUser} from "@fortawesome/free-solid-svg-icons";
import {faCalendar} from "@fortawesome/free-regular-svg-icons";
import {Card} from "react-bootstrap";
import CardTabs from "../../CardTabs.jsx";
import {useTranslation} from "react-i18next";
import Datapoint from "../../Datapoint.jsx";

const Body = () => {
    return (
        <Card.Body>
            <Datapoint name={"Example"} value={1234} icon={faUser}/>
            <hr/>
            <Datapoint name={"Example"} value={1234} icon={faUser}/>
        </Card.Body>
    )
}

const TabsUseCase = () => {
    const {t} = useTranslation();
    const tabs = [
        {
            name: t('html.label.dayByDay'), icon: faChartArea, color: 'players-unique', href: '1',
            element: <Body/>,
        }, {
            name: t('html.label.hourByHour'), icon: faChartArea, color: 'players-unique', href: '2',
            element: <Body/>,
        }, {
            name: t('html.label.serverCalendar'), icon: faCalendar, color: 'sessions', href: '3',
            element: <Body/>,
        }, {
            name: t('html.label.punchcard30days'), icon: faBraille, color: 'text', href: '4',
            element: <Body/>,
        },
    ];
    return <Card id={"online-activity-graphs"}>
        <CardTabs tabs={tabs}/>
    </Card>
};

export default TabsUseCase