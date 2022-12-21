package com.flink.streaming.web.service;

import com.flink.streaming.web.enums.UserStatusEnum;
import com.flink.streaming.web.model.dto.PageModel;
import com.flink.streaming.web.model.dto.UserDTO;
import com.flink.streaming.web.model.dto.UserSession;
import com.flink.streaming.web.model.page.PageParam;

import java.util.List;

/**
 * @author zhuhuipei
 * @Description:
 * @date 2020-07-13
 * @time 21:52
 */
public interface UserService {

    /**
     * 登陆校验,并且返回sessionId
     */
    String login(String userName, String password);

    /**
     * 登陆帐号Session校验
     */
    boolean checkLogin(UserSession userSession);

    /**
     * 新增用户
     */
    void addUser(String userName, String fullname, String password, String operator);

    /**
     * 修改密码
     */
    void updatePassword(String userName, String oldPassword, String newPassword, String operator);

    /**
     * 修改密码
     */
    void updatePassword(Integer userId, String password, String operator);

    /**
     * 修改用户名称
     */
    void updateFullName(Integer userid, String fullname, String operator);

    /**
     * 开启或者关闭
     */
    void stopOrOpen(String userName, UserStatusEnum userStatusEnum, String operator);

    /**
     * 获取全部账号
     */
    List<UserDTO> queryAll();

    /**
     * 获取全部账号(分布)
     */
    PageModel<UserDTO> queryAllByPage(PageParam pageparam);

    /**
     * 根据用户名称查询用户
     */
    UserDTO qyeryByUserName(String userName);

    /**
     * 根据用户编号查询用户
     */
    UserDTO qyeryByUserId(Integer userid);
}
