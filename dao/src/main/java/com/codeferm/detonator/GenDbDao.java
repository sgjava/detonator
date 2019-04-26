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
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 * Generic Database DAO.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 * @param <I> Identity Object.
 * @param <D> Data Transfer Object.
 */
public class GenDbDao<I, D> implements Dao<I, D> {

    /**
     * DataSource.
     */
    private final DataSource dataSource;
    /**
     * Database DAO.
     */
    private final DbDao dbDao;
    /**
     * Query map.
     */
    private final Map<String, String> sqlMap;
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
    private final List<Method> idReadMethods;

    /**
     * Constructor to initialize DataSource.
     *
     * @param dataSource DataSource to use for connections.
     * @param sqlMap Map of SQL for select, insert, update and delete operations.
     * @param idClass ID class type.
     * @param dtoClass DTO class type.
     */
    public GenDbDao(final DataSource dataSource, final Map<String, String> sqlMap, final Class idClass, final Class dtoClass) {
        this.dataSource = dataSource;
        this.sqlMap = sqlMap;
        this.idClass = idClass;
        this.dtoClass = dtoClass;
        // Get ID class read methods
        idReadMethods = getReadMethods(idClass.getDeclaredFields(), idClass);
        dbDao = new DbUtilsDsDao(this.dataSource);
    }

    /**
     * Get read method of each property.
     *
     * @param fields {@code Array} containing bean field names
     * @param clazz {@code Class} of bean
     * @return {@code Map} of bean write methods
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
     * @param id Identity object.
     * @return Array of parameters.
     */
    public Object[] beanToParams(final I id) {
        final var params = new Object[idReadMethods.size()];
        int i = 0;
        for (final var idReadMethod : idReadMethods) {
            try {
                params[i++] = idReadMethod.invoke(id, (Object[]) null);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return params;
    }

    @Override
    public List<D> findAll() {
        return dbDao.selectList(sqlMap.get("findAll"), dtoClass);
    }

    @Override
    public D findById(I Id) {
        return dbDao.select(sqlMap.get("findById"), beanToParams(Id), dtoClass);
    }

    @Override
    public void save(D dto) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(I Id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void update(I Id, D dto) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
