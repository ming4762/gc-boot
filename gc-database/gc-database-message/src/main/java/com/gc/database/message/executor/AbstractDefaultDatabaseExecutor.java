package com.gc.database.message.executor;

import com.gc.common.base.exception.SqlRuntimeException;
import com.gc.common.base.utils.ReflectUtil;
import com.gc.database.message.annotation.DatabaseField;
import com.gc.database.message.constants.ExceptionConstant;
import com.gc.database.message.constants.ExtMappingConstant;
import com.gc.database.message.constants.TableTypeConstants;
import com.gc.database.message.constants.TypeMappingConstant;
import com.gc.database.message.converter.DbJavaTypeConverter;
import com.gc.database.message.exception.SmartDatabaseException;
import com.gc.database.message.pojo.bo.ColumnBO;
import com.gc.database.message.pojo.bo.DatabaseConnectionBO;
import com.gc.database.message.pojo.dbo.*;
import com.gc.database.message.utils.CacheUtils;
import com.gc.database.message.utils.DatabaseUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author shizhongming
 * 2020/1/18 9:02 下午
 */
@Slf4j
public abstract class AbstractDefaultDatabaseExecutor implements DatabaseExecutor {

    private final DbJavaTypeConverter dbJavaTypeConverter;

    public AbstractDefaultDatabaseExecutor(DbJavaTypeConverter dbJavaTypeConverter) {
        this.dbJavaTypeConverter = dbJavaTypeConverter;
    }

    static {
        AbstractDefaultDatabaseExecutor.mappingDatabaseFieldToCache();
        AbstractDefaultDatabaseExecutor.initTypeMappingCache();
    }

    /**
     * IN 占位符
     */
    private static final String IN_PLACEHOLDER = "%in";

    /**
     * 获取数据库连接
     * @param databaseConnection 数据库连接信息
     * @return 数据库连接
     */
    @Override
    public @NonNull
    Connection getConnection(@NonNull DatabaseConnectionBO databaseConnection) throws SQLException {
        final String key = databaseConnection.createConnectionKey();
        Connection connection = CacheUtils.getConnection(key);
        if (connection == null || connection.isClosed()) {
            connection = this.createConnection(databaseConnection);
            CacheUtils.setConnectionCache(key, connection);
        }
        return connection;
    }

    /**
     * 创建数据库连接
     * @param databaseConnection 数据库连接信息
     * @return 数据库连接
     */
    @Override
    public Connection createConnection(@NonNull DatabaseConnectionBO databaseConnection) throws SQLException {
        final Driver driver = databaseConnection.doGetDriver();
        final Properties properties = new Properties();
        properties.setProperty("user", databaseConnection.getUsername());
        properties.setProperty("remarks", Boolean.TRUE.toString());
        properties.setProperty("useInformationSchema", Boolean.TRUE.toString());
        properties.setProperty("password", databaseConnection.getPassword());
        return driver.connect(this.getUrl(databaseConnection), properties);
    }

    /**
     * 测试数据库连接
     * @param connection 数据库连接
     * @return 连接是否成功
     * @throws SQLException SQLException
     */
    @Override
    public boolean testConnection(Connection connection) throws SQLException {
        return connection != null && !connection.isClosed();
    }

    /**
     * 测试数据库连接
     * @param databaseConnection
     * @return
     * @throws SQLException
     */
    @Override
    public @NonNull Boolean testConnection(@NonNull DatabaseConnectionBO databaseConnection) throws SQLException {
        final String key = databaseConnection.createConnectionKey();
        if (StringUtils.isEmpty(key)) {
            return Boolean.FALSE;
        }
        final Connection connection = this.getConnection(databaseConnection);
        boolean result = this.testConnection(connection);
        if (!result) {
            CacheUtils.removeConnection(key);
        }
        return result;
    }

