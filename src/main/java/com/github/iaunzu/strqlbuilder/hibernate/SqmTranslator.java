package com.github.iaunzu.strqlbuilder.hibernate;

import java.util.List;
import java.util.Map;

import org.hibernate.engine.jdbc.mutation.internal.MutationQueryOptions;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.mapping.MappingModelExpressible;
import org.hibernate.query.hql.HqlTranslator;
import org.hibernate.query.internal.ParameterMetadataImpl;
import org.hibernate.query.internal.QueryParameterBindingsImpl;
import org.hibernate.query.spi.HqlInterpretation;
import org.hibernate.query.spi.ParameterMetadataImplementor;
import org.hibernate.query.spi.QueryEngine;
import org.hibernate.query.spi.QueryParameterImplementor;
import org.hibernate.query.sqm.internal.DomainParameterXref;
import org.hibernate.query.sqm.internal.SqmUtil;
import org.hibernate.query.sqm.spi.SqmParameterMappingModelResolutionAccess;
import org.hibernate.query.sqm.sql.SqmTranslation;
import org.hibernate.query.sqm.tree.expression.SqmParameter;
import org.hibernate.query.sqm.tree.select.SqmSelectStatement;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.FromClauseAccess;
import org.hibernate.sql.ast.tree.select.SelectStatement;
import org.hibernate.sql.exec.spi.JdbcOperationQuerySelect;
import org.hibernate.sql.exec.spi.JdbcParameterBindings;
import org.hibernate.sql.exec.spi.JdbcParametersList;

import jakarta.persistence.EntityManager;

public class SqmTranslator {

	private SharedSessionContractImplementor session;
	private SessionFactoryImplementor sessionFactory;
	private QueryEngine queryEngine;
	private HqlTranslator hqlTranslator;

	public SqmTranslator(EntityManager entityManager) {
		this(entityManager.unwrap(SharedSessionContractImplementor.class));
	}

	public SqmTranslator(SharedSessionContractImplementor session) {
		this.session = session;
		this.sessionFactory = session.getFactory();
		this.queryEngine = sessionFactory.getQueryEngine();
		this.hqlTranslator = queryEngine.getHqlTranslator();
	}

	public String translateNativeSelect(String queryString) {
		HqlInterpretation<Object> hqlInterpretation = queryEngine.getInterpretationCache()
			.resolveHqlInterpretation(
				queryString,
				Object.class,
				hqlTranslator);
		DomainParameterXref domainParameterXref = hqlInterpretation.getDomainParameterXref();

		if (!(hqlInterpretation.getSqmStatement() instanceof SqmSelectStatement)) {
			throw new IllegalStateException("Cannot translate non-select statement to SQL");
		}
		SqmSelectStatement<Object> sqm = (SqmSelectStatement<Object>) hqlInterpretation.getSqmStatement();
		ParameterMetadataImplementor parameterMetadata;
		if (!domainParameterXref.hasParameters()) {
			parameterMetadata = ParameterMetadataImpl.EMPTY;
		} else {
			parameterMetadata = new ParameterMetadataImpl(domainParameterXref.getQueryParameters());
		}

		QueryParameterBindingsImpl parameterBindings = QueryParameterBindingsImpl.from(parameterMetadata,
			sessionFactory);

		final MutationQueryOptions noQueryOptions = new MutationQueryOptions();
		final SqmTranslation<SelectStatement> sqmInterpretation = sessionFactory.getQueryEngine()
			.getSqmTranslatorFactory()
			.createSelectTranslator(
				sqm,
				noQueryOptions,
				domainParameterXref,
				parameterBindings,
				session.getLoadQueryInfluencers(),
				sessionFactory,
				true)
			.translate();

		final FromClauseAccess tableGroupAccess = sqmInterpretation.getFromClauseAccess();
		final Map<QueryParameterImplementor<?>, Map<SqmParameter<?>, List<JdbcParametersList>>> jdbcParamsXref = SqmUtil
			.generateJdbcParamsXref(domainParameterXref, sqmInterpretation::getJdbcParamsBySqmParam);
		final JdbcParameterBindings jdbcParameterBindings = SqmUtil.createJdbcParameterBindings(
			parameterBindings,
			domainParameterXref,
			jdbcParamsXref,
			session.getFactory().getRuntimeMetamodels().getMappingMetamodel(),
			tableGroupAccess::findTableGroup,
			new SqmParameterMappingModelResolutionAccess() {
				@Override
				@SuppressWarnings("unchecked")
				public <T> MappingModelExpressible<T> getResolvedMappingModelType(SqmParameter<T> parameter) {
					return (MappingModelExpressible<T>) sqmInterpretation.getSqmParameterMappingModelTypeResolutions()
						.get(parameter);
				}
			},
			session);
		final SqlAstTranslator<JdbcOperationQuerySelect> selectTranslator = session.getJdbcServices()
			.getJdbcEnvironment().getSqlAstTranslatorFactory()
			.buildSelectTranslator(sessionFactory, sqmInterpretation.getSqlAst());
		JdbcOperationQuerySelect translated = selectTranslator.translate(jdbcParameterBindings,
			noQueryOptions);

		return translated.getSqlString();
	}

}
