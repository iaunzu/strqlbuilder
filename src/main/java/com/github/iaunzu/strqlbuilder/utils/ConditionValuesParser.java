package com.github.iaunzu.strqlbuilder.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class ConditionValuesParser
{

	ConditionValues conditionValues;

	private int IN_MAX_SIZE = 1000;

	public ConditionValuesParser(String condition, Object... values)
	{
		this.conditionValues = new ConditionValues(condition, values);
	}

	public ConditionValuesParser(ConditionValues conditionValues, int inMaxSize)
	{
		this.conditionValues = conditionValues;
		this.IN_MAX_SIZE = inMaxSize;
	}

	public static ConditionValuesParser getInstance(String condition, Object... values)
	{
		return new ConditionValuesParser(condition, values);
	}

	public ConditionValues parse()
	{
		ConditionValues parsed = new ConditionValues();
		String condition = clean(conditionValues.getCondition());
		Matcher paramMatcher = Pattern.compile(":[^\\s\\)]+", Pattern.CASE_INSENSITIVE).matcher(condition);

		int j = 0;
		List<Object> list = new ArrayList<Object>(); // lista final values
		Object[] values = conditionValues.getValues();
		while (paramMatcher.find())
		{
			String parameter = paramMatcher.group();
			Matcher subConditionMatcher = Pattern.compile("[^\\s\\(]+\\s+(?:not\\s+)?in\\s+" + Pattern.quote(parameter), Pattern.CASE_INSENSITIVE).matcher(condition);
			Object subconditionValue = values[j]; // original value
			subconditionValue = convertToListIfArray(subconditionValue);
			boolean isInParameter = subConditionMatcher.find();
			if (isInParameter)
			{
				String subcondition = subConditionMatcher.group();
				if (!shouldSplit(subconditionValue))
				{
					list.add(subconditionValue);
				}
				else
				{
					// split
					List<?> listValue = (List<?>)subconditionValue;
					int chunks = splitValueIntoList(listValue, list);

					condition = StringUtils.replace(condition, subcondition, getReplacement(subcondition, chunks), 1);
				}
			}
			else
			{
				list.add(subconditionValue);
			}
			j++;

		}
		parsed.setCondition(wrapInParametersWithParenthesis(condition));
		parsed.setValues(list.toArray());
		return parsed;
	}

	private String wrapInParametersWithParenthesis(String condition)
	{
		// This method prevents errors when list parameter is inside some parenthesis group in hibernate
		return condition.replaceAll("(?i)(in)\\s+(:[^\\s\\)]+)", "$1 ($2)"); // replace "IN :list" --> "IN (:list)"
	}

	private Object convertToListIfArray(Object obj)
	{
		if (obj != null && obj.getClass().isArray())
		{
			return StrQLUtils.arrayToList(obj);
		}
		return obj;

	}

	private String getReplacement(String subcondition, int chunks)
	{
		boolean isNotIn = Pattern.compile("\\snot\\s", Pattern.CASE_INSENSITIVE).matcher(subcondition).find();
		String logicalOperator = isNotIn ? " and " : " or ";
		StringBuilder replacement = new StringBuilder("(").append(subcondition).append("__0");
		for (int i = 1; i < chunks; i++)
		{
			replacement.append(logicalOperator).append(subcondition).append("__").append(i);
		}
		replacement.append(")");
		return replacement.toString();

	}

	private boolean shouldSplit(Object value)
	{
		if (!(value instanceof List))
		{
			return false;
		}
		List<?> listValue = (List<?>)value;
		if (listValue.size() > IN_MAX_SIZE)
		{
			return true;
		}
		return false;
	}

	private int splitValueIntoList(List<?> value, List<Object> list)
	{
		int chunks = 0;
		int sep = 0;
		do
		{
			int fromIndex = sep;
			int toIndex = Math.min(sep + IN_MAX_SIZE, value.size());
			list.add(value.subList(fromIndex, toIndex));
			sep += IN_MAX_SIZE;
			chunks++;
		}
		while (sep < value.size());

		return chunks;
	}

	private String clean(String str)
	{
		str = str.replaceAll("[\"']", ""); // delete " and '
		return str.replaceAll("\\(:(\\S+)\\)", ":$1"); // (:param) -> :param
	}

}
