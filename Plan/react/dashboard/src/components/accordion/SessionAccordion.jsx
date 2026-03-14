import React, {useCallback, useMemo} from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCrosshairs, faServer, faSignal, faSkull, faUser, faUserPlus} from "@fortawesome/free-solid-svg-icons";
import {faClock, faMap} from "@fortawesome/free-regular-svg-icons";
import Datapoint from "../datapoint/Datapoint.tsx";
import {Col, Row} from "react-bootstrap";
import WorldPie from "../graphs/WorldPie";
import {SimpleKillsTable} from "../table/KillsTable";
import {useTranslation} from "react-i18next";
import {usePreferences} from "../../hooks/preferencesHook.jsx";
import FormattedDate from "../text/FormattedDate.jsx";
import FormattedTime from "../text/FormattedTime.jsx";
import PlayerPageLinkButton from "../input/button/PlayerPageLinkButton.jsx";
import ServerPageLinkButton from "../input/button/ServerPageLinkButton.jsx";
import {useDecimalFormatter} from "../../util/format/useDecimalFormatter.js";
import {usePingFormatter} from "../../util/format/usePingFormatter.js";
import DataTablesTable from "../table/DataTablesTable.jsx";
import {ChartLoader} from "../navigation/Loader.tsx";

const SessionBody = ({session}) => {
    const {t} = useTranslation();
    const {formatDecimals} = useDecimalFormatter();
    const {formatPing} = usePingFormatter();
    return (
        <Row>
            <Col lg={6}>
                <Datapoint
                    icon={faClock} color={"sessions"}
                    name={t('html.label.sessionEnded')} value={<FormattedDate date={session.end}/>} bold
                />
                <Datapoint
                    icon={faClock} color={"sessions"}
                    name={t('html.label.length')} value={<FormattedTime timeMs={session.length}/>} bold
                />
                <Datapoint
                    icon={faClock} color={"playtime-afk"}
                    name={t('html.label.afkTime')} value={<FormattedTime timeMs={session.afk_time}/>} bold
                />
                <Datapoint
                    icon={faServer} color={"servers"}
                    name={t('html.label.server')} value={session.server_name} bold
                />
                {session.avg_ping ? <Datapoint
                    icon={faSignal} color={"ping"}
                    name={t('html.label.averagePing')} value={formatPing(formatDecimals(session.avg_ping))} bold
                /> : ''}
                <br/>
                <Datapoint
                    icon={faCrosshairs} color="player-kills"
                    name={t('html.label.playerKills')} value={session.player_kills.length} bold
                />
                <Datapoint
                    icon={faCrosshairs} color="mob-kills"
                    name={t('html.label.mobKills')} value={session.mob_kills} bold
                />
                <Datapoint
                    icon={faSkull} color="deaths"
                    name={t('html.label.deaths')} value={session.deaths} bold
                />
                <hr/>
                <SimpleKillsTable kills={session.player_kills}/>
            </Col>
            <div className="col-xs-12 col-sm-12 col-md-12 col-lg-6">
                <WorldPie id={"worldpie_" + session.player_uuid + session.start}
                          worldSeries={session.world_series}
                          gmSeries={session.gm_series}/>
                <PlayerPageLinkButton uuid={session.player_uuid} className={'float-end'}/>
                <ServerPageLinkButton uuid={session.server_uuid} className={'float-end me-2'}/>
            </div>
        </Row>
    )
}

const SessionAccordion = (
    {
        sessions,
        isPlayer,
        isNetwork
    }
) => {
    const {t} = useTranslation();
    const {preferencesLoaded} = usePreferences();

    const firstColumn = isPlayer ? (<><Fa icon={faUser}/> {t('html.label.player')}</>)
        : (<><Fa icon={faServer}/> {t('html.label.server')}</>)
    const lastColumn = isNetwork ? {
        title: <><Fa icon={faServer}/> {t('html.label.server')}</>,
        data: "server_name"
    } : {
        title: <><Fa icon={faMap}/> {t('html.label.mostPlayedWorld')}</>,
        data: "most_used_world"
    }

    const columns = [{
        title: firstColumn,
        data: {"_": "name", display: "nameFormatted"}
    }, {
        title: <><Fa icon={faClock}/> {t('html.label.sessionStart')}</>,
        data: {_: "start", display: "startFormatted"}
    }, {
        title: <><Fa icon={faClock}/> {t('html.label.length')}</>,
        data: {_: "length", display: "lengthFormatted"}
    },
        lastColumn];

    const rows = useMemo(() => sessions?.map(session => {
        return {
            ...session,
            nameFormatted: <>
                {session.name}
                {session.first_session &&
                    <span className={"ms-2"}
                          title={t('html.label.registered') + ' (' + t('html.label.firstSession') + ')'}>
                        <Fa icon={faUserPlus}/>
                    </span>}
            </>,
            startFormatted: <>
                <FormattedDate date={session.start}/>
                {session.online && ` (${t('html.value.online').trim()})`}
            </>,
            lengthFormatted: <FormattedTime timeMs={session.length}/>,
            colorClass: `bg-sessions${session.online ? '' : '-outline'}`
        }
    }) || [], [sessions]);

    const rowKeyFunction = useCallback((session, column) => {
        return session.player_uuid + session.server_uuid + session.start + JSON.stringify(column?.data);
    }, [])

    const options = useMemo(() => {
        return {
            responsive: true,
            deferRender: true,
            columns: columns,
            data: rows,
            order: [[1, "desc"]]
        };
    }, [columns, rows]);

    if (!sessions || !preferencesLoaded) return <ChartLoader/>

    return <DataTablesTable id={"sessions"} className={"thick-outlines"}
                            rowKeyFunction={rowKeyFunction}
                            options={options}
                            clickableRows
                            expandComponent={({row}) => <SessionBody session={row}/>}/>
}

export default SessionAccordion