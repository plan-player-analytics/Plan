import React from 'react';
import CardHeader from "../CardHeader";
import {Card} from "react-bootstrap-v5";
import PlayersOnlineGraph from "../../graphs/PlayersOnlineGraph";
import {faChartArea} from "@fortawesome/free-solid-svg-icons";
import {useTranslation} from "react-i18next";

const QuickViewGraphCard = ({server}) => {
    const {t} = useTranslation();
    return (
        <Card>
            <CardHeader icon={faChartArea} color={'light-blue'}
                        label={server.name + ' ' + t('html.label.onlineActivity') + ' (' + t('html.label.thirtyDays') + ')'}/>
            <PlayersOnlineGraph data={server.playersOnline}/>
        </Card>
    )
};

export default QuickViewGraphCard