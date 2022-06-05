import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap-v5";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import React, {useEffect, useState} from "react";
import {faUsers} from "@fortawesome/free-solid-svg-icons";
import DataTablesTable from "../../table/DataTablesTable";
import {CardLoader} from "../../navigation/Loader";

const PlayerListCard = ({data}) => {
    const {t} = useTranslation();
    const [options, setOptions] = useState(undefined);

    useEffect(() => {
        for (const row of data.data) {
            row.name = row.name.replace('../player/', '../../player/');
        }

        setOptions({
            responsive: true,
            deferRender: true,
            columns: data.columns,
            data: data?.data,
            order: [[5, "desc"]]
        });
    }, [data])

    if (!options) return <CardLoader/>

    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa icon={faUsers} className="col-black"/> {t('html.label.playerList')}
                </h6>
            </Card.Header>
            <DataTablesTable id={"players-table"} options={options}/>
        </Card>
    )
}

export default PlayerListCard;