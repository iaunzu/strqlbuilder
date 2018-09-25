package com.github.iaunzu.strqlbuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.hql.internal.ast.ASTQueryTranslatorFactory;
import org.hibernate.hql.spi.ParameterTranslations;
import org.hibernate.hql.spi.QueryTranslator;
import org.hibernate.hql.spi.QueryTranslatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import com.github.iaunzu.strqlbuilder.chunks.Chunk;
import com.github.iaunzu.strqlbuilder.chunks.From;
import com.github.iaunzu.strqlbuilder.chunks.GroupBy;
import com.github.iaunzu.strqlbuilder.chunks.Having;
import com.github.iaunzu.strqlbuilder.chunks.Join;
import com.github.iaunzu.strqlbuilder.chunks.Join.JoinOn;
import com.github.iaunzu.strqlbuilder.chunks.Join.JoinType;
import com.github.iaunzu.strqlbuilder.chunks.OrderBy;
import com.github.iaunzu.strqlbuilder.chunks.OrderBy.Direction;
import com.github.iaunzu.strqlbuilder.chunks.Parameter;
import com.github.iaunzu.strqlbuilder.chunks.Select;
import com.github.iaunzu.strqlbuilder.chunks.Where;
import com.github.iaunzu.strqlbuilder.chunks.like.CaseInsensitiveLike;
import com.github.iaunzu.strqlbuilder.exceptions.ParseSqlException;
import com.github.iaunzu.strqlbuilder.hibernate.StrTypedQuery;
import com.github.iaunzu.strqlbuilder.hibernate.StrTypedQueryFactory;
import com.github.iaunzu.strqlbuilder.pagination.PagedTypedQuery;
import com.github.iaunzu.strqlbuilder.pagination.PagedTypedQueryImpl;
import com.github.iaunzu.strqlbuilder.utils.StrQLUtils;

public class StrQLBuilder {

    public static final Logger log = LoggerFactory.getLogger(StrQLBuilder.class);

    private UnionType unionType;

    private StrQLBuilder unionParent;

    private Select select;

    private From from;

    private List<Join> joins;

    private Where where;

    private GroupBy groupBy;

    private Having having;

    private OrderBy orderBy;

    private List<StrQLBuilder> unions;

    private Map<String, StrQLBuilder> subSelects;

    private Chunk lastChunk;

    private boolean isNative = false;

    private StrQLBuilder(StrQLBuilder unionParent, UnionType unionType, boolean isNative) {
	this.unionParent = unionParent;
	this.unionType = unionType;
	this.isNative = isNative;

	select = new Select();
	from = new From();
	joins = new ArrayList<Join>();
	where = new Where(this);
	groupBy = new GroupBy();
	having = new Having();
	orderBy = null;
	unions = new ArrayList<StrQLBuilder>();
	subSelects = new HashMap<String, StrQLBuilder>();
    }

    /**
     * Factory method that returns an instance that builds native SQLs.
     * 
     * @return a {@code StrQLBuilder} instance that builds native SQLs.
     */
    public static final StrQLBuilder createNative() {
	return new StrQLBuilder(null, UnionType.NO_UNION, true);
    }

    /**
     * Factory method that returns an instance that builds JPQL queries.
     * 
     * @return a {@code StrQLBuilder} instance that builds JPQL queries.
     */
    public static final StrQLBuilder createJPQL() {
	return new StrQLBuilder(null, UnionType.NO_UNION, false);
    }

    /**
     * Appends one or multiple column name statements with or without alias.
     * <p>
     * To map a column name to a property of a POJO, you may use an alias. If
     * needed, you can also use quotes (<tt>""</tt>).
     * <p>
     * For example, you can map a field <tt>name</tt> of a table/entity
     * <tt>profiles</tt> to a property <tt>profileName</tt> in a ProfileData.
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
     * Mapping to an inner POJO can be done with a <tt>.</tt> operator.
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
     *            a {@code String} with column names and aliases, separated by a
     *            comma.
     * @return a reference to this object.
     */
    public StrQLBuilder select(String select) {
	return select(select, new Object[0]);
    }

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
     *            a {@code String} with column names and aliases, separated by a
     *            comma.
     * @param values
     *            parameter values of the condition.
     * @return a reference to this object.
     * @see #select(String)
     */
    public StrQLBuilder select(String select, Object... values) {
	lastChunk = this.select;
	this.select.select(select, values);
	return this;
    }

