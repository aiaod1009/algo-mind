package com.example.demo.config;

import com.example.demo.repository.KnowledgeArticleRepository;
import com.example.demo.repository.KnowledgeBaseConfigRepository;
import com.example.demo.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KnowledgeBaseBootstrap implements CommandLineRunner {

    private final KnowledgeArticleRepository knowledgeArticleRepository;
    private final KnowledgeBaseConfigRepository knowledgeBaseConfigRepository;
    private final KnowledgeBaseService knowledgeBaseService;

    @Value("${algo.knowledge.seed.enabled:false}")
    private boolean algorithmKnowledgeSeedEnabled;

    @Override
    public void run(String... args) {
        if (algorithmKnowledgeSeedEnabled) {
            return;
        }
        bootstrapConfig();
        bootstrapArticles();
    }

    private void bootstrapConfig() {
        if (!knowledgeBaseConfigRepository.findAll().isEmpty()) {
            return;
        }

        knowledgeBaseService.updateConfig(new KnowledgeBaseService.AdminConfigInput(
                "AlgoMind 知识库",
                "支持后台维护、文章发布和运营配置的算法知识中心。",
                "没有找到匹配的知识主题",
                "可以试试搜索“动态规划”“最短路”“线段树”等关键词，或者直接从左侧目录开始阅读。",
                "dp-basics",
                List.of("动态规划", "图论最短路", "线段树", "二分答案", "指针与内存")));
    }

    private void bootstrapArticles() {
        if (knowledgeArticleRepository.count() > 0) {
            return;
        }

        List<KnowledgeBaseService.AdminArticleInput> seeds = List.of(
                new KnowledgeBaseService.AdminArticleInput(
                        "dp-basics",
                        "动态规划入门",
                        "Dynamic Programming",
                        "algorithm-basics",
                        "算法基础",
                        "适合建立状态设计、转移推导和复杂度表达的基础能力。",
                        "刷题核心",
                        "动态规划最重要的不是记住模板，而是先把状态定义清楚，再明确当前答案依赖哪些更小子问题。",
                        "当题目出现最值、方案数、可达性等信号时，优先尝试写出状态定义，并用小样例验证转移方向。",
                        "常见为 O(n) 到 O(n^2)",
                        "7 min read",
                        List.of("状态设计", "转移方程", "初始化", "滚动数组"),
                        List.of(
                                "能用自然语言说清楚 dp[i] 或 dp[i][j] 的语义。",
                                "知道转移、初始化和遍历顺序为什么要成套出现。",
                                "能判断什么时候适合做空间优化。"),
                        List.of(
                                new KnowledgeBaseService.KnowledgeStep("01", "先定义状态", "优先回答“当前这个位置的答案代表什么”，避免一上来就写公式。", "思维入口"),
                                new KnowledgeBaseService.KnowledgeStep("02", "找依赖来源", "把当前答案依赖的更小问题写出来，再翻译成代码里的下标关系。", "转移"),
                                new KnowledgeBaseService.KnowledgeStep("03", "补边界与顺序", "很多 DP 出错不是转移错，而是初始化缺了或遍历方向反了。", "实现")),
                        List.of(
                                new KnowledgeBaseService.KnowledgeInsight("面试表达", "先讲状态定义，再讲转移来源，最后补复杂度，表达会明显更稳。", "emerald"),
                                new KnowledgeBaseService.KnowledgeInsight("常见误区", "状态里塞了太多信息，会让维度暴涨。先从最少必要信息开始。", "amber"),
                                new KnowledgeBaseService.KnowledgeInsight("优化思路", "只有依赖方向非常清楚时，才建议做滚动数组优化。", "cyan")),
                        List.of(
                                new KnowledgeBaseService.KnowledgeCodeBlock(
                                        "Java",
                                        "爬楼梯的一维 DP 模板",
                                        """
                                        public int climbStairs(int n) {
                                            if (n <= 2) {
                                                return n;
                                            }

                                            int[] dp = new int[n + 1];
                                            dp[1] = 1;
                                            dp[2] = 2;
                                            for (int i = 3; i <= n; i++) {
                                                dp[i] = dp[i - 1] + dp[i - 2];
                                            }
                                            return dp[n];
                                        }
                                        """,
                                        List.of("dp[i] 表示到达第 i 阶的方法数。", "遍历顺序必须从小到大。"))),
                        List.of(
                                "写转移前，先把状态定义写成一句自然语言。",
                                "确认初始化覆盖了最小规模的子问题。",
                                "检查遍历顺序是否满足依赖方向。",
                                "能够口头说出时间复杂度和空间复杂度。"),
                        List.of("greedy-strategy", "binary-search-answer"),
                        true,
                        "面试速刷",
                        "先定义状态，再谈转移",
                        "适合临近面试时快速过一遍 DP 的思考路径和典型表达方式。",
                        "emerald",
                        true,
                        10),
                new KnowledgeBaseService.AdminArticleInput(
                        "greedy-strategy",
                        "贪心策略",
                        "Greedy Strategy",
                        "algorithm-basics",
                        "算法基础",
                        "帮助你判断什么时候可以放心地做局部最优选择。",
                        "面试高频",
                        "贪心不是拍脑袋选局部最优，而是要证明这种选择不会破坏全局最优。",
                        "看到区间调度、最少覆盖、跳跃类题目时，先观察是否能通过排序把决策空间变成单调结构。",
                        "通常为 O(n log n)",
                        "6 min read",
                        List.of("排序", "局部最优", "正确性证明"),
                        List.of(
                                "识别常见的贪心信号：可排序、可局部决策、可证明不后悔。",
                                "理解为什么排序常常是贪心成立的前提。",
                                "会用一句话解释策略为什么成立。"),
                        List.of(
                                new KnowledgeBaseService.KnowledgeStep("01", "观察能否排序", "很多贪心题都要先按结束时间、收益或代价排序。", "预处理"),
                                new KnowledgeBaseService.KnowledgeStep("02", "定义局部选择", "每一步到底选最早结束还是最小代价，要和目标严格对齐。", "决策"),
                                new KnowledgeBaseService.KnowledgeStep("03", "补正确性解释", "说明为什么当前选择不会让后续更差，这是答案能站住的关键。", "证明")),
                        List.of(
                                new KnowledgeBaseService.KnowledgeInsight("高频题型", "区间调度、最少箭射气球、跳跃游戏、股票题都常见贪心。", "emerald"),
                                new KnowledgeBaseService.KnowledgeInsight("风险点", "如果局部最优会锁死后续选择，通常就该回到 DP 或二分。", "amber"),
                                new KnowledgeBaseService.KnowledgeInsight("答题建议", "先给出策略，再补“为什么它成立”，会比只报结论更有说服力。", "cyan")),
                        List.of(),
                        List.of(
                                "先确认题目是否存在单调的局部选择。",
                                "思考排序字段和目标函数是否一致。",
                                "别只背模板，要准备一句正确性说明。"),
                        List.of("dp-basics", "binary-search-answer"),
                        false,
                        "",
                        "",
                        "",
                        "emerald",
                        true,
                        20),
                new KnowledgeBaseService.AdminArticleInput(
                        "binary-search-answer",
                        "二分答案",
                        "Binary Search on Answer",
                        "algorithm-basics",
                        "算法基础",
                        "适合处理“求最小可行值 / 最大可行值”这类题目。",
                        "易错专题",
                        "二分的不一定是数组下标，也可以是满足条件的数值区间或某个候选答案。",
                        "当你能写出一个随答案变化而单调的判定函数时，就可以考虑把枚举答案改成二分答案。",
                        "O(log range * check)",
                        "6 min read",
                        List.of("单调性", "判定函数", "边界"),
                        List.of(
                                "能判断题目是否具备可二分的单调性。",
                                "会设计 check(mid) 并解释其返回值含义。"),
                        List.of(
                                new KnowledgeBaseService.KnowledgeStep("01", "找答案空间", "先明确二分的是数值范围还是候选结果集合。", "范围"),
                                new KnowledgeBaseService.KnowledgeStep("02", "写判定函数", "check(mid) 只负责判断当前 mid 是否可行。", "判定"),
                                new KnowledgeBaseService.KnowledgeStep("03", "收缩边界", "根据要找的是最小可行值还是最大可行值决定更新规则。", "边界")),
                        List.of(
                                new KnowledgeBaseService.KnowledgeInsight("常见搭配", "二分答案经常和贪心、前缀和、图搜索一起出现。", "emerald"),
                                new KnowledgeBaseService.KnowledgeInsight("高危细节", "边界循环条件、mid 取法和最终返回值最容易出错。", "amber")),
                        List.of(),
                        List.of("先问自己：答案是否具备单调性。", "判定函数只做“是否可行”的判断。"),
                        List.of("greedy-strategy", "graph-shortest-path"),
                        false,
                        "",
                        "",
                        "",
                        "cyan",
                        true,
                        30),
                new KnowledgeBaseService.AdminArticleInput(
                        "segment-tree",
                        "线段树",
                        "Segment Tree",
                        "data-structures",
                        "数据结构与图论",
                        "围绕区间查询和区间更新整理的一篇高频结构文档。",
                        "高频结构",
                        "线段树擅长处理数组频繁修改且需要大量区间查询的场景，是区间问题里的高频主力结构。",
                        "当你既要查区间和、区间最值，又不能接受每次线性扫描时，线段树通常是最稳的选择。",
                        "单次操作 O(log n)",
                        "8 min read",
                        List.of("区间查询", "区间更新", "懒标记"),
                        List.of("理解每个节点维护的区间语义。", "能写出 build、query、update 的核心骨架。"),
                        List.of(
                                new KnowledgeBaseService.KnowledgeStep("01", "明确节点语义", "每个节点维护区间和、最大值还是最小值，决定了聚合方式。", "建模"),
                                new KnowledgeBaseService.KnowledgeStep("02", "递归划分区间", "不断把区间二分到叶子节点，再自底向上合并信息。", "建树"),
                                new KnowledgeBaseService.KnowledgeStep("03", "只访问相关路径", "查询和更新只递归访问与目标区间相交的节点。", "复杂度")),
                        List.of(
                                new KnowledgeBaseService.KnowledgeInsight("适用场景", "数组持续变化，同时要频繁查询区间聚合值时，线段树明显优于前缀和。", "emerald"),
                                new KnowledgeBaseService.KnowledgeInsight("常见失误", "mid 划分、递归边界和下标范围最容易写错。", "amber")),
                        List.of(),
                        List.of("统一使用一套区间表示方式，如 [l, r]。", "区间修改时再考虑懒标记，不要一上来就写复杂。"),
                        List.of("graph-shortest-path", "dp-basics"),
                        true,
                        "高频结构",
                        "区间问题的主力武器",
                        "如果你在刷题里频繁碰到“区间最值 / 区间求和 + 修改”，这篇值得反复看。",
                        "cyan",
                        true,
                        10),
                new KnowledgeBaseService.AdminArticleInput(
                        "graph-shortest-path",
                        "最短路算法",
                        "Shortest Path",
                        "data-structures",
                        "数据结构与图论",
                        "帮助你快速判断 BFS、Dijkstra 和 Bellman-Ford 的适用边界。",
                        "图论专题",
                        "最短路题最重要的不是背算法名，而是先判断图是否带权、边权是否非负，以及图是稀疏还是稠密。",
                        "无权图优先 BFS，非负权图常用 Dijkstra，存在负权边时再考虑 Bellman-Ford 或相关变体。",
                        "Dijkstra 常见为 O((n + m) log n)",
                        "8 min read",
                        List.of("建图", "堆优化", "非负权边"),
                        List.of("知道 BFS、Dijkstra、Bellman-Ford 的典型边界。", "会写优先队列版本的最短路模板。"),
                        List.of(
                                new KnowledgeBaseService.KnowledgeStep("01", "先判断图类型", "看清楚是有向图还是无向图，边权是否存在、是否可能为负。", "建模"),
                                new KnowledgeBaseService.KnowledgeStep("02", "选择算法", "无权图用 BFS，非负权图用 Dijkstra，有负权时再换方案。", "选型"),
                                new KnowledgeBaseService.KnowledgeStep("03", "维护距离数组", "dist 记录当前最短距离，优先队列负责每次扩展最优状态。", "实现")),
                        List.of(
                                new KnowledgeBaseService.KnowledgeInsight("面试常问", "为什么 Dijkstra 遇到负权边会失效，这是很高频的追问。", "emerald"),
                                new KnowledgeBaseService.KnowledgeInsight("易错点", "重复入堆是正常现象，弹出时要判断状态是否过期。", "amber")),
                        List.of(),
                        List.of("先确认边权是否非负。", "先想清楚建图结构，再写算法。"),
                        List.of("segment-tree", "binary-search-answer"),
                        false,
                        "",
                        "",
                        "",
                        "cyan",
                        true,
                        20),
                new KnowledgeBaseService.AdminArticleInput(
                        "pointer-memory",
                        "指针与内存",
                        "Pointer & Memory",
                        "engineering-foundation",
                        "工程实现",
                        "把刷题思维延伸到底层实现、生命周期和边界意识。",
                        "底层补位",
                        "很多底层 bug 不在语法层，而在生命周期、所有权和边界意识上。理解指针与内存模型，会让你的实现更稳。",
                        "即使主语言不是 C/C++，理解堆、栈、空指针和悬空引用这些概念，也会帮助你写出更安全的代码。",
                        "重点在风险控制而不是复杂度",
                        "7 min read",
                        List.of("栈与堆", "所有权", "空指针"),
                        List.of("理解栈内存和堆内存的分工。", "把边界检查当成实现的一部分。"),
                        List.of(
                                new KnowledgeBaseService.KnowledgeStep("01", "区分栈和堆", "局部变量通常在栈上，动态申请对象在堆上，两者生命周期完全不同。", "内存模型"),
                                new KnowledgeBaseService.KnowledgeStep("02", "明确所有权", "谁申请、谁释放，或者是否显式转移所有权，要在设计阶段说清楚。", "所有权"),
                                new KnowledgeBaseService.KnowledgeStep("03", "访问前做边界判断", "空指针、越界和释放后继续访问，都是底层题里最致命的错误来源。", "安全性")),
                        List.of(
                                new KnowledgeBaseService.KnowledgeInsight("迁移价值", "理解内存模型后，看 Java、Go、Rust 的抽象边界会更轻松。", "emerald"),
                                new KnowledgeBaseService.KnowledgeInsight("工程提醒", "释放资源后立刻置空，是规避 use-after-free 的简单习惯。", "amber")),
                        List.of(),
                        List.of("明确对象在栈上还是堆上。", "每次访问前做空指针和边界检查。"),
                        List.of("graph-shortest-path", "dp-basics"),
                        true,
                        "工程补位",
                        "补齐底层与实现细节",
                        "当你刷题没问题，但表达和实现细节不够稳时，这篇最容易补差距。",
                        "amber",
                        true,
                        10)
        );

        for (KnowledgeBaseService.AdminArticleInput seed : seeds) {
            knowledgeBaseService.createArticle(seed, 1L);
        }
    }
}
