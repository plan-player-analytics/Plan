import React, {useEffect, useState} from 'react';
import {faCompass} from "@fortawesome/free-solid-svg-icons";
import {useTranslation} from "react-i18next";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {InputGroup} from "react-bootstrap";
import {useLocation, useNavigate} from "react-router-dom";
import {useMetadata} from "../../hooks/metadataHook";
import {useAuth} from "../../hooks/authenticationHook";

const PageNavigationItem = ({page}) => {
    const {t} = useTranslation();
    const navigate = useNavigate();
    const location = useLocation();
    const {authRequired, loggedIn, user} = useAuth();
    const metadata = useMetadata();
    const [currentPage, setCurrentPage] = useState(undefined);
    const [items, setItems] = useState([]);

    useEffect(() => {
        const networkMetadata = metadata?.networkMetadata;
        if (networkMetadata && networkMetadata.servers) {
            const hasProxy = networkMetadata.servers.filter(server => server.proxy).length

            let newItems = [
                {id: 'players', displayName: t('html.label.players'), href: "/players", permission: 'page.players'},
                {
                    id: 'query',
                    displayName: t('html.query.title.text').replace('<', ''),
                    href: "/query",
                    permission: 'page.players'
                },
                ...networkMetadata.servers
                    .filter(server => !server.proxy)
                    .map(server => {
                        return {
                            id: 'server-' + server.serverUUID,
                            displayName: t('html.label.server') + ', ' + server.serverName,
                            href: '/server/' + server.serverUUID,
                            permission: 'page.server'
                        }
                    })
            ];

            if (hasProxy) {
                newItems.unshift({
                    id: 'network',
                    displayName: t('html.label.network'),
                    href: "/network",
                    permission: 'page.network'
                });
            }
            if (page) {
                newItems.unshift({id: 'page', displayName: page, href: location.pathname, permission: undefined})
            }
            if (authRequired && loggedIn) {
                newItems = newItems.filter(item => !item.permission || user.permissions.includes(item.permission))
            }
            setItems(newItems);
            setCurrentPage(newItems.find(item => location.pathname.startsWith(item.href))?.id);
        }
    }, [t, metadata, location, authRequired, loggedIn, user, page]);

    const getSharedPrefix = (one, two) => {
        let i = 0;
        while (one[i] && two[i] && one[i] === two[i]) {
            i++;
        }
        return one.substring(0, i);
    }

    const onSelect = ({target}) => {
        const selected = target.value;
        const selectedItem = items.find(item => item.id === selected);
        const selectedHref = selectedItem.href;
        const currentHref = document.location.pathname + document.location.hash;

        const sharedPrefix = getSharedPrefix(selectedHref, currentHref);
        if (sharedPrefix === '/server/') {
            // Moves to the same page of a different server, for example /server/{uuid}/performance
            navigate(selectedHref + currentHref.substring(selectedHref.length));
        } else {
            navigate(selectedHref);
        }
    }

    if (!items.length) {
        return <li className={"nav-item nav-button nav-link"}
                   style={{
                       padding: "0.5rem",
                       paddingLeft: "1rem",
                       paddingRight: "1rem"
                   }}>
            <p className={"p-0 m-0"}>&nbsp;</p>
        </li>
    }

    return (
        <li className={"nav-item nav-button nav-link"}
            style={{padding: "1rem"}}>
            <InputGroup>
                <div className="input-group-text bg-theme col-white"
                     style={{paddingLeft: "0.5rem", paddingRight: "0.5rem"}}><Fa icon={faCompass}/></div>
                <select onChange={onSelect}
                        aria-label="Page selector"
                        className="form-select form-select-sm scrollbar"
                        id="pageSelector"
                        defaultValue={currentPage ? currentPage : items[0].id}>
                    {items.map(item =>
                        <option key={item.id} value={item.id}>{item.displayName}</option>)}
                </select>
            </InputGroup>
        </li>
    )
};

export default PageNavigationItem