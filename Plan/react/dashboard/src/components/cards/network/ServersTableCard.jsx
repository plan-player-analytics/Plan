import React, {useCallback, useState} from 'react';
import {Card, Dropdown} from "react-bootstrap";
import ServersTable, {ServerSortOption} from "../../table/ServersTable";
import {faNetworkWired} from "@fortawesome/free-solid-svg-icons";
import {useTranslation} from "react-i18next";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {CardLoader} from "../../navigation/Loader";

const SortDropDown = ({sortBy, sortReversed, setSortBy}) => {
    const {t} = useTranslation();

    const sortOptions = Object.values(ServerSortOption);

    const getSortIcon = useCallback(() => {
        return sortReversed ? sortBy.iconDesc : sortBy.iconAsc;
    }, [sortBy, sortReversed]);

    return (
        <Dropdown className="float-end" style={{position: "absolute", right: "0.5rem"}}>
            <Dropdown.Toggle variant=''>
                <Fa icon={getSortIcon()}/> {t(sortBy.label)}
            </Dropdown.Toggle>

            <Dropdown.Menu>
                <h6 className="dropdown-header">{t('html.label.sortBy')}</h6>
                {sortOptions.map(option => (
                    <Dropdown.Item key={option} onClick={() => setSortBy(option)}>
                        {t(option.label)}
                    </Dropdown.Item>
                ))}
            </Dropdown.Menu>
        </Dropdown>
    )
}

const ServersTableCard = ({loaded, servers, onSelect}) => {
    const {t} = useTranslation();
    const [sortBy, setSortBy] = useState(ServerSortOption.ALPHABETICAL);
    const [sortReversed, setSortReversed] = useState(false);

    if (!loaded) {
        return <CardLoader/>
    }

    const setSort = option => {
        if (sortBy === option) {
            setSortReversed(!sortReversed);
        } else {
            setSortBy(option);
            setSortReversed(false);
        }
    }

    return (
        <Card>
            <Card.Header style={{width: "100%"}}>
                <h6 className="col-text">
                    <Fa icon={faNetworkWired} className={"col-servers"}/> {t('html.label.servers')}
                </h6>
                <SortDropDown sortBy={sortBy} setSortBy={setSort} sortReversed={sortReversed}/>
            </Card.Header>
            {!servers.length && <Card.Body>
                <p>No servers found in the database.</p>
                <p>It appears that Plan is not installed on any game servers or not connected to the same database.
                    See <a href="https://github.com/plan-player-analytics/Plan/wiki">wiki</a> for Network tutorial.</p>
            </Card.Body>}
            {Boolean(servers.length) && <ServersTable servers={servers}
                                                      onSelect={onSelect}
                                                      sortBy={sortBy}
                                                      sortReversed={sortReversed}/>}
        </Card>
    )
};

export default ServersTableCard