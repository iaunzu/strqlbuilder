package io.github.iaunzu.strqlbuilder;

import io.github.iaunzu.strqlbuilder.chunks.Chunk;
import io.github.iaunzu.strqlbuilder.chunks.From;
import io.github.iaunzu.strqlbuilder.chunks.GroupBy;
import io.github.iaunzu.strqlbuilder.chunks.Having;
import io.github.iaunzu.strqlbuilder.chunks.Join;
import io.github.iaunzu.strqlbuilder.chunks.Join.JoinType;
import io.github.iaunzu.strqlbuilder.chunks.OrderBy;
import io.github.iaunzu.strqlbuilder.chunks.OrderBy.Direction;
import io.github.iaunzu.strqlbuilder.chunks.Parameter;
import io.github.iaunzu.strqlbuilder.chunks.Select;
import io.github.iaunzu.strqlbuilder.chunks.Where;
import io.github.iaunzu.strqlbuilder.chunks.like.CaseInsensitiveLike;
import io.github.iaunzu.strqlbuilder.exceptions.ParseSqlException;
import io.github.iaunzu.strqlbuilder.hibernate.StrTypedQuery;
import io.github.iaunzu.strqlbuilder.hibernate.StrTypedQueryFactory;
import io.github.iaunzu.strqlbuilder.pagination.PagedTypedQuery;
import io.github.iaunzu.strqlbuilder.pagination.PagedTypedQueryImpl;
import io.github.iaunzu.strqlbuilder.utils.StrQLUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

public abstract class AbstractQueryBuilder<Q extends QueryBuilder<Q>> implements QueryBuilder<Q> {

    public static final Logger log = LoggerFactory.getLogger(AbstractQueryBuilder.class);

    private Q self;

    private Select<Q> select;

    private From<Q> from;

    private List<Join<Q>> joins;

    private Where<Q> where;

    private GroupBy<Q> groupBy;

    private Having<Q> having;

    private OrderBy<Q> orderBy;

    private Map<String, Q> subSelects;

    private Chunk<Q> lastChunk;

    private boolean isNative = false;

    protected AbstractQueryBuilder(boolean isNative) {
        this.self = (Q) this;
        this.isNative = isNative;

        select = new Select<>();
        from = new From<>();
        joins = new ArrayList<>();
        where = new Where<>(self);
        groupBy = new GroupBy<>();
        having = new Having<>();
        orderBy = null;
        subSelects = new HashMap<>();
    }

    @Override
    public Q select(String select) {
        return select(select, new Object[0]);
    }

    @Override
    public Q select(String select, Object... values) {
        lastChunk = this.select;
        this.select.select(select, values);
        return self;
    }

    @Override
    public Q selectDistinct(String select, Object... values) {
        return select("distinct " + select, values);
    }

    @Override
    public Q count(String count) {
        return select("count(" + count + ")");
    }

    @Override
    public Q from(String from) {
        return from(from, new Object[0]);
    }

    @Override
    public Q from(String from, Object... values) {
        lastChunk = this.from;
        this.from.from(from, values);
        return self;
    }

    @Override
    public Join<Q>.JoinOn join(String table) {
        return join(JoinType.INNER_JOIN, table);
    }

    @Override
    public Join<Q>.JoinOn leftjoin(String table) {
        return join(JoinType.LEFT_JOIN, table);
    }

    @Override
    public Join<Q>.JoinOn rightjoin(String table) {
        return join(JoinType.RIGHT_JOIN, table);
    }

    @Override
    public Join<Q>.JoinOn fulljoin(String table) {
        return join(JoinType.FULL_JOIN, table);
    }

    protected Join<Q>.JoinOn join(JoinType joinType, String table) {
        Join<Q> join = new Join<>(self);
        this.joins.add(join);
        lastChunk = join;
        return join.join(joinType, table);
    }

    @Override
    public Q where() {
        lastChunk = this.where;
        return self;
    }

    @Override
    public Q where(String str) {
        return where(str, new Object[0]);
    }

    @Override
    public Q where(String str, Object... values) {
        where();
        if (values != null && values.length == 1 && values[0] == null) {
            return self;
        }
        return this.where.and(str, values);
    }

    @Override
    public Q groupBy(String groupBy) {
        lastChunk = this.groupBy;
        this.groupBy.groupBy(groupBy);
        return self;
    }

    @Override
    public Q having(String having, Object... values) {
        lastChunk = this.having;
        this.having.having(having, values);
        return self;
    }