    /**
     * Appends a <tt>SELECT DISTINCT</tt>statement. Equivalent to
     * {@code .select("distinct" + select, values)}.
     * 
     * @param select
     *            a {@code String} with column names and aliases, separated by a
     *            comma.
     * @param values
     *            parameter values of the condition
     * @return a reference to this object.
     * @see #select(String, Object...)
     */
    public StrQLBuilder selectDistinct(String select, Object... values) {
	return select("distinct " + select, values);
    }

    /**
     * Appends a {@code count()} selection. Equivalent to
     * {@code .select("count(" + count + ")")}.
     * 
     * @param count
     *            a {@code String} as argument to the <tt>COUNT</tt> aggregator.
     * @return a reference to this object.
     */
    public StrQLBuilder count(String count) {
	return select("count(" + count + ")");
    }

    /**
     * Appends a table/entity name to the <tt>FROM</tt> clause. NOTE: this
     * method only exists for Eclipse Content-Assist issues.
     * 
     * @param from
     *            a {@code String} with the <tt>FROM</tt> clause.
     * @return a reference to this object.
     */
    public StrQLBuilder from(String from) {
	return from(from, new Object[0]);
    }

    /**
     * Appends a table/entity name to the <tt>FROM</tt> clause, and the
     * parameters associated to it.
     * 
     * @param from
     *            a {@code String} with the <tt>FROM</tt> clause.
     * @param values
     *            parameter values of the condition.
     * @return a reference to this object.
     */
    public StrQLBuilder from(String from, Object... values) {
	lastChunk = this.from;
	this.from.from(from, values);
	return this;
    }

    /**
     * Appends a table/entity name with a <tt>JOIN</tt> statement.
     * <p>
     * Returns a {@link JoinOn} instance that will allow you to invoke
     * {@link JoinOn#on(String)} or {@link JoinOn#with(String)} and specify the
     * <tt>ON</tt> clause.
     * 
     * @param table
     *            a {@code String} with the table/entity name.
     * @return a reference the {@link JoinOn} instance.
     */
    public JoinOn join(String table) {
	return join(JoinType.INNER_JOIN, table);
    }

    /**
     * Appends a table/entity name with a <tt>LEFT JOIN</tt> statement.
     * <p>
     * Returns a {@link JoinOn} instance that will allow you to invoke
     * {@link JoinOn#on(String)} or {@link JoinOn#with(String)} and specify the
     * <tt>ON</tt> clause.
     * 
     * @param table
     *            a {@code String} with the table/entity name.
     * @return a reference the {@link JoinOn} instance.
     */
    public JoinOn leftjoin(String table) {
	return join(JoinType.LEFT_JOIN, table);
    }

    /**
     * Appends a table/entity name with a <tt>RIGHT JOIN</tt> statement.
     * <p>
     * Returns a {@link JoinOn} instance that will allow you to invoke
     * {@link JoinOn#on(String)} or {@link JoinOn#with(String)} and specify the
     * <tt>ON</tt> clause.
     * 
     * @param table
     *            a {@code String} with the table/entity name.
     * @return a reference the {@link JoinOn} instance.
     */
    public JoinOn rightjoin(String table) {
	return join(JoinType.RIGHT_JOIN, table);
    }

    /**
     * Appends a table/entity name with a <tt>FULL JOIN</tt> statement.
     * <p>
     * Returns a {@link JoinOn} instance that will allow you to invoke
     * {@link JoinOn#on(String)} or {@link JoinOn#with(String)} and specify the
     * <tt>ON</tt> clause.
     * 
     * @param table
     *            a {@code String} with the table/entity name.
     * @return a reference the {@link JoinOn} instance.
     */
    public JoinOn fulljoin(String table) {
	return join(JoinType.FULL_JOIN, table);
    }

