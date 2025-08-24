import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faClock} from "@fortawesome/free-regular-svg-icons";
import WorldPie from "../../graphs/WorldPie";
import React from "react";
import {CardLoader} from "../../navigation/Loader";

const WorldPieCard = ({worldSeries, gmSeries}) => {
    const {t} = useTranslation();

    if (!worldSeries || !gmSeries) return <CardLoader/>;

    return (
        <Card>
            <Card.Header>
                <h6 className="col-text" style={{width: '100%'}}>
                    <Fa icon={faClock} className="col-sessions"/> {t('html.label.worldPlaytime')}
                </h6>
            </Card.Header>
            <WorldPie
                id="world-pie"
                worldSeries={worldSeries}
                gmSeries={gmSeries}
            />
        </Card>
    )
}

export default WorldPieCard;