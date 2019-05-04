/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import javax.sql.DataSource;

/**
 * Generic Database DAO. The SQL parameter markers must match the order of the DTO and ID fields for mapping to work correctly.
 * Currently DTO, ID and SQL generation put fields is in alpha order. DTO and ID methods are cached on construction to improve
 * mapping performance.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 * @param <T> Identity Object.
 * @param <ID> Data Transfer Object.
 */
public class GenDbDao<T, ID> implements Dao<T, ID> {

    /**
     * DataSource.
     */
    private final DataSource dataSource;
    /**
     * Database DAO.
     */
    private final DbDao dbDao;
    /**
     * SQL as properties.
     */
    private final Properties sql;
    /**
     * DTO class type.
     */
    private final Class dtoClass;
    /**
     * DTO class type.
     */
    private final Class idClass;
    /**
     * DTO read methods.
     */
    private final List<Method> dtoReadMethods;
    /**
     * ID read methods.
     */
    private final List<Method> idReadMethods;
    /**
     * ID read methods.
     */
    private final List<Method> idWriteMethods;

    /**
     * Constructor to initialize DataSource and cache DTO and ID methods.
     *
     * @param dataSource DataSource to use for connections.
     * @param properties SQL statements as properties.
     * @param idClass ID class type.
     * @param dtoClass DTO class type.
     */
    public GenDbDao(final DataSource dataSource, final Properties properties, final Class idClass, final Class dtoClass) {
        this.dataSource = dataSource;
        this.idClass = idClass;
        this.dtoClass = dtoClass;
        this.sql = properties;
        // Get DTO read methods
        dtoReadMethods = getReadMethods(dtoClass.getDeclaredFields(), dtoClass);
        // Get ID read methods
        idReadMethods = getReadMethods(idClass.getDeclaredFields(), idClass);
        // Get ID write methods
        idWriteMethods = getWriteMethods(idClass.getDeclaredFields(), idClass);
        dbDao = new DbUtilsDsDao(this.dataSource);
    }

    /**
     * Get read method of each property.
     *
     * @param fields {@code Array} containing bean field names.
     * @param clazz {@code Class} of bean.
     * @return {@code Map} of bean write methods.
     */
    public final List<Method> getReadMethods(final Field[] fields, final Class clazz) {
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
     * Return Object array with values in order of then bean's accessor methods.
     *
     * @param bean Bean object.
     * @param readMethods Bean's read methods.
     * @return Array of parameters.
     */
    public Object[] beanToParams(final Object bean, final List<Method> readMethods) {
        final var params = new Object[readMethods.size()];
        int i = 0;
        for (final var readMethod : readMethods) {
            try {
                params[i++] = readMethod.invoke(bean, (Object[]) null);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return params;
    }

    /**
     * Return all records.
     *
     * @return List of all records.
     */
    @Override
    public List<T> findAll() {
        return dbDao.selectList(sql.getProperty("findAll"), dtoClass);
    }

    /**
     * Return one record by ID.
     *
     * @param id ID of record to return.
     * @return Single record.
     */
    @Override
    public T findById(final ID id) {
        return dbDao.select(sql.getProperty("findById"), beanToParams(id, idReadMethods), dtoClass);
    }

    /**
     * Return List of records using named query parameters.
     *
     * @param name Query name.
     * @param params Query parameters,
     * @return List of records.
     */
    @Override
    public List<T> findBy(final String name, final Object[] params) {
        return dbDao.selectList(sql.getProperty(name), params, dtoClass);
    }

    /**
     * Save the record.
     *
     * @param dto Record to save.
     */
    @Override
    public void save(final T dto) {
        dbDao.update(sql.getProperty("save"), beanToParams(dto, dtoReadMethods));
    }

    /**
     * Save List of records using batch operation.
     *
     * @param list Save List of records.
     */
    @Override
    public void save(final List<T> list) {
        final var params = new Object[list.size()][];
        var i = 0;
        for (final T t : list) {
            params[i++] = beanToParams(t, dtoReadMethods);
        }
        dbDao.batch(sql.getProperty("save"), params);
    }

    /**
     * Save the record and return identity key.
     *
     * @param dto Record to save.
     * @return Generated ID.
     */
    @Override
    public ID saveReturnId(final T dto) {
        // Create sorted Map of returned ID keys
        final var map = new TreeMap<String, Object>(dbDao.updateReturnKeys(sql.getProperty("save"), beanToParams(dto,
                dtoReadMethods)));
        // Create new ID instabnce
        try {
            final ID id = (ID) idClass.getDeclaredConstructor().newInstance();
            // Write off returned key fields to bean
            final var it = map.entrySet().iterator();
            idWriteMethods.forEach((final  var writeMethod) -> {
                try {
                    // Get returned value from Map
                    final var pair = it.next();
                    writeMethod.invoke(id, pair.getValue());
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            });
            return id;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException("Error creating ID class", e);
        }
    }

    /**
     * Delete the record by ID.
     *
     * @param id ID of record to delete.
     */
    @Override
    public void delete(final ID id) {
        dbDao.update(sql.getProperty("delete"), beanToParams(id, idReadMethods));
    }

    /**
     * Delete records using named query and parameters.
     *
     * @param name Query name.
     * @param params Query parameters,
     */
    @Override
    public void deleteBy(final String name, final Object[] params) {
        dbDao.update(sql.getProperty(name), params);
    }

    /**
     * Delete the record by ID.
     *
     * @param list List of IDs to delete.
     */
    @Override
    public void delete(final List<ID> list) {
        final var params = new Object[list.size()][];
        var i = 0;
        for (final ID id : list) {
            params[i++] = beanToParams(id, idReadMethods);
        }
        dbDao.batch(sql.getProperty("delete"), params);
    }

    /**
     * Update the record.
     *
     * @param dto Updated record.
     * @param id ID of record to update.
     */
    @Override
    public void update(final T dto, final ID id) {
        // DTO params array as List
        final var list = new ArrayList(Arrays.asList(beanToParams(dto, dtoReadMethods)));
        // Add ID params array to List
        list.addAll(Arrays.asList(beanToParams(id, idReadMethods)));
        dbDao.update(sql.getProperty("update"), list.toArray());
    }

    /**
     * Update records using named query and parameters.
     *
     * @param name Query name.
     * @param params Query parameters,
     */
    @Override
    public void updateBy(final String name, final Object[] params) {
        dbDao.update(sql.getProperty(name), params);
    }

    /**
     * Update map of records.
     *
     * @param map Map of DTOs and IDs to update.
     */
    @Override
    public void update(final Map<ID, T> map) {
        final var params = new Object[map.size()][];
        var i = 0;
        for (final var entry : map.entrySet()) {
            // DTO params array as List
            final var list = new ArrayList(Arrays.asList(beanToParams(entry.getValue(), dtoReadMethods)));
            // Add ID params array to List
            list.addAll(Arrays.asList(beanToParams(entry.getKey(), idReadMethods)));
            params[i++] = list.toArray();
        }
        dbDao.batch(sql.getProperty("update"), params);
    }
}
