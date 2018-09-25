package com.github.iaunzu.strqlbuilder.hibernate;

import java.util.Map;

import org.hibernate.Filter;
import org.hibernate.engine.query.spi.HQLQueryPlan;
import org.hibernate.engine.query.spi.QueryPlanCache;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.query.ParameterMetadata;
import org.hibernate.query.internal.ParameterMetadataImpl;

public class StrTypedQueryFactory {

    public static <X> StrTypedQuery<X> create(SharedSessionContractImplementor session, String sql,
	    boolean createNative) {
	if (createNative) {
	    return createNative(session, sql);
	}
	return createHQL(session, sql);
    }

    private static <X> StrTypedQuery<X> createNative(SharedSessionContractImplementor session, String sqlString) {
	QueryPlanCache queryPlanCache = session.getFactory().getQueryPlanCache();
	ParameterMetadata sqlParameterMetadata = queryPlanCache.getSQLParameterMetadata(sqlString, false);
	return new TypedNativeQueryImpl<X>(sqlString, session, sqlParameterMetadata);
    }

    private static <X> StrTypedQuery<X> createHQL(SharedSessionContractImplementor session, String queryString) {
	QueryPlanCache queryPlanCache = session.getFactory().getQueryPlanCache();
	Map<String, Filter> enabledFilters = session.getLoadQueryInfluencers().getEnabledFilters();
	HQLQueryPlan hqlQueryPlan = queryPlanCache.getHQLQueryPlan(queryString, false, enabledFilters);
	ParameterMetadataImpl parameterMetadata = hqlQueryPlan.getParameterMetadata();
	return new TypedQueryImpl<>(session, parameterMetadata, queryString);
    }

}
