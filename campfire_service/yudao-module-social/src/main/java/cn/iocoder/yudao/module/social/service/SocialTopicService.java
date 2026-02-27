package cn.iocoder.yudao.module.social.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.social.controller.admin.vo.TopicCreateReqVO;
import cn.iocoder.yudao.module.social.controller.admin.vo.TopicPageReqVO;
import cn.iocoder.yudao.module.social.controller.admin.vo.TopicUpdateReqVO;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialTopicDO;

import java.util.List;

/**
 * 社交话题 Service 接口
 */
public interface SocialTopicService {

    // ========== C端方法 ==========

    /**
     * 获取热门话题列表
     *
     * @param limit 数量限制
     * @return 话题列表
     */
    List<SocialTopicDO> getHotTopicList(Integer limit);

    /**
     * 解析内容中的 #标签 并创建/更新话题
     *
     * @param content 帖子内容
     * @return 解析出的标签列表
     */
    List<String> parseAndCreateTopics(String content);

    /**
     * 异步增加话题热度
     *
     * @param content 帖子内容
     * @param delta   热度增量
     */
    void incrementHeatScoreAsync(String content, Integer delta);

    /**
     * 根据话题名称查询
     *
     * @param name 话题名称
     * @return 话题
     */
    SocialTopicDO getTopicByName(String name);

    // ========== 管理端方法 ==========

    /**
     * 获取话题分页
     *
     * @param reqVO 分页查询请求
     * @return 话题分页结果
     */
    PageResult<SocialTopicDO> getTopicPage(TopicPageReqVO reqVO);

    /**
     * 获取话题详情
     *
     * @param id 话题ID
     * @return 话题
     */
    SocialTopicDO getTopic(Long id);

    /**
     * 创建话题
     *
     * @param reqVO 创建请求
     * @return 话题ID
     */
    Long createTopic(TopicCreateReqVO reqVO);

    /**
     * 更新话题
     *
     * @param reqVO 更新请求
     */
    void updateTopic(TopicUpdateReqVO reqVO);

    /**
     * 删除话题
     *
     * @param id 话题ID
     */
    void deleteTopic(Long id);

}
