import React, {useCallback, useEffect, useState} from 'react';
import {useTranslation} from "react-i18next";
import {fetchJoinAddressByDay} from "../../../../service/serverService";
import {ErrorViewCard} from "../../../../views/ErrorView";
import {ChartLoader} from "../../../navigation/Loader";
import {Card} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faChartColumn} from "@fortawesome/free-solid-svg-icons";
import JoinAddressGraph from "../../../graphs/JoinAddressGraph";
import Toggle from "../../../input/Toggle";
import {useJoinAddressListContext} from "../../../../hooks/context/joinAddressListContextHook.jsx";
import {useNavigation} from "../../../../hooks/navigationHook.jsx";

const JoinAddressGraphCard = ({identifier}) => {
    const {t} = useTranslation();
    const [stack, setStack] = useState(true);
    const {updateRequested} = useNavigation();

    const {list} = useJoinAddressListContext();
    const noSelectedAddresses = !list.filter(group => group.addresses.length).length;

    const [data, setData] = useState(undefined);
    const [loadingError, setLoadingError] = useState(undefined);
    const loadAddresses = useCallback(async () => {
        if (noSelectedAddresses) return;

        let colors = ['#4ab4de'];
        const dataByGroup = [];
        for (const group of list.filter(group => group.addresses.length)) {
            const {data, error} = await fetchJoinAddressByDay(updateRequested, group.addresses, identifier);
            if (error) {
                setLoadingError(error);
                return;
            }
            colors = data?.colors;
            dataByGroup.push({...group, data: data?.join_addresses_by_date || []});
        }

        // First group points from endpoint into frontend based groups
        const points = {};
        for (const group of dataByGroup) {
            const groupName = group.name;
            for (const point of group.data || []) {
                if (!points[point.date]) points[point.date] = [];

                const count = point.joinAddresses.map(j => j.count).reduce((partialSum, a) => partialSum + a, 0);
                points[point.date].push({date: point.date, joinAddresses: [{joinAddress: groupName, count}]})
            }
        }

        // expected output: [{date: number, addresses: [{joinAddress: "name", count: number}]}]
        const flattened = Object.entries(points)
            .sort((a, b) => Number(b.date) - Number(a.date))
            .map(([date, pointList]) => {
                return {
                    date: Number(date), joinAddresses: pointList.map(point => point.joinAddresses).flat()
                }
            });

        setData({
            join_addresses_by_date: flattened,
            colors
        });
    }, [setData, setLoadingError, identifier, updateRequested, list]);

    useEffect(() => {
        loadAddresses();
    }, [loadAddresses]);

    if (loadingError) return <ErrorViewCard error={loadingError}/>

    return (
        <Card>
            <Card.Header>
                <h6 className="col-black" style={{width: '100%'}}>
                    <Fa icon={faChartColumn} className="col-amber"/> {t('html.label.joinAddresses')}
                </h6>
                <Toggle value={stack} onValueChange={setStack} color={'amber'}>{t('html.label.stacked')}</Toggle>
            </Card.Header>
            {data &&
                <JoinAddressGraph id={'join-address-graph'} data={data?.join_addresses_by_date} colors={data?.colors}
                                  stack={stack}/>}
            {!data && noSelectedAddresses &&
                <div className="chart-area" style={{height: "450px"}}><p>Select some addresses</p></div>}
            {!data && !noSelectedAddresses && <ChartLoader/>}
        </Card>
    )
};

export default JoinAddressGraphCard