import { SidebarUseCase } from './usecase/SidebarUseCase.tsx';
import InfoBoxUseCase from './usecase/InfoBoxUseCase.tsx';
import { ChartLoader } from '../navigation/Loader.tsx';
import TrendUseCase from './usecase/TrendUseCase.jsx';
import CardUseCase from './usecase/CardUseCase.tsx';
import CalendarUseCase from './usecase/CalendarUseCase.jsx';
import DataUseCase from './usecase/DataUseCase.tsx';
import { faChartLine, faServer } from '@fortawesome/free-solid-svg-icons';
import DataPlayUseCase from './usecase/DataPlayUseCase.tsx';
import DataPlayersUseCase from './usecase/DataPlayersUseCase.tsx';
import DataPerformanceUseCase from './usecase/DataPerformanceUseCase.tsx';
import DataCalculatedUseCase from './usecase/DataCalculatedUseCase.tsx';
import DataPlayerVersusUseCase from './usecase/DataPlayerVersusUseCase.tsx';
import DataPlayerStatusUseCase from './usecase/DataPlayerStatusUseCase.tsx';
import React from 'react';
import { useTranslation } from 'react-i18next';
import CollapseWithButton from '../layout/CollapseWithButton.tsx';
import FormsUseCase from './usecase/FormsUseCase.jsx';
import { addToObject } from '../../util/mutator.js';
import { graphUseCases } from './usecase/GraphUseCases.jsx';
import { TableUseCase } from './usecase/TableUseCases.tsx';
import { formatLabel } from './UseCase.jsx';
import TabsUseCase from './usecase/TabsUseCase.tsx';
import DataPluginsUseCase from './usecase/DataPluginsUseCase.tsx';

const findExample = (path, examples) => {
    if (!path?.length) return undefined;
    const found = examples[path];
    if (found) return found;
    return findExample(path.split('.').slice(0, -1).join('.'), examples);
};

const ExampleSection = ({ displayedItem, className }) => {
    const { t } = useTranslation();
    const examples = {
        sidebar: <SidebarUseCase />,
        'layout.background': <SidebarUseCase />,
        'layout.title': <SidebarUseCase />,
        tabs: <TabsUseCase />,
        infoBox: <InfoBoxUseCase />,
        'layout.loader': <ChartLoader />,
        'data.trend': <TrendUseCase />,
        cards: <CardUseCase />,
        'layout.helpIcon': <CardUseCase />,
        'layout.divider': <CardUseCase />,
        calendar: <CalendarUseCase />,
        'data.servers': <DataUseCase label={'servers'} icon={faServer} card />,
        'data.play': <DataPlayUseCase />,
        'data.players': <DataPlayersUseCase />,
        'data.playerPeakLast': (
            <DataUseCase label={'player-peak-last'} icon={faChartLine} card />
        ),
        'data.playerPeakAllTime': (
            <DataUseCase
                label={'player-peak-all-time'}
                icon={faChartLine}
                card
            />
        ),
        'data.performance': <DataPerformanceUseCase />,
        'data.calculated': <DataCalculatedUseCase />,
        'data.playerVersus': <DataPlayerVersusUseCase />,
        'data.playerStatus': <DataPlayerStatusUseCase />,
        forms: <FormsUseCase />,
        tables: <TableUseCase />,
        plugin: <DataPluginsUseCase />,
    };
    addToObject(examples, graphUseCases);
    const example = findExample(displayedItem, examples);

    const displayText = displayedItem
        ? displayedItem.split('.').map(formatLabel).join(' › ')
        : '';

    return (
        <div
            className={'example-section ' + className}
            style={{ position: 'sticky', top: '0' }}>
            <CollapseWithButton
                disabled={!example}
                open
                title={
                    <h5 className={'col-text'}>
                        {t('html.label.themeEditor.example')}
                        {example ? <> &middot; {displayText}</> : ''}
                    </h5>
                }>
                {example && <div className={'example'}>{example}</div>}
            </CollapseWithButton>
        </div>
    );
};

export default ExampleSection;