    /**
     * Appends an entity name with a <tt>JOIN FETCH</tt> statement.
     * <p>
     * Returns a {@link JoinOn} instance that will allow you to invoke
     * {@link JoinOn#on(String)} or {@link JoinOn#with(String)} and specify the
     * <tt>ON</tt> clause.
     * 
     * This method should only be invoked on a JPQL instance.
     * 
     * @param entity
     *            a {@code String} with the entity name.
     * @return a reference the {@link JoinOn} instance.
     * @see #createJPQL
     */
    public JoinOn joinfetch(String entity) {
	return join(JoinType.JOIN_FETCH, entity);
    }

    /**
     * Appends an entity name with a <tt>LEFT JOIN FETCH</tt> statement.
     * <p>
     * Returns a {@link JoinOn} instance that will allow you to invoke
     * {@link JoinOn#on(String)} or {@link JoinOn#with(String)} and specify the
     * <tt>ON</tt> clause.
     * 
     * This method should only be invoked on a JPQL instance.
     * 
     * @param entity
     *            a {@code String} with the entity name.
     * @return a reference the {@link JoinOn} instance.
     * @see #createJPQL
     */
    public JoinOn leftjoinfetch(String entity) {
	return join(JoinType.LEFT_JOIN_FETCH, entity);
    }

    private JoinOn join(JoinType joinType, String table) {
	Join join = new Join(this);
	this.joins.add(join);
	lastChunk = join;
	return join.join(joinType, table);
    }

    /**
     * Sets <tt>WHERE</tt> as last clause, in order to invoke
     * {@link #and(String)} or {@link #andlike(String, String, String)}
     * accordingly.
     * 
     * @return a reference to this object.
     */
    public StrQLBuilder where() {
	lastChunk = this.where;
	return this;
    }

    /**
     * Appends a new condition to the <tt>WHERE</tt> clause.
     * 
     * @param str
     *            a {@code String} with the condition.
     * @return a reference to this object.
     */
    public StrQLBuilder where(String str) {
	return where(str, new Object[0]);
    }

    /**
     * Appends a new condition to the <tt>WHERE</tt> clause, and the parameters
     * associated to it.
     * <p>
     * If there is only one argument and {@code null}, it does not add the
     * condition.
     * 
     * @param str
     *            a {@code String} with the condition.
     * @param values
     *            parameter values of the condition.
     * @return a reference to this object.
     */
    public StrQLBuilder where(String str, Object... values) {
	where();
	if (values != null && values.length == 1 && values[0] == null) {
	    return this;
	}
	return this.where.and(str, values);
    }

    /**
     * Appends a <tt>GROUP BY</tt> clause.
     * 
     * @param groupBy
     *            a {@code String} with the group clause.
     * @return a reference to this object.
     */
    public StrQLBuilder groupBy(String groupBy) {
	lastChunk = this.groupBy;
	this.groupBy.groupBy(groupBy);
	return this;
    }

    /**
     * Appends a <tt>HAVING</tt> clause, and the parameters associated to it.
     * 
     * @param having
     *            a {@code String} with the <tt>HAVING</tt> clause.
     * @param values
     *            parameter values of the condition.
     * @return a reference to this object.
     */
    public StrQLBuilder having(String having, Object... values) {
	lastChunk = this.having;
	this.having.having(having, values);
	return this;
    }

    /**
     * Sets an order through an {@link OrderBy} instance, which contains
     * multi-column ordering.
     * 
     * @param orderBy
     *            a {@link OrderBy} instance
     * @return a reference to this object.
     */
    public StrQLBuilder order(OrderBy orderBy) {
	this.orderBy = orderBy;
	lastChunk = this.orderBy;
	// save aliases to replace later if needed
	this.orderBy.setSelectAliases(this.select.getAliases());
	return this;
    }