    @Override
    public Q order(OrderBy<Q> orderBy) {
        this.orderBy = orderBy;
        lastChunk = this.orderBy;
        // save aliases to replace later if needed
        this.orderBy.setSelectAliases(this.select.getAliases());
        return self;
    }

    @Override
    public Q order(Sort sort) {
        if (sort != null) {
            for (Order order : sort) {
                String direction = order.getDirection().name().toUpperCase();
                String alias = order.getProperty();
                order(new OrderBy<>(alias, Direction.valueOf(direction)));
            }
        }
        return self;
    }

    @Override
    public Q and(String and) {
        return and(and, new Object[0]);
    }

    @Override
    public Q and(String and, Object... values) {
        if (!(lastChunk instanceof Join || lastChunk instanceof Where)) {
            throw new ParseSqlException("You can only call this method after invoking join() or where() methods.");
        }
        if (lastChunk instanceof Where && values != null && values.length == 1 && values[0] == null) {
            return self;
        }
        return (Q) lastChunk.and(and, values);
    }

    @Override
    public Q andlike(String alias, String paramName, String value) {
        if (!(lastChunk instanceof Join || lastChunk instanceof Where)) {
            throw new ParseSqlException("You can only call this method after invoking join() or where() methods.");
        }
        if (value == null) {
            return self;
        }
        return (Q) lastChunk.andlike(new CaseInsensitiveLike(alias, paramName, value));
    }

    @Override
    public <X> StrTypedQuery<X> createQuery(EntityManager entityManager, Class<X> clazz) {
        return createQuery(entityManager, clazz, false);
    }

    @Override
    public <X> PagedTypedQuery<X> createPagedQuery(EntityManager entityManager, Class<X> clazz, Pageable pageable) {
        TypedQuery<X> pageQuery = createQuery(entityManager, clazz, false);
        pageQuery.setFirstResult((int) pageable.getOffset());
        pageQuery.setMaxResults(pageable.getPageSize());
        TypedQuery<Long> countQuery = createQuery(entityManager, Long.class, true);
        return new PagedTypedQueryImpl<X>(pageQuery, pageable, countQuery);
    }

    protected AbstractQueryBuilder<Q> getRoot() {
        return this;
    }

    protected <X> StrTypedQuery<X> createQuery(EntityManager entityManager, Class<X> clazz, boolean pagedCount) {
        AbstractQueryBuilder<Q> root = getRoot();
        String sql = root.build(pagedCount, entityManager);
        Map<String, Object> parametersMap = root.getParametersMap();
        StrTypedQuery<X> typedQuery = this.createTypedQuery(entityManager, sql, pagedCount, clazz);
        typedQuery.setParameters(parametersMap);
        typedQuery.setAlias(root.select.getAliases());

        return typedQuery;
    }

    private <X> StrTypedQuery<X> createTypedQuery(
            EntityManager entityManager, String sql, boolean pagedCount, Class<X> targetClass) {
        SharedSessionContractImplementor session = entityManager.unwrap(SharedSessionContractImplementor.class);
        return StrTypedQueryFactory.create(session, sql, targetClass, this.sqlNative(pagedCount));
    }

    private boolean sqlNative(boolean pagedCount) {
        return this.isNative || pagedCount;
    }

    @Override
    public String build() {
        return build(null);
    }

    public String build(EntityManager entityManager) {
        return build(false, entityManager);
    }

