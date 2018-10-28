package com.west2.fzuTimeMachine.service.impl;

import com.west2.fzuTimeMachine.dao.UserDao;
import com.west2.fzuTimeMachine.exception.error.ApiException;
import com.west2.fzuTimeMachine.exception.error.UserErrorEnum;
import com.west2.fzuTimeMachine.model.dto.UserAdminLoginDTO;
import com.west2.fzuTimeMachine.model.dto.UserOAuthDTO;
import com.west2.fzuTimeMachine.model.po.Jscode2session;
import com.west2.fzuTimeMachine.model.po.WechatUser;
import com.west2.fzuTimeMachine.model.vo.UserVO;
import com.west2.fzuTimeMachine.service.UserService;
import com.west2.fzuTimeMachine.util.AESUtil;
import com.west2.fzuTimeMachine.util.WechatUtil;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @description: 用户服务实现类
 * @author: hlx 2018-10-02
 **/
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private ModelMapper modelMapper;

    private UserDao userDao;

    // 数据库session-AES-密钥
    private static final String SESSION_PWD = "8X1V2EoXH79CZ3zS";

    // Spring-Session-Redis操作DAO,管理控制session
    private RedisOperationsSessionRepository sessionRepository;

    @Autowired
    public UserServiceImpl(UserDao userDao, RedisOperationsSessionRepository sessionRepository, ModelMapper modelMapper) {
        this.userDao = userDao;
        this.sessionRepository = sessionRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public void oauth(UserOAuthDTO userOAuthDTO, HttpServletRequest request) {
        log.info("userOAuthDTO->>" + userOAuthDTO);
        Jscode2session jscode2session = WechatUtil.getJscode2session(userOAuthDTO.getCode());
        if (null == jscode2session) {
            throw new ApiException(UserErrorEnum.CODE_INVALID);
        }
        WechatUser test = userDao.getByOpenId(jscode2session.getOpenid());
        if (test != null) {
            initSession(request, test.getUserId(), jscode2session.getSessionKey());
        } else {
            WechatUser wechatUser = WechatUtil.decryptUser(jscode2session.getSessionKey(), userOAuthDTO.getEncryptedData(), userOAuthDTO.getIvStr());
            wechatUser.setCreateTime(System.currentTimeMillis() / 1000);
            userDao.save(wechatUser);
            initSession(request, wechatUser.getUserId(), jscode2session.getSessionKey());
        }
    }

    @Override
    public void login(String code, HttpServletRequest request) {
        log.info("code->>" + code);
        Jscode2session jscode2session = WechatUtil.getJscode2session(code);
        if (null == jscode2session) {
            throw new ApiException(UserErrorEnum.CODE_INVALID);
        }
        log.info("openId->>" + jscode2session.getOpenid());
        WechatUser wechatUser = userDao.getByOpenId(jscode2session.getOpenid());
        if (null == wechatUser) {
            throw new ApiException(UserErrorEnum.OAUTH_NOT_FOUND);
        }
        log.info("session->>" + jscode2session);

        initSession(request, wechatUser.getUserId(), jscode2session.getSessionKey());
    }

    private void initSession(HttpServletRequest request, Integer userId, String sessionKey) {
        // 清除已有的sessionID,保证同一时间一处登录
        String sessionId = userDao.getSessionIdByUserId(userId);
        if (sessionId != null) {
            // 解密AES-Session
            sessionId = AESUtil.decrypt(sessionId, SESSION_PWD);
            sessionRepository.deleteById(sessionId);
        }

        HttpSession httpSession = request.getSession(true);
        httpSession.setMaxInactiveInterval(3600 * 24);
        httpSession.setAttribute("userId", userId);
        httpSession.setAttribute("sessionKey", sessionKey);

        String encryptSessionId = AESUtil.encrypt(httpSession.getId(), SESSION_PWD);
        userDao.updateSessionIdByUserId(userId, encryptSessionId);

        log.info("userId->>" + httpSession.getAttribute("userId"));
    }

    @Override
    public void adminLogin(UserAdminLoginDTO userAdminLoginDTO, HttpServletRequest request) {
        if (userAdminLoginDTO.getName().equals("w2") && userAdminLoginDTO.getPass().equals("10086")) {
            request.getSession(true);
        } else {
            throw new ApiException(UserErrorEnum.PASS_INVALID);
        }
    }

    @Override
    public UserVO getMe(Integer userId) {
        WechatUser wechatUser = userDao.getInfo(userId);
        return modelMapper.map(wechatUser, UserVO.class);
    }


}
