import {useTranslation} from "react-i18next";
import {Card, Col, Row} from "react-bootstrap-v5";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import React from "react";
import {faExclamationTriangle, faGlobe} from "@fortawesome/free-solid-svg-icons";
import GeolocationBarGraph from "../../graphs/GeolocationBarGraph";
import GeolocationWorldMap from "../../graphs/GeolocationWorldMap";
import {CardLoader} from "../../navigation/Loader";

const GeolocationsCard = ({data}) => {
    const {t} = useTranslation();

    if (!data) return <CardLoader/>

    if (!data?.geolocations_enabled) {
        return (
            <div className="alert alert-warning mb-0" id="geolocation-warning">
                <Fa icon={faExclamationTriangle}/>{' '}
                {t('html.description.noGeolocations')}
            </div>
        )
    }

    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa icon={faGlobe} className="col-green"/> {t('html.label.geolocations')}
                </h6>
            </Card.Header>
            <Card.Body className="chart-area" style={{height: "100%"}}>
                <Row>
                    <Col md={3}>
                        <GeolocationBarGraph series={data.geolocation_bar_series} color={data.colors.bars}/>
                    </Col>
                    <Col md={9}>
                        <GeolocationWorldMap series={data.geolocation_series} colors={data.colors}/>
                    </Col>
                </Row>
            </Card.Body>
        </Card>
    )
}

export default GeolocationsCard;