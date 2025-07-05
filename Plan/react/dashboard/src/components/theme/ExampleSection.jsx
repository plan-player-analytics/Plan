import {SidebarUseCase} from "./usecase/SidebarUseCase.jsx";
import InfoBoxUseCase from "./usecase/InfoBoxUseCase.jsx";
import {ChartLoader} from "../navigation/Loader.jsx";
import TrendUseCase from "./usecase/TrendUseCase.jsx";
import CardUseCase from "./usecase/CardUseCase.jsx";
import CalendarUseCase from "./usecase/CalendarUseCase.jsx";
import DataUseCase from "./usecase/DataUseCase.jsx";
import {faChartLine, faServer} from "@fortawesome/free-solid-svg-icons";
import DataPlayUseCase from "./usecase/DataPlayUseCase.jsx";
import DataPlayersUseCase from "./usecase/DataPlayersUseCase.jsx";
import DataPerformanceUseCase from "./usecase/DataPerformanceUseCase.jsx";
import DataCalculatedUseCase from "./usecase/DataCalculatedUseCase.jsx";
import DataPlayerVersusUseCase from "./usecase/DataPlayerVersusUseCase.jsx";
import DataPlayerStatusUseCase from "./usecase/DataPlayerStatusUseCase.jsx";
import React from "react";
import {useTranslation} from "react-i18next";
import CollapseWithButton from "../layout/CollapseWithButton.jsx";
import FormsUseCase from "./usecase/FormsUseCase.jsx";

const findExample = (path, examples) => {
    if (!path || !path.length) return undefined;
    const found = examples[path];
    if (found) return found;
    return findExample(path.split('.').slice(0, -1).join('.'), examples);
}

const ExampleSection = ({displayedItem, className}) => {
    const {t} = useTranslation();
    const examples = {
        "sidebar": <SidebarUseCase/>,
        "layout.background": <SidebarUseCase/>,
        "layout.title": <SidebarUseCase/>,
        "infoBox": <InfoBoxUseCase/>,
        "layout.loader": <ChartLoader/>,
        "data.trend": <TrendUseCase/>,
        "cards": <CardUseCase/>,
        "layout.helpIcon": <CardUseCase/>,
        "layout.divider": <CardUseCase/>,
        "calendar": <CalendarUseCase/>,
        "data.servers": <DataUseCase label={"servers"} icon={faServer} card/>,
        "data.play": <DataPlayUseCase/>,
        "data.players": <DataPlayersUseCase/>,
        "data.playerPeakLast": <DataUseCase label={"player-peak-last"} icon={faChartLine} card/>,
        "data.playerPeakAllTime": <DataUseCase label={"player-peak-all-time"} icon={faChartLine} card/>,
        "data.performance": <DataPerformanceUseCase/>,
        "data.calculated": <DataCalculatedUseCase/>,
        "data.playerVersus": <DataPlayerVersusUseCase/>,
        "data.playerStatus": <DataPlayerStatusUseCase/>,
        "forms": <FormsUseCase/>
    }
    const example = findExample(displayedItem, examples);

    return (
        <div className={"example-section " + className}
             style={{position: 'sticky', top: '0'}}>
            <CollapseWithButton disabled={!example} open title={<h5
                className={"col-text"}>{t('html.label.themeEditor.example')}{example ? <>{' '}&middot; {displayedItem}</> : ''}</h5>}>
                {example && <div className={"example"}>
                    {example}
                </div>}
            </CollapseWithButton>
        </div>
    )
}

export default ExampleSection;