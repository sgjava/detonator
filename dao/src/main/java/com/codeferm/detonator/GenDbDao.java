/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;

/**
 * Generic Database DAO.
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
    private final Properties properties;
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
     * @param propertyFile Name of property file.
     * @param idClass ID class type.
     * @param dtoClass DTO class type.
     */
    public GenDbDao(final DataSource dataSource, final String propertyFile, final Class idClass, final Class dtoClass) {
        this.dataSource = dataSource;
        this.idClass = idClass;
        this.dtoClass = dtoClass;
        this.properties = loadProperties(propertyFile);
        // Get ID class read methods
        idReadMethods = getReadMethods(idClass.getDeclaredFields(), idClass);
        dbDao = new DbUtilsDsDao(this.dataSource);
    }

    /**
     * Load properties file from class path.
     *
     * @param propertyFile Name of property file.
     * @return Properties.
     */
    public final Properties loadProperties(final String propertyFile) {
        Properties props = new Properties();
        // Get properties from classpath
        try (final var stream = GenDbDao.class.getClassLoader().getResourceAsStream(propertyFile)) {
            props.load(stream);
        } catch (IOException e) {
            throw new RuntimeException("Property file exception", e);
        }
        return props;
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
    public Object[] beanToParams(final ID id) {
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
    public List<T> findAll() {
        return dbDao.selectList(properties.getProperty("findAll"), dtoClass);
    }

    @Override
    public T findById(ID Id) {
        return dbDao.select(properties.getProperty("findById"), beanToParams(Id), dtoClass);
    }

    @Override
    public void save(T dto) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(ID Id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void update(T dto, ID Id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
