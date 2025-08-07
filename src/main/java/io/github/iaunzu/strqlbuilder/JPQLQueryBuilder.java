package io.github.iaunzu.strqlbuilder;

import io.github.iaunzu.strqlbuilder.chunks.Join;
import io.github.iaunzu.strqlbuilder.chunks.Join.JoinOn;

public interface JPQLQueryBuilder extends QueryBuilder<JPQLQueryBuilder> {

    /**
     * Appends an entity name with a <code>JOIN FETCH</code> statement.
     * <p>
     * Returns a {@link JoinOn} instance that will allow you to invoke
     * {@link JoinOn#on(String)} or {@link JoinOn#with(String)} and specify the
     * <code>ON</code> clause.
     *
     * This method should only be invoked on a JPQL instance.
     *
     * @param entity
     *               a {@code String} with the entity name.
     * @return a reference the {@link JoinOn} instance.
     */
    Join<JPQLQueryBuilder>.JoinOn joinfetch(String entity);

    /**
     * Appends an entity name with a <code>LEFT JOIN FETCH</code> statement.
     * <p>
     * Returns a {@link JoinOn} instance that will allow you to invoke
     * {@link JoinOn#on(String)} or {@link JoinOn#with(String)} and specify the
     * <code>ON</code> clause.
     *
     * This method should only be invoked on a JPQL instance.
     *
     * @param entity
     *               a {@code String} with the entity name.
     * @return a reference the {@link JoinOn} instance.
     */
    Join<JPQLQueryBuilder>.JoinOn leftjoinfetch(String entity);
}
