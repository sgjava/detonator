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
import java.util.Properties;
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
     * ID class type.
     */
    private final Class idClass;
    /**
     * DTO class type.
     */
    private final Class dtoClass;
    /**
     * ID read methods.
     */
    private final List<Method> dtoReadMethods;
    /**
     * ID read methods.
     */
    private final List<Method> idReadMethods;

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

    @Override
    public List<T> findAll() {
        return dbDao.selectList(sql.getProperty("findAll"), dtoClass);
    }

    @Override
    public T findById(ID id) {
        return dbDao.select(sql.getProperty("findById"), beanToParams(id, idReadMethods), dtoClass);
    }

    @Override
    public void save(T dto) {
        dbDao.update(sql.getProperty("save"), beanToParams(dto, dtoReadMethods));
    }

    @Override
    public void delete(ID id) {
        dbDao.update(sql.getProperty("delete"), beanToParams(id, idReadMethods));
    }

    @Override
    public void update(T dto, ID id) {
        // DTO params array as List
        final var list = new ArrayList(Arrays.asList(beanToParams(dto, dtoReadMethods)));
        // Add ID params array to List
        list.addAll(Arrays.asList(beanToParams(id, idReadMethods)));
        dbDao.update(sql.getProperty("update"), list.toArray());
    }
}
