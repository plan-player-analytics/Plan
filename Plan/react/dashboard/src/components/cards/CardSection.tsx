import {PropsWithChildren} from "react";
import styles from './CardSection.module.scss';
import {classNames} from "../../util/classNames";

type Props = {
    noPadding?: boolean
} & PropsWithChildren

export const CardSection = ({noPadding, children}: Props) => {
    return (
        <section className={classNames(styles.cardSection, {[styles.noPadding]: noPadding})}>
            {children}
        </section>
    )
}