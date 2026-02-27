package cn.iocoder.yudao.module.ai.service.agent.orchestrator;

/**
 * 讨论阶段接口
 *
 * @author campfire
 */
public interface DiscussionPhase {

    /**
     * 执行阶段
     *
     * @param context 房间上下文
     */
    void execute(RoomContext context);

    /**
     * 获取阶段名称
     */
    String getName();

    /**
     * 获取阶段顺序
     */
    int getOrder();

}
