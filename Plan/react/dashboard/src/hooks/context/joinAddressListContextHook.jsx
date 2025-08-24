import {createContext, useCallback, useContext, useEffect, useMemo, useState} from "react";
import {randomUuid} from "../../util/uuid.js";
import {fetchPlayerJoinAddresses} from "../../service/serverService.js";
import {useNavigation} from "../navigationHook.jsx";
import {usePreferences} from "../preferencesHook.jsx";
import {useTranslation} from "react-i18next";

const JoinAddressListContext = createContext({});

export const JoinAddressListContextProvider = ({identifier, children, loadIndividualAddresses, isAllowed}) => {
    const {t} = useTranslation();
    const {updateRequested} = useNavigation();
    const {preferencesLoaded, getKeyedPreference, setSomePreferences} = usePreferences();
    const [list, setList] = useState([]);

    const updateList = useCallback(newValue => {
        setList(newValue);
        const userPreferences = {}
        userPreferences["join-addresses-" + identifier] = newValue;
        setSomePreferences(userPreferences);
    }, [setList])

    useEffect(() => {
        if (preferencesLoaded && !list.length) {
            const value = getKeyedPreference("join-addresses-" + identifier);
            if (value?.length) {
                setList(value)
            }
        }
    }, [list, setList, preferencesLoaded, getKeyedPreference]);

    const add = useCallback(() => {
        updateList([...list, {
            name: t('html.label.addressGroup').replace("{{n}}", list.length + 1),
            addresses: [],
            uuid: randomUuid()
        }])
    }, [updateList, list]);
    const remove = useCallback(index => {
        updateList(list.filter((f, i) => i !== index));
    }, [updateList, list]);
    const replace = useCallback((replacement, index) => {
        const newList = [...list];
        newList[index] = replacement;
        updateList(newList)
    }, [updateList, list]);

    const [allAddresses, setAllAddresses] = useState([]);
    const [playerAddresses, setPlayerAddresses] = useState(undefined);
    const loadAddresses = useCallback(async () => {
        if (!isAllowed) return;
        const {data, error} = await fetchPlayerJoinAddresses(updateRequested, identifier, !loadIndividualAddresses);
        setAllAddresses(data?.joinAddresses || [error]);
        setPlayerAddresses(data?.joinAddressByPlayer);
    }, [setAllAddresses, identifier, updateRequested, isAllowed]);
    useEffect(() => {
        loadAddresses();
    }, [loadAddresses]);

    const sharedState = useMemo(() => {
        return {list, add, remove, replace, allAddresses, playerAddresses};
    }, [list, add, remove, replace, allAddresses, playerAddresses]);
    return (<JoinAddressListContext.Provider value={sharedState}>
            {children}
        </JoinAddressListContext.Provider>
    )
}

export const useJoinAddressListContext = () => {
    return useContext(JoinAddressListContext);
}