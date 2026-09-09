import {Card} from "react-bootstrap";
import React, {useMemo} from "react";
import {CardLoader} from "../../navigation/Loader.tsx";
import ServerPie from "../../graphs/ServerPie";
import {faNetworkWired} from "@fortawesome/free-solid-svg-icons";
import CardHeader from "../CardHeader.tsx";
import {ErrorViewCard} from "../../../views/ErrorView.tsx";
import {useThemeStorage} from "../../../hooks/context/themeContextHook.tsx";
import {nameToCssVariable} from "../../../util/colors.js";
import {useServerPie} from "../../../dataHooks/graphHooks.ts";
import {useGenericFilter} from "../../../dataHooks/genericFilterContextHook.tsx";
import {TitleWithDates} from "../../text/TitleWithDates.tsx";
import {MS_MONTH} from "../../../util/format/useDateFormatter.js";

const ServerPieCard = () => {
    const {after, before, server} = useGenericFilter();
    const filter = useMemo(() => ({
        after,
        afterMillisAgo: after ? undefined : MS_MONTH,
        before,
        server
    }), [after, before, server]);

    const {data, error} = useServerPie(filter);
    const {usedUseCases} = useThemeStorage()

    if (error) return <ErrorViewCard error={error}/>;
    if (!data) return <CardLoader/>;

    const series = data.value.slices;
    const colors = usedUseCases?.graphs?.pie?.colors?.map(nameToCssVariable);
    const title = <TitleWithDates label={'html.label.serverPlaytime'} fallback={'html.label.serverPlaytime30days'}
                                  after={filter.after} before={filter.before}/>;

    return (
        <Card>
            <CardHeader icon={faNetworkWired} color={'sessions'} label={title}/>
            <ServerPie series={series} colors={colors}/>
        </Card>
    )
}

export default ServerPieCard;