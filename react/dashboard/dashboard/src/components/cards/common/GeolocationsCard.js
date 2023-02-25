import {useTranslation} from "react-i18next";
import {Card, Col, Dropdown} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import React, {useState} from "react";
import {faExclamationTriangle, faGlobe, faLayerGroup} from "@fortawesome/free-solid-svg-icons";
import GeolocationBarGraph from "../../graphs/GeolocationBarGraph";
import GeolocationWorldMap, {ProjectionOptions} from "../../graphs/GeolocationWorldMap";
import {CardLoader} from "../../navigation/Loader";
import ExtendableRow from "../../layout/extension/ExtendableRow";

const ProjectionDropDown = ({projection, setProjection}) => {
    const {t} = useTranslation();

    const projectionOptions = Object.values(ProjectionOptions);

    return (
        <Dropdown className="float-end" style={{position: "absolute", right: "0.5rem"}}
                  title={t('html.label.geoProjection.dropdown')}>
            <Dropdown.Toggle variant=''>
                <Fa icon={faLayerGroup}/> {t(projection)}
            </Dropdown.Toggle>

            <Dropdown.Menu>
                <h6 className="dropdown-header">{t('html.label.geoProjection.dropdown')}</h6>
                {projectionOptions.map((option, i) => (
                    <Dropdown.Item key={i} onClick={() => setProjection(option)}>
                        {t(option)}
                    </Dropdown.Item>
                ))}
            </Dropdown.Menu>
        </Dropdown>
    )
}

const GeolocationsCard = ({data}) => {
    const {t} = useTranslation();
    const [projection, setProjection] = useState(ProjectionOptions.MILLER);

    if (!data) return <CardLoader/>

    if (!data?.geolocations_enabled) {
        return (
            <div className="alert alert-warning mb-0" id="geolocation-warning">
                <Fa icon={faExclamationTriangle}/>{' '}{t('html.description.noGeolocations')}
            </div>
        )
    }

    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa icon={faGlobe} className="col-green"/> {t('html.label.geolocations')}
                </h6>
                <ProjectionDropDown projection={projection} setProjection={setProjection}/>
            </Card.Header>
            <Card.Body className="chart-area" style={{height: "100%"}}>
                <ExtendableRow id={'row-geolocations-graphs-card-0'}>
                    <Col md={3}>
                        <GeolocationBarGraph series={data.geolocation_bar_series} color={data.colors.bars}/>
                    </Col>
                    <Col md={9}>
                        <GeolocationWorldMap series={data.geolocation_series} colors={data.colors}
                                             projection={projection}/>
                    </Col>
                </ExtendableRow>
            </Card.Body>
        </Card>
    )
}

export default GeolocationsCard;