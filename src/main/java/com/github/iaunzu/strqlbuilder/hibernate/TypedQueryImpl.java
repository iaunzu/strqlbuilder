package com.github.iaunzu.strqlbuilder.hibernate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.mapping.MappingModelExpressible;
import org.hibernate.query.spi.DomainQueryExecutionContext;
import org.hibernate.query.spi.HqlInterpretation;
import org.hibernate.query.spi.QueryParameterImplementor;
import org.hibernate.query.sqm.internal.DomainParameterXref;
import org.hibernate.query.sqm.internal.QuerySqmImpl;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;

import com.github.iaunzu.beanwrapper.IBeanWrapper;
import com.github.iaunzu.beanwrapper.impl.BeanWrapper;
import com.github.iaunzu.beanwrapper.propertyeditor.IPropertyEditor;
import com.github.iaunzu.strqlbuilder.chunks.Aliases;
import com.github.iaunzu.strqlbuilder.exceptions.ParseSqlException;
import com.github.iaunzu.strqlbuilder.hibernate.propertyeditor.BeanPropertyEditors;
import com.github.iaunzu.strqlbuilder.utils.pojo.DefaultPojoFactory;
import com.github.iaunzu.strqlbuilder.utils.pojo.IPojoFactory;

import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Parameter;
import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

public class TypedQueryImpl<X> implements StrTypedQuery<X> {

	private static final Logger log = LoggerFactory.getLogger(TypedQueryImpl.class);

	private QuerySqmImpl<Object> delegate;
	HqlInterpretation<Object> hqlInterpretation;
	private Aliases aliases;
	private Class<X> targetClass;
	private IPojoFactory<X> pojoFactory;

	public TypedQueryImpl(String queryString, HqlInterpretation<Object> hqlInterpretation,
		SharedSessionContractImplementor session, Class<X> targetClass) {
		this.delegate = new QuerySqmImpl<Object>(queryString, hqlInterpretation, Object.class, session);
		this.hqlInterpretation = hqlInterpretation;
		this.targetClass = targetClass;
	}

