import {PropsWithChildren} from "react";
import styles from './CardSection.module.scss';

export const CardSection = ({children}: PropsWithChildren) => {
    return (
        <section className={styles.cardSection}>
            {children}
        </section>
    )
}