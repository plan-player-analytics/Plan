import {useTranslation} from "react-i18next";
import {Card, Col, Dropdown} from "react-bootstrap";
import {FontAwesomeIcon, FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import React, {useCallback, useState} from "react";
import {faExclamationTriangle, faGlobe, faLayerGroup} from "@fortawesome/free-solid-svg-icons";
import GeolocationBarGraph from "../../graphs/GeolocationBarGraph";
import GeolocationWorldMap, {ProjectionOptions} from "../../graphs/GeolocationWorldMap";
import {CardLoader} from "../../navigation/Loader";
import ExtendableRow from "../../layout/extension/ExtendableRow";
import Highcharts from "highcharts/highstock";
import "highcharts/modules/accessibility";
import "highcharts/modules/no-data-to-display"
import {postQuery} from "../../../service/queryService";
import {useMetadata} from "../../../hooks/metadataHook";
import QueryPlayerListModal from "../../modal/QueryPlayerListModal";
import {faHandPointer} from "@fortawesome/free-regular-svg-icons";
import CardHeader from "../CardHeader";

const ProjectionDropDown = ({projection, setProjection}) => {
    const {t} = useTranslation();

    const projectionOptions = Object.values(ProjectionOptions);

    return (
        <Dropdown className="float-end" style={{margin: "-0.5rem", marginLeft: 0}}
                  title={t('html.label.geoProjection.dropdown')}>
            <Dropdown.Toggle variant='' style={{'--bs-btn-color': 'var(--color-forms-input-text)'}}>
                <Fa icon={faLayerGroup}/> {t(projection)}
            </Dropdown.Toggle>

            <Dropdown.Menu>
                <h6 className="dropdown-header">{t('html.label.geoProjection.dropdown')}</h6>
                {projectionOptions.map(option => (
                    <Dropdown.Item key={option} onClick={() => setProjection(option)}>
                        {t(option)}
                    </Dropdown.Item>
                ))}
            </Dropdown.Menu>
        </Dropdown>
    )
}

const GeolocationsCard = ({identifier, data}) => {
    const {t} = useTranslation();
    const {networkMetadata} = useMetadata();
    const [projection, setProjection] = useState(ProjectionOptions.MILLER);

    const [modalOpen, setModalOpen] = useState(false);
    const [queryData, setQueryData] = useState(undefined);
    const [country, setCountry] = useState(undefined);

    const closeModal = useCallback(() => {
        setModalOpen(false);
    }, [setModalOpen]);

    const onClickCountry = useCallback(async selectionInfo => {
        const selectedCountry = selectionInfo?.point['iso-a3'];
        if (!selectedCountry) return;
        const end = Highcharts.dateFormat('%d/%m/%Y', Date.now());
        const query = {
            filters: [{
                kind: "geolocations",
                parameters: {
                    selected: `["${selectedCountry}"]`
                }
            }],
            view: {
                afterDate: "01/01/1970", afterTime: "00:00",
                beforeDate: end, beforeTime: "00:00",
                servers: networkMetadata?.servers.filter(server => server.serverUUID === identifier) || []
            }
        }
        setQueryData(undefined);
        setCountry(undefined);
        setModalOpen(true);
        const data = await postQuery(query);
        const loaded = data?.data;
        if (loaded) {
            setQueryData(loaded);
            setCountry(selectionInfo.point.name);
        }
    }, [setQueryData, setModalOpen, networkMetadata, identifier, setCountry]);

    if (!data) return <CardLoader/>

    if (!data?.geolocations_enabled) {
        return (
            <div className="alert alert-warning mb-0" id="geolocation-warning">
                <Fa icon={faExclamationTriangle}/>{' '}{t('html.description.noGeolocations')}
            </div>
        )
    }

    return (
        <Card id={"geolocations"}>
            <QueryPlayerListModal open={modalOpen} toggle={closeModal} queryData={queryData}
                                  title={"View " + t('html.query.filter.generic.start') + t('html.query.filter.country.text') + ': ' + country}/>
            <CardHeader icon={faGlobe} color={"geolocation"} label={'html.label.geolocations'}>
                <ProjectionDropDown projection={projection} setProjection={setProjection}/>
                <p style={{margin: 0, fontWeight: "normal"}} className={"float-end"}>
                    <FontAwesomeIcon icon={faHandPointer}/> {t('html.text.click')}
                </p>
            </CardHeader>
            <Card.Body className="chart-area" style={{height: "100%"}}>
                <ExtendableRow id={'row-geolocations-graphs-card-0'}>
                    <Col md={3}>
                        <GeolocationBarGraph series={data.geolocation_bar_series}/>
                    </Col>
                    <Col md={9}>
                        <GeolocationWorldMap series={data.geolocation_series} colors={data.colors}
                                             projection={projection} onClickCountry={onClickCountry}/>
                    </Col>
                </ExtendableRow>
            </Card.Body>
        </Card>
    )
}

export default GeolocationsCard;