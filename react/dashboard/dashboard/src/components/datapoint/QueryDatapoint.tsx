import {
    Datapoint,
    DatapointType,
    DatapointTypeMap,
    FormatType,
    getDatapointUrl
} from "../../dataHooks/model/datapoint/Datapoint";
import {GenericFilter} from "../../dataHooks/model/GenericFilter";
import {useNavigation} from "../../hooks/navigationHook";
import {useQuery} from "@tanstack/react-query";
import {queryRetry} from "../../dataHooks/queryRetry";
import {useEffect} from "react";
import {Datapoint as DatapointComponent, DatapointProps} from "./Datapoint";
import FormattedTime from "../text/FormattedTime";
import Loader from "../navigation/Loader";
import {useAuth} from "../../hooks/authenticationHook";
import {useDecimalFormatter} from "../../util/format/useDecimalFormatter";
import {isOutOf, OutOf} from "../../dataHooks/model/datapoint/OutOf";
import {isOutOfCategory, OutOfCategory} from "../../dataHooks/model/datapoint/OutOfCategory";
import {useTranslation} from "react-i18next";

type Props<K extends DatapointType> = {
    dataType: K;
    filter?: GenericFilter;
    permission?: string;
} & Omit<DatapointProps, 'value'>;

type FormatProps<K extends DatapointType> = Readonly<{
    value?: DatapointTypeMap[K],
    formatType?: FormatType
}>

const FormattedOutOf = ({outOf}: { outOf: OutOf }) => {
    const {formatDecimals} = useDecimalFormatter();

    return (
        <>
            <Format value={outOf.value} formatType={outOf.formatType}/>
            {' '}({formatDecimals(outOf.percentage * 100)}%)
        </>
    )
}

const FormattedOutOfCategory = ({outOf}: { outOf: OutOfCategory }) => {
    const {t} = useTranslation();
    const {formatDecimals} = useDecimalFormatter();
    if (!outOf.category || !outOf.percentage) return t('generic.noData');
    return (
        <>
            <b>{t(`html.label.${outOf.category}`)}</b>
            {' '}({formatDecimals(outOf.percentage * 100)}%)
        </>
    )
}

function Format<K extends DatapointType>({value, formatType}: FormatProps<K>) {
    const {formatDecimals} = useDecimalFormatter();
    if (value === undefined) return null;
    switch (formatType) {
        case "TIME_AMOUNT":
            return <FormattedTime timeMs={value}/>
        case "PERCENTAGE":
            return formatDecimals(value as number * 100) + '%'
        case "SPECIAL":
            if (isOutOf(value)) {
                return <FormattedOutOf outOf={value as OutOf}/>
            }
            if (isOutOfCategory(value)) {
                return <FormattedOutOfCategory outOf={value as OutOfCategory}/>;
            }
            return String(value);
        case "NONE":
        default:
            return String(value);
    }
}

function calculatePermission(dataType: DatapointType, permission?: string, filter?: GenericFilter) {
    if (!dataType) return '';
    const asPermission = permission || dataType.toLowerCase().replaceAll('_', '.');
    if (filter) {
        if (filter.player) return 'data.player.' + asPermission;
        if (filter.server?.length) return 'data.server.' + asPermission;
    }
    return 'data.network.' + asPermission;
}

export function QueryDatapoint<K extends DatapointType>({permission, dataType, filter, ...props}: Props<K>) {
    const {hasPermission} = useAuth();
    const allowed = hasPermission(calculatePermission(dataType, permission, filter));
    const {data, error} = useDatapointQuery(allowed, dataType, filter);

    if (!allowed) return null;
    if (error) {
        console.error(error);
        return <DatapointComponent
            {...props}
            value={error.message}
        />
    }

    const cast = data as Datapoint<K>;
    return <DatapointComponent
        {...props}
        title={props.name}
        value={cast ? <Format value={cast.value} formatType={cast.formatType}/> : <Loader/>}
    />
}

export function QueryDatapointValue<K extends DatapointType>({permission, dataType, filter}: Props<K>) {
    const {hasPermission} = useAuth();
    const allowed = hasPermission(calculatePermission(dataType, permission, filter));
    const {data, error} = useDatapointQuery(allowed, dataType, filter);

    if (!allowed) return null;
    if (error) {
        console.error(error);
        return error.message;
    }

    return data ? <Format value={data.value} formatType={data.formatType}/> : <Loader/>
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
    const url = getDatapointUrl(dataType, filter);
    const response = await fetch(url);
    if (!response.ok) throw {status: response.status, message: response.statusText, data: response.body};
    return await response.json() as Promise<Datapoint<K>>;
}