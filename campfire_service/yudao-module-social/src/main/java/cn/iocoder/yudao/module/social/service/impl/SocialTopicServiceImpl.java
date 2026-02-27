package cn.iocoder.yudao.module.social.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.social.controller.admin.vo.TopicCreateReqVO;
import cn.iocoder.yudao.module.social.controller.admin.vo.TopicPageReqVO;
import cn.iocoder.yudao.module.social.controller.admin.vo.TopicUpdateReqVO;
import cn.iocoder.yudao.module.social.convert.SocialTopicConvert;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialTopicDO;
import cn.iocoder.yudao.module.social.dal.mysql.SocialTopicMapper;
import cn.iocoder.yudao.module.social.service.SocialTopicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.social.enums.ErrorCodeConstants.*;

/**
 * 社交话题 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SocialTopicServiceImpl implements SocialTopicService {

    /**
     * 标签解析正则：匹配 #xxx（不含空格和#）
     */
    private static final Pattern TOPIC_PATTERN = Pattern.compile("#([^\\s#]+)");

    @Resource
    private SocialTopicMapper socialTopicMapper;

    // ========== C端方法 ==========

    @Override
    public List<SocialTopicDO> getHotTopicList(Integer limit) {
        return socialTopicMapper.selectHotList(limit);
    }

    @Override
    public List<String> parseAndCreateTopics(String content) {
        List<String> topics = parseTopics(content);
        for (String topicName : topics) {
            // 查询话题是否存在
            SocialTopicDO exist = socialTopicMapper.selectByName(topicName);
            if (exist == null) {
                // 不存在则创建
                SocialTopicDO topic = SocialTopicDO.builder()
                        .name(topicName)
                        .heatScore(0)
                        .status(0)
                        .build();
                socialTopicMapper.insert(topic);
                log.info("[parseAndCreateTopics] 创建新话题: {}", topicName);
            }
        }
        return topics;
    }

    @Override
    @Async
    public void incrementHeatScoreAsync(String content, Integer delta) {
        try {
            List<String> topics = parseTopics(content);
            for (String topicName : topics) {
                SocialTopicDO topic = socialTopicMapper.selectByName(topicName);
                if (topic != null) {
                    socialTopicMapper.incrementHeatScore(topic.getId(), delta);
                    log.debug("[incrementHeatScoreAsync] 话题 {} 热度 +{}", topicName, delta);
                }
            }
        } catch (Exception e) {
            log.error("[incrementHeatScoreAsync] 更新话题热度失败", e);
        }
    }

    @Override
    public SocialTopicDO getTopicByName(String name) {
        return socialTopicMapper.selectByName(name);
    }

    // ========== 管理端方法 ==========

    @Override
    public PageResult<SocialTopicDO> getTopicPage(TopicPageReqVO reqVO) {
        return socialTopicMapper.selectPage(reqVO);
    }

    @Override
    public SocialTopicDO getTopic(Long id) {
        return socialTopicMapper.selectById(id);
    }

    @Override
    public Long createTopic(TopicCreateReqVO reqVO) {
        // 校验名称唯一
        validateTopicNameUnique(null, reqVO.getName());
        // 创建话题
        SocialTopicDO topic = SocialTopicConvert.INSTANCE.convert(reqVO);
        topic.setHeatScore(0);
        topic.setStatus(0);
        socialTopicMapper.insert(topic);
        log.info("[createTopic] 管理员创建话题: {}", reqVO.getName());
        return topic.getId();
    }

    @Override
    public void updateTopic(TopicUpdateReqVO reqVO) {
        // 校验存在
        validateTopicExists(reqVO.getId());
        // 校验名称唯一
        validateTopicNameUnique(reqVO.getId(), reqVO.getName());
        // 更新话题
        SocialTopicDO updateObj = SocialTopicConvert.INSTANCE.convert(reqVO);
        socialTopicMapper.updateById(updateObj);
        log.info("[updateTopic] 管理员更新话题: id={}, name={}", reqVO.getId(), reqVO.getName());
    }

    @Override
    public void deleteTopic(Long id) {
        // 校验存在
        validateTopicExists(id);
        // 删除话题
        socialTopicMapper.deleteById(id);
        log.info("[deleteTopic] 管理员删除话题: id={}", id);
    }

    // ========== 私有方法 ==========

    /**
     * 校验话题是否存在
     */
    private void validateTopicExists(Long id) {
        if (socialTopicMapper.selectById(id) == null) {
            throw exception(TOPIC_NOT_EXISTS);
        }
    }

    /**
     * 校验话题名称唯一
     */
    private void validateTopicNameUnique(Long id, String name) {
        SocialTopicDO topic = socialTopicMapper.selectByName(name);
        if (topic == null) {
            return;
        }
        // 如果是更新操作，且找到的话题是当前话题，则不报错
        if (id != null && id.equals(topic.getId())) {
            return;
        }
        throw exception(TOPIC_NAME_EXISTS);
    }

    /**
     * 解析内容中的 #标签
     *
     * @param content 内容
     * @return 标签列表（含#前缀）
     */
    private List<String> parseTopics(String content) {
        List<String> topics = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return topics;
        }
        Matcher matcher = TOPIC_PATTERN.matcher(content);
        while (matcher.find()) {
            String topic = "#" + matcher.group(1);
            if (!topics.contains(topic)) {
                topics.add(topic);
            }
        }
        return topics;
    }

}