    /**
     * 获取表格信息
     * @param types 类型
     */
    @Override
    public @NonNull List<TableViewDO> listBaseTable(@NonNull DatabaseConnectionBO databaseConnection, @Nullable String tableNamePattern, TableTypeConstants... types) {
        ResultSet resultSet = null;
        try {
            final Connection connection = this.getConnection(databaseConnection);
            if (!this.testConnection(connection)|| ArrayUtils.isEmpty(types)) {
                return Lists.newArrayList();
            }
            // 转换类型
            String[] typesStr = Arrays.stream(types)
                    .map(Enum::name)
                    .toArray(String[]::new);
            // 获取resultSet
            resultSet = connection.getMetaData().getTables(connection.getCatalog(), connection.getSchema(), tableNamePattern, typesStr);
            Map<String, Field> mapping = this.getDatabaseMapping(TableViewDO.class);
            return DatabaseUtils.resultSetToModel(resultSet, TableViewDO.class, mapping);
        } catch (SQLException e) {
            throw new SqlRuntimeException(e);
        } finally {
            close(resultSet);
        }
    }

    /**
     * 查询主键信息
     * @param databaseConnection 数据库连接信息
     * @param tableName 标明
     * @return 主键列表
     */
    @Override
    public List<PrimaryKeyDO> listPrimaryKey(@NonNull DatabaseConnectionBO databaseConnection, String tableName)  {
        ResultSet resultSet = null;
        try {
            Connection connection = this.getConnection(databaseConnection);
            if (!this.testConnection(connection)) {
                return Lists.newArrayList();
            }
            resultSet = connection.getMetaData().getPrimaryKeys(connection.getCatalog(), null, tableName);
            Map<String, Field> mapping = CacheUtils.getFieldMapping(PrimaryKeyDO.class);
            if (mapping == null) {
                throw new SmartDatabaseException(ExceptionConstant.DATABASE_FILE_MAPPING_NOT_FOUND, PrimaryKeyDO.class.getName());
            }
            return DatabaseUtils.resultSetToModel(resultSet, PrimaryKeyDO.class, mapping);
        } catch (SQLException e) {
            throw new SqlRuntimeException(e);
        } finally {
            close(resultSet);
        }
    }

    /**
     * 查询外键信息
     * @param databaseConnection 数据库连接信息
     * @param tableName 表名
     * @return 外键列表
     */
    @Override
    public List<ImportKeyDO> listImportedKeys(@NonNull DatabaseConnectionBO databaseConnection, String tableName){
        ResultSet resultSet = null;
        try {
            Connection connection = this.getConnection(databaseConnection);
            if (!this.testConnection(connection)) {
                return Lists.newArrayList();
            }
            resultSet  = connection.getMetaData().getImportedKeys(connection.getCatalog(), connection.getSchema(), tableName);
            Map<String, Field> mapping = CacheUtils.getFieldMapping(ImportKeyDO.class);
            if (mapping == null) {
                throw new SmartDatabaseException(ExceptionConstant.DATABASE_FILE_MAPPING_NOT_FOUND, ImportKeyDO.class.getName());
            }
            return DatabaseUtils.resultSetToModel(resultSet, ImportKeyDO.class, mapping);
        } catch (SQLException e) {
            throw new SqlRuntimeException(e);
        } finally {
            close(resultSet);
        }
    }

    /**
     * 查询主键信息
     * @param databaseConnection 数据库连接信息
     * @param tableName 表名
     * @param unique 是否查询唯一索引
     * @param approximate
     * @return
     */
    @Override
    public List<IndexDO> listIndex(@NonNull DatabaseConnectionBO databaseConnection, String tableName, Boolean unique, Boolean approximate) {
        ResultSet resultSet = null;
        try {
            Connection connection = this.getConnection(databaseConnection);
            if (!this.testConnection(connection)) {
                return Lists.newArrayList();
            }
            if (approximate == null) {
                approximate = true;
            }
            if (unique == null) {
                unique = true;
            }
            resultSet  = connection.getMetaData().getIndexInfo(connection.getCatalog(), connection.getSchema(), tableName, unique, approximate);
            Map<String, Field> mapping = CacheUtils.getFieldMapping(IndexDO.class);
            if (mapping == null) {
                throw new SmartDatabaseException(ExceptionConstant.DATABASE_FILE_MAPPING_NOT_FOUND, IndexDO.class.getName());
            }
            return DatabaseUtils.resultSetToModel(resultSet, IndexDO.class, mapping);
        } catch (SQLException e) {
            throw new SqlRuntimeException(e);
        } finally {
            close(resultSet);
        }
    }

