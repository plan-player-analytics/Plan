import {createContext, useCallback, useContext, useEffect, useMemo, useState} from "react";
import {randomUuid} from "../../util/uuid.js";
import {fetchPlayerJoinAddresses} from "../../service/serverService.js";
import {useNavigation} from "../navigationHook.jsx";
import {usePreferences} from "../preferencesHook.jsx";
import {useTranslation} from "react-i18next";

const JoinAddressListContext = createContext({});

export const JoinAddressListContextProvider = ({identifier, children}) => {
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
    const loadAddresses = useCallback(async () => {
        const {data, error} = await fetchPlayerJoinAddresses(updateRequested, identifier, true);
        setAllAddresses(data?.joinAddresses || [error]);
    }, [setAllAddresses, identifier, updateRequested]);
    useEffect(() => {
        loadAddresses();
    }, [loadAddresses]);

    const sharedState = useMemo(() => {
        return {list, add, remove, replace, allAddresses};
    }, [list, add, remove, replace, allAddresses]);
    return (<JoinAddressListContext.Provider value={sharedState}>
            {children}
        </JoinAddressListContext.Provider>
    )
}

export const useJoinAddressListContext = () => {
    return useContext(JoinAddressListContext);
}