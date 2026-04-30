import {
    Datapoint,
    DatapointType,
    DatapointTypeMap,
    differingPermissions,
    FormatType,
    getDatapointUrl,
    NumericDatapointType
} from "../../dataHooks/model/datapoint/Datapoint";
import {GenericFilter} from "../../dataHooks/model/GenericFilter";
import {useNavigation} from "../../hooks/navigationHook";
import {useQueries, useQuery} from "@tanstack/react-query";
import {queryRetry} from "../../dataHooks/queryRetry";
import React, {useEffect, useRef} from "react";
import {Datapoint as DatapointComponent, DatapointProps} from "./Datapoint";
import FormattedTime from "../text/FormattedTime";
import {DatapointLoader} from "../navigation/Loader";
import {useAuth} from "../../hooks/authenticationHook";
import {useDecimalFormatter} from "../../util/format/useDecimalFormatter";
import {isOutOf, OutOf} from "../../dataHooks/model/datapoint/OutOf";
import {isOutOfCategory, OutOfCategory} from "../../dataHooks/model/datapoint/OutOfCategory";
import {useTranslation} from "react-i18next";
import {DateObj, isDateObj} from "../../dataHooks/model/datapoint/DateObj";
import FormattedDate from "../text/FormattedDate";
import BigTrend from "../trend/BigTrend";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faQuestionCircle} from "@fortawesome/free-regular-svg-icons";

type Props<K extends DatapointType> = {
    dataType: K;
    filter?: GenericFilter;
    fallbackUnavailableExplanation?: string
} & Omit<DatapointProps, 'value'>;

type ValueProps<K extends DatapointType> = Omit<Props<K>, 'color' | 'name' | 'icon'>;

type FormatProps<K extends DatapointType> = Readonly<{
    value?: DatapointTypeMap[K],
    formatType?: FormatType
}>

const FormattedOutOf = ({outOf}: { outOf: OutOf }) => {
    const {formatDecimals} = useDecimalFormatter();

    return (
        <>
            <Format value={outOf.value} formatType={outOf.formatType}/>
            {' / '}
            <Format value={outOf.max} formatType={outOf.formatType}/>
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
        case "DECIMAL":
            return formatDecimals(value as number)
        case "SPECIAL":
            if (isOutOf(value)) {
                return <FormattedOutOf outOf={value as OutOf}/>
            }
            if (isOutOfCategory(value)) {
                return <FormattedOutOfCategory outOf={value as OutOfCategory}/>;
            }
            if (isDateObj(value)) {
                return (value as DateObj).value;
            }
            return String(value);
        case "NONE":
        default:
            return String(value);
    }
}

export function calculatePermission(dataType: DatapointType, filter?: GenericFilter) {
    if (!dataType) return '';
    const asPermission = differingPermissions[dataType] || dataType.toLowerCase().replaceAll('_', '.');
    if (filter) {
        if (filter.player) return 'data.player.' + asPermission;
        if (filter.server?.length) return 'data.server.' + asPermission;
    }
    return 'data.network.' + asPermission;
}

export function QueryDatapoint<K extends DatapointType>({
                                                            dataType,
                                                            filter,
                                                            fallbackUnavailableExplanation,
                                                            ...props
                                                        }: Props<K>) {
    const {hasPermission} = useAuth();
    const allowed = hasPermission(calculatePermission(dataType, filter));
    const {data, isFetching, error} = useDatapointQuery(allowed, dataType, filter);

    if (!allowed) return null;
    if (error) {
        if (error.status === 404) {
            return <DatapointComponent
                {...props}
                value={'generic.noData'}
                valueLabel={fallbackUnavailableExplanation
                    ? <span title={fallbackUnavailableExplanation}><FontAwesomeIcon icon={faQuestionCircle}/></span>
                    : undefined}
            />
        }
        console.error(error);
        return <DatapointComponent
            {...props}
            value={error.message}
        />
    }

    const name = data && isDateObj(data.value) ? <>{props.name} (<FormattedDate
        date={(data.value as DateObj).date}/>)</> : props.name

    return <DatapointComponent
        {...props}
        name={name}
        value={data && !isFetching ? <Format value={data.value} formatType={data.formatType}/> : <DatapointLoader/>}
    />
}

