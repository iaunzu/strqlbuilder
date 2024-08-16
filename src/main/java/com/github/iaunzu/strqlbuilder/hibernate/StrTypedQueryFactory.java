package com.github.iaunzu.strqlbuilder.hibernate;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.query.spi.HqlInterpretation;
import org.hibernate.query.spi.QueryEngine;

public class StrTypedQueryFactory {

	public static <X> StrTypedQuery<X> create(SharedSessionContractImplementor session, String sql,
		Class<X> targetClass, boolean createNative) {
		if (createNative) {
			return createNative(session, sql, targetClass);
		}
		return createHQL(session, sql, targetClass);
	}

	private static <X> StrTypedQuery<X> createNative(SharedSessionContractImplementor session, String sqlString,
		Class<X> targetClass) {
		return new TypedNativeQueryImpl<X>(sqlString, session, targetClass);
	}

	private static <X> StrTypedQuery<X> createHQL(SharedSessionContractImplementor session, String queryString,
		Class<X> targetClass) {
		final SessionFactoryImplementor sessionFactory = session.getFactory();
		QueryEngine queryEngine = sessionFactory.getQueryEngine();
		HqlInterpretation<Object> hqlInterpretation = queryEngine.getInterpretationCache()
			.resolveHqlInterpretation(
				queryString,
				Object.class,
				queryEngine.getHqlTranslator());
		return new TypedQueryImpl<>(queryString, hqlInterpretation, session, targetClass);
	}

}