    /**
     * Sets an order through an {@link Sort} instance, which contains
     * multi-column ordering.
     * 
     * @param sort
     *            a {@link Sort} instance
     * @return a reference to this object.
     */
    public StrQLBuilder order(Sort sort) {
	if (sort != null) {
	    for (Order order : sort) {
		String direction = order.getDirection().name().toUpperCase();
		String alias = order.getProperty();
		order(new OrderBy(alias, Direction.valueOf(direction)));
	    }
	}
	return this;
    }

    /**
     * 
     * Appends a new condition to the <tt>WHERE</tt> or <tt>JOIN</tt> clause,
     * depending on the last method invoked.
     * <p>
     * For example, this example appends it to <tt>WHERE</tt> clause:
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
     * But, this example appends it to the <tt>JOIN</tt> clause:
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
     *             if this method is invoked before invoking
     *             {@link #join(String)} or {@link #where()}.
     */
    public StrQLBuilder and(String and) {
	return and(and, new Object[0]);
    }

    /**
     * Appends a new condition to the <tt>WHERE</tt> or <tt>JOIN</tt> clause,
     * depending on the last method invoked, and the parameters associated to
     * it.
     * <p>
     * If there is only one argument and {@code null}, it does not add the
     * condition.
     * 
     * @param and
     *            a {@code String} with the condition.
     * @param values
     *            parameter values of the condition.
     * @return a reference to this object.
     * @throws ParseSqlException
     *             if this method is invoked before invoking
     *             {@link #join(String)} or {@link #where()}.
     * @see #and(String)
     */
    public StrQLBuilder and(String and, Object... values) {
	if (!(lastChunk instanceof Join || lastChunk instanceof Where)) {
	    throw new ParseSqlException("You can only call this method after invoking join() or where() methods.");
	}
	if (lastChunk instanceof Where && values != null && values.length == 1 && values[0] == null) {
	    return this;
	}
	return lastChunk.and(and, values);
    }

    /**
     * Appends a new <tt>LIKE</tt> operator condition to the <tt>WHERE</tt> or
     * <tt>JOIN</tt> clause, depending on the last method invoked, and the
     * parameters associated.
     * <p>
     * If the argument is {@code null}, it does not add the condition.
     * 
     * @param alias
     *            a {@code String} with the column alias to compare to.
     * @param paramName
     *            an arbitrary {@code String} with the parameter name.
     * @param value
     *            parameter value of the condition.
     * @return a reference to this object.
     * @throws ParseSqlException
     *             if this method is invoked before invoking
     *             {@link #join(String)} or {@link #where()}.
     * @see #and(String)
     */
    public StrQLBuilder andlike(String alias, String paramName, String value) {
	if (!(lastChunk instanceof Join || lastChunk instanceof Where)) {
	    throw new ParseSqlException("You can only call this method after invoking join() or where() methods.");
	}
	if (value == null) {
	    return this;
	}
	return lastChunk.andlike(new CaseInsensitiveLike(alias, paramName, value));
    }

    /**
     * Appends a <tt>UNION</tt> operator and returns a new {@link StrQLBuilder}
     * instance. Should invoke {@link #endUnion()} once you are done with the
     * new statement.
     * 
     * @return a reference to the new {@code StrQLBuilder} instance.
     */
    public StrQLBuilder union() {
	StrQLBuilder sql = new StrQLBuilder(this, UnionType.UNION, isNative);
	getRoot().unions.add(sql);
	return sql;
    }

    /**
     * Appends a <tt>UNION ALL</tt> operator and returns a new
     * {@link StrQLBuilder} instance. Should invoke {@link #endUnion()} once you
     * are done with the new statement.
     * 
     * @return a reference to the new {@code StrQLBuilder} instance.
     */
    public StrQLBuilder unionAll() {
	StrQLBuilder sql = new StrQLBuilder(this, UnionType.UNIONALL, isNative);
	getRoot().unions.add(sql);
	return sql;
    }