    /**
     * 查询列信息
     * @param databaseConnection 数据库连接信息
     * @param tableName 表名
     * @return 列信息
     */
    @Override
    @NonNull
    public List<ColumnBO> listColumn(@NonNull DatabaseConnectionBO databaseConnection, @NonNull String tableName) {
        final String join = "#";
        final List<ColumnDO> baseColumnList = this.listBaseColumn(databaseConnection, tableName);
        final List<ColumnBO> columnList = ColumnBO.batchCreateFromDo(baseColumnList);
        if (!columnList.isEmpty()) {
            Map<String, ColumnBO> columnMap = columnList.stream().collect(Collectors.toMap(column -> String.join(join, column.getTableName(), column.getColumnName()), item -> item));
            // 查询主键信息并设置
            List<PrimaryKeyDO> primaryKeyList = this.listPrimaryKey(databaseConnection, tableName);
            primaryKeyList.forEach(item -> {
                String key = String.join(join, item.getTableName(), item.getColumnName());
                ColumnBO column = columnMap.get(key);
                if (column != null) {
                    column.setPrimaryKey(Boolean.TRUE);
                    column.setKeySeq(item.getKeySeq());
                    column.setPkName(item.getPkName());
                }
            });
            // 查询外键信息并设置
            List<ImportKeyDO> importKeyList = this.listImportedKeys(databaseConnection, tableName);
            importKeyList.forEach(item -> {
                ColumnBO column = columnMap.get(String.join(join, item.getFktableName(), item.getPkcolumnName()));
                if (column != null) {
                    column.setImportKey(Boolean.TRUE);
                    column.setImportPkName(item.getPkName());
                }
            });
            // 查询索引信息并设置
            List<IndexDO> indexList = this.listUniqueIndex(databaseConnection, tableName);
            indexList.forEach(item -> {
                ColumnBO column = columnMap.get(String.join(join, item.getTableName(), item.getColumnName()));
                if (column != null) {
                    column.setIndexed(Boolean.TRUE);
                    column.setUnique(Boolean.TRUE);
                    column.setIndexType(item.getType());
                }
            });
            // TODO:查询其他索引
            // 设置其他信息
            columnList.forEach(item -> {
                TypeMappingConstant typeMappingConstant = this.dbJavaTypeConverter.convert(item);
                if (typeMappingConstant != null) {
                    item.setJavaType(typeMappingConstant.getJavaClass().getName());
                    item.setSimpleJavaType(typeMappingConstant.getJavaClass().getSimpleName());
                    // 设置extjs类型
                    final String extType = Optional.ofNullable(CacheUtils.getExtTypeMapping(item.getJavaType()))
                            .map(ExtMappingConstant::getExtClass)
                            .orElse(null);
                    item.setExtType(extType);
                }
            });
        }
        return columnList;
    }

    /**
     * 查询列基本信息
     * @param databaseConnection 数据库连接
     * @param tableName 表名
     * @return 列基本信息
     */
    @Override
    public List<ColumnDO> listBaseColumn(@NonNull DatabaseConnectionBO databaseConnection, @NonNull String tableName) {
        ResultSet resultSet = null;
        try {
            Connection connection = this.getConnection(databaseConnection);
            if (!this.testConnection(connection)) {
                return Lists.newArrayList();
            }
            resultSet  = connection.getMetaData().getColumns(connection.getCatalog(), connection.getSchema(), tableName, null);
            Map<String, Field> mapping = CacheUtils.getFieldMapping(ColumnDO.class);
            if (mapping == null) {
                throw new SmartDatabaseException(ExceptionConstant.DATABASE_FILE_MAPPING_NOT_FOUND, ColumnBO.class.getName());
            }
            return DatabaseUtils.resultSetToModel(resultSet, ColumnDO.class, mapping);
        } catch (SQLException sqlException) {
            throw new SqlRuntimeException(sqlException);
        } finally {
            close(resultSet);
        }
    }

