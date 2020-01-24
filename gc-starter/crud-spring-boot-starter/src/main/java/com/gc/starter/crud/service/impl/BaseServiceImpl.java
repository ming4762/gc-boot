package com.gc.starter.crud.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gc.common.base.utils.ReflectUtil;
import com.gc.starter.crud.constants.CrudConstants;
import com.gc.starter.crud.mapper.BaseMapper;
import com.gc.starter.crud.model.BaseModel;
import com.gc.starter.crud.model.Sort;
import com.gc.starter.crud.service.BaseService;
import com.gc.starter.crud.utils.CrudUtils;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author shizhongming
 * 2020/1/10 9:51 下午
 */
public abstract class BaseServiceImpl<K extends BaseMapper<T>, T extends BaseModel> extends ServiceImpl<K, T> implements BaseService<T> {

    /**
     * 通过实体删除
     * @param model 实体类
     * @return 删除的数量
     */
    @Override
    @NotNull
    public Integer delete(@NotNull T model) {
        final List<String> keyList = this.getKeyList();
        if (keyList.isEmpty()) {
            log.warn("未找到主键无法执行删除");
            return 0;
        }
        if (keyList.size() == 1) {
            return this.baseMapper.deleteById((Serializable) ReflectUtil.getFieldValue(model, keyList.get(0)));
        }
        final Class<? extends BaseModel> clazz = CrudUtils.getModelClassByType(this.getModelType());
        if (clazz == null) {
            return 0;
        }
        final QueryWrapper<T> queryWrapper = new QueryWrapper<T>();
        keyList.forEach(key -> queryWrapper.eq(CrudUtils.getDbField(clazz, key), ReflectUtil.getFieldValue(model, key)));
        return this.baseMapper.delete(queryWrapper);
    }

    /**
     * 通过实体类批量删除
     * @param modelList
     * @return
     */
    @Override
    public @NotNull Integer batchDelete(@NotNull List<T> modelList) {
        int num;
        final List<String> keyList = this.getKeyList();
        if (keyList.isEmpty()) {
            log.warn("未找到实体类主键，无法执行删除");
            return 0;
        }
        if (keyList.size() == 1) {
            final String key = keyList.get(0);
            final Set<Serializable> keyValueList = modelList
                    .stream()
                    .map(model -> (Serializable)ReflectUtil.getFieldValue(model, key))
                    .collect(Collectors.toSet());
            num = this.baseMapper.deleteBatchIds(keyValueList);
        } else {
            num = modelList
                    .stream()
                    .mapToInt(this::delete)
                    .sum();
        }
        return num;
    }

    /**
     * 查询单一实体
     * @param model
     * @return
     */
    @Nullable
    @Override
    public T get(@NotNull T model) {
        final List<String> keyList = this.getKeyList();
        if (keyList.isEmpty()) {
            return null;
        }
        if (keyList.size() == 1) {
            return this.getById((Serializable) ReflectUtil.getFieldValue(model, keyList.get(0)));
        } else {
            final Class<? extends BaseModel> clazz = CrudUtils.getModelClassByType(this.getModelType());
            if (clazz == null) {
                return null;
            }
            final QueryWrapper<T> queryWrapper = new QueryWrapper<T>();
            keyList.forEach(key -> queryWrapper.eq(CrudUtils.getDbField(clazz, key), ReflectUtil.getFieldValue(model, key)));
            return this.getOne(queryWrapper);
        }
    }


    /**
     * 重写方法
     * 防止idList为空时发生错误
     * @param idList
     * @return
     */
    @Override
    public List<T> listByIds(Collection<? extends Serializable> idList) {
        if (idList.isEmpty()) {
            return Lists.newArrayList();
        }
        final Set<? extends Serializable> idSet = new HashSet<>(idList);
        if (idSet.size() == 1) {
            final T result = this.getById(idSet.iterator().next());
            return result == null ? Lists.newArrayList() : Lists.newArrayList(result);
        }
        return super.listByIds(idList);
    }

    /**
     * 查询实体函数
     * @param queryWrapper
     * @param parameter
     * @param paging
     * @return
     */
    @Override
    public @NotNull List<T> list(@NotNull QueryWrapper<T> queryWrapper, @NotNull Map<String, Object> parameter, @NotNull Boolean paging) {
        this.analysisOrder(queryWrapper, parameter, paging);
        return super.list(queryWrapper);
    }

