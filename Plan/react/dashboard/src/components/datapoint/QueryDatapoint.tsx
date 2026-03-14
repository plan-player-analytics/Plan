import {Datapoint, DatapointType, DatapointTypeMap, FormatType} from "../../dataHooks/model/datapoint/Datapoint";
import {filterToQueryString, GenericFilter} from "../../dataHooks/model/GenericFilter";
import {useNavigation} from "../../hooks/navigationHook";
import {useQuery} from "@tanstack/react-query";
import {queryRetry} from "../../dataHooks/queryRetry";
import {useEffect} from "react";
import {baseAddress, staticSite} from "../../service/backendConfiguration";
import {ErrorViewText} from "../../views/ErrorView";
import {DatapointProps, default as DatapointComponent} from "./Datapoint";
import FormattedTime from "../text/FormattedTime";
import Loader from "../navigation/Loader";
import {useAuth} from "../../hooks/authenticationHook";
import {useDecimalFormatter} from "../../util/format/useDecimalFormatter";

type Props<K extends DatapointType> = {
    dataType: K;
    filter?: GenericFilter;
    permission?: string;
} & Omit<DatapointProps, 'value'>;

type FormatProps<K extends DatapointType> = {
    value?: DatapointTypeMap[K],
    formatType?: FormatType
}

function Format<K extends DatapointType>({value, formatType}: FormatProps<K>) {
    const {formatDecimals} = useDecimalFormatter();
    if (value === undefined) return null;
    switch (formatType) {
        case "TIME_AMOUNT":
            return <FormattedTime timeMs={value}/>
        case "PERCENTAGE":
            return formatDecimals(value as number * 100) + '%'
        case "NONE":
        default:
            return String(value);
    }
}

function calculatePermission(dataType: DatapointType, filter?: GenericFilter) {
    const asPermission = dataType.toLowerCase().replaceAll('_', '.');
    if (filter) {
        if (filter.player) return 'data.' + asPermission + '.player';
        if (filter.server?.length) return 'data.' + asPermission + '.server';
    }
    return 'data.' + asPermission + '.network';
}

export function QueryDatapoint<K extends DatapointType>({permission, dataType, filter, ...props}: Props<K>) {
    const {hasPermission} = useAuth();
    const allowed = hasPermission(permission ?? calculatePermission(dataType, filter));
    const {data, error} = useDatapointQuery(allowed, dataType, filter);

    if (!allowed) return null;
    if (error) return <ErrorViewText error={error}/>

    const cast = data as Datapoint<K>;
    return <DatapointComponent
        {...props}
        value={cast ? <Format value={cast.value} formatType={cast.formatType}/> : <Loader/>}
    />
}

export function QueryDatapointValue<K extends DatapointType>({permission, dataType, filter, ...props}: Props<K>) {
    const {hasPermission} = useAuth();
    const allowed = hasPermission(permission ?? calculatePermission(dataType, filter));
    const {data, error} = useDatapointQuery(allowed, dataType, filter);

    if (!allowed) return null;
    if (error) return <ErrorViewText error={error}/>

    const cast = data as Datapoint<K>;
    return cast ? <Format value={cast.value} formatType={cast.formatType}/> : <Loader/>
}


function useDatapointQuery<K extends DatapointType>(allowed: boolean, dataType: K, filter?: GenericFilter) {
    const {updateRequested} = useNavigation() as { updateRequested: number };
    const query = useQuery({
        queryKey: filter ? ['datapoint', dataType, ...Object.values(filter)] : ['datapoint', dataType],
        queryFn: () => getDatapoint(dataType, filter),
        retry: queryRetry,
        enabled: Boolean(allowed)
    });
    useEffect(() => {
        query.refetch()
    }, [updateRequested]);
    return query;
}

async function getDatapoint<K extends DatapointType>(dataType: K, filter?: GenericFilter) {
    let url = baseAddress + `/v1/datapoint?type=${dataType}&${filterToQueryString(filter)}`;
    if (staticSite) return baseAddress + `/v1/datapoint?type=${dataType}`;
    const response = await fetch(url);
    if (!response.ok) throw {status: response.status, message: response.statusText, data: response.body};
    return await response.json() as Promise<Datapoint<K>>;
}