import {useTranslation} from "react-i18next";
import {Card, Col, Dropdown, Row} from "react-bootstrap-v5";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import React, {useState} from "react";
import {faExclamationTriangle, faGlobe, faLayerGroup} from "@fortawesome/free-solid-svg-icons";
import GeolocationBarGraph from "../../graphs/GeolocationBarGraph";
import GeolocationWorldMap, {ProjectionOptions} from "../../graphs/GeolocationWorldMap";
import {CardLoader} from "../../navigation/Loader";
import DropdownToggle from "react-bootstrap-v5/lib/esm/DropdownToggle";
import DropdownMenu from "react-bootstrap-v5/lib/esm/DropdownMenu";
import DropdownItem from "react-bootstrap-v5/lib/esm/DropdownItem";

const ProjectionDropDown = ({projection, setProjection}) => {
    const {t} = useTranslation();

    const projectionOptions = Object.values(ProjectionOptions);

    return (
        <Dropdown className="float-end" style={{position: "absolute", right: "0.5rem"}}
                  title={t('html.label.geoProjection.dropdown')}>
            <DropdownToggle variant=''>
                <Fa icon={faLayerGroup}/> {t(projection)}
            </DropdownToggle>

            <DropdownMenu>
                <h6 className="dropdown-header">{t('html.label.geoProjection.dropdown')}</h6>
                {projectionOptions.map((option, i) => (
                    <DropdownItem key={i} onClick={() => setProjection(option)}>
                        {t(option)}
                    </DropdownItem>
                ))}
            </DropdownMenu>
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
                <Row>
                    <Col md={3}>
                        <GeolocationBarGraph series={data.geolocation_bar_series} color={data.colors.bars}/>
                    </Col>
                    <Col md={9}>
                        <GeolocationWorldMap series={data.geolocation_series} colors={data.colors}
                                             projection={projection}/>
                    </Col>
                </Row>
            </Card.Body>
        </Card>
    )
}

export default GeolocationsCard;