    /**
     * 插入更新带有创建用户
     * @param model
     * @return
     */
    @Override
    public boolean saveOrUpdateWithCreateUser(@NotNull T model, Long userId) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        boolean isAdd = this.isAdd(model);
        if (isAdd) {
            return this.saveWithUser(model, userId);
        } else {
            return this.updateById(model);
        }
    }

    /**
     * 插入更新带有更新人员
     * @param model
     * @param userId
     * @return
     */
    @Override
    public boolean saveOrUpdateWithUpdateUser(@NotNull T model, Long userId) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        boolean isAdd = this.isAdd(model);
        if (isAdd) {
            return this.save(model);
        } else {
            return this.updateWithUserById(model, userId);
        }
    }

    /**
     * 插入更新带有所有人员
     * @param model
     * @param userId
     * @return
     */
    @Override
    public boolean saveOrUpdateWithAllUser(@NotNull T model, Long userId) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        boolean isAdd = this.isAdd(model);
        if (isAdd) {
            return this.saveWithUser(model, userId);
        } else {
            return this.updateWithUserById(model, userId);
        }
    }


    @Override
    public boolean saveWithUser(@NotNull T model, Long userId) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        this.setCreateUserId(model, userId);
        this.setCreateTime(model);
        return this.save(model);
    }

    @Override
    public boolean updateWithUserById(@NotNull T model, Long userId) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        this.setUpdateTime(model);
        this.setUpdateUserId(model, userId);
        return updateById(model);
    }

    /**
     * 解析排序字段
     * @param queryWrapper
     * @param parameter
     * @param paging
     */
    public void analysisOrder(@NotNull QueryWrapper<T> queryWrapper, @NotNull Map<String, Object> parameter, @NotNull Boolean paging) {
        final String sortName = (String) parameter.get(CrudConstants.sortName.name());
        // 如果灭有分页且存在排序字段手动进行排序
        if (!paging && sortName != null) {
            final String sortOrder = (String) parameter.get(CrudConstants.sortOrder.name());
            final Class<? extends BaseModel> clazz = CrudUtils.getModelClassByType(this.getModelType());
            final List<Sort> sortList = CrudUtils.analysisOrder(sortName, sortOrder, clazz);
            if (!sortList.isEmpty()) {
                sortList.forEach(sort -> {
                    if ("ASC".equals(sort.getOrder().toUpperCase())) {
                        queryWrapper.orderByAsc(sort.getDbName());
                    } else {
                        queryWrapper.orderByDesc(sort.getDbName());
                    }
                });
            }
        }
    }

    /**
     * 获取实体类类型
     * @return 实体类类型
     */
    private Type getModelType() {
        return ((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * 获取数据库表的key
     * @return
     */
    private List<String> getKeyList() {
        final List<Field> keyList = CrudUtils.getModelKeysByType(this.getModelType());
        return keyList
                .stream()
                .map(Field::getName)
                .collect(Collectors.toList());

    }

    /**
     * 判断是否是添加
     * @param entity
     * @return
     */
    protected boolean isAdd(@NotNull T entity) {
        Serializable key = this.getKeyValue(entity);
        return StringUtils.checkValNull(key) || Objects.isNull(this.getById(key));
    }

    /**
     * 设置创建用户ID
     * @param model
     * @param userId
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private void setCreateUserId(T model, Long userId) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method method = model.getClass().getMethod("setCreateUserId", Long.class);
        method.invoke(model, userId);
    }

    /**
     * 设置创建时间
     * @param model
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private void setCreateTime(T model) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method method = model.getClass().getMethod("setCreateTime", Date.class);
        method.invoke(model, new Date());
    }

    private void setUpdateUserId(T model, Long userId) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method method = model.getClass().getMethod("setUpdateUserId", Long.class);
        method.invoke(model, userId);
    }

    private void setUpdateTime(T model) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method method = model.getClass().getMethod("setUpdateTime", Date.class);
        method.invoke(model, new Date());
    }

    /**
     * 获取主键的值
     * @param entity
     * @return
     */
    private Serializable getKeyValue(@NotNull T entity) {
        Class<?> cls = entity.getClass();
        TableInfo tableInfo = TableInfoHelper.getTableInfo(cls);
        Assert.notNull(tableInfo, "error: can not execute. because can not find cache of TableInfo for entity!");
        String keyProperty = tableInfo.getKeyProperty();
        Assert.notEmpty(keyProperty, "error: can not execute. because can not find column for id from entity!");
        return (Serializable) ReflectionKit.getMethodValue(cls, entity, tableInfo.getKeyProperty());
    }
}