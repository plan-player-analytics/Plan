import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap-v5";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faClock} from "@fortawesome/free-regular-svg-icons";
import WorldPie from "../../graphs/WorldPie";
import React from "react";

const WorldPieCard = ({worldSeries, gmSeries}) => {
    const {t} = useTranslation();

    if (!worldSeries || !gmSeries) return <></>

    return (
        <Card>
            <Card.Header>
                <h6 className="col-black" style={{width: '100%'}}>
                    <Fa icon={faClock} className="col-teal"/> {t('html.label.worldPlaytime')}
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