package ${packageName}.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ${packageName}.common.ErrorCode;
import ${packageName}.constants.CommonConstant;
import ${packageName}.common.ThrowUtils;
import ${packageName}.mapper.${upperDataKey}Mapper;
import ${packageName}.common.exception.BusinessException;
import ${packageName}.model.dto.${dataKey}.${upperDataKey}QueryRequest;
import ${packageName}.model.entity.${upperDataKey};
import ${packageName}.model.entity.User;
import ${packageName}.model.vo.${upperDataKey}VO;
import ${packageName}.model.vo.UserVO;
import ${packageName}.service.${upperDataKey}Service;
import ${packageName}.service.UserService;
import ${packageName}.utils.sql.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * ${dataName}服务实现
 *
 * @author stephen qiu
 */
@Service
@Slf4j
public class ${upperDataKey}ServiceImpl extends ServiceImpl<${upperDataKey}Mapper, ${upperDataKey}> implements ${upperDataKey}Service {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param ${dataKey} ${dataKey}
     * @param add      对创建的数据进行校验
     */
    @Override
    public void valid${upperDataKey}(${upperDataKey} ${dataKey}, boolean add) {
        ThrowUtils.throwIf(${dataKey} == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = ${dataKey}.getTitle();
        // 创建数据时，参数不能为空
        if (add) {
            // todo 补充校验规则
            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);
        }
        // 修改数据时，有参数则校验
        // todo 补充校验规则
        if (StringUtils.isNotBlank(title)) {
            ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
        }
    }

    /**
     * 获取查询条件
     *
     * @param ${dataKey}QueryRequest ${dataKey}QueryRequest
     * @return {@link QueryWrapper<${upperDataKey}>}
     */
    @Override
    public QueryWrapper<${upperDataKey}> getQueryWrapper(${upperDataKey}QueryRequest ${dataKey}QueryRequest) {
        QueryWrapper<${upperDataKey}> queryWrapper = new QueryWrapper<>();
        if (${dataKey}QueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = ${dataKey}QueryRequest.getId();
        Long notId = ${dataKey}QueryRequest.getNotId();
        String title = ${dataKey}QueryRequest.getTitle();
        String content = ${dataKey}QueryRequest.getContent();
        String searchText = ${dataKey}QueryRequest.getSearchText();
        String sortField = ${dataKey}QueryRequest.getSortField();
        String sortOrder = ${dataKey}QueryRequest.getSortOrder();
        List<String> tagList = ${dataKey}QueryRequest.getTags();
        Long userId = ${dataKey}QueryRequest.getUserId();
        // todo 补充需要的查询条件
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取${dataName}封装
     *
     * @param ${dataKey} ${dataKey}
     * @param request request
     * @return {@link ${upperDataKey}VO}
     */
    @Override
    public ${upperDataKey}VO get${upperDataKey}VO(${upperDataKey} ${dataKey}, HttpServletRequest request) {
        // 对象转封装类
        ${upperDataKey}VO ${dataKey}VO = ${upperDataKey}VO.objToVo(${dataKey});

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = ${dataKey}.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user, request);
        ${dataKey}VO.setUserVO(userVO);

        // endregion
        return ${dataKey}VO;
    }

    /**
     * 分页获取${dataName}封装
     *
     * @param ${dataKey}Page ${dataKey}Page
     * @param request request
     * @return {@link Page<${upperDataKey}VO>}
     */
    @Override
    public Page<${upperDataKey}VO> get${upperDataKey}VOPage(Page<${upperDataKey}> ${dataKey}Page, HttpServletRequest request) {
        List<${upperDataKey}> ${dataKey}List = ${dataKey}Page.getRecords();
        Page<${upperDataKey}VO> ${dataKey}VOPage = new Page<>(${dataKey}Page.getCurrent(), ${dataKey}Page.getSize(), ${dataKey}Page.getTotal());
        if (CollUtil.isEmpty(${dataKey}List)) {
            return ${dataKey}VOPage;
        }
        // 对象列表 => 封装对象列表
        List<${upperDataKey}VO> ${dataKey}VOList = ${dataKey}List.stream()
                            .map(${upperDataKey}VO::objToVo)
                            .collect(Collectors.toList());
        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = ${dataKey}List.stream().map(${upperDataKey}::getUserId).collect(Collectors.toSet());
        // 填充信息
        if (CollUtil.isNotEmpty(userIdSet)) {
        CompletableFuture<Map<Long, List<User>>> mapCompletableFuture = CompletableFuture.supplyAsync(() -> userService.listByIds(userIdSet).stream()
            .collect(Collectors.groupingBy(User::getId)));
            try { 
                Map<Long, List<User>> userIdUserListMap = mapCompletableFuture.get();
                // 填充信息
                ${dataKey}VOList.forEach(${dataKey}VO -> {
                    Long userId = ${dataKey}VO.getUserId();
                    User user = null;
                    if (userIdUserListMap.containsKey(userId)) {
                        user = userIdUserListMap.get(userId).get(0);
                    }
                    ${dataKey}VO.setUserVO(userService.getUserVO(user, request));
                });
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取信息失败" + e.getMessage());
            }
        }
        // endregion
        ${dataKey}VOPage.setRecords(${dataKey}VOList);
        return ${dataKey}VOPage;
    }

}
