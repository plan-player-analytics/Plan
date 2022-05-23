import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap-v5";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faBookOpen} from "@fortawesome/free-solid-svg-icons";
import React from "react";
import OnlineActivityAsNumbersTable from "../../../table/OnlineActivityAsNumbersTable";

const OnlineActivityAsNumbersCard = ({data}) => {
    const {t} = useTranslation();
    if (!data) return <></>

    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa icon={faBookOpen} className="col-light-blue"/> {t('html.label.onlineActivityAsNumbers')}
                </h6>
            </Card.Header>
            <OnlineActivityAsNumbersTable data={data}/>
        </Card>
    )
}

export default OnlineActivityAsNumbersCard;