    /**
     * Appends new query through a <tt>UNION</tt> operator. No calls to
     * {@link #endUnion()} are needed.
     *
     * @param sql
     *            a {@code StrQLBuilder} instance to append with <tt>UNION</tt>.
     * @return a reference to this object.
     */
    public StrQLBuilder union(StrQLBuilder sql) {
	sql.unionType = UnionType.UNION;
	getRoot().unions.add(sql);
	return this;
    }

    /**
     * Appends new query through a <tt>UNION ALL</tt> operator. No calls to
     * {@link #endUnion()} are needed.
     *
     * @param sql
     *            a {@code StrQLBuilder} instance to append with
     *            <tt>UNION ALL</tt>.
     * @return a reference to this object.
     */
    public StrQLBuilder unionAll(StrQLBuilder sql) {
	sql.unionType = UnionType.UNIONALL;
	getRoot().unions.add(sql);
	return this;
    }

    /**
     * Returns the root {@code StrQLBuilder} instance.
     * <p>
     * Invoke this once you are done building the new instance after invoking
     * {@link #union()}.
     * 
     * @return a reference to the root {@link StrQLBuilder} instance.
     */
    public StrQLBuilder endUnion() {
	return getRoot();
    }

    protected StrQLBuilder getRoot() {
	if (unionType == UnionType.NO_UNION) {
	    assert (unionParent == null);
	    return this;
	}
	StrQLBuilder root = this.unionParent;
	while (root.unionType != UnionType.NO_UNION) {
	    assert (root.unionParent != null);
	    root = root.unionParent;
	}
	return root;
    }

    /**
     * Create an instance of {@code javax.persistence.TypedQuery<X>} for
     * executing a query.
     * 
     * @param entityManager
     *            the {@link javax.persistence.EntityManager} that will manage
     *            transaction.
     * @param clazz
     *            the class of the resulting instance(s).
     * @return the new query instance
     */
    public <X> StrTypedQuery<X> createQuery(EntityManager entityManager, Class<X> clazz) {
	return createQuery(entityManager, clazz, false);
    }

    /**
     * Create an instance of
     * {@code com.github.iaunzu.strqlbuilder.pagination.PagedTypedQuery<X>} for
     * executing a paged query.
     * 
     * @param entityManager
     *            the {@link javax.persistence.EntityManager} that will manage
     *            transaction.
     * @param clazz
     *            Map&lt;Integer, Object&gt; positionParametersMap = new
     *            HashMapMap&lt;Integer, Object&gt;(); ParameterTranslations
     *            parameterTranslations = translator.getParameterTranslations();
     *            for (Entry&lt;Integer, Object&gt; parameter :
     *            parametersMap.entrySet()) { String name = parameter.getKey();
     *            for (int position :
     *            parameterTranslations.getNamedParameterSqlLocations(name)) {
     *            positionParametersMap.put(position + 1, parameter.getValue());
     *            // Note that the +1 on the position is needed because of a
     *            mismatch between 0-based and 1-based indexing of both APIs. }
     *            the class of the resulting instance(s).
     * @param pageable
     *            an object with pagination info.
     * @return the new query instance
     */
    public <X> PagedTypedQuery<X> createPagedQuery(EntityManager entityManager, Class<X> clazz, Pageable pageable) {
	TypedQuery<X> pageQuery = createQuery(entityManager, clazz, false);
	pageQuery.setFirstResult((int) pageable.getOffset());
	pageQuery.setMaxResults(pageable.getPageSize());
	TypedQuery<Long> countQuery = createQuery(entityManager, Long.class, true);
	return new PagedTypedQueryImpl<X>(pageQuery, pageable, countQuery);
    }

