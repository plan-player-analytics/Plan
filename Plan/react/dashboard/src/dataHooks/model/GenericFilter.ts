export type GenericFilter = {
    after?: number;
    afterMillisAgo?: number;
    before?: number;
    beforeMillisAgo?: number;
    server?: string[] | string;
    player?: string;
}

export const filterToQueryString = (filter?: GenericFilter) => {
    if (!filter) return "";
    let query = [];
    if (filter.after) query.push("after=" + filter.after);
    if (filter.afterMillisAgo) query.push("afterMillisAgo=" + filter.afterMillisAgo);
    if (filter.before) query.push("before=" + filter.before);
    if (filter.beforeMillisAgo) query.push("beforeMillisAgo=" + filter.beforeMillisAgo);
    if (filter.server) query.push("server=" + (Array.isArray(filter.server) ? filter.server.join() : filter.server));
    if (filter.player) query.push("player=" + filter.player);
    return query.join("&");
}