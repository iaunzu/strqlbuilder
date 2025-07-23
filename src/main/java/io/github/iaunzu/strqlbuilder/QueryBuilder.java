package io.github.iaunzu.strqlbuilder;

import io.github.iaunzu.strqlbuilder.chunks.Join;
import io.github.iaunzu.strqlbuilder.chunks.Join.JoinOn;
import io.github.iaunzu.strqlbuilder.chunks.OrderBy;
import io.github.iaunzu.strqlbuilder.hibernate.StrTypedQuery;
import io.github.iaunzu.strqlbuilder.pagination.PagedTypedQuery;
import jakarta.persistence.EntityManager;
import java.util.Map;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface QueryBuilder<T extends QueryBuilder<T>> {

    /**
     * Returns a {@code String} with the complete SQL/JPQL query.
     *
     * @return a SQL/JPQL statement
     */
    String build();

    /**
     * Returns a {@code String} with a SQL with parameters replaced with values.
     *
     * @since 1.1.0
     * @return a {@code String} with a SQL.
     */
    String toSQL();

    /**
     * Appends one or multiple column name statements with or without alias.
     * <p>
     * To map a column name to a property of a POJO, you may use an alias. If
     * needed, you can also use quotes (<code>""</code>).
     * <p>
     * For example, you can map a field <code>name</code> of a table/entity
     * <code>profiles</code> to a property <code>profileName</code> in a ProfileData.
     *
     * <pre>
     * public class ProfileData {
     *     String profileName;
     *     (...)
     * }
     *
     * StrQLBuilder.createNative()
     *     .select("p.name as profileName")
     *     .from("profiles p")
     *     .createQuery(entityManager, ProfileData.class);
     * </pre>
     * <p>
     * Mapping to an inner POJO can be done with a <code>.</code> operator.
     *
     * <pre>
     * public class UserData {
     *     ProfileData profile;
     *     (...)
     * }
     *
     * StrQLBuilder.createNative()
     *     .select("p.name as profile.profileName")
     *     .from("profiles p")
     *     .createQuery(entityManager, UserData.class);
     * </pre>
     *
     * @param select
     *               a {@code String} with column names and aliases, separated by a
     *               comma.
     * @return a reference to this object.
     */
    T select(String select);

    /**
     * Appends one or multiple column name statements with or without alias, and
     * the parameters associated to them.
     *
     * <pre>
     * StrQLBuilder.createNative().select("CASE WHEN p.name IS NOT NULL THEN :p1 END as hasName", true)
     * 	.from("profiles p")
     * </pre>
     *
     * @param select
     *               a {@code String} with column names and aliases, separated by a
     *               comma.
     * @param values
     *               parameter values of the condition.
     * @return a reference to this object.
     * @see #select(String)
     */
    T select(String select, Object... values);

    /**
     * Appends a <code>SELECT DISTINCT</code>statement. Equivalent to
     * {@code .select("distinct" + select, values)}.
     *
     * @param select
     *               a {@code String} with column names and aliases, separated by a
     *               comma.
     * @param values
     *               parameter values of the condition
     * @return a reference to this object.
     * @see #select(String, Object...)
     */
    T selectDistinct(String select, Object... values);

    /**
     * Appends a {@code count()} selection. Equivalent to
     * {@code .select("count(" + count + ")")}.
     *
     * @param count
     *              a {@code String} as argument to the <code>COUNT</code> aggregator.
     * @return a reference to this object.
     */
    T count(String count);

    /**
     * Appends a table/entity name to the <code>FROM</code> clause. NOTE: this
     * method only exists for Eclipse Content-Assist issues.
     *
     * @param from
     *             a {@code String} with the <code>FROM</code> clause.
     * @return a reference to this object.
     */
    T from(String from);

    /**
     * Appends a table/entity name to the <code>FROM</code> clause, and the
     * parameters associated to it.
     *
     * @param from
     *               a {@code String} with the <code>FROM</code> clause.
     * @param values
     *               parameter values of the condition.
     * @return a reference to this object.
     */
    T from(String from, Object... values);

    /**
     * Appends a table/entity name with a <code>JOIN</code> statement.
     * <p>
     * Returns a {@link JoinOn} instance that will allow you to invoke
     * {@link JoinOn#on(String)} or {@link JoinOn#with(String)} and specify the
     * <code>ON</code> clause.
     *
     * @param table
     *              a {@code String} with the table/entity name.
     * @return a reference the {@link JoinOn} instance.
     */
    Join<T>.JoinOn join(String table);

    /**
     * Appends a table/entity name with a <code>LEFT JOIN</code> statement.
     * <p>
     * Returns a {@link JoinOn} instance that will allow you to invoke
     * {@link JoinOn#on(String)} or {@link JoinOn#with(String)} and specify the
     * <code>ON</code> clause.
     *
     * @param table
     *              a {@code String} with the table/entity name.
     * @return a reference the {@link JoinOn} instance.
     */
    Join<T>.JoinOn leftjoin(String table);

    /**
     * Appends a table/entity name with a <code>RIGHT JOIN</code> statement.
     * <p>
     * Returns a {@link JoinOn} instance that will allow you to invoke
     * {@link JoinOn#on(String)} or {@link JoinOn#with(String)} and specify the
     * <code>ON</code> clause.
     *
     * @param table
     *              a {@code String} with the table/entity name.
     * @return a reference the {@link JoinOn} instance.
     */
    Join<T>.JoinOn rightjoin(String table);

    /**
     * Appends a table/entity name with a <code>FULL JOIN</code> statement.
     * <p>
     * Returns a {@link JoinOn} instance that will allow you to invoke
     * {@link JoinOn#on(String)} or {@link JoinOn#with(String)} and specify the
     * <code>ON</code> clause.
     *
     * @param table
     *              a {@code String} with the table/entity name.
     * @return a reference the {@link JoinOn} instance.
     */
    Join<T>.JoinOn fulljoin(String table);

    /**
     * Sets <code>WHERE</code> as last clause, in order to invoke
     * {@link #and(String)} or {@link #andlike(String, String, String)}
     * accordingly.
     *
     * @return a reference to this object.
     */
    T where();

    /**
     * Appends a new condition to the <code>WHERE</code> clause.
     *
     * @param str
     *            a {@code String} with the condition.
     * @return a reference to this object.
     */
    T where(String str);

    /**
     * Appends a new condition to the <code>WHERE</code> clause, and the parameters
     * associated to it.
     * <p>
     * If there is only one argument and {@code null}, it does not add the
     * condition.
     *
     * @param str
     *               a {@code String} with the condition.
     * @param values
     *               parameter values of the condition.
     * @return a reference to this object.
     */
    T where(String str, Object... values);

    /**
     * Appends a <code>GROUP BY</code> clause.
     *
     * @param groupBy
     *                a {@code String} with the group clause.
     * @return a reference to this object.
     */
    T groupBy(String groupBy);

    /**
     * Appends a <code>HAVING</code> clause, and the parameters associated to it.
     *
     * @param having
     *               a {@code String} with the <code>HAVING</code> clause.
     * @param values
     *               parameter values of the condition.
     * @return a reference to this object.
     */
    T having(String having, Object... values);

    /**
     * Sets an order through an {@link OrderBy} instance, which contains
     * multi-column ordering.
     *
     * @param orderBy
     *                a {@link OrderBy} instance
     * @return a reference to this object.
     */
    T order(OrderBy<T> orderBy);

    /**
     * Sets an order through an {@link Sort} instance, which contains
     * multi-column ordering.
     *
     * @param sort
     *             a {@link Sort} instance
     * @return a reference to this object.
     */
    T order(Sort sort);

    /**
     *
     * Appends a new condition to the <code>WHERE</code> or <code>JOIN</code> clause,
     * depending on the last method invoked.
     * <p>
     * For example, this example appends it to <code>WHERE</code> clause:
     *
     * <pre>
     * StrQLBuilder.createNative()
     * 	.select("u.name, p.name")
     * 	.from("users u")
     * 	.leftjoin("profiles p").on("u.id_profile = p.id_profile")
     * 	.where("u.name is not null")
     * 	.and("p.id_profile = 1");
     *
     * SELECT u.name, p.name
     * FROM users u
     * LEFT JOIN profiles p ON u.id_profile = p.id_profile
     * WHERE u.name IS NOT NULL
     * AND p.id_profile = 1;
     * </pre>
     *
     * But, this example appends it to the <code>JOIN</code> clause:
     *
     * <pre>
     * StrQLBuilder.createNative()
     * 	.select("u.name, p.name")
     * 	.from("users u")
     * 	.leftjoin("profiles p").on("u.id_profile = p.id_profile").and("p.id_profile = 1")
     * 	.where("u.name is not null");
     *
     * SELECT u.name, p.name
     * FROM users u
     * LEFT JOIN profiles p ON u.id_profile = p.id_profile AND p.id_profile = 1
     * WHERE u.name IS NOT NULL;
     * </pre>
     *
     * If there is only one argument and {@code null}, it does not add the
     * condition.
     *
     * @param and
     *            a {@code String} with the condition.
     * @return a reference to this object.
     * @throws ParseSqlException
     *                           if this method is invoked before invoking
     *                           {@link #join(String)} or {@link #where()}.
     */
    T and(String and);

    /**
     * Appends a new condition to the <code>WHERE</code> or <code>JOIN</code> clause,
     * depending on the last method invoked, and the parameters associated to
     * it.
     * <p>
     * If there is only one argument and {@code null}, it does not add the
     * condition.
     *
     * @param and
     *               a {@code String} with the condition.
     * @param values
     *               parameter values of the condition.
     * @return a reference to this object.
     * @throws ParseSqlException
     *                           if this method is invoked before invoking
     *                           {@link #join(String)} or {@link #where()}.
     * @see #and(String)
     */
    T and(String and, Object... values);

    /**
     * Appends a new <code>LIKE</code> operator condition to the <code>WHERE</code> or
     * <code>JOIN</code> clause, depending on the last method invoked, and the
     * parameters associated.
     * <p>
     * If the argument is {@code null}, it does not add the condition.
     *
     * @param alias
     *                  a {@code String} with the column alias to compare to.
     * @param paramName
     *                  an arbitrary {@code String} with the parameter name.
     * @param value
     *                  parameter value of the condition.
     * @return a reference to this object.
     * @throws ParseSqlException
     *                           if this method is invoked before invoking
     *                           {@link #join(String)} or {@link #where()}.
     * @see #and(String)
     */
    T andlike(String alias, String paramName, String value);

    Map<? extends String, ? extends Object> buildParametersMap();

    /**
     * Create an instance of {@code jakarta.persistence.TypedQuery<X>} for
     * executing a query.
     *
     * @param entityManager
     *                      the {@link jakarta.persistence.EntityManager} that will
     *                      manage
     *                      transaction.
     * @param clazz
     *                      the class of the resulting instance(s).
     * @return the new query instance
     */
    <X> StrTypedQuery<X> createQuery(EntityManager entityManager, Class<X> clazz);

    /**
     * Create an instance of
     * {@code io.github.iaunzu.strqlbuilder.pagination.PagedTypedQuery<X>} for
     * executing a paged query.
     *
     * @param entityManager
     *                      the {@link jakarta.persistence.EntityManager} that will
     *                      manage
     *                      transaction.
     * @param clazz
     *                      Map&lt;Integer, Object&gt; positionParametersMap = new
     *                      HashMapMap&lt;Integer, Object&gt;();
     *                      ParameterTranslations
     *                      parameterTranslations =
     *                      translator.getParameterTranslations();
     *                      for (Entry&lt;Integer, Object&gt; parameter :
     *                      parametersMap.entrySet()) { String name =
     *                      parameter.getKey();
     *                      for (int position :
     *                      parameterTranslations.getNamedParameterSqlLocations(name))
     *                      {
     *                      positionParametersMap.put(position + 1,
     *                      parameter.getValue());
     *                      // Note that the +1 on the position is needed because of
     *                      a
     *                      mismatch between 0-based and 1-based indexing of both
     *                      APIs. }
     *                      the class of the resulting instance(s).
     * @param pageable
     *                      an object with pagination info.
     * @return the new query instance
     */
    <X> PagedTypedQuery<X> createPagedQuery(EntityManager entityManager, Class<X> clazz, Pageable pageable);
}