    private String build(boolean pagedCount, EntityManager entityManager) {

        StringBuilder sql = new StringBuilder();
        appendHook(sql, new HookContext(pagedCount, entityManager, HookPhase.BEFORE, HookStage.CREATE));
        appendHook(sql, new HookContext(pagedCount, entityManager, HookPhase.AFTER, HookStage.CREATE));

        appendHook(sql, new HookContext(pagedCount, entityManager, HookPhase.BEFORE, HookStage.SELECT));
        sql.append(select.build());
        appendHook(sql, new HookContext(pagedCount, entityManager, HookPhase.AFTER, HookStage.SELECT));

        appendHook(sql, new HookContext(pagedCount, entityManager, HookPhase.BEFORE, HookStage.FROM));
        if (from.isNotEmpty()) {
            sql.append(from.build());
        }
        appendHook(sql, new HookContext(pagedCount, entityManager, HookPhase.AFTER, HookStage.FROM));
        appendHook(sql, new HookContext(pagedCount, entityManager, HookPhase.BEFORE, HookStage.JOIN));
        for (Join<Q> join : joins) {
            sql.append(join.build());
        }
        appendHook(sql, new HookContext(pagedCount, entityManager, HookPhase.AFTER, HookStage.JOIN));
        appendHook(sql, new HookContext(pagedCount, entityManager, HookPhase.BEFORE, HookStage.WHERE));
        if (where.isNotEmpty()) {
            sql.append(where.build());
        }
        appendHook(sql, new HookContext(pagedCount, entityManager, HookPhase.AFTER, HookStage.WHERE));
        appendHook(sql, new HookContext(pagedCount, entityManager, HookPhase.BEFORE, HookStage.GROUP_BY));
        if (groupBy.isNotEmpty()) {
            sql.append(groupBy.build());
        }
        appendHook(sql, new HookContext(pagedCount, entityManager, HookPhase.AFTER, HookStage.GROUP_BY));
        appendHook(sql, new HookContext(pagedCount, entityManager, HookPhase.BEFORE, HookStage.HAVING));
        if (having.isNotEmpty()) {
            sql.append(having.build());
        }
        appendHook(sql, new HookContext(pagedCount, entityManager, HookPhase.AFTER, HookStage.HAVING));
        appendHook(sql, new HookContext(pagedCount, entityManager, HookPhase.BEFORE, HookStage.ORDER_BY));
        if (!pagedCount) {
            // Si la query es el count de una paginada, no es necesario ordenar
            if (orderBy != null && orderBy.isNotEmpty()) {
                sql.append(orderBy.build());
            }
        }
        appendHook(sql, new HookContext(pagedCount, entityManager, HookPhase.AFTER, HookStage.ORDER_BY));

        appendHook(sql, new HookContext(pagedCount, entityManager, HookPhase.BEFORE, HookStage.BUILD_PARAMETERS));
        buildParametersMap();
        // Replace subselect strings
        for (Entry<String, Q> entry : subSelects.entrySet()) {
            String paramName = entry.getKey();
            StrQLUtils.replaceStringBuilder(
                    sql, ":" + paramName, "(" + entry.getValue().build() + ")");
        }
        appendHook(sql, new HookContext(pagedCount, entityManager, HookPhase.AFTER, HookStage.BUILD_PARAMETERS));

        appendHook(sql, new HookContext(pagedCount, entityManager, HookPhase.BEFORE, HookStage.END));
        appendHook(sql, new HookContext(pagedCount, entityManager, HookPhase.AFTER, HookStage.END));
        return sql.toString();
    }

    protected void appendHook(StringBuilder sql, HookContext hookContext) {}

    protected String buildPagedCount(EntityManager entityManager) {
        return build(true, entityManager);
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

    @Override
    public Map<String, Object> buildParametersMap() {
        parametersMap = new HashMap<String, Object>();
        addParametersToMap(select);
        addParametersToMap(from);
        addParametersToMap(joins);
        addParametersToMap(where);
        addParametersToMap(having);

        addParametersToMap(subSelects);
        return parametersMap;
    }

    private void addParametersToMap(Map<String, Q> subSelects) {
        for (Q sql : subSelects.values()) {
            parametersMap.putAll(sql.buildParametersMap());
        }
    }

    private void addParametersToMap(List<? extends Chunk<Q>> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return;
        }
        for (Chunk<Q> chunk : chunks) {
            addParametersToMap(chunk);
        }
    }

    private <T extends Chunk<Q>> void addParametersToMap(T chunk) {
        List<Parameter<Q>> sqls = chunk.getSqls();
        if (sqls != null && !sqls.isEmpty()) {
            for (Parameter<Q> param : sqls) {
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

    public static class HookContext {
        private final boolean pagedCount;
        private final EntityManager entityManager;
        private final HookPhase phase;
        private final HookStage stage;

        public HookContext(boolean pagedCount, EntityManager entityManager, HookPhase phase, HookStage stage) {
            this.pagedCount = pagedCount;
            this.entityManager = entityManager;
            this.phase = phase;
            this.stage = stage;
        }

        public boolean isPagedCount() {
            return pagedCount;
        }

        public EntityManager getEntityManager() {
            return entityManager;
        }

        public HookPhase getPhase() {
            return phase;
        }

        public HookStage getStage() {
            return stage;
        }
    }

    public enum HookPhase {
        BEFORE,
        AFTER;
    }

    public enum HookStage {
        CREATE,
        SELECT,
        FROM,
        JOIN,
        WHERE,
        GROUP_BY,
        HAVING,
        ORDER_BY,
        BUILD_PARAMETERS,
        END;
    }
}