export function QueryDatapointValue<K extends DatapointType>({dataType, filter}: ValueProps<K>) {
    const {hasPermission} = useAuth();
    const allowed = hasPermission(calculatePermission(dataType, filter));
    const {data, isFetching, error} = useDatapointQuery(allowed, dataType, filter);
    const {t} = useTranslation();

    if (!allowed) return null;
    if (error) {
        if (error.status === 404) {
            return t('generic.noData');
        }
        console.error(error);
        return error.message;
    }

    return data && !isFetching ? <Format value={data.value} formatType={data.formatType}/> : <DatapointLoader/>
}

type TrendProps<K extends NumericDatapointType> = { downGood?: boolean, filter2: GenericFilter } & ValueProps<K>

export function QueryDatapointTrend<K extends NumericDatapointType>({
                                                                        dataType,
                                                                        downGood,
                                                                        filter,
                                                                        filter2
                                                                    }: TrendProps<K>) {
    const {hasPermission} = useAuth();
    const allowed = hasPermission(calculatePermission(dataType, filter));
    const {data: before, error: error1} = useDatapointQuery(allowed, dataType, filter);
    const {data: after, error: error2} = useDatapointQuery(allowed, dataType, filter2);

    if (!allowed) return null;
    if (error1) {
        console.error(error1);
        return error1.message;
    }
    if (error2) {
        console.error(error2);
        return error2.message;
    }

    return before && after ?
        <BigTrend trend={{
            text: before.value - after.value,
            reversed: downGood,
            direction: before.value - after.value === 0 ? undefined : (before.value - after.value > 0 ? '+' : '-')
        }} format={(value: number) => <Format value={value} formatType={before.formatType}/>}/>
        : <DatapointLoader/>
}


export function useDatapointQuery<K extends DatapointType>(allowed: boolean, dataType: K, filter?: GenericFilter) {
    const {updateRequested} = useNavigation() as { updateRequested: number };
    const prevUpdateRef = useRef<number | undefined>(undefined);
    const query = useQuery({
        queryKey: filter ? ['datapoint', dataType, ...Object.values(filter)] : ['datapoint', dataType],
        queryFn: () => getDatapoint(dataType, filter),
        retry: queryRetry,
        enabled: Boolean(allowed)
    });
    useEffect(() => {
        if (allowed && prevUpdateRef.current && prevUpdateRef.current <= updateRequested) {
            query.refetch()
        }
        prevUpdateRef.current = updateRequested;
    }, [updateRequested, allowed]);
    return query;
}

export function useDatapointQueries<K extends DatapointType>(allowed: boolean, dataType: K, filters: GenericFilter[]) {
    const {updateRequested} = useNavigation() as { updateRequested: number };
    const prevUpdateRef = useRef<number | undefined>(undefined);
    const query = useQueries({
        queries: filters.map(filter => ({
            queryKey: filter ? ['datapoint', dataType, ...Object.values(filter)] : ['datapoint', dataType],
            queryFn: () => getDatapoint(dataType, filter),
            retry: queryRetry,
            enabled: Boolean(allowed)
        }))
    });
    useEffect(() => {
        if (allowed && prevUpdateRef.current && prevUpdateRef.current <= updateRequested) {
            query.forEach(q => q.refetch())
        }
        prevUpdateRef.current = updateRequested;
    }, [updateRequested, allowed]);
    return query;
}


async function getDatapoint<K extends DatapointType>(dataType: K, filter?: GenericFilter) {
    const url = getDatapointUrl(dataType, filter);
    const response = await fetch(url);
    if (!response.ok) throw {status: response.status, message: response.statusText, data: response.body};
    return await response.json() as Promise<Datapoint<K>>;
}