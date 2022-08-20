import {useParams} from "react-router-dom";
import {useDataRequest} from "../../../../hooks/dataFetchHook";
import {fetchOptimizedPerformance} from "../../../../service/serverService";
import {ErrorViewBody} from "../../../../views/ErrorView";
import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap-v5";
import CardTabs from "../../../CardTabs";
import {faGears, faHdd, faMap, faMicrochip, faSignal, faTachometerAlt} from "@fortawesome/free-solid-svg-icons";
import React, {useEffect, useState} from "react";
import {ChartLoader} from "../../../navigation/Loader";

const PunchCardTab = ({data, loadingError}) => {

    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!data) return <ChartLoader/>;

    return <ChartLoader/>
}

function mapToDataSeries(performanceData) {
    const playersOnline = [];
    const tps = [];
    const cpu = [];
    const ram = [];
    const entities = [];
    const chunks = [];
    const disk = [];

    return new Promise((resolve => {
        let i = 0;
        const length = performanceData.length;

        function processNextThousand() {
            const to = Math.min(i + 1000, length);
            for (i; i < to; i++) {
                const entry = performanceData[i];
                const date = entry[0];
                playersOnline[i] = [date, entry[1]];
                tps[i] = [date, entry[2]];
                cpu[i] = [date, entry[3]];
                ram[i] = [date, entry[4]];
                entities[i] = [date, entry[5]];
                chunks[i] = [date, entry[6]];
                disk[i] = [date, entry[7]];
            }
            if (i >= length) {
                resolve({playersOnline, tps, cpu, ram, entities, chunks, disk})
            } else {
                setTimeout(processNextThousand, 10);
            }
        }

        processNextThousand();
    }))
}

const PerformanceGraphsCard = () => {
    const {t} = useTranslation();

    const {identifier} = useParams();
    const {data, loadingError} = useDataRequest(fetchOptimizedPerformance, [identifier]);
    const [parsedData, setParsedData] = useState(undefined)

    useEffect(() => {
        if (data) {
            mapToDataSeries(data).then(parsed => setParsedData(parsed))
        }
    }, [data]);

    return <Card>
        <CardTabs tabs={[
            {
                name: t('html.label.all'), icon: faGears, color: 'blue-grey', href: 'all',
                element: <PunchCardTab data={parsedData} loadingError={loadingError}/>
            }, {
                name: t('html.label.tps'), icon: faTachometerAlt, color: 'red', href: 'tps',
                element: <PunchCardTab data={parsedData} loadingError={loadingError}/>
            }, {
                name: t('html.label.cpuRam'), icon: faMicrochip, color: 'light-green', href: 'cpu-ram',
                element: <PunchCardTab data={parsedData} loadingError={loadingError}/>
            }, {
                name: t('html.label.world'), icon: faMap, color: 'purple', href: 'world-load',
                element: <PunchCardTab data={parsedData} loadingError={loadingError}/>
            }, {
                name: t('html.label.ping'), icon: faSignal, color: 'amber', href: 'ping',
                element: <PunchCardTab data={parsedData} loadingError={loadingError}/>
            }, {
                name: t('html.label.diskSpace'), icon: faHdd, color: 'green', href: 'disk',
                element: <PunchCardTab data={parsedData} loadingError={loadingError}/>
            },
        ]}/>
    </Card>
}

export default PerformanceGraphsCard;