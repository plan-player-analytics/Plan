export class RequestError extends Error {
    constructor(details) {
        super();

        for (const entry in Object.entries(details)) {
            this[entry[0]] = entry[1];
        }
    }
}