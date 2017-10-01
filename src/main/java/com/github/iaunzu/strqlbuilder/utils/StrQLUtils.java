package com.github.iaunzu.strqlbuilder.utils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StrQLUtils {

    // @formatter:off
    /**
    * Convert the supplied array into a List. A primitive array gets converted
    * into a List of the appropriate wrapper type.
    * <p><b>NOTE:</b> Generally prefer the standard {@link Arrays#asList} method.
    * This {@code arrayToList} method is just meant to deal with an incoming Object
    * value that might be an {@code Object[]} or a primitive array at runtime.
    * <p>A {@code null} source value will be converted to an empty List.
    * @param source the (potentially primitive) array
    * @return the converted List result
    * @see #toObjectArray(Object)
    * @see Arrays#asList(Object[])
    */
    // @formatter:on
    @SuppressWarnings("rawtypes")
    public static List arrayToList(Object source) {
	return Arrays.asList(toObjectArray(source));
    }

    // @formatter:off
    /**
    * Convert the given array (which may be a primitive array) to an
    * object array (if necessary of primitive wrapper objects).
    * <p>A {@code null} source value will be converted to an
    * empty Object array.
    * @param source the (potentially primitive) array
    * @return the corresponding object array (never {@code null})
    * @throws IllegalArgumentException if the parameter is not an array
    */
    // @formatter:on
    public static Object[] toObjectArray(Object source) {
	if (source instanceof Object[]) {
	    return (Object[]) source;
	}
	if (source == null) {
	    return new Object[0];
	}
	if (!source.getClass().isArray()) {
	    throw new IllegalArgumentException("Source is not an array: " + source);
	}
	int length = Array.getLength(source);
	if (length == 0) {
	    return new Object[0];
	}
	Class<?> wrapperType = Array.get(source, 0).getClass();
	Object[] newArray = (Object[]) Array.newInstance(wrapperType, length);
	for (int i = 0; i < length; i++) {
	    newArray[i] = Array.get(source, i);
	}
	return newArray;
    }

    public static void replaceStringBuilder(StringBuilder sb, String target, String replacement) {
	int index = 0;
	int fromIndex = 0;
	while ((index = sb.indexOf(target, fromIndex)) != -1) {
	    fromIndex = index + replacement.length();
	    sb.replace(index, index + target.length(), replacement);
	}
    }

    /**
     * Compara objetos por valor para las clases Boolean, Number, Date y Calendar. Si los objetos no son de ninguna de estas clases, invoca al método
     * equals(Object obj).
     * 
     * @param obj1
     * @param obj2
     * @return true si obj1 y obj2 son null, sus valores como Boolean, Number, Date o Calendar son iguales o si equals(Object obj) devuelve true. En caso
     *         contrario, devuelve false.
     */
    public static boolean equalObjects(Object obj1, Object obj2) {
	if (obj1 == null && obj2 == null) {
	    return true;
	} else if (obj1 == null || obj2 == null) {
	    return false;
	}
	if (isAssignable(Number.class, obj1, obj2)) {
	    Number number1 = (Number) obj1;
	    Number number2 = (Number) obj2;
	    return number1.doubleValue() == number2.doubleValue();
	} else if (isAssignable(CharSequence.class, obj1, obj2)) {
	    CharSequence cs1 = (CharSequence) obj1;
	    CharSequence cs2 = (CharSequence) obj2;
	    return cs2.equals(cs1);
	} else if (isAssignable(Date.class, obj1, obj2)) {
	    Date date1 = (Date) obj1;
	    Date date2 = (Date) obj2;
	    return date1.getTime() == date2.getTime();
	} else if (isAssignable(Calendar.class, obj1, obj2)) {
	    Calendar date1 = (Calendar) obj1;
	    Calendar date2 = (Calendar) obj2;
	    return date1.getTime().getTime() == date2.getTime().getTime();
	} else if (isAssignable(Comparable.class, obj1, obj2)) {
	    @SuppressWarnings("unchecked")
	    Comparable<Object> comp1 = (Comparable<Object>) obj1;
	    @SuppressWarnings("unchecked")
	    Comparable<Object> comp2 = (Comparable<Object>) obj2;
	    return comp1.compareTo(comp2) == 0;
	}
	return obj1.equals(obj2);
    }

    private static boolean isAssignable(Class<?> clazz, Object obj1, Object obj2) {
	return clazz.isAssignableFrom(obj1.getClass()) && clazz.isAssignableFrom(obj2.getClass());
    }
}
