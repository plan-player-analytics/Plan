import {Card} from "react-bootstrap";
import React from "react";
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

const ServerPieCard = () => {
    const filter = useGenericFilter();
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