    /**
     * 查询唯一索引
     * @param databaseConnection 数据库连接信息
     * @param tableName 表名
     * @return 唯一索引列表
     */
    @Override
    public List<IndexDO> listUniqueIndex(@NonNull DatabaseConnectionBO databaseConnection, String tableName) {
        return this.listIndex(databaseConnection, tableName, true, true);
    }

    /**
     * 映射数据库字段与实体类属性关系
     */
    private static void mappingDatabaseFieldToCache() {
        if (CacheUtils.isFieldMappingEmpty()) {
            CacheUtils.setFieldMapping(TableViewDO.class, mappingDatabaseField(TableViewDO.class));
            CacheUtils.setFieldMapping(PrimaryKeyDO.class, mappingDatabaseField(PrimaryKeyDO.class));
            CacheUtils.setFieldMapping(IndexDO.class, mappingDatabaseField(IndexDO.class));
            CacheUtils.setFieldMapping(ImportKeyDO.class, mappingDatabaseField(ImportKeyDO.class));
            CacheUtils.setFieldMapping(ColumnDO.class, mappingDatabaseField(ColumnDO.class));
            CacheUtils.setFieldMapping(ColumnRemarkDO.class, mappingDatabaseField(ColumnRemarkDO.class));
            CacheUtils.setFieldMapping(TableRemarkDO.class, mappingDatabaseField(TableRemarkDO.class));
        }
    }

    /**
     * 初始化类型映射
     */
    private static void initTypeMappingCache() {
        CacheUtils.setTypeMapping(
                Arrays.stream(TypeMappingConstant.values())
                .collect(Collectors.toMap(TypeMappingConstant :: getDataType, item -> item))
        );
        CacheUtils.setExtTypeMapping(
                Arrays.stream(ExtMappingConstant.values())
                .collect(Collectors.toMap(item -> item.getJavaClass().getName(), item -> item))
        );
    }

    /**
     * 映射实体类属性
     * @param clazz
     * @return
     */
    private static Map<String, Field> mappingDatabaseField(Class<? extends AbstractDatabaseBaseDO> clazz) {
        final Map<String, Field> mapping = Maps.newHashMap();
        Set<Field> fieldSet = Sets.newHashSet();
        // 获取所有field
        ReflectUtil.getAllFields(clazz, fieldSet);
        fieldSet.forEach(field -> {
                    final DatabaseField databaseFieldAnnotation = AnnotationUtils.getAnnotation(field, DatabaseField.class);
                    if (databaseFieldAnnotation != null) {
                        final String value = databaseFieldAnnotation.value();
                        if (!StringUtils.isEmpty(value)) {
                            field.setAccessible(true);
                            mapping.put(value, field);
                        }
                    }
                });
        return mapping;
    }

    /**
     * 查询表格备注
     * @param tableList 表格列表
     * @param commentSql 查询sql
     */
    @SneakyThrows
    protected void queryTableRemark(@NonNull DatabaseConnectionBO databaseConnection, @NonNull List<TableViewDO> tableList, @NonNull String commentSql) {
        if (tableList.isEmpty()) {
            return;
        }
        // 获取数据库连接
        final Connection connection = this.getConnection(databaseConnection);
        // 获取表名
        List<String> tableNameList = tableList.stream()
                .map(TableViewDO::getTableName).collect(Collectors.toList());
        PreparedStatement psmt = null;
        ResultSet rs = null;
        try {
            psmt = this.setInParameter(connection, commentSql, tableNameList);
            rs = psmt.executeQuery();
            final List<TableRemarkDO> tableRemarkList = DatabaseUtils.resultSetToModel(rs, TableRemarkDO.class, this.getDatabaseMapping(TableRemarkDO.class));
            // 转为map
            if (CollectionUtils.isNotEmpty(tableRemarkList)) {
                Map<String, String> tableRemarkMap = tableRemarkList.stream()
                        .collect(Collectors.toMap(TableRemarkDO :: getTableName, TableRemarkDO :: getRemark));
                // 遍历设置备注
                tableList.forEach(table -> table.setRemarks(tableRemarkMap.get(table.getTableName())));
            }
        } finally {
            close(rs);
            close(psmt);
        }
    }