    private <X> StrTypedQuery<X> createQuery(EntityManager entityManager, Class<X> clazz, boolean pagedCount) {
	StrQLBuilder oThis = this;
	while (oThis.unionParent != null) {
	    log.warn(
		    "Warning: you are trying to execute a query over an auxiliar instance -used to build union union queries-. Use .endUnion() to remove this message.");
	    oThis = oThis.unionParent;
	}
	String sql = oThis.build(pagedCount);
	Map<String, Object> parametersMap = oThis.getParametersMap();
	QueryTranslator translator = null;
	boolean fixJpqlCountQuery = !isNative && pagedCount;
	if (fixJpqlCountQuery) {
	    // always native
	    translator = translator(sql, entityManager);
	    sql = "select count(*) from (" + translator.getSQLString() + ") x";
	}
	StrTypedQuery<X> typedQuery = this.createTypedQuery(entityManager, sql, pagedCount);
	if (fixJpqlCountQuery) {
	    typedQuery.setPositionParameters(getPositionParametersMap(translator, parametersMap));
	} else {
	    typedQuery.setParameters(parametersMap);
	}
	typedQuery.setAlias(oThis.select.getAliases());
	typedQuery.setTargetClass(clazz);

	return typedQuery;
    }

    private <X> StrTypedQuery<X> createTypedQuery(EntityManager entityManager, String sql, boolean pagedCount) {
	SharedSessionContractImplementor session = entityManager.unwrap(SharedSessionContractImplementor.class);
	return StrTypedQueryFactory.create(session, sql, this.sqlNative(pagedCount));
    }

    private boolean sqlNative(boolean pagedCount) {
	return this.isNative || pagedCount;
    }

    private Map<Integer, Object> getPositionParametersMap(QueryTranslator translator,
	    Map<String, Object> parametersMap) {
	Map<Integer, Object> positionParametersMap = new HashMap<Integer, Object>();
	ParameterTranslations parameterTranslations = translator.getParameterTranslations();
	for (Entry<String, Object> parameter : parametersMap.entrySet()) {
	    String name = parameter.getKey();
	    for (int position : parameterTranslations.getNamedParameterInformation(name).getSourceLocations()) {
		positionParametersMap.put(position + 1, parameter.getValue());
		// Note that the +1 on the position is needed because of a
		// mismatch between 0-based and 1-based indexing of both APIs.
	    }
	}
	return positionParametersMap;
    }

    private QueryTranslator translator(String sql, EntityManager entityManager) {
	QueryTranslatorFactory translatorFactory = new ASTQueryTranslatorFactory();
	QueryTranslator translator = translatorFactory.createQueryTranslator(sql, sql, Collections.EMPTY_MAP,
		(SessionFactoryImplementor) entityManager.getEntityManagerFactory().unwrap(SessionFactory.class), null);
	translator.compile(Collections.EMPTY_MAP, false);
	return translator;
    }

    /**
     * Returns a {@code String} with the complete SQL/JPQL query.
     * 
     * @return a SQL/JPQL statement
     */
    public String build() {
	return build(false);
    }

    private String build(boolean pagedCount) {

	StringBuilder sql = new StringBuilder();
	sql.append(select.build());

	if (from.isNotEmpty()) {
	    sql.append(from.build());
	}
	for (Join join : joins) {
	    sql.append(join.build());
	}
	if (where.isNotEmpty()) {
	    sql.append(where.build());
	}
	if (groupBy.isNotEmpty()) {
	    sql.append(groupBy.build());
	}
	if (having.isNotEmpty()) {
	    sql.append(having.build());
	}
	if (!pagedCount) {
	    // Si la query es el count de una paginada, no es necesario ordenar
	    if (orderBy != null && orderBy.isNotEmpty()) {
		sql.append(orderBy.build());
	    }
	}
	for (StrQLBuilder union : unions) {
	    sql.append(union.build());
	}

	buildParametersMap();

	// Replace subselect strings
	for (Entry<String, StrQLBuilder> entry : subSelects.entrySet()) {
	    String paramName = entry.getKey();
	    StrQLUtils.replaceStringBuilder(sql, ":" + paramName, "(" + entry.getValue().build() + ")");
	}
	String result = (unionType.getOperator() + sql.toString()).replaceAll("\\s+", " ");
	if (pagedCount && isNative) {
	    return "select count(*) from (" + sql + ") x";
	}
	return result;
    }

