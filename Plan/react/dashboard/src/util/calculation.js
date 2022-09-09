export const calculateSum = array => {
    let sum = 0;
    for (let item of array) {
        if (typeof (item) === "number") sum += item;
    }
    return sum;
}