    /**
     * 查询列备注
     * @param databaseConnection 数据库连接
     * @param columnList 表格列表
     * @param commentSql 查询sql
     */
    @SneakyThrows
    protected void queryColumnRemark(@NonNull DatabaseConnectionBO databaseConnection, @NonNull List<ColumnDO> columnList, @NonNull String commentSql) {
        if (columnList.isEmpty()) {
            return;
        }
        // 获取数据库连接
        final Connection connection = this.getConnection(databaseConnection);
        // 获取表名
        Set<String> tableNames = columnList.stream()
                .map(ColumnDO::getTableName).collect(Collectors.toSet());
        PreparedStatement psmt = null;
        ResultSet rs = null;
        try {
            psmt = this.setInParameter(connection, commentSql, tableNames);
            rs = psmt.executeQuery();
            final List<ColumnRemarkDO> columnRemarkList = DatabaseUtils.resultSetToModel(rs, ColumnRemarkDO.class, this.getDatabaseMapping(ColumnRemarkDO.class));
            if (CollectionUtils.isNotEmpty(columnRemarkList)) {
                Map<String, String> columnRemarkMap = columnRemarkList.stream()
                        .collect(Collectors.toMap(item -> item.getTableName() + item.getColumnName(), ColumnRemarkDO::getRemark));
                columnList.forEach(column -> column.setRemarks(columnRemarkMap.get(column.getTableName() + column.getColumnName())));
            }
        } finally {
            close(rs);
            close(psmt);
        }
    }

    /**
     * 设置IN参数
     * @param connection 数据库连接
     * @param commentSql sql
     * @param parameterList 参数列表
     * @return PreparedStatement
     */
    @SneakyThrows
    private PreparedStatement setInParameter(@NonNull Connection connection, @NonNull String commentSql, @NonNull Collection<String> parameterList) {
        if (CollectionUtils.isNotEmpty(parameterList)) {
            if (commentSql.contains(IN_PLACEHOLDER)) {
                commentSql = commentSql.replace(IN_PLACEHOLDER, String.format(" (%s) ", parameterList.stream().map(item -> "?").collect(Collectors.joining(","))));
            }
        }
        final PreparedStatement preparedStatement = connection.prepareStatement(commentSql);
        // 设置参数值
        AtomicInteger atomicInteger = new AtomicInteger(0);
        for (String parameter : parameterList) {
            preparedStatement.setString(atomicInteger.incrementAndGet(), parameter);
        }
        return preparedStatement;
    }

    /**
     * 获取数据库实体映射
     * @param clazz 实体类
     */
    protected Map<String, Field> getDatabaseMapping(Class<? extends AbstractDatabaseBaseDO> clazz) {
        final Map<String, Field> fieldMap = CacheUtils.getFieldMapping(clazz);
        if (MapUtils.isEmpty(fieldMap)) {
            throw new SmartDatabaseException(ExceptionConstant.DATABASE_FILE_MAPPING_NOT_FOUND, clazz.getName());
        }
        return fieldMap;
    }

    /**
     * 关闭 ResultSet
     * @param rs ResultSet
     */
    static  void close(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            log.info(e.getMessage(), e);
        }
    }

    /**
     * 关闭Statement
     * @param st Statement
     */
    static void close(Statement st) {
        try {
            if (st != null) {
                st.close();
            }
        } catch (SQLException e) {
            log.info(e.getMessage(), e);
        }
    }
}