    /**
     * Returns a {@code String} with a SQL with parameters replaced with values.
     * 
     * @since 1.1.0
     * @return a {@code String} with a SQL.
     */
    public String toSQL() {
	String sql = build();
	for (Entry<String, Object> entry : parametersMap.entrySet()) {
	    String param = Matcher.quoteReplacement(entry.getKey());
	    sql = sql.replaceAll(":" + param + "(?=[\\s\\)])", valueToSQLString(entry.getValue()));
	}
	return sql;
    }

    private String valueToSQLString(Object obj) {
	if (obj == null) {
	    return "null";
	}
	if (obj instanceof Number) {
	    return obj.toString();
	}
	if (obj instanceof CharSequence) {
	    return "'" + obj + "'";
	}
	StringBuilder sb = new StringBuilder();
	if (obj.getClass().isArray()) {
	    Object[] arr = (Object[]) obj;
	    boolean first = true;
	    for (Object elem : arr) {
		if (first) {
		    first = false;
		} else {
		    sb.append(",");
		}
		sb.append(valueToSQLString(elem));
	    }
	    return sb.toString();
	}
	if (obj instanceof Iterable<?>) {
	    @SuppressWarnings("unchecked")
	    Iterable<Object> iterable = (Iterable<Object>) obj;
	    boolean first = true;
	    for (Object j : iterable) {
		if (first) {
		    first = false;
		} else {
		    sb.append(",");
		}
		sb.append(valueToSQLString(j));
	    }
	    return sb.toString();
	}
	return obj.toString();
    }

    ////// PARAMETERS HANDLING
    ////// ========== ========
    private Map<String, Object> parametersMap;

    private Map<String, Object> getParametersMap() {
	if (parametersMap == null) {
	    throw new ParseSqlException("Cannot access parametersMap if instance has not been builded before.");
	}
	return parametersMap;
    }

    private Map<String, Object> buildParametersMap() {
	parametersMap = new HashMap<String, Object>();
	addParametersToMap(select);
	addParametersToMap(from);
	addParametersToMap(joins);
	addParametersToMap(where);
	addParametersToMap(having);

	addParametersToMap(subSelects);
	addUnionParametersToMap(unions);
	return parametersMap;
    }

    private void addUnionParametersToMap(List<StrQLBuilder> unions) {
	for (StrQLBuilder sql : unions) {
	    parametersMap.putAll(sql.buildParametersMap());
	}
    }

    private void addParametersToMap(Map<String, StrQLBuilder> subSelects) {
	for (StrQLBuilder sql : subSelects.values()) {
	    parametersMap.putAll(sql.buildParametersMap());
	}
    }

    private void addParametersToMap(List<? extends Chunk> chunks) {
	if (chunks == null || chunks.isEmpty()) {
	    return;
	}
	for (Chunk chunk : chunks) {
	    addParametersToMap(chunk);
	}
    }

    private <T extends Chunk> void addParametersToMap(T chunk) {
	List<Parameter<StrQLBuilder>> sqls = chunk.getSqls();
	if (sqls != null && !sqls.isEmpty()) {
	    for (Parameter<StrQLBuilder> param : sqls) {
		if (param != null) {
		    subSelects.put(param.getName(), param.getValue());
		}
	    }
	}
	List<Parameter<?>> params = chunk.getParams();
	if (params != null && !params.isEmpty()) {
	    for (Parameter<?> param : params) {
		if (param != null) {
		    parametersMap.put(param.getName(), param.getValue());
		}
	    }
	}
    }

    @Override
    public String toString() {
	return build();
    }

    private enum UnionType {
	NO_UNION(""), UNION(" union "), UNIONALL(" union all ");
	private String operator;

	private UnionType(String operator) {
	    this.operator = operator;
	}

	String getOperator() {
	    return operator;
	}
    }

}
