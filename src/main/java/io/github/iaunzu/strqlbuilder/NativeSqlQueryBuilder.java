package io.github.iaunzu.strqlbuilder;

import io.github.iaunzu.strqlbuilder.chunks.With;
import io.github.iaunzu.strqlbuilder.chunks.With.WithAs;
import io.github.iaunzu.strqlbuilder.utils.StrQLUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NativeSqlQueryBuilder extends AbstractQueryBuilder<NativeQueryBuilder> implements NativeQueryBuilder {

    private List<With> withs;
    private UnionType unionType;

    private NativeSqlQueryBuilder unionParent;
    protected List<NativeQueryBuilder> unions;

    protected NativeSqlQueryBuilder(NativeSqlQueryBuilder unionParent, UnionType unionType) {
        super(true);

        this.withs = new ArrayList<>();
        this.unionParent = unionParent;
        this.unionType = unionType;
        this.unions = new ArrayList<>();
    }

    @Override
    protected NativeSqlQueryBuilder getRoot() {
        NativeSqlQueryBuilder root = this;
        while (root.unionParent != null) {
            log.warn(
                    "Warning: you are trying to execute a query over an auxiliar instance -used to build union union queries-. Use .endUnion() to remove this message.");
            root = root.unionParent;
        }
        return root;
    }

    @Override
    protected void appendHook(StringBuilder sql, HookContext hookContext) {
        super.appendHook(sql, hookContext);
        boolean pagedCount = hookContext.isPagedCount();
        HookPhase phase = hookContext.getPhase();
        HookStage stage = hookContext.getStage();
        if (phase == HookPhase.BEFORE && stage == HookStage.CREATE) {
            for (int i = 0; i < withs.size(); i++) {
                With with = withs.get(i);
                if (i == 0) {
                    sql.append(with.buildFirst());
                } else {
                    sql.append(with.buildSubsequent());
                }
            }
        } else if (phase == HookPhase.BEFORE && stage == HookStage.BUILD_PARAMETERS) {
            for (NativeQueryBuilder union : unions) {
                sql.append(union.build());
            }
        } else if (phase == HookPhase.BEFORE && stage == HookStage.END) {
            StrQLUtils.prependStringBuilder(sql, unionType.getOperator());
            StrQLUtils.replaceAllStringBuilder(sql, "\\s+", " ");
            if (pagedCount) {
                StrQLUtils.prependStringBuilder(sql, "select count(*) from (");
                sql.append(") x");
            }
        }
    }

    @Override
    public Map<String, Object> buildParametersMap() {
        Map<String, Object> map = super.buildParametersMap();
        addUnionParametersToMap(unions, map);
        return map;
    }

    private void addUnionParametersToMap(List<NativeQueryBuilder> unions, Map<String, Object> map) {
        for (NativeQueryBuilder sql : unions) {
            map.putAll(sql.buildParametersMap());
        }
    }

    @Override
    public WithAs with(String cteName) {
        With with = new With(this);
        this.withs.add(with);
        return with.with(cteName);
    }

    @Override
    public NativeQueryBuilder union() {
        NativeQueryBuilder sql = new NativeSqlQueryBuilder(this, UnionType.UNION);
        getParent().unions.add(sql);
        return sql;
    }

    @Override
    public NativeQueryBuilder unionAll() {
        NativeQueryBuilder sql = new NativeSqlQueryBuilder(this, UnionType.UNIONALL);
        getParent().unions.add(sql);
        return sql;
    }

    @Override
    public NativeQueryBuilder union(NativeQueryBuilder sql) {
        sql.setUnionType(UnionType.UNION);
        getParent().unions.add(sql);
        return this;
    }

    @Override
    public NativeQueryBuilder unionAll(NativeQueryBuilder sql) {
        sql.setUnionType(UnionType.UNIONALL);
        getParent().unions.add(sql);
        return this;
    }

    @Override
    public NativeQueryBuilder endUnion() {
        return getParent();
    }

    protected NativeSqlQueryBuilder getParent() {
        if (unionType == UnionType.NO_UNION) {
            assert (unionParent == null);
            return this;
        }
        NativeSqlQueryBuilder root = this.unionParent;
        while (root.unionType != UnionType.NO_UNION) {
            assert (root.unionParent != null);
            root = root.unionParent;
        }
        return root;
    }

    @Override
    public UnionType getUnionType() {
        return unionType;
    }

    @Override
    public void setUnionType(UnionType unionType) {
        this.unionType = unionType;
    }

    public static NativeQueryBuilder create() {
        return new NativeSqlQueryBuilder(null, UnionType.NO_UNION);
    }
}
