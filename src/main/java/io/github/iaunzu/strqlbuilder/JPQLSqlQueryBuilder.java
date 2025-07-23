package io.github.iaunzu.strqlbuilder;

import io.github.iaunzu.strqlbuilder.chunks.Join;
import io.github.iaunzu.strqlbuilder.chunks.Join.JoinType;
import io.github.iaunzu.strqlbuilder.hibernate.SqmTranslator;
import jakarta.persistence.EntityManager;

public class JPQLSqlQueryBuilder extends AbstractQueryBuilder<JPQLQueryBuilder> implements JPQLQueryBuilder {

    protected JPQLSqlQueryBuilder() {
        super(false);
    }

    @Override
    public Join<JPQLQueryBuilder>.JoinOn joinfetch(String entity) {
        return join(JoinType.JOIN_FETCH, entity);
    }

    @Override
    public Join<JPQLQueryBuilder>.JoinOn leftjoinfetch(String entity) {
        return join(JoinType.LEFT_JOIN_FETCH, entity);
    }

    public static JPQLSqlQueryBuilder create() {
        return new JPQLSqlQueryBuilder();
    }

    @Override
    protected void appendHook(StringBuilder sql, HookContext context) {
        super.appendHook(sql, context);
        HookPhase phase = context.getPhase();
        HookStage stage = context.getStage();
        boolean pagedCount = context.isPagedCount();
        EntityManager entityManager = context.getEntityManager();

        if (phase == HookPhase.AFTER && stage == HookStage.END) {
            if (pagedCount && entityManager != null) {
                // // always native
                String nativeSelect = new SqmTranslator(entityManager).translateNativeSelect(sql.toString());
                sql.setLength(0);
                sql.append("select count(*) from (").append(nativeSelect).append(") x");
            }
        }
    }
}
