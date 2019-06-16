/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import javax.sql.DataSource;

/**
 * Generic Database DAO. The SQL parameter markers must match the order of the value and key fields for mapping to work correctly.
 * Currently value, key and SQL generation put fields is in alpha order. Value and key methods are cached on construction to improve
 * mapping performance.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class GenDbDao<K, V> implements DbDao<K, V> {

    /**
     * DataSource.
     */
    private final DataSource dataSource;
    /**
     * Database DAO.
     */
    private final Db dbDao;
    /**
     * SQL as properties.
     */
    private final Properties sql;
    /**
     * Value class type.
     */
    private final Class vClass;
    /**
     * Key class type.
     */
    private final Class kClass;
    /**
     * Value read methods.
     */
    private final List<Method> vReadMethods;
    /**
     * Key read methods.
     */
    private final List<Method> kReadMethods;
    /**
     * Key write methods.
     */
    private final List<Method> kWriteMethods;
    /**
     * Key method parameters.
     */
    private final List<Parameter[]> kParams;
    /**
     * getKey method of value object.
     */
    private Method keyMethod;

    /**
     * Constructor to initialize DataSource and cache value and key methods.
     *
     * @param dataSource DataSource to use for connections.
     * @param properties SQL statements as properties.
     * @param kClass Key class type.
     * @param vClass Value class type.
     */
    public GenDbDao(final DataSource dataSource, final Properties properties, final Class kClass, final Class vClass) {
        this.dataSource = dataSource;
        this.kClass = kClass;
        this.vClass = vClass;
        this.sql = properties;
        // Get value read methods
        vReadMethods = getReadMethods(vClass.getDeclaredFields(), vClass);
        // Get key read methods
        kReadMethods = getReadMethods(kClass.getDeclaredFields(), kClass);
        // Get key write methods
        kWriteMethods = getWriteMethods(kClass.getDeclaredFields(), kClass);
        // If key is a simple class then ignore params
        if (kWriteMethods != null) {
            kParams = new ArrayList<>();
            // Get key write method parameters
            kWriteMethods.forEach(kWriteMethod -> {
                kParams.add(kWriteMethod.getParameters());
            });
        } else {
            kParams = null;
        }
        dbDao = new DbUtilsDs(this.dataSource);
        // Get value fields
        final var fields = vClass.getDeclaredFields();
        // Get last field
        final var field = fields[fields.length - 1];
        // Last field should be key if it exists
        if (field.getName().equals("key")) {
            try {
                keyMethod = new PropertyDescriptor(field.getName(), vClass).getReadMethod();
            } catch (IntrospectionException e) {
                throw new RuntimeException(e);
            }
        } else {
            keyMethod = null;
        }
    }

    /**
     * Get key or null;
     *
     * @param value Value to get key from.
     * @return ID;
     */
    public K getKey(V value) {
        K k = null;
        if (keyMethod != null) {
            try {
                k = (K) keyMethod.invoke(value, (Object[]) null);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return k;
    }

    /**
     * Get read method of each property. Built in key field is ignored if present.
     *
     * @param fields {@code Array} containing bean field names.
     * @param clazz {@code Class} of bean.
     * @return {@code Map} of bean write methods.
     */
    public final List<Method> getReadMethods(final Field[] fields, final Class clazz) {
        final List<Method> list = new ArrayList<>();
        for (Field field : fields) {
            // Ignore synthetic classes, dynamic proxies and key field
            if (!field.isSynthetic() && !field.getName().equals("key")) {
                PropertyDescriptor propertyDescriptor;
                try {
                    propertyDescriptor = new PropertyDescriptor(field.getName(), clazz);
                } catch (IntrospectionException e) {
                    throw new RuntimeException(e);
                }
                list.add(propertyDescriptor.getReadMethod());
            }
        }
        return list;
    }

    /**
     * Get write method of each property.
     *
     * @param fields {@code Array} containing bean field names.
     * @param clazz {@code Class} of bean.
     * @return {@code Map} of bean write methods.
     */
    public final List<Method> getWriteMethods(final Field[] fields, final Class clazz) {
        final List<Method> list = new ArrayList<>();
        for (Field field : fields) {
            // Ignore synthetic classes or dynamic proxies.
            if (!field.isSynthetic()) {
                PropertyDescriptor propertyDescriptor;
                try {
                    propertyDescriptor = new PropertyDescriptor(field.getName(), clazz);
                } catch (IntrospectionException e) {
                    throw new RuntimeException(e);
                }
                list.add(propertyDescriptor.getWriteMethod());
            }
        }
        return list;
    }

    /**
     * Return Object array with values in order of then bean's accessor methods. If no read methods then it is considered a simple
     * type.
     *
     * @param bean Bean object.
     * @param readMethods Bean's read methods.
     * @return Array of parameters.
     */
    public Object[] beanToParams(final Object bean, final List<Method> readMethods) {
        Object[] params;
        // If read methods then it's a bean
        if (readMethods != null) {
            params = new Object[readMethods.size()];
            int i = 0;
            for (final var readMethod : readMethods) {
                try {
                    params[i++] = readMethod.invoke(bean, (Object[]) null);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            params = new Object[]{bean};
        }
        return params;
    }

    /**
     * Convert Map to new key. Handle BigDecimal to integer type mapping.
     *
     * @param map Map should be ordered by field name in ascending order.
     * @return Populated key.
     */
    public K mapToKey(final Map<String, Object> map) {
        try {
            // Create new key instance
            final K key = (K) kClass.getDeclaredConstructor().newInstance();
            // Write off returned key fields to bean
            final var it = map.entrySet().iterator();
            // Interate Map
            kWriteMethods.forEach(writeMethod -> {
                var i = 0;
                try {
                    // Get returned value from Map
                    final var paramValue = it.next().getValue();
                    final var paramType = kParams.get(i++);
                    // If parameter type and value type match
                    if (paramType[0].getType() == paramValue.getClass()) {
                        writeMethod.invoke(key, paramValue);
                    } else if (paramValue instanceof BigDecimal) {
                        if (paramType[0].getType() == Long.class) {
                            writeMethod.invoke(key, ((BigDecimal) paramValue).longValueExact());
                        } else {
                            // Type didn't match Long, so convert BigDecimal to int value
                            writeMethod.invoke(key, ((BigDecimal) paramValue).intValueExact());
                        }
                    } else {
                        throw new RuntimeException(String.format("Was expecting %s and got %s", paramType[0].getType(), paramValue.
                                getClass()));
                    }
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            });
            return key;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException("Error creating key class", e);
        }
    }

    /**
     * Return all values.
     *
     * @return List of all values.
     */
    @Override
    public List<V> findAll() {
        return dbDao.selectList(sql.getProperty("findAll"), vClass);
    }

    /**
     * Return one value by key.
     *
     * @param key Key of record to return.
     * @return Single record.
     */
    @Override
    public V find(final K key) {
        return dbDao.select(sql.getProperty("find"), beanToParams(key, kReadMethods), vClass);
    }

    /**
     * Return range of values using from and to keys inclusive.
     *
     * @param fromKey Search from.
     * @param toKey Search to.
     * @return List of values.
     */
    @Override
    public List<V> findRange(final K fromKey, final K toKey) {
        // Value params array as List
        final var list = new ArrayList(Arrays.asList(beanToParams(fromKey, kReadMethods)));
        // Add ID params array to List
        list.addAll(Arrays.asList(beanToParams(toKey, kReadMethods)));
        return findBy("findRange", list.toArray());
    }

    /**
     * Return List of values using named query and parameters.
     *
     * @param name Query name.
     * @param params Query parameters,
     * @return List of values.
     */
    @Override
    public List<V> findBy(final String name, final Object[] params) {
        return dbDao.selectList(sql.getProperty(name), params, vClass);
    }

    /**
     * Save the value.
     *
     * @param value Value to save.
     */
    @Override
    public void save(final V value) {
        dbDao.update(sql.getProperty("save"), beanToParams(value, vReadMethods));
    }

    /**
     * Save Map of value using batch operation. Note for RDBMS implementation ID is not used.
     *
     * @param map Map of keys and values to save.
     */
    @Override
    public void save(final Map<K, V> map) {
        final var params = new Object[map.size()][];
        var i = 0;
        for (final var entry : map.entrySet()) {
            params[i++] = beanToParams(entry.getValue(), vReadMethods);
        }
        dbDao.batch(sql.getProperty("save"), params);
    }

    /**
     * Save the value and return generated key.
     *
     * @param value Value to save.
     * @return Generated key.
     */
    @Override
    public K saveReturnKey(final V value, final String[] keyNames) {
        // Create sorted Map of returned ID keys
        final var map = new TreeMap<String, Object>(dbDao.updateReturnKeys(sql.getProperty("save"), beanToParams(value,
                vReadMethods), keyNames));
        return mapToKey(map);
    }

    /**
     * Delete the value by key.
     *
     * @param key Key of value to delete.
     */
    @Override
    public void delete(final K key) {
        dbDao.update(sql.getProperty("delete"), beanToParams(key, kReadMethods));
    }

    /**
     * Delete values using named query and parameters.
     *
     * @param name Query name.
     * @param params Query parameters,
     */
    @Override
    public void deleteBy(final String name, final Object[] params) {
        dbDao.update(sql.getProperty(name), params);
    }

    /**
     * Delete list of values by key.
     *
     * @param list List of keys to delete.
     */
    @Override
    public void delete(final List<K> list) {
        final var params = new Object[list.size()][];
        var i = 0;
        for (final K id : list) {
            params[i++] = beanToParams(id, kReadMethods);
        }
        dbDao.batch(sql.getProperty("delete"), params);
    }

    /**
     * Update value by key.
     *
     * @param key Key of value to update.
     * @param value Updated value.
     */
    @Override
    public void update(final K key, final V value) {
        // Value params array as List
        final var list = new ArrayList(Arrays.asList(beanToParams(value, vReadMethods)));
        // Add ID params array to List
        list.addAll(Arrays.asList(beanToParams(key, kReadMethods)));
        dbDao.update(sql.getProperty("update"), list.toArray());
    }

    /**
     * Update value using named query and parameters.
     *
     * @param name Query name.
     * @param params Query parameters,
     */
    @Override
    public void updateBy(final String name, final Object[] params) {
        dbDao.update(sql.getProperty(name), params);
    }

    /**
     * Update map of values.
     *
     * @param map Map of keys and values to update.
     */
    @Override
    public void update(final Map<K, V> map) {
        final var params = new Object[map.size()][];
        var i = 0;
        for (final var entry : map.entrySet()) {
            // Value params array as List
            final var list = new ArrayList(Arrays.asList(beanToParams(entry.getValue(), vReadMethods)));
            // Add ID params array to List
            list.addAll(Arrays.asList(beanToParams(entry.getKey(), kReadMethods)));
            params[i++] = list.toArray();
        }
        dbDao.batch(sql.getProperty("update"), params);
    }
}