	@Override
	public String getSQL() {
		SharedSessionContractImplementor session = delegate.getSession();
		SessionFactoryImplementor sessionFactory = session.getFactory();
		SqmSelectStatement<Object> sqm = (SqmSelectStatement<Object>) hqlInterpretation.getSqmStatement();

		DomainParameterXref domainParameterXref = delegate.getDomainParameterXref();
		SqmTranslation<SelectStatement> sqmTraslation = getSqmTraslation(sqm,
			hqlInterpretation.getDomainParameterXref(),
			delegate);

		final SqlAstTranslator<JdbcOperationQuerySelect> selectTranslator = session.getJdbcServices()
			.getJdbcEnvironment().getSqlAstTranslatorFactory()
			.buildSelectTranslator(sessionFactory, sqmTraslation.getSqlAst());

		DomainQueryExecutionContext executionContext = delegate;

		final SqmTranslation<SelectStatement> sqmInterpretation = session.getFactory().getQueryEngine()
			.getSqmTranslatorFactory()
			.createSelectTranslator(
				sqm,
				executionContext.getQueryOptions(),
				domainParameterXref,
				executionContext.getQueryParameterBindings(),
				executionContext.getSession().getLoadQueryInfluencers(),
				sessionFactory,
				true)
			.translate();

		final FromClauseAccess tableGroupAccess = sqmInterpretation.getFromClauseAccess();
		final Map<QueryParameterImplementor<?>, Map<SqmParameter<?>, List<JdbcParametersList>>> jdbcParamsXref = SqmUtil
			.generateJdbcParamsXref(domainParameterXref, sqmInterpretation::getJdbcParamsBySqmParam);
		final JdbcParameterBindings jdbcParameterBindings = SqmUtil.createJdbcParameterBindings(
			executionContext.getQueryParameterBindings(),
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
		JdbcOperationQuerySelect translated = selectTranslator.translate(jdbcParameterBindings,
			executionContext.getQueryOptions());
		return translated.getSqlString();
	}

	private static SqmTranslation<SelectStatement> getSqmTraslation(
		SqmSelectStatement<?> sqm,
		DomainParameterXref domainParameterXref,
		DomainQueryExecutionContext executionContext) {
		final SharedSessionContractImplementor session = executionContext.getSession();
		final SessionFactoryImplementor sessionFactory = session.getFactory();

		final SqmTranslation<SelectStatement> sqmInterpretation = sessionFactory.getQueryEngine()
			.getSqmTranslatorFactory()
			.createSelectTranslator(
				sqm,
				executionContext.getQueryOptions(),
				domainParameterXref,
				executionContext.getQueryParameterBindings(),
				executionContext.getSession().getLoadQueryInfluencers(),
				sessionFactory,
				true)
			.translate();
		return sqmInterpretation;
	}

	@Override
	public X getSingleResult() {
		Object rs = delegate.uniqueResult();
		return parseResultSet(rs);
	}

	@Override
	public List<X> getResultList() {
		List<X> list = new ArrayList<X>();
		for (Object res : delegate.list()) {
			X obj = parseResultSet(res);
			list.add(obj);
		}

		return list;
	}

	private X parseResultSet(Object rs) {
		try {

			IPojoFactory<X> pojoFactory = getPojoFactory();
			if (pojoFactory.isPrimitive()) {
				return pojoFactory.parsePrimitive(rs);
			}

			Object[] row;
			if (rs instanceof Object[]) {
				row = (Object[]) rs;
			} else {
				row = new Object[] { rs };
			}

			if (row.length < aliases.size()) {
				throw new ParseSqlException(
					"En número de alias de la select no coindicen con el numero de columnas devueltas por la query. Alias definidos: "
						+ aliases);
			}

			X result = pojoFactory.newInstance();
			IBeanWrapper bean = new BeanWrapper(result);
			prepareBeanWrapper(bean);

			int index = 0;
			for (String propertyName : aliases.getBeanAliases()) {
				Object value = row[index++];
				bean.setPropertyValue(propertyName, value);
			}

			return result;
		} catch (BeansException e) {
			log.error("Error haciendo set " + targetClass, e);
		}
		return null;
	}

	public void setParameters(Map<String, Object> parameters) {
		if (parameters == null) {
			return;
		}
		for (Entry<String, Object> entry : parameters.entrySet()) {
			setParameter(entry.getKey(), entry.getValue());
		}
	}

	public void setPositionParameters(Map<Integer, Object> parameters) {
		if (parameters == null) {
			return;
		}
		for (Entry<Integer, Object> entry : parameters.entrySet()) {
			setParameter(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void setAlias(Aliases aliases) {
		this.aliases = aliases;
	}

	private void prepareBeanWrapper(IBeanWrapper bean) {
		Map<Class<?>, IPropertyEditor> defaultPropertyEditors = BeanPropertyEditors.getBeanPropertyEditors();
		for (Entry<Class<?>, IPropertyEditor> entry : defaultPropertyEditors.entrySet()) {
			bean.addPropertyEditor(entry.getKey(), entry.getValue());
		}
	}

	private IPojoFactory<X> getPojoFactory() {
		if (pojoFactory == null) {
			pojoFactory = new DefaultPojoFactory<X>(targetClass);
		}
		return pojoFactory;
	}

	public void setPojoFactory(IPojoFactory<X> pojoFactory) {
		this.pojoFactory = pojoFactory;
	}

	@Override
	public TypedQuery<X> setMaxResults(int maxResult) {
		delegate.setMaxResults(maxResult);
		return this;
	}

	@Override
	public TypedQuery<X> setFirstResult(int startPosition) {
		delegate.setFirstResult(startPosition);
		return this;
	}

	@Override
	public TypedQuery<X> setHint(String hintName, Object value) {
		delegate.setHint(hintName, value);
		return this;
	}

	@Override
	public <T> TypedQuery<X> setParameter(Parameter<T> param, T value) {
		delegate.setParameter(param, value);
		return this;
	}

	@Override
	public TypedQuery<X> setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
		delegate.setParameter(param, value, temporalType);
		return this;
	}

	@Override
	public TypedQuery<X> setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
		delegate.setParameter(param, value, temporalType);
		return this;
	}

	@Override
	public TypedQuery<X> setParameter(String name, Object value) {
		delegate.setParameter(name, value);
		return this;
	}

	@Override
	public TypedQuery<X> setParameter(String name, Calendar value, TemporalType temporalType) {
		delegate.setParameter(name, value, temporalType);
		return this;
	}

	@Override
	public TypedQuery<X> setParameter(String name, Date value, TemporalType temporalType) {
		delegate.setParameter(name, value, temporalType);
		return this;
	}

	@Override
	public TypedQuery<X> setParameter(int position, Object value) {
		delegate.setParameter(position, value);
		return this;
	}

	@Override
	public TypedQuery<X> setParameter(int position, Calendar value, TemporalType temporalType) {
		delegate.setParameter(position, value, temporalType);
		return this;
	}

	@Override
	public TypedQuery<X> setParameter(int position, Date value, TemporalType temporalType) {
		delegate.setParameter(position, value, temporalType);
		return this;
	}

	@Override
	public TypedQuery<X> setFlushMode(FlushModeType flushMode) {
		delegate.setFlushMode(flushMode);
		return this;
	}

	@Override
	public TypedQuery<X> setLockMode(LockModeType lockMode) {
		delegate.setLockMode(lockMode);
		return this;
	}

	@Override
	public int executeUpdate() {
		return delegate.executeUpdate();
	}

	@Override
	public int getMaxResults() {
		return delegate.getMaxResults();
	}

	@Override
	public int getFirstResult() {
		return delegate.getFirstResult();
	}

	@Override
	public Map<String, Object> getHints() {
		return delegate.getHints();
	}

	@Override
	public Set<Parameter<?>> getParameters() {
		return delegate.getParameters();
	}

	@Override
	public Parameter<?> getParameter(String name) {
		return delegate.getParameter(name);
	}

	@Override
	public <T> Parameter<T> getParameter(String name, Class<T> type) {
		return delegate.getParameter(name, type);
	}

	@Override
	public Parameter<?> getParameter(int position) {
		return delegate.getParameter(position);
	}

	@Override
	public <T> Parameter<T> getParameter(int position, Class<T> type) {
		return delegate.getParameter(position, type);
	}

	@Override
	public boolean isBound(Parameter<?> param) {
		return delegate.isBound(param);
	}

	@Override
	public <T> T getParameterValue(Parameter<T> param) {
		return delegate.getParameterValue(param);
	}

	@Override
	public Object getParameterValue(String name) {
		return delegate.getParameterValue(name);
	}

	@Override
	public Object getParameterValue(int position) {
		return delegate.getParameterValue(position);
	}

	@Override
	public FlushModeType getFlushMode() {
		return delegate.getFlushMode();
	}

	@Override
	public LockModeType getLockMode() {
		return delegate.getLockMode();
	}

	@Override
	public <T> T unwrap(Class<T> cls) {
		return delegate.unwrap(cls);
	}

}
