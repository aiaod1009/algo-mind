package com.example.demo.config;

import com.example.demo.entity.Level;
import com.example.demo.repository.LevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final LevelRepository levelRepository;

    @Override
    public void run(String... args) {
        if (levelRepository.count() > 0) {
            log.info("关卡数据已存在，跳过初始化");
            return;
        }

        log.info("开始初始化关卡数据...");

        List<Level> levels = List.of(
                createLevel("algo", 1, "两数之和", true, 10, "single",
                        "给定一个整数数组 nums 和一个目标值 target，请你在该数组中找出和为目标值的那两个整数，并返回它们的数组下标。时间复杂度最优解是？",
                        "O(n)", "经典哈希表应用"),
                createLevel("algo", 2, "二分查找", false, 10, "single",
                        "在一个有序数组中查找目标值，二分查找的时间复杂度是？",
                        "O(log n)", "基础算法"),
                createLevel("algo", 3, "快速排序", false, 15, "single",
                        "快速排序的平均时间复杂度是？",
                        "O(n log n)", "分治算法经典"),
                createLevel("algo", 4, "归并排序", false, 15, "single",
                        "归并排序的空间复杂度是？",
                        "O(n)", "稳定排序"),
                createLevel("algo", 5, "动态规划入门", false, 20, "single",
                        "斐波那契数列使用动态规划的时间复杂度是？",
                        "O(n)", "DP基础"),
                createLevel("algo", 6, "爬楼梯问题", false, 20, "single",
                        "爬n阶楼梯，每次可爬1或2阶，共有多少种方法？本质是什么问题？",
                        "斐波那契", "DP经典"),
                createLevel("algo", 7, "最大子数组和", false, 25, "single",
                        "Kadane算法求解最大子数组和的时间复杂度是？",
                        "O(n)", "贪心思想"),
                createLevel("algo", 8, "背包问题", false, 30, "single",
                        "0-1背包问题的时间复杂度是？",
                        "O(nW)", "DP经典"),

                createLevel("ds", 1, "数组基础", true, 10, "single",
                        "数组随机访问元素的时间复杂度是？",
                        "O(1)", "线性表基础"),
                createLevel("ds", 2, "链表插入", false, 10, "single",
                        "在链表头部插入节点的时间复杂度是？",
                        "O(1)", "链表操作"),
                createLevel("ds", 3, "栈的特性", false, 15, "single",
                        "栈的特点是什么？",
                        "后进先出", "线性结构"),
                createLevel("ds", 4, "队列的特性", false, 15, "single",
                        "队列的特点是什么？",
                        "先进先出", "线性结构"),
                createLevel("ds", 5, "二叉树遍历", false, 20, "single",
                        "二叉树前序遍历的顺序是？",
                        "根左右", "树形结构"),
                createLevel("ds", 6, "堆的性质", false, 25, "single",
                        "最大堆的根节点是？",
                        "最大值", "优先队列"),
                createLevel("ds", 7, "哈希表查询", false, 20, "single",
                        "哈希表平均查询时间复杂度是？",
                        "O(1)", "查找结构"),
                createLevel("ds", 8, "平衡二叉树", false, 30, "single",
                        "AVL树保持平衡的条件是？",
                        "高度差不超过1", "自平衡树"),

                createLevel("contest", 1, "滑动窗口", true, 15, "single",
                        "滑动窗口算法适合解决什么类型的问题？",
                        "连续子数组", "双指针技巧"),
                createLevel("contest", 2, "前缀和", false, 15, "single",
                        "前缀和数组用于优化什么操作？",
                        "区间求和", "预处理技巧"),
                createLevel("contest", 3, "单调栈", false, 20, "single",
                        "单调栈用于解决什么问题？",
                        "下一个更大元素", "栈的应用"),
                createLevel("contest", 4, "并查集", false, 25, "single",
                        "并查集主要用于解决什么问题？",
                        "连通性问题", "图论基础"),
                createLevel("contest", 5, "线段树", false, 30, "single",
                        "线段树支持什么操作？",
                        "区间查询更新", "高级数据结构"),
                createLevel("contest", 6, "字典树", false, 25, "single",
                        "字典树主要用于什么场景？",
                        "字符串查找", "树形结构"),
                createLevel("contest", 7, "状态压缩DP", false, 35, "single",
                        "状态压缩DP通常用什么表示状态？",
                        "二进制位", "高级DP"),
                createLevel("contest", 8, "数位DP", false, 35, "single",
                        "数位DP用于解决什么问题？",
                        "数字统计", "高级DP")
        );

        levelRepository.saveAll(levels);
        log.info("关卡数据初始化完成，共 {} 条", levels.size());
    }

    private Level createLevel(String track, int order, String name,
                              boolean isUnlocked, int rewardPoints, String type,
                              String question, String answer, String description) {
        Level level = new Level();
        level.setTrack(track);
        level.setOrder(order);
        level.setName(name);
        level.setIsUnlocked(isUnlocked);
        level.setRewardPoints(rewardPoints);
        level.setType(type);
        level.setQuestion(question);
        level.setAnswer(answer);
        level.setDescription(description);
        return level;
    }
}
