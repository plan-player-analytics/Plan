import {createContext, useCallback, useContext, useEffect, useMemo, useState} from "react";
import {randomUuid} from "../../util/uuid.js";
import {fetchPlayerJoinAddresses} from "../../service/serverService.js";
import {useNavigation} from "../navigationHook.jsx";

const JoinAddressListContext = createContext({});

export const JoinAddressListContextProvider = ({identifier, children}) => {
    const {updateRequested} = useNavigation();
    const [list, setList] = useState([]);

    const add = useCallback(() => {
        setList([...list, {name: "Address group " + (list.length + 1), addresses: [], uuid: randomUuid()}])
    }, [list, setList]);
    const remove = useCallback(index => {
        setList(list.filter((f, i) => i !== index));
    }, [setList, list]);
    const replace = useCallback((replacement, index) => {
        const newList = [...list];
        newList[index] = replacement;
        setList(newList)
    }, [setList, list]);

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
    }, [list, add, remove, replace]);
    return (<JoinAddressListContext.Provider value={sharedState}>
            {children}
        </JoinAddressListContext.Provider>
    )
}

export const useJoinAddressListContext = () => {
    return useContext(JoinAddressListContext);
}