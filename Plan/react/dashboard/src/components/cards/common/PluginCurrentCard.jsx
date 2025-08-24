import React from 'react';
import CardHeader from "../CardHeader";
import {faCube, faCubes, faSignal} from "@fortawesome/free-solid-svg-icons";
import {Card} from "react-bootstrap";
import {ErrorViewCard} from "../../../views/ErrorView";
import FormattedDate from "../../text/FormattedDate";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCalendar} from "@fortawesome/free-regular-svg-icons";
import {useTranslation} from "react-i18next";
import {CardLoader} from "../../navigation/Loader";
import DataTablesTable from "../../table/DataTablesTable";

const PluginCurrentCard = ({data, loadingError}) => {
    const {t} = useTranslation();

    if (loadingError) return <ErrorViewCard error={loadingError}/>;
    if (!data) return <CardLoader/>;

    const history = [];

    for (const entry of data.history) {
        if (history.find(e => e.name === entry.name)) continue;
        history.push(entry);
    }

    const table = {
        columns: [{
            title: <><Fa icon={faCube}/> {t('html.label.name')}</>,
            data: "name"
        }, {
            title: <><Fa icon={faSignal}/> {t('html.label.version')}</>,
            data: "version"
        }, {
            title: <><Fa icon={faCalendar}/> {t('html.label.modified')}</>,
            data: {_: "modified", display: "modifiedDisplay"}
        }],
        data: history.length ? history.filter(entry => entry.version)
            .map(entry => {
                return {
                    name: entry.name,
                    version: t(entry.version),
                    modified: entry.modified,
                    modifiedDisplay: <FormattedDate date={entry.modified}/>
                }
            }) : [{name: t('generic.noData'), version: '', 'modified': 0, modifiedDisplay: ''}]
    };
    const options = {
        responsive: true,
        deferRender: true,
        columns: table.columns,
        data: table.data,
        pagingType: "numbers",
        order: [[0, "desc"]]
    }

    const rowKeyFunction = (row, column) => {
        return row.name + "-" + row.version + '-' + row.modified + '-' + JSON.stringify(column?.data);
    }

    return (
        <Card>
            <CardHeader icon={faCubes} label={'html.label.currentlyInstalledPlugins'} color={"plugin-versions"}/>
            <DataTablesTable id={"plugin-current"} options={options} rowKeyFunction={rowKeyFunction}/>
        </Card>
    )
};

export default PluginCurrentCard