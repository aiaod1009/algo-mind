package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AlgorithmKnowledgeSeedFactory {

    private static final Section BASIC_DATA_STRUCTURES = new Section(
            "01-basic-data-structures",
            "基础数据结构算法",
            "覆盖数组、链表、栈、队列、树、并查集等高频基础结构，帮助建立算法建模和常见操作模板。");
    private static final Section SORTING_ALGORITHMS = new Section(
            "02-sorting-algorithms",
            "排序算法",
            "系统整理交换类、插入类、分治类和计数类排序方法，方便在面试中快速比较稳定性与复杂度。");
    private static final Section SEARCHING_ALGORITHMS = new Section(
            "03-searching-algorithms",
            "查找算法",
            "从顺序查找、二分查找到哈希查找与边界查找，覆盖最常用的检索思路。");
    private static final Section DYNAMIC_PROGRAMMING = new Section(
            "04-dynamic-programming",
            "动态规划算法",
            "从背包模型到树形、区间、数位和状态压缩 DP，建立系统化的状态设计框架。");
    private static final Section GREEDY_ALGORITHMS = new Section(
            "05-greedy-algorithms",
            "贪心算法",
            "总结能用局部最优逼近全局最优的高频模型，并突出正确性证明方法。");
    private static final Section BACKTRACKING = new Section(
            "06-backtracking",
            "回溯算法",
            "覆盖排列、组合、棋盘搜索与约束满足问题，强调剪枝与状态恢复。");
    private static final Section DIVIDE_AND_CONQUER = new Section(
            "07-divide-and-conquer",
            "分治算法",
            "把大问题拆成同构小问题，通过递归与合并策略获得更优复杂度。");
    private static final Section GRAPH_ALGORITHMS = new Section(
            "08-graph-algorithms",
            "图算法",
            "覆盖最短路径、最小生成树、拓扑排序等高频图论算法。");
    private static final Section STRING_ALGORITHMS = new Section(
            "09-string-algorithms",
            "字符串处理算法",
            "聚焦模式匹配、回文处理、多模式搜索与哈希比较等核心字符串技巧。");

    public List<KnowledgeBaseService.AdminArticleInput> buildSeeds() {
        List<KnowledgeBaseService.AdminArticleInput> seeds = new ArrayList<>();
        seeds.addAll(basicDataStructureArticles());
        seeds.addAll(sortingArticles());
        seeds.addAll(searchingArticles());
        seeds.addAll(dynamicProgrammingArticles());
        seeds.addAll(greedyArticles());
        seeds.addAll(backtrackingArticles());
        seeds.addAll(divideAndConquerArticles());
        seeds.addAll(graphArticles());
        seeds.addAll(stringArticles());
        if (seeds.size() != 60) {
            throw new IllegalStateException("Expected 60 algorithm knowledge articles, but got " + seeds.size());
        }
        return seeds;
    }

    private List<KnowledgeBaseService.AdminArticleInput> basicDataStructureArticles() {
        return List.of(
                article(
                        BASIC_DATA_STRUCTURES,
                        "algo-array-two-pointers",
                        "数组遍历与双指针",
                        "Array Traversal and Two Pointers",
                        "基础结构",
                        "双指针把两个下标放在同一段数组上协同移动，用指针位置本身表达状态，从而把许多 O(n^2) 的枚举压缩为 O(n) 的线性扫描。它的关键不在于“两个指针”这个形式，而在于始终维持一个明确的不变量，例如窗口内元素满足条件、慢指针左侧都是合法答案或左右端点之间仍保留候选解。",
                        "时间复杂度通常是 O(n)，因为每个元素最多被左右指针访问常数次；空间复杂度通常是 O(1)，只额外维护少量下标和计数变量。若双指针嵌套在排序之后使用，总复杂度一般是 O(n log n) + O(n)。",
                        "8 min read",
                        List.of("数组", "双指针", "原地算法"),
                        List.of("适合有序数组去重、区间收缩、滑动统计等需要边遍历边维护答案的场景。", "当题目允许原地修改数组且只关心合法区间或左右边界时，双指针通常是最直接的解法。"),
                        List.of(
                                step("定义左右指针语义", "先明确 left 和 right 分别代表窗口边界、当前候选区间还是已处理区域的尾部。", "建模"),
                                step("设计移动规则", "根据条件是否满足决定移动哪一个指针，保证每次移动后不变量仍然成立。", "推进"),
                                step("同步更新答案", "在指针变化的同时更新长度、和、覆盖位置或最终写回数组的位置。", "收尾")),
                        List.of(
                                caseStudy("有序数组去重", "慢指针指向下一个可写位置，快指针负责扫描新元素，能在线性时间内完成原地去重。"),
                                caseStudy("盛最多水的容器", "左右指针从两端向中间收缩，每次移动短板一侧，通过反证可以保证不会错过最优解。")),
                        code("Python", "有序数组原地去重", """
                                def remove_duplicates(nums):
                                    # slow 指向下一个可写位置
                                    if not nums:
                                        return 0
                                    slow = 1
                                    for fast in range(1, len(nums)):
                                        if nums[fast] != nums[fast - 1]:
                                            nums[slow] = nums[fast]
                                            slow += 1
                                    return slow
                                """,
                                "慢指针左侧始终保存去重后的结果。",
                                "快指针只负责扫描，不直接决定最终数组长度。"),
                        true,
                        "刷题起点",
                        "先把枚举写成指针移动",
                        "数组题里只要能把状态浓缩到左右边界和当前答案，双指针通常就是最稳的首选套路。",
                        "emerald",
                        10),
                article(
                        BASIC_DATA_STRUCTURES,
                        "algo-prefix-sum",
                        "前缀和",
                        "Prefix Sum",
                        "高频基础",
                        "前缀和用“累计量”把任意区间查询转成两个前缀值的差，核心思想是把重复累加提前一次性完成。只要数据在查询阶段不再频繁修改，前缀和就能把区间和、区间计数、字符出现次数等问题从 O(n) 查询优化到 O(1) 查询。",
                        "构建前缀和需要一次 O(n) 扫描；构建完成后，每次区间查询仅需 O(1) 计算。额外空间为 O(n)，若题目允许覆盖原数组或只保留累计值，也可以通过原地复用降低常数。",
                        "7 min read",
                        List.of("前缀和", "区间查询", "预处理"),
                        List.of("适合静态数组、多次区间求和、多次统计某段中目标元素数量的场景。", "当查询远多于修改，并且区间运算满足可减性时，前缀和几乎是必备预处理。"),
                        List.of(
                                step("定义累计数组", "prefix[i] 表示前 i 个元素的累计结果，通常令 prefix[0] = 0 方便统一边界。", "预处理"),
                                step("顺序构建前缀值", "依次把当前元素并入累计量，得到每个位置之前的信息汇总。", "构建"),
                                step("用差值回答查询", "区间 [l, r] 的结果等于 prefix[r + 1] - prefix[l]，避免重复遍历。", "查询")),
                        List.of(
                                caseStudy("子数组和判定", "很多“子数组和等于 k”的问题都会先用前缀和描述任意区间的和，再搭配哈希表求解。"),
                                caseStudy("字符统计", "在字符串中统计任意区间的元音数量或某类字符出现次数时，前缀和能把查询压缩到 O(1)。")),
                        code("Python", "一维前缀和模板", """
                                def build_prefix(nums):
                                    prefix = [0] * (len(nums) + 1)
                                    for i, value in enumerate(nums, start=1):
                                        prefix[i] = prefix[i - 1] + value
                                    return prefix

                                def range_sum(prefix, left, right):
                                    return prefix[right + 1] - prefix[left]
                                """,
                                "prefix[0] = 0 可以统一处理从下标 0 开始的区间。",
                                "查询时注意右端点需要加一。"),
                        false,
                        "",
                        "",
                        "",
                        "cyan",
                        20),
                article(
                        BASIC_DATA_STRUCTURES,
                        "algo-difference-array",
                        "差分数组",
                        "Difference Array",
                        "区间更新",
                        "差分数组把“对整段区间同时加值”的操作转成两个端点的标记修改，再通过一次前缀累加还原最终数组。它本质上是在原数组变化量上做文章，适合离线处理多次区间增减更新。",
                        "每次区间更新只需要 O(1) 改动两个边界，所有更新完成后再用一次 O(n) 的前缀和恢复真实值，因此总复杂度为 O(n + q)。额外空间为 O(n)，其中 q 表示区间更新次数。",
                        "7 min read",
                        List.of("差分", "区间更新", "前缀和"),
                        List.of("适合离线批量处理多次区间增减、航班预订、座位统计等题目。", "当修改次数很多、查询只在最后统一发生时，差分数组能显著降低总时间。"),
                        List.of(
                                step("初始化差分数组", "令 diff[0] = nums[0]，其余位置记录相邻元素的变化量。", "建模"),
                                step("标记区间边界", "对区间 [l, r] 加 val 时执行 diff[l] += val，diff[r + 1] -= val。", "更新"),
                                step("前缀还原结果", "最后对 diff 做一次累加，得到每个位置的真实值。", "恢复")),
                        List.of(
                                caseStudy("航班预订统计", "每条预订记录只改动起止边界，最后统一恢复每个航班的累计座位数。"),
                                caseStudy("区间加一计数", "在线段较少但覆盖范围很大时，差分法比逐元素更新更高效。")),
                        code("Python", "差分数组区间加法模板", """
                                def range_increment(length, updates):
                                    diff = [0] * (length + 1)
                                    for left, right, delta in updates:
                                        diff[left] += delta
                                        if right + 1 < len(diff):
                                            diff[right + 1] -= delta

                                    result = [0] * length
                                    running = 0
                                    for i in range(length):
                                        running += diff[i]
                                        result[i] = running
                                    return result
                                """,
                                "所有区间修改都延迟到最后一次性恢复。",
                                "right + 1 越界时不要再回写。"),
                        false,
                        "",
                        "",
                        "",
                        "amber",
                        30),
                article(
                        BASIC_DATA_STRUCTURES,
                        "algo-linked-list-fast-slow",
                        "链表快慢指针",
                        "Fast and Slow Pointers on Linked List",
                        "链表专题",
                        "链表题往往不能随机访问节点，因此需要靠指针本身表达状态。快慢指针通过不同速度的遍历，在一次扫描中完成中点查找、环检测、倒数第 k 个节点定位等任务，是链表算法最常见的核心技巧。",
                        "多数快慢指针算法只需遍历链表 1 到 2 次，因此时间复杂度为 O(n)，空间复杂度为 O(1)。复杂度优势来自只维护少量指针而不借助数组或哈希表保存全部节点。", 
                        "8 min read",
                        List.of("链表", "快慢指针", "原地操作"),
                        List.of("适合链表中点、环入口、倒数第 k 个节点、链表重排等依赖相对位置关系的问题。", "当不能把链表复制到数组、又希望用常数空间解决时，快慢指针尤其重要。"),
                        List.of(
                                step("明确指针速度", "慢指针每次走一步，快指针每次走两步，用速度差表达相对位置。", "定位"),
                                step("设计相遇条件", "根据题目决定是以相遇、到尾部还是提前偏移作为停止条件。", "判定"),
                                step("做二次推导", "相遇后可以再次利用距离关系寻找中点、入口节点或分割位置。", "扩展")),
                        List.of(
                                caseStudy("环形链表入口", "Floyd 判圈先找相遇点，再从头结点和相遇点同步前进，第一次相遇就是入口。"),
                                caseStudy("回文链表", "先用快慢指针找到中点，再反转后半段，最后两边同步比较。")),
                        code("Python", "检测链表是否有环", """
                                class ListNode:
                                    def __init__(self, val=0, next=None):
                                        self.val = val
                                        self.next = next

                                def has_cycle(head):
                                    slow = head
                                    fast = head
                                    while fast and fast.next:
                                        slow = slow.next
                                        fast = fast.next.next
                                        if slow is fast:
                                            return True
                                    return False
                                """,
                                "快慢指针相遇说明存在环。",
                                "循环条件要同时判断 fast 和 fast.next。"),
                        false,
                        "",
                        "",
                        "",
                        "emerald",
                        40),
                article(
                        BASIC_DATA_STRUCTURES,
                        "algo-stack-parentheses",
                        "栈与括号匹配",
                        "Stack and Parentheses Matching",
                        "栈结构",
                        "栈遵循后进先出，特别适合处理“最近打开但尚未关闭”的状态。括号匹配、表达式求值、单调栈等问题都可以视作“遇到新元素就与栈顶最近状态交互”，因此栈经常作为线性扫描时的状态容器。",
                        "每个元素最多入栈一次、出栈一次，所以典型栈算法时间复杂度是 O(n)。空间复杂度最坏为 O(n)，对应所有元素都暂时不能被弹出的情况。", 
                        "7 min read",
                        List.of("栈", "括号匹配", "线性扫描"),
                        List.of("适合需要匹配最近未处理状态、逆序恢复或维护单调性的场景。", "当题目频繁提到“最近一个”“上一个更大/更小”“括号成对”时，栈往往就是答案。"),
                        List.of(
                                step("定义入栈对象", "可以把字符、下标、区间边界或状态元组压入栈中，取决于后续需要的信息。", "建模"),
                                step("只与栈顶交互", "每次读到新元素时，只需看它与当前栈顶是否能形成匹配或触发弹栈。", "匹配"),
                                step("处理剩余元素", "扫描结束后，还在栈中的内容往往对应未匹配状态或答案的补充部分。", "收尾")),
                        List.of(
                                caseStudy("有效括号", "遇到左括号就入栈，遇到右括号时验证是否能与栈顶对应配对。"),
                                caseStudy("每日温度", "单调栈维护还没找到更高温度的日期，下一个更大元素问题都能用同一思路解决。")),
                        code("Python", "有效括号判断", """
                                def is_valid_parentheses(s):
                                    pairs = {')': '(', ']': '[', '}': '{'}
                                    stack = []
                                    for ch in s:
                                        if ch in pairs.values():
                                            stack.append(ch)
                                        elif not stack or stack.pop() != pairs[ch]:
                                            return False
                                    return not stack
                                """,
                                "栈里保存尚未匹配的左括号。",
                                "最后栈为空才说明全部匹配完成。"),
                        false,
                        "",
                        "",
                        "",
                        "amber",
                        50),
                article(
                        BASIC_DATA_STRUCTURES,
                        "algo-queue-level-order",
                        "队列与层序遍历",
                        "Queue and Level Order Traversal",
                        "队列结构",
                        "队列遵循先进先出，天然适合按时间顺序、按层次顺序推进状态。二叉树层序遍历、无权图最短路径、任务调度等问题都可以理解为“先到达的状态先扩展”，因此队列是 BFS 的基础容器。",
                        "典型队列遍历会让每个节点或状态出队一次、入队一次，所以时间复杂度为 O(n) 或 O(n + m)。额外空间取决于队列峰值大小，树层序遍历的最坏空间复杂度为 O(width)。",
                        "7 min read",
                        List.of("队列", "层序遍历", "BFS"),
                        List.of("适合按层、按轮次、按最短步数推进的搜索问题。", "当题目强调“最少步数”“每一轮扩散”“一层一层处理”时，队列通常是首选结构。"),
                        List.of(
                                step("初始化起始状态", "把所有起点入队，并根据需要标记已访问状态。", "起点"),
                                step("按先进先出扩展", "每次弹出最早进入队列的状态，再把它的下一层邻居加入。", "扩展"),
                                step("按层统计结果", "如果题目与层数有关，可在每轮记录当前队列长度来切分层次。", "分层")),
                        List.of(
                                caseStudy("二叉树层序遍历", "每一轮记录队列长度即可得到当前层节点列表，适合层高、右视图等变体。"),
                                caseStudy("最短步数问题", "在无权图中，第一次到达目标状态的层数就是最短路径长度。")),
                        code("Python", "二叉树层序遍历", """
                                from collections import deque

                                def level_order(root):
                                    if not root:
                                        return []
                                    queue = deque([root])
                                    answer = []
                                    while queue:
                                        level = []
                                        for _ in range(len(queue)):
                                            node = queue.popleft()
                                            level.append(node.val)
                                            if node.left:
                                                queue.append(node.left)
                                            if node.right:
                                                queue.append(node.right)
                                        answer.append(level)
                                    return answer
                                """,
                                "每轮先固定队列长度，就能得到完整的一层。",
                                "队列里只放待扩展节点，不要重复入队已访问状态。"),
                        false,
                        "",
                        "",
                        "",
                        "cyan",
                        60),
                article(
                        BASIC_DATA_STRUCTURES,
                        "algo-monotonic-queue",
                        "双端队列与单调队列",
                        "Deque and Monotonic Queue",
                        "窗口结构",
                        "双端队列既能从头部弹出，也能从尾部弹出，非常适合滑动窗口里同时维护“过期元素”和“最优候选”。单调队列进一步要求队列内保持单调性，从而在 O(1) 均摊时间内获取窗口最大值或最小值。",
                        "每个元素最多进队一次、出队一次，所以滑动窗口单调队列的总时间复杂度是 O(n)。额外空间为 O(k) 到 O(n)，其中 k 是窗口大小。", 
                        "8 min read",
                        List.of("双端队列", "单调队列", "滑动窗口"),
                        List.of("适合固定窗口最值、窗口内最优决策、维护候选下标的场景。", "当既要处理窗口过期，又要快速查询当前窗口最值时，单调队列通常比堆更直接。"),
                        List.of(
                                step("队列中存下标", "存下标而不是值，便于判断元素是否已经滑出窗口。", "建模"),
                                step("维护单调性", "新元素入队前，从队尾移除所有不可能成为最优答案的元素。", "维护"),
                                step("处理窗口过期", "窗口左端移动时，如果队首下标过期，就把它弹出。", "更新")),
                        List.of(
                                caseStudy("滑动窗口最大值", "队首始终是当前窗口最大值对应的下标，能在线性时间求完整个数组。"),
                                caseStudy("受限跳跃 DP", "在带窗口限制的动态规划里，单调队列能把状态转移从 O(k) 优化到 O(1) 均摊。")),
                        code("Python", "滑动窗口最大值", """
                                from collections import deque

                                def max_sliding_window(nums, k):
                                    window = deque()
                                    answer = []
                                    for i, value in enumerate(nums):
                                        while window and window[0] <= i - k:
                                            window.popleft()
                                        while window and nums[window[-1]] <= value:
                                            window.pop()
                                        window.append(i)
                                        if i >= k - 1:
                                            answer.append(nums[window[0]])
                                    return answer
                                """,
                                "队列里的下标始终对应递减值序列。",
                                "先删过期元素，再维护单调性。"),
                        false,
                        "",
                        "",
                        "",
                        "emerald",
                        70),
                article(
                        BASIC_DATA_STRUCTURES,
                        "algo-hash-table-lookup",
                        "哈希表查找与计数",
                        "Hash Table Lookup and Counting",
                        "查找核心",
                        "哈希表通过散列函数把键映射到桶位置，目标是用期望 O(1) 时间完成查找、插入和计数。算法题里常把“是否出现过”“某个前缀和出现了几次”“值到下标的映射”统一交给哈希表处理。",
                        "在散列均匀的前提下，查找、插入和删除的平均时间复杂度都是 O(1)，最坏会退化到 O(n)。空间复杂度为 O(n)，用于存储已经出现的键值对。", 
                        "7 min read",
                        List.of("哈希表", "计数", "映射"),
                        List.of("适合频繁查重、频次统计、值到位置映射、前缀状态去重等场景。", "当题目要求一次遍历内快速判断“之前是否出现过某个状态”时，哈希表通常最有效。"),
                        List.of(
                                step("选定键和值", "键可能是元素值、前缀和、字符串模式或状态编码，值则存频次、索引或布尔标记。", "设计"),
                                step("扫描并同步更新", "在遍历过程中，先查询再更新还是先更新再查询，取决于题意对“当前元素是否可重复使用”的限制。", "遍历"),
                                step("处理冲突与默认值", "编码时利用字典默认值或条件判断，减少空键和缺省状态带来的边界错误。", "边界")),
                        List.of(
                                caseStudy("两数之和", "把已经遍历过的数字映射到下标，扫描当前值时只需 O(1) 查找补数是否存在。"),
                                caseStudy("子数组和等于 K", "把前缀和出现次数存入哈希表，查询当前前缀减去目标值是否出现过。")),
                        code("Python", "两数之和哈希解法", """
                                def two_sum(nums, target):
                                    index_by_value = {}
                                    for i, value in enumerate(nums):
                                        need = target - value
                                        if need in index_by_value:
                                            return [index_by_value[need], i]
                                        index_by_value[value] = i
                                    return []
                                """,
                                "先查补数再写入当前值，避免重复使用同一个元素。",
                                "哈希表最常见的值是下标、频次或出现次数。"),
                        false,
                        "",
                        "",
                        "",
                        "amber",
                        80),
                article(
                        BASIC_DATA_STRUCTURES,
                        "algo-heap-priority-queue",
                        "堆与优先队列",
                        "Heap and Priority Queue",
                        "优先级结构",
                        "堆是一棵满足堆序性质的完全二叉树，能够在对数时间内维护全局最大值或最小值。优先队列在图算法、Top K、实时调度里特别常见，因为它适合“每次取当前最优状态，再把新候选加入”的过程。",
                        "建堆可以在 O(n) 时间内完成；单次插入和弹出堆顶为 O(log n)，查询堆顶为 O(1)。额外空间通常为 O(n)，用于维护堆数组。", 
                        "8 min read",
                        List.of("堆", "优先队列", "TopK"),
                        List.of("适合持续维护最值、Top K、任务调度、最短路径和合并有序流。", "当数据不断到来，且每次只需要当前最优元素时，优先队列的性价比很高。"),
                        List.of(
                                step("定义堆序规则", "先决定要维护最小堆还是最大堆，以及比较依据是值本身还是复合键。", "规则"),
                                step("推入新候选", "每当出现新的潜在答案，就把它入堆，让堆自动保持全局最优在顶端。", "入堆"),
                                step("弹出并校验", "取出堆顶时，必要时判断该状态是否过期，再决定是否真正使用。", "出堆")),
                        List.of(
                                caseStudy("数据流中第 K 大元素", "维护一个大小为 K 的最小堆，堆顶就是当前第 K 大值。"),
                                caseStudy("Dijkstra 最短路径", "每次从小根堆弹出当前距离最短的节点，再扩展相邻边。")),
                        code("Python", "求数组中前 k 个高频元素", """
                                import heapq
                                from collections import Counter

                                def top_k_frequent(nums, k):
                                    counter = Counter(nums)
                                    heap = []
                                    for value, freq in counter.items():
                                        heapq.heappush(heap, (freq, value))
                                        if len(heap) > k:
                                            heapq.heappop(heap)
                                    return [value for _, value in heap]
                                """,
                                "小根堆保留 k 个候选，能把复杂度控制在 O(n log k)。",
                                "复合键入堆时，比较规则取决于元组顺序。"),
                        false,
                        "",
                        "",
                        "",
                        "cyan",
                        90),
                article(
                        BASIC_DATA_STRUCTURES,
                        "algo-binary-tree-traversal",
                        "二叉树遍历算法",
                        "Binary Tree Traversal",
                        "树结构",
                        "二叉树遍历的核心在于选择访问顺序：前序适合先处理根节点，中序适合输出有序结果，后序适合先处理子树再汇总。递归写法表达力强，迭代写法更能体现栈与访问状态的控制能力。",
                        "无论前中后序还是层序，每个节点都只会被访问常数次，因此时间复杂度为 O(n)。递归遍历的空间复杂度取决于树高，平均 O(log n)、最坏 O(n)。",
                        "8 min read",
                        List.of("二叉树", "递归", "迭代"),
                        List.of("适合层级结构、表达式树、目录结构、递归定义天然清晰的问题。", "当题目要求“先处理子树再合并结果”时，树遍历几乎是默认出发点。"),
                        List.of(
                                step("确定遍历顺序", "根据题意决定根节点是在子树之前、之间还是之后处理。", "顺序"),
                                step("写出递归终止条件", "空节点直接返回，避免在访问属性前触发空指针错误。", "边界"),
                                step("把局部结果向上汇总", "遍历返回值可以是高度、路径和、布尔判定等，为后续树形 DP 打基础。", "汇总")),
                        List.of(
                                caseStudy("验证二叉搜索树", "利用中序遍历得到严格递增序列，可以快速判断一棵树是否满足 BST 性质。"),
                                caseStudy("二叉树最大深度", "后序遍历先求左右子树深度，再在当前节点做加一汇总。")),
                        code("Python", "递归前序遍历", """
                                def preorder(root):
                                    result = []

                                    def dfs(node):
                                        if not node:
                                            return
                                        result.append(node.val)
                                        dfs(node.left)
                                        dfs(node.right)

                                    dfs(root)
                                    return result
                                """,
                                "先处理当前节点，再递归左右子树。",
                                "树题大多数都可以先从递归模板出发。"),
                        true,
                        "树论基础",
                        "先学会遍历，再谈树形 DP",
                        "树算法的第一步不是背复杂题型，而是把前中后序和层序遍历写到足够熟练。",
                        "emerald",
                        100),
                article(
                        BASIC_DATA_STRUCTURES,
                        "algo-binary-search-tree",
                        "二叉搜索树",
                        "Binary Search Tree",
                        "树模型",
                        "二叉搜索树利用“左子树小于根、右子树大于根”的有序结构，把查找、插入、删除局部化到一条根到叶子的路径上。它是平衡树、区间树和有序集合实现的基础认知模型。",
                        "平均情况下，查找、插入和删除复杂度都为 O(log n)；若树退化成链表，最坏会变成 O(n)。递归或迭代实现的额外空间通常与树高一致。", 
                        "8 min read",
                        List.of("二叉搜索树", "有序结构", "递归"),
                        List.of("适合维护动态有序集合、区间边界、前驱后继查询等场景。", "当题目同时要求插入、删除和有序遍历时，BST 思维很有价值。"),
                        List.of(
                                step("利用有序性质定位", "比较目标值与当前节点值，决定继续进入左子树还是右子树。", "查找"),
                                step("插入时找到空位", "沿着有序路径向下走，直到遇到空指针，再把新节点挂上。", "插入"),
                                step("删除时分类讨论", "删除叶子、单子树节点和双子树节点时处理方式不同，双子树节点通常用后继替换。", "删除")),
                        List.of(
                                caseStudy("有序字典", "通过 BST 可以支持插入、查找、删除和按序输出，是 TreeMap 一类结构的基础模型。"),
                                caseStudy("区间边界查询", "寻找第一个大于等于目标值的元素时，本质上是在 BST 上找 lower bound。")),
                        code("Python", "二叉搜索树查找", """
                                def bst_search(root, target):
                                    node = root
                                    while node:
                                        if node.val == target:
                                            return node
                                        if target < node.val:
                                            node = node.left
                                        else:
                                            node = node.right
                                    return None
                                """,
                                "每次比较都能排除一整棵子树。",
                                "删除节点时最关键的是处理双子树节点的替换。"),
                        false,
                        "",
                        "",
                        "",
                        "amber",
                        110),
                article(
                        BASIC_DATA_STRUCTURES,
                        "algo-union-find",
                        "并查集",
                        "Union Find",
                        "连通性结构",
                        "并查集把元素划分为若干不相交集合，核心操作是查询两个元素是否属于同一集合，以及把两个集合合并。路径压缩和按秩合并使得并查集在动态图连通性、最小生成树和岛屿类问题中非常高效。",
                        "单次查找与合并的均摊时间复杂度接近 O(1)，更精确地说是 O(alpha(n))，其中 alpha 是反阿克曼函数。空间复杂度为 O(n)，主要用于保存父节点和秩信息。", 
                        "8 min read",
                        List.of("并查集", "连通性", "路径压缩"),
                        List.of("适合动态连通性判定、图中连通块统计、冗余边检测和 Kruskal 最小生成树。", "当题目只有“连不连得通”而不关心完整路径时，并查集通常比 DFS/BFS 更轻量。"),
                        List.of(
                                step("初始化父节点", "开始时让每个元素的父节点指向自己，表示每个元素单独成集。", "初始化"),
                                step("实现带压缩的查找", "find 在递归或迭代返回时把节点直接挂到根上，减少后续查找路径长度。", "查找"),
                                step("按秩合并集合", "union 时把较浅的树挂到较深的树上，避免树高退化。", "合并")),
                        List.of(
                                caseStudy("省份数量", "把相连城市不断 union，最后统计根节点个数就能得到连通块数量。"),
                                caseStudy("Kruskal 最小生成树", "按边权排序后，只有连接两个不同集合的边才会被选入生成树。")),
                        code("Python", "带路径压缩的并查集", """
                                class UnionFind:
                                    def __init__(self, n):
                                        self.parent = list(range(n))
                                        self.rank = [0] * n

                                    def find(self, x):
                                        if self.parent[x] != x:
                                            self.parent[x] = self.find(self.parent[x])
                                        return self.parent[x]

                                    def union(self, a, b):
                                        root_a = self.find(a)
                                        root_b = self.find(b)
                                        if root_a == root_b:
                                            return False
                                        if self.rank[root_a] < self.rank[root_b]:
                                            root_a, root_b = root_b, root_a
                                        self.parent[root_b] = root_a
                                        if self.rank[root_a] == self.rank[root_b]:
                                            self.rank[root_a] += 1
                                        return True
                                """,
                                "路径压缩和按秩合并需要配合使用。",
                                "union 返回值常用来判断一条边是否形成环。"),
                        false,
                        "",
                        "",
                        "",
                        "cyan",
                        120)
        );
    }

    private List<KnowledgeBaseService.AdminArticleInput> sortingArticles() {
        return List.of(
                article(
                        SORTING_ALGORITHMS,
                        "algo-bubble-sort",
                        "冒泡排序",
                        "Bubble Sort",
                        "交换排序",
                        "冒泡排序通过反复比较相邻元素并在逆序时交换，把较大的元素逐轮“冒”到右端。它的价值不在于工程性能，而在于帮助初学者理解稳定排序、逆序对减少和交换式排序的基本机制。",
                        "最坏和平均时间复杂度都是 O(n^2)，因为比较次数约为 n(n-1)/2；若增加“本轮是否发生交换”的标记，最好情况可以优化到 O(n)。空间复杂度为 O(1)，只使用常数级辅助变量。",
                        "6 min read",
                        List.of("冒泡排序", "稳定排序", "交换排序"),
                        List.of("适合教学场景、小规模数据和近乎有序数组的早停演示。", "当题目更强调排序思想或稳定性概念，而非工程效率时，冒泡排序仍然值得掌握。"),
                        List.of(
                                step("外层控制轮数", "每一轮都把一个最大元素交换到当前未排序区间的末尾。", "轮次"),
                                step("内层相邻比较", "从左到右比较相邻元素，若逆序就交换位置。", "交换"),
                                step("利用有序标记提前结束", "若某一轮没有发生交换，说明整个数组已经有序，可以直接停止。", "优化")),
                        List.of(
                                caseStudy("教学演示稳定排序", "通过相邻交换的方式，能直观看到稳定排序不会打乱相等元素的先后关系。"),
                                caseStudy("近乎有序序列检测", "加上 early stop 后，一旦数组本身有序，只需要一轮扫描即可结束。")),
                        code("Python", "冒泡排序模板", """
                                def bubble_sort(nums):
                                    nums = nums[:]
                                    for end in range(len(nums) - 1, 0, -1):
                                        swapped = False
                                        for i in range(end):
                                            if nums[i] > nums[i + 1]:
                                                nums[i], nums[i + 1] = nums[i + 1], nums[i]
                                                swapped = True
                                        if not swapped:
                                            break
                                    return nums
                                """,
                                "swapped 标记可以把最好情况降到 O(n)。",
                                "相等元素不会跨越彼此，因此它是稳定排序。"),
                        false,
                        "",
                        "",
                        "",
                        "emerald",
                        10),
                article(
                        SORTING_ALGORITHMS,
                        "algo-selection-sort",
                        "选择排序",
                        "Selection Sort",
                        "选择排序",
                        "选择排序每一轮从未排序区间中找出最小值，再把它放到当前起始位置。它的比较次数固定，适合帮助理解“通过选择最优元素逐步扩大有序区”的排序思路。",
                        "无论最好、最坏还是平均情况，选择排序都要进行 O(n^2) 次比较；交换次数最多只有 O(n)，因此在交换成本高于比较成本的环境中有一定讨论价值。空间复杂度为 O(1)。",
                        "6 min read",
                        List.of("选择排序", "原地排序", "基础排序"),
                        List.of("适合教学、交换成本高且数据规模很小的场景。", "当需要展示“有序区逐步扩大”的朴素排序思路时，选择排序很直观。"),
                        List.of(
                                step("固定当前起点", "第 i 轮确定第 i 个位置应该放哪个元素。", "定位"),
                                step("在后缀里找最小值", "扫描未排序区间，记录最小值下标。", "选择"),
                                step("只做一次交换", "扫描结束后再把最小值换到前面，扩大有序前缀。", "放置")),
                        List.of(
                                caseStudy("写入次数受限的介质", "选择排序每轮只做一次交换，在某些写入成本高的环境里比冒泡排序更节制。"),
                                caseStudy("排序思想教学", "它能清楚展示“每一轮确定一个最终位置”的策略。")),
                        code("Python", "选择排序模板", """
                                def selection_sort(nums):
                                    nums = nums[:]
                                    n = len(nums)
                                    for i in range(n):
                                        best = i
                                        for j in range(i + 1, n):
                                            if nums[j] < nums[best]:
                                                best = j
                                        nums[i], nums[best] = nums[best], nums[i]
                                    return nums
                                """,
                                "比较次数固定，不会因为序列接近有序而明显变快。",
                                "选择排序默认不稳定。"),
                        false,
                        "",
                        "",
                        "",
                        "amber",
                        20),
                article(
                        SORTING_ALGORITHMS,
                        "algo-insertion-sort",
                        "插入排序",
                        "Insertion Sort",
                        "插入类排序",
                        "插入排序把数组前缀始终视为有序区，每次取出下一个元素，向左寻找合适位置后插入。它对近乎有序的数据非常友好，也是希尔排序和若干高级排序优化中的基础操作。",
                        "最坏和平均时间复杂度为 O(n^2)，因为每个元素可能向左移动 O(n) 距离；最好情况是 O(n)，对应数组本身已经有序。空间复杂度为 O(1)，而且它是稳定排序。", 
                        "6 min read",
                        List.of("插入排序", "稳定排序", "近乎有序"),
                        List.of("适合小规模数组、近乎有序数组以及复杂排序中的小区间优化。", "在快排和归并排序的小分段优化里，插入排序常作为收尾策略使用。"),
                        List.of(
                                step("维护有序前缀", "默认下标 0 到 i-1 已经有序，本轮要把 nums[i] 插进去。", "前缀"),
                                step("向左寻找位置", "不断比较前一个元素是否更大，若更大就整体右移。", "移动"),
                                step("放回待插入元素", "在最终停下的位置写回 key，完成一轮插入。", "插入")),
                        List.of(
                                caseStudy("扑克牌整理", "手工整理扑克牌时，就是把新拿到的牌插入到左边已经有序的牌组。"),
                                caseStudy("小数组优化", "当区间很小时，插入排序常比递归分治排序更快，因为常数更小。")),
                        code("Python", "插入排序模板", """
                                def insertion_sort(nums):
                                    nums = nums[:]
                                    for i in range(1, len(nums)):
                                        key = nums[i]
                                        j = i - 1
                                        while j >= 0 and nums[j] > key:
                                            nums[j + 1] = nums[j]
                                            j -= 1
                                        nums[j + 1] = key
                                    return nums
                                """,
                                "移动元素比频繁交换更贴近插入排序本质。",
                                "数组越接近有序，while 循环执行次数越少。"),
                        false,
                        "",
                        "",
                        "",
                        "cyan",
                        30),
                article(
                        SORTING_ALGORITHMS,
                        "algo-shell-sort",
                        "希尔排序",
                        "Shell Sort",
                        "增量排序",
                        "希尔排序先按较大步长把数组分成若干子序列分别做插入排序，再逐步缩小步长直到 1。它利用“先粗调、后细排”的思想，缓解了普通插入排序一次只能局部移动元素的问题。",
                        "复杂度取决于增量序列，常见实现的平均复杂度介于 O(n^1.3) 到 O(n^1.5) 左右，最坏情况通常仍高于 O(n log n)。空间复杂度是 O(1)，但它不是稳定排序。", 
                        "7 min read",
                        List.of("希尔排序", "插入排序", "增量优化"),
                        List.of("适合中等规模数组、内存敏感且对稳定性无要求的场景。", "当需要在原地排序的前提下追求比 O(n^2) 更好的平均性能时，希尔排序是一个过渡方案。"),
                        List.of(
                                step("选择增量序列", "常见做法是 gap 逐轮减半，也可以使用更优的 Knuth 序列。", "增量"),
                                step("对子序列做插入排序", "对每个相隔 gap 的元素链进行插入操作，让远距离逆序先被修正。", "粗排"),
                                step("最终 gap 为 1", "最后一轮等价于普通插入排序，但此时数组已经接近有序。", "收敛")),
                        List.of(
                                caseStudy("嵌入式设备排序", "希尔排序原地、实现短，在一些对内存特别敏感的小系统里仍有人使用。"),
                                caseStudy("排序算法性能对比", "它适合作为理解“增量能改变局部排序行为”的过渡案例。")),
                        code("Python", "希尔排序模板", """
                                def shell_sort(nums):
                                    nums = nums[:]
                                    gap = len(nums) // 2
                                    while gap > 0:
                                        for i in range(gap, len(nums)):
                                            value = nums[i]
                                            j = i
                                            while j >= gap and nums[j - gap] > value:
                                                nums[j] = nums[j - gap]
                                                j -= gap
                                            nums[j] = value
                                        gap //= 2
                                    return nums
                                """,
                                "gap 越大，越能先修复远距离逆序。",
                                "最后一轮 gap=1 时，数组通常已经接近有序。"),
                        false,
                        "",
                        "",
                        "",
                        "amber",
                        40),
                article(
                        SORTING_ALGORITHMS,
                        "algo-quick-sort",
                        "快速排序",
                        "Quick Sort",
                        "分治排序",
                        "快速排序通过选取基准值，把数组划分成小于基准和大于基准的两部分，再递归处理左右区间。它把“比较”与“分区”结合得非常自然，因此平均性能优秀，是工程里最常见的基础排序思想之一。",
                        "平均时间复杂度为 O(n log n)，因为每轮分区大致把问题平衡拆分；最坏情况在每次都极不平衡时会退化到 O(n^2)。原地版额外空间主要来自递归栈，平均 O(log n)、最坏 O(n)。", 
                        "8 min read",
                        List.of("快速排序", "分治", "原地排序"),
                        List.of("适合通用数组排序、需要原地完成排序且平均性能优先的场景。", "在理解分治与 partition 机制后，快排还能迁移到快速选择等算法。"),
                        List.of(
                                step("选择基准值", "基准值可以取首元素、尾元素、随机元素或三数取中。", "基准"),
                                step("完成原地分区", "通过双指针交换，把小于基准的元素放到左边，大于基准的放到右边。", "分区"),
                                step("递归左右区间", "基准最终位置确定后，再递归处理左右两边的子数组。", "递归")),
                        List.of(
                                caseStudy("通用内存排序", "很多库排序都吸收了快速排序的 partition 思想，并在坏情况上做进一步保护。"),
                                caseStudy("Top K 扩展", "快速选择算法直接复用 partition，把排序问题降成选择问题。")),
                        code("Python", "快速排序模板", """
                                def quick_sort(nums):
                                    nums = nums[:]

                                    def sort(left, right):
                                        if left >= right:
                                            return
                                        pivot = nums[right]
                                        store = left
                                        for i in range(left, right):
                                            if nums[i] <= pivot:
                                                nums[store], nums[i] = nums[i], nums[store]
                                                store += 1
                                        nums[store], nums[right] = nums[right], nums[store]
                                        sort(left, store - 1)
                                        sort(store + 1, right)

                                    sort(0, len(nums) - 1)
                                    return nums
                                """,
                                "partition 的正确性比背模板更重要。",
                                "随机化基准能显著降低最坏情况出现概率。"),
                        true,
                        "排序主力",
                        "把比较问题转成分区问题",
                        "快速排序几乎是所有分治排序与 Top K 题型的共同起点，值得重点掌握 partition 的推导方式。",
                        "emerald",
                        50),
                article(
                        SORTING_ALGORITHMS,
                        "algo-merge-sort",
                        "归并排序",
                        "Merge Sort",
                        "稳定分治",
                        "归并排序先递归拆分数组，再把两个有序子数组线性合并。它最重要的特征是复杂度稳定、天然支持链表排序和外部排序，是稳定排序和分治合并思想的经典代表。",
                        "无论最好、最坏还是平均情况，归并排序时间复杂度都稳定在 O(n log n)。数组版需要 O(n) 辅助空间用于合并，链表版则可以把额外空间降到递归栈级别。", 
                        "8 min read",
                        List.of("归并排序", "稳定排序", "分治"),
                        List.of("适合对稳定性有要求、数据规模较大或需要外部排序的场景。", "当题目要统计逆序对、合并多个有序结果时，归并思想尤其常见。"),
                        List.of(
                                step("递归拆分问题", "把区间不断二分，直到每个子数组长度为 1。", "拆分"),
                                step("有序合并子数组", "用两个指针线性扫描左右数组，把较小元素依次写入临时数组。", "合并"),
                                step("回写结果", "把合并后的有序结果写回原数组，为上层继续合并做准备。", "回写")),
                        List.of(
                                caseStudy("外部排序", "当数据量远大于内存时，可以先局部排序，再多路归并得到全局有序结果。"),
                                caseStudy("逆序对统计", "在合并阶段统计左侧剩余元素个数，就能得到跨区间逆序对数量。")),
                        code("Python", "归并排序模板", """
                                def merge_sort(nums):
                                    if len(nums) <= 1:
                                        return nums
                                    mid = len(nums) // 2
                                    left = merge_sort(nums[:mid])
                                    right = merge_sort(nums[mid:])

                                    merged = []
                                    i = j = 0
                                    while i < len(left) and j < len(right):
                                        if left[i] <= right[j]:
                                            merged.append(left[i])
                                            i += 1
                                        else:
                                            merged.append(right[j])
                                            j += 1
                                    merged.extend(left[i:])
                                    merged.extend(right[j:])
                                    return merged
                                """,
                                "归并排序稳定，是很多业务排序的底层选择之一。",
                                "统计类题目常在 merge 阶段顺手完成。"),
                        false,
                        "",
                        "",
                        "",
                        "cyan",
                        60),
                article(
                        SORTING_ALGORITHMS,
                        "algo-heap-sort",
                        "堆排序",
                        "Heap Sort",
                        "堆排序",
                        "堆排序先把数组建成大根堆，再不断把堆顶与末尾交换，并缩小堆大小继续下沉。它不依赖递归分区，能在 O(1) 额外空间下实现 O(n log n) 排序，是原地比较排序里的重要选手。",
                        "建堆为 O(n)，之后进行 n 次删除堆顶操作，每次 O(log n)，总时间复杂度为 O(n log n)。空间复杂度为 O(1)，但由于远距离交换，它不是稳定排序。", 
                        "7 min read",
                        List.of("堆排序", "原地排序", "堆"),
                        List.of("适合要求原地完成 O(n log n) 排序且不关心稳定性的场景。", "当内存受限又想避免快排最坏退化时，堆排序是稳定的备选策略。"),
                        List.of(
                                step("原地建堆", "从最后一个非叶子节点开始下沉，在线性时间内建立大根堆。", "建堆"),
                                step("交换堆顶到末尾", "把当前最大值放到数组尾部，并缩小有效堆区间。", "交换"),
                                step("对根节点下沉", "恢复剩余区间的大根堆性质，继续下一轮。", "调整")),
                        List.of(
                                caseStudy("内存敏感排序", "相比归并排序，堆排序不需要额外 O(n) 辅助空间。"),
                                caseStudy("实时优先级系统", "虽然完整堆排序不常直接用于调度，但其维护最值的机制与优先队列完全一致。")),
                        code("Python", "堆排序模板", """
                                def heap_sort(nums):
                                    nums = nums[:]

                                    def sift_down(start, end):
                                        root = start
                                        while True:
                                            child = root * 2 + 1
                                            if child > end:
                                                return
                                            if child + 1 <= end and nums[child] < nums[child + 1]:
                                                child += 1
                                            if nums[root] >= nums[child]:
                                                return
                                            nums[root], nums[child] = nums[child], nums[root]
                                            root = child

                                    for start in range((len(nums) - 2) // 2, -1, -1):
                                        sift_down(start, len(nums) - 1)
                                    for end in range(len(nums) - 1, 0, -1):
                                        nums[0], nums[end] = nums[end], nums[0]
                                        sift_down(0, end - 1)
                                    return nums
                                """,
                                "建堆从最后一个非叶子节点开始。",
                                "每轮交换后都要把新的根下沉。"),
                        false,
                        "",
                        "",
                        "",
                        "amber",
                        70),
                article(
                        SORTING_ALGORITHMS,
                        "algo-counting-sort",
                        "计数排序",
                        "Counting Sort",
                        "非比较排序",
                        "计数排序不通过元素间两两比较决定顺序，而是统计每个值出现的次数，再按数值范围重建结果。只要数据值域有限，它就能打破比较排序 O(n log n) 的下界，在整数排序中非常高效。",
                        "时间复杂度为 O(n + k)，其中 k 是值域大小；空间复杂度也为 O(n + k) 或 O(k)。当 k 远小于 n log n 时，计数排序往往比比较排序更快，但值域过大时就不再划算。", 
                        "7 min read",
                        List.of("计数排序", "桶思想", "整数排序"),
                        List.of("适合值域有限的整数数组、考试成绩、年龄统计等离散小范围数据。", "在需要稳定排序时，可以通过前缀计数数组实现稳定版计数排序。"),
                        List.of(
                                step("统计频次", "先扫描所有元素，记录每个值出现了多少次。", "计数"),
                                step("确定写回顺序", "若只求排序结果，可直接按值从小到大展开频次；若要求稳定性，可先计算前缀位置。", "定位"),
                                step("重建有序数组", "根据计数结果把元素按顺序回写到输出数组。", "输出")),
                        List.of(
                                caseStudy("成绩排名", "分数范围固定时，计数排序能在线性时间内完成统计和有序输出。"),
                                caseStudy("基数排序子步骤", "基数排序常在每一位上调用稳定版计数排序完成分配。")),
                        code("Python", "计数排序模板", """
                                def counting_sort(nums):
                                    if not nums:
                                        return []
                                    low = min(nums)
                                    high = max(nums)
                                    count = [0] * (high - low + 1)
                                    for value in nums:
                                        count[value - low] += 1
                                    result = []
                                    for offset, freq in enumerate(count):
                                        result.extend([offset + low] * freq)
                                    return result
                                """,
                                "值域偏移可以处理包含负数的情况。",
                                "当值域太大时，计数排序的空间成本会迅速升高。"),
                        false,
                        "",
                        "",
                        "",
                        "cyan",
                        80)
        );
    }

    private List<KnowledgeBaseService.AdminArticleInput> searchingArticles() {
        return List.of(
                article(
                        SEARCHING_ALGORITHMS,
                        "algo-linear-search",
                        "顺序查找",
                        "Linear Search",
                        "查找基础",
                        "顺序查找按元素出现顺序逐个比较目标值，直到找到答案或扫描结束。虽然它看起来简单，但在数据无序、规模很小或只能流式访问时，顺序查找反而是唯一合理的选择。",
                        "最坏和平均时间复杂度为 O(n)，最好情况是 O(1)，即目标刚好在首元素。空间复杂度为 O(1)，因为只需维护当前下标和目标值。", 
                        "5 min read",
                        List.of("顺序查找", "无序数组", "流式数据"),
                        List.of("适合无序小数组、链表、一次性流式输入和无法随机访问的数据结构。", "当数据量小到不足以支撑额外预处理时，顺序查找往往最划算。"),
                        List.of(
                                step("从头开始扫描", "依次比较当前元素与目标值是否相同。", "扫描"),
                                step("遇到目标立即返回", "一旦匹配成功就结束，避免不必要的遍历。", "终止"),
                                step("扫描完仍未命中则失败", "若所有元素都不匹配，就返回未找到标记。", "结果")),
                        List.of(
                                caseStudy("日志流关键字检测", "数据以流形式到达且不能回溯时，顺序扫描是最自然的做法。"),
                                caseStudy("链表目标节点查找", "链表不能随机访问，只能按节点顺序一个一个比较。")),
                        code("Python", "顺序查找模板", """
                                def linear_search(nums, target):
                                    for index, value in enumerate(nums):
                                        if value == target:
                                            return index
                                    return -1
                                """,
                                "顺序查找无需预处理，也不依赖数据有序。",
                                "适合作为其他复杂查找策略的基线方案。"),
                        false,
                        "",
                        "",
                        "",
                        "emerald",
                        10),
                article(
                        SEARCHING_ALGORITHMS,
                        "algo-binary-search",
                        "二分查找",
                        "Binary Search",
                        "查找核心",
                        "二分查找要求搜索空间具备单调性或整体有序性，每次通过中点比较把答案空间直接缩小一半。它最重要的不是模板本身，而是对“区间不变量”和边界闭开方式的严格维护。",
                        "每轮比较都能将搜索区间减半，因此时间复杂度为 O(log n)。空间复杂度为 O(1)；如果写成递归形式，额外递归栈为 O(log n)。", 
                        "6 min read",
                        List.of("二分查找", "有序数组", "边界"),
                        List.of("适合有序数组精确查找、边界定位、单调答案判定等场景。", "当搜索空间可以被“可行/不可行”二分时，二分查找往往能显著降复杂度。"),
                        List.of(
                                step("确定区间语义", "先约定搜索区间是闭区间 [l, r] 还是左闭右开 [l, r)。", "区间"),
                                step("计算中点并比较", "根据 nums[mid] 与 target 的大小关系选择保留左半段或右半段。", "比较"),
                                step("更新边界直到收敛", "循环结束时根据区间定义返回答案或未找到标记。", "收敛")),
                        List.of(
                                caseStudy("字典序查词", "有序词典可以用二分查找快速定位单词是否存在。"),
                                caseStudy("版本回归定位", "当版本是否出错具有单调性时，二分能迅速找到第一个坏版本。")),
                        code("Python", "闭区间二分查找", """
                                def binary_search(nums, target):
                                    left, right = 0, len(nums) - 1
                                    while left <= right:
                                        mid = left + (right - left) // 2
                                        if nums[mid] == target:
                                            return mid
                                        if nums[mid] < target:
                                            left = mid + 1
                                        else:
                                            right = mid - 1
                                    return -1
                                """,
                                "mid 的写法能避免极端语言里的整型溢出风险。",
                                "最容易出错的地方是区间定义和更新规则不一致。"),
                        true,
                        "查找必修",
                        "任何单调空间都值得试二分",
                        "掌握二分查找之后，很多“最小可行值 / 最大合法值”问题都会变得更容易建模。",
                        "cyan",
                        20),
                article(
                        SEARCHING_ALGORITHMS,
                        "algo-interpolation-search",
                        "插值查找",
                        "Interpolation Search",
                        "分布敏感",
                        "插值查找在有序数组中不固定取中点，而是根据目标值在区间值域中的相对位置估算更可能的落点。它适用于数值分布比较均匀的场景，能比普通二分更快逼近目标位置。",
                        "平均情况下在均匀分布数据上可达到接近 O(log log n) 的表现，但最坏仍可能退化到 O(n)。空间复杂度为 O(1)。因此它适合作为有序数值查找的分布优化，而不是二分查找的完全替代。", 
                        "6 min read",
                        List.of("插值查找", "均匀分布", "有序数组"),
                        List.of("适合有序且近似均匀分布的数值型数组，例如学生成绩、连续编号等。", "若数据分布严重偏斜，插值查找的优势会明显下降。"),
                        List.of(
                                step("确认值域有效", "只有当 nums[left] 到 nums[right] 能代表一个相对均匀的数值区间时，插值估算才有意义。", "前提"),
                                step("按比例估算中点", "根据 target 在值域中的比例计算 probe 位置，而不是简单取区间中点。", "估算"),
                                step("像二分一样缩小区间", "比较 probe 位置的值后，继续保留可能包含目标的一侧。", "收缩")),
                        List.of(
                                caseStudy("电话簿数字编号检索", "编号分布接近连续时，插值位置往往更接近真实目标。"),
                                caseStudy("均匀传感器读数表", "当数据密度稳定时，插值查找能减少比较轮数。")),
                        code("Python", "插值查找模板", """
                                def interpolation_search(nums, target):
                                    left, right = 0, len(nums) - 1
                                    while left <= right and nums[left] <= target <= nums[right]:
                                        if nums[left] == nums[right]:
                                            return left if nums[left] == target else -1
                                        probe = left + (target - nums[left]) * (right - left) // (nums[right] - nums[left])
                                        if nums[probe] == target:
                                            return probe
                                        if nums[probe] < target:
                                            left = probe + 1
                                        else:
                                            right = probe - 1
                                    return -1
                                """,
                                "probe 按值域比例而不是下标中点计算。",
                                "边界条件要先防止除零。"),
                        false,
                        "",
                        "",
                        "",
                        "amber",
                        30),
                article(
                        SEARCHING_ALGORITHMS,
                        "algo-hash-search",
                        "哈希查找",
                        "Hash Based Search",
                        "期望 O(1)",
                        "哈希查找把目标键通过哈希函数映射到桶位置，利用数组下标快速定位，再通过拉链法或开放寻址解决冲突。它追求的是期望 O(1) 的查询性能，因此在频繁查重、缓存命中和字典检索中非常常见。",
                        "平均查询时间复杂度为 O(1)，最坏情况在冲突集中时退化为 O(n)。空间复杂度为 O(n)，并且负载因子越高，冲突越多，查询性能越容易下降。", 
                        "6 min read",
                        List.of("哈希查找", "散列", "字典"),
                        List.of("适合缓存、字典、频繁会员查重、快速存在性判断等需要高频查找的场景。", "当输入是离散键且不要求有序输出时，哈希查找通常比平衡树更快。"),
                        List.of(
                                step("设计键的散列方式", "根据键类型选择可靠的哈希函数，尽量让不同键均匀落到桶中。", "散列"),
                                step("选择冲突处理策略", "常见策略是链地址法或开放寻址法，二者在实现和性能上各有权衡。", "冲突"),
                                step("控制负载因子", "桶太少会导致冲突增多，因此需要在查询效率和空间之间做平衡。", "调优")),
                        List.of(
                                caseStudy("用户会话缓存", "根据用户 ID 直接定位缓存槽位，能快速判断会话是否存在。"),
                                caseStudy("单词词频统计", "哈希表对字符串计数的平均效率很高，是文本分析的常见基础组件。")),
                        code("Python", "基于 dict 的哈希查找", """
                                def hash_search(items, target):
                                    table = {value: index for index, value in enumerate(items)}
                                    return table.get(target, -1)
                                """,
                                "高级语言自带的 dict / map 就是成熟的哈希查找实现。",
                                "关注负载因子和冲突，才能真正理解哈希查找的工程表现。"),
                        false,
                        "",
                        "",
                        "",
                        "emerald",
                        40),
                article(
                        SEARCHING_ALGORITHMS,
                        "algo-lower-upper-bound",
                        "边界查找（Lower Bound / Upper Bound）",
                        "Lower Bound and Upper Bound",
                        "边界定位",
                        "边界查找是二分查找最常见的工程化变形，目标不再是“找到某个值”，而是“找到第一个大于等于 target 的位置”或“第一个大于 target 的位置”。很多区间计数、去重压缩和有序插入问题，本质上都是边界定位问题。",
                        "时间复杂度为 O(log n)，空间复杂度为 O(1)。它与普通二分查找复杂度相同，但更强调区间语义和返回值定义。", 
                        "7 min read",
                        List.of("lower bound", "upper bound", "二分边界"),
                        List.of("适合有序数组插入位置、频次统计、查找某值出现区间的左右端点。", "在“第一个满足条件的位置”类问题里，边界查找比普通二分更通用。"),
                        List.of(
                                step("把问题改写成布尔判定", "例如 lower bound 可以理解为“第一个使 nums[i] >= target 为真”的位置。", "改写"),
                                step("使用左闭右开区间", "很多实现用 [l, r) 更容易避免死循环，并让返回值直接等于插入位置。", "区间"),
                                step("循环结束返回 left", "当 left == right 时，left 就是第一个满足条件的位置。", "返回")),
                        List.of(
                                caseStudy("统计某个值出现次数", "右边界减去左边界即可得到目标值在有序数组中的出现次数。"),
                                caseStudy("有序插入位置", "数据库索引和搜索建议里常常需要快速找到新元素的插入点。")),
                        code("Python", "lower bound 模板", """
                                def lower_bound(nums, target):
                                    left, right = 0, len(nums)
                                    while left < right:
                                        mid = left + (right - left) // 2
                                        if nums[mid] < target:
                                            left = mid + 1
                                        else:
                                            right = mid
                                    return left
                                """,
                                "使用左闭右开区间时，循环条件是 left < right。",
                                "返回 left 即插入位置。"),
                        false,
                        "",
                        "",
                        "",
                        "amber",
                        50),
                article(
                        SEARCHING_ALGORITHMS,
                        "algo-exponential-search",
                        "指数查找",
                        "Exponential Search",
                        "范围扩张",
                        "指数查找先通过 1、2、4、8 这类指数级扩张快速找到目标可能所在的区间，再在该区间内执行二分查找。它适用于有序但长度未知、或者目标值可能非常靠前的数据结构。",
                        "区间扩张最多进行 O(log p) 次，其中 p 是目标位置；之后再执行一次 O(log p) 的二分查找，因此总复杂度为 O(log p)。空间复杂度为 O(1)。", 
                        "6 min read",
                        List.of("指数查找", "未知边界", "二分扩展"),
                        List.of("适合长度未知的有序流、稀疏表以及目标通常出现在前部的场景。", "当无法直接得到数组上界，但能按下标访问元素时，指数查找很实用。"),
                        List.of(
                                step("指数扩张上界", "从 1 开始不断翻倍，直到越界或 nums[bound] >= target。", "扩张"),
                                step("确定候选区间", "目标若存在，一定落在上一个边界和当前边界之间。", "定位"),
                                step("在候选区间二分", "复用标准二分查找在局部区间内精确定位目标。", "搜索")),
                        List.of(
                                caseStudy("未知长度有序接口", "很多在线接口只能按下标访问元素，却不直接给出总长度，此时指数查找先探边界再二分。"),
                                caseStudy("跳表思想过渡", "指数级跳跃再局部精查的思路，与跳表多层索引非常接近。")),
                        code("Python", "指数查找模板", """
                                def exponential_search(nums, target):
                                    if not nums:
                                        return -1
                                    if nums[0] == target:
                                        return 0

                                    bound = 1
                                    while bound < len(nums) and nums[bound] < target:
                                        bound *= 2

                                    left = bound // 2
                                    right = min(bound, len(nums) - 1)
                                    while left <= right:
                                        mid = left + (right - left) // 2
                                        if nums[mid] == target:
                                            return mid
                                        if nums[mid] < target:
                                            left = mid + 1
                                        else:
                                            right = mid - 1
                                    return -1
                                """,
                                "先指数扩张，再局部二分，是它和普通二分的区别。",
                                "特别适合目标位置较靠前的有序数据。"),
                        false,
                        "",
                        "",
                        "",
                        "cyan",
                        60)
        );
    }

    private List<KnowledgeBaseService.AdminArticleInput> dynamicProgrammingArticles() {
        return List.of(
                article(
                        DYNAMIC_PROGRAMMING,
                        "algo-01-knapsack",
                        "0/1 背包",
                        "0/1 Knapsack",
                        "DP 核心",
                        "0/1 背包要求每件物品只能选或不选一次，关键在于把“前 i 件物品、容量为 j 时的最优值”定义成状态，再通过“选 / 不选当前物品”建立转移。它是理解状态设计、转移方程和滚动优化的经典入口。",
                        "二维写法时间复杂度为 O(n * capacity)，空间复杂度为 O(n * capacity)；若改成一维滚动数组，空间复杂度可以压到 O(capacity)。因为每个状态都只依赖上一层同容量或更小容量的状态。", 
                        "8 min read",
                        List.of("动态规划", "0/1 背包", "滚动数组"),
                        List.of("适合资源容量固定、每个选择只能使用一次、目标是最大收益或最优值的场景。", "很多选课、投资、子集选择和容量限制问题都能抽象成 0/1 背包。"),
                        List.of(
                                step("定义状态", "令 dp[j] 表示容量为 j 时能获得的最大价值，或二维定义为前 i 件物品的最优值。", "建模"),
                                step("写出选与不选转移", "如果放入当前物品，状态从 dp[j - weight] 转移；否则保持原值。", "转移"),
                                step("倒序枚举容量", "一维写法必须从大到小更新，避免同一件物品被重复使用。", "优化")),
                        List.of(
                                caseStudy("课程资源分配", "学分上限固定时，为获得最大收益课程组合，可以直接套 0/1 背包模型。"),
                                caseStudy("子集和判定", "若价值等于重量，0/1 背包可以判断是否存在恰好凑出目标和的子集。")),
                        code("Python", "0/1 背包一维 DP", """
                                def knapsack_01(capacity, weights, values):
                                    dp = [0] * (capacity + 1)
                                    for weight, value in zip(weights, values):
                                        for c in range(capacity, weight - 1, -1):
                                            dp[c] = max(dp[c], dp[c - weight] + value)
                                    return dp[capacity]
                                """,
                                "倒序枚举是 0/1 背包与完全背包最重要的区别。",
                                "dp[c] 代表当前处理到的物品范围内的最优值。"),
                        true,
                        "DP 入门",
                        "先吃透 0/1 背包，再扩展其他模型",
                        "0/1 背包几乎把动态规划最核心的建模方式都浓缩在一个模板里，是所有 DP 题型的训练起点。",
                        "emerald",
                        10),
                article(
                        DYNAMIC_PROGRAMMING,
                        "algo-complete-knapsack",
                        "完全背包",
                        "Complete Knapsack",
                        "DP 基础",
                        "完全背包与 0/1 背包的区别在于每件物品可以使用无限次，因此状态转移允许从“当前轮已经更新过的容量”继续扩展。理解这点后，就能解释为什么完全背包的一维优化需要正序枚举容量。",
                        "时间复杂度同样是 O(n * capacity)，空间复杂度用一维数组时为 O(capacity)。与 0/1 背包相比，差别不在复杂度，而在更新顺序和状态依赖方向。", 
                        "7 min read",
                        List.of("完全背包", "动态规划", "容量 DP"),
                        List.of("适合硬币找零、无限材料拼装、完全可重复选择的最优值问题。", "当每种资源可以重复使用任意次时，应优先尝试完全背包模型。"),
                        List.of(
                                step("保留容量状态定义", "仍让 dp[j] 表示容量为 j 时的最优结果。", "状态"),
                                step("允许重复使用当前物品", "转移时可以从 dp[j - weight] + value 来，因为当前物品还能继续选。", "转移"),
                                step("正序枚举容量", "从小到大更新容量，才能让本轮刚更新的状态继续服务于更大容量。", "顺序")),
                        List.of(
                                caseStudy("零钱兑换最少硬币数", "每种面额可以无限使用，本质上就是完全背包求最小值。"),
                                caseStudy("无限次切割材料", "同一长度材料能重复使用时，可以直接套完全背包模型。")),
                        code("Python", "完全背包一维 DP", """
                                def complete_knapsack(capacity, weights, values):
                                    dp = [0] * (capacity + 1)
                                    for weight, value in zip(weights, values):
                                        for c in range(weight, capacity + 1):
                                            dp[c] = max(dp[c], dp[c - weight] + value)
                                    return dp[capacity]
                                """,
                                "正序枚举表示当前物品可以在同一轮被重复使用。",
                                "求最小值问题时，把 max 改成 min 并调整初始化即可。"),
                        false,
                        "",
                        "",
                        "",
                        "cyan",
                        20),
                article(
                        DYNAMIC_PROGRAMMING,
                        "algo-longest-increasing-subsequence",
                        "最长递增子序列",
                        "Longest Increasing Subsequence",
                        "序列 DP",
                        "LIS 的核心在于“以 nums[i] 结尾的最长递增子序列长度”这一状态定义。它既可以用 O(n^2) DP 完整理解转移来源，也可以进一步优化到 O(n log n) 的贪心加二分版本，是从朴素 DP 过渡到高级优化的代表题。",
                        "朴素 DP 时间复杂度为 O(n^2)，空间复杂度为 O(n)；贪心加二分优化后，时间复杂度降到 O(n log n)，空间复杂度仍为 O(n)。两种写法都很重要，前者帮助理解状态，后者强调性能优化。", 
                        "8 min read",
                        List.of("LIS", "序列 DP", "二分优化"),
                        List.of("适合处理序列趋势、最长上升链、信封嵌套和多维排序后的链式选择问题。", "当题目要求某种“严格递增最长链”时，LIS 往往是底层模型。"),
                        List.of(
                                step("定义结尾状态", "dp[i] 表示以 nums[i] 结尾的 LIS 长度。", "状态"),
                                step("枚举前驱位置", "遍历所有 j < i，若 nums[j] < nums[i]，就尝试从 dp[j] 转移。", "转移"),
                                step("必要时做二分优化", "维护长度对应的最小结尾值数组 tails，可把每次转移优化到对数级。", "优化")),
                        List.of(
                                caseStudy("俄罗斯套娃信封", "先排序再对高度求 LIS，就能把二维嵌套问题转成一维序列问题。"),
                                caseStudy("股票趋势分段", "寻找最长持续增长区间时，LIS 是评估序列上升趋势的重要指标。")),
                        code("Python", "LIS 的 O(n log n) 写法", """
                                from bisect import bisect_left

                                def length_of_lis(nums):
                                    tails = []
                                    for value in nums:
                                        index = bisect_left(tails, value)
                                        if index == len(tails):
                                            tails.append(value)
                                        else:
                                            tails[index] = value
                                    return len(tails)
                                """,
                                "tails[i] 表示长度为 i+1 的递增子序列最小结尾值。",
                                "它不直接还原序列，但能高效求长度。"),
                        false,
                        "",
                        "",
                        "",
                        "amber",
                        30),
                article(
                        DYNAMIC_PROGRAMMING,
                        "algo-longest-common-subsequence",
                        "最长公共子序列",
                        "Longest Common Subsequence",
                        "二维 DP",
                        "LCS 用二维状态描述两个字符串前缀的最优答案：dp[i][j] 表示 s 前 i 个字符和 t 前 j 个字符的最长公共子序列长度。它体现了经典的“前缀状态 + 分类讨论最后一个字符”建模方式。",
                        "时间复杂度为 O(n * m)，因为每个状态都要计算一次；空间复杂度为 O(n * m)，若只求长度可优化到 O(min(n, m))。", 
                        "7 min read",
                        List.of("LCS", "二维 DP", "字符串 DP"),
                        List.of("适合文本比对、版本 diff、DNA 序列相似性分析等场景。", "当两个序列要按原相对顺序匹配但不要求连续时，LCS 模型很常用。"),
                        List.of(
                                step("定义前缀状态", "dp[i][j] 表示两个前缀的最优答案，天然适合从小问题推到大问题。", "状态"),
                                step("按末尾字符分类", "若 s[i-1] == t[j-1]，就从左上角加一；否则从上方和左方取最大值。", "转移"),
                                step("按行或按列推进", "依赖关系只来自左、上、左上，因此逐行或逐列遍历都可行。", "遍历")),
                        List.of(
                                caseStudy("代码 diff 工具", "LCS 是许多文本对比工具寻找公共保留部分的基础思路。"),
                                caseStudy("版本合并冲突分析", "比较两个修改序列时，公共子序列能够帮助识别稳定内容。")),
                        code("Python", "LCS 长度模板", """
                                def lcs_length(s, t):
                                    n, m = len(s), len(t)
                                    dp = [[0] * (m + 1) for _ in range(n + 1)]
                                    for i in range(1, n + 1):
                                        for j in range(1, m + 1):
                                            if s[i - 1] == t[j - 1]:
                                                dp[i][j] = dp[i - 1][j - 1] + 1
                                            else:
                                                dp[i][j] = max(dp[i - 1][j], dp[i][j - 1])
                                    return dp[n][m]
                                """,
                                "下标 i-1、j-1 对应当前参与比较的字符。",
                                "如果要恢复序列内容，可以从 dp 表逆推。"),
                        false,
                        "",
                        "",
                        "",
                        "cyan",
                        40),
                article(
                        DYNAMIC_PROGRAMMING,
                        "algo-interval-dp",
                        "区间动态规划",
                        "Interval Dynamic Programming",
                        "区间模型",
                        "区间 DP 把状态定义在连续区间上，例如 dp[l][r] 表示区间 [l, r] 内的最优值。它特别适合括号合并、回文串、石子合并等问题，因为这些题目的决策天然围绕区间切分展开。",
                        "常见区间 DP 需要枚举区间长度、左端点和切分点，时间复杂度多为 O(n^3)，空间复杂度为 O(n^2)。若状态转移能减少切分枚举，复杂度还可以进一步优化。", 
                        "8 min read",
                        List.of("区间 DP", "括号问题", "石子合并"),
                        List.of("适合处理连续子串、区间合并、区间博弈、回文处理等场景。", "当状态必须同时依赖左右边界，且内部还能拆成更小区间时，应优先考虑区间 DP。"),
                        List.of(
                                step("定义区间状态", "明确 dp[l][r] 代表该区间上的最优代价、最大收益或可行性。", "状态"),
                                step("按区间长度递增遍历", "确保求大区间之前，小区间已经全部计算完成。", "顺序"),
                                step("枚举切分点或边界动作", "根据题意从 [l, k] 和 [k+1, r] 合并，或从两端做选择。", "转移")),
                        List.of(
                                caseStudy("最长回文子序列", "dp[l][r] 依赖去掉一端或去掉两端后的更小区间。"),
                                caseStudy("石子合并", "每次选择一个切分点，把左右子区间的最优代价合并起来。")),
                        code("Python", "区间 DP：最长回文子序列", """
                                def longest_palindrome_subseq(s):
                                    n = len(s)
                                    dp = [[0] * n for _ in range(n)]
                                    for i in range(n - 1, -1, -1):
                                        dp[i][i] = 1
                                        for j in range(i + 1, n):
                                            if s[i] == s[j]:
                                                dp[i][j] = dp[i + 1][j - 1] + 2
                                            else:
                                                dp[i][j] = max(dp[i + 1][j], dp[i][j - 1])
                                    return dp[0][n - 1]
                                """,
                                "区间 DP 常见遍历顺序是长度递增或左端点递减。",
                                "先想清楚大区间依赖哪些更小区间。"),
                        false,
                        "",
                        "",
                        "",
                        "amber",
                        50),
                article(
                        DYNAMIC_PROGRAMMING,
                        "algo-tree-dp",
                        "树形动态规划",
                        "Tree Dynamic Programming",
                        "树上状态",
                        "树形 DP 在树的每个节点上定义状态，再把子树信息向父节点汇总。由于树天然无环，很多全局最优问题都能拆成“当前节点选或不选、当前子树如何贡献答案”的局部决策。",
                        "时间复杂度通常为 O(n * state_size)，因为每个节点只遍历一次或有限次；空间复杂度主要由递归栈和状态数组构成，常见为 O(n)。", 
                        "8 min read",
                        List.of("树形 DP", "树上背包", "递归"),
                        List.of("适合树上选择、最大独立集、路径和、树上资源分配等问题。", "当图结构无环且决策受父子关系影响时，树形 DP 是高频模型。"),
                        List.of(
                                step("确定节点状态含义", "例如 dp[node][0/1] 可以表示当前节点不选/选时子树的最优值。", "状态"),
                                step("后序遍历子树", "先求出所有子节点状态，再在当前节点完成合并。", "遍历"),
                                step("根据父子约束转移", "如果当前节点被选，子节点可能就不能被选；若当前不选，则子节点可以自由取最优。", "转移")),
                        List.of(
                                caseStudy("打家劫舍 III", "二叉树版打家劫舍就是典型的树形 DP：节点选与不选的状态互相制约。"),
                                caseStudy("公司聚会最大快乐值", "员工参加或不参加会影响直属下属是否能参加，天然构成树形 DP。")),
                        code("Python", "树形 DP：打家劫舍 III", """
                                def rob(root):
                                    def dfs(node):
                                        if not node:
                                            return (0, 0)
                                        left_not, left_yes = dfs(node.left)
                                        right_not, right_yes = dfs(node.right)
                                        not_take = max(left_not, left_yes) + max(right_not, right_yes)
                                        take = node.val + left_not + right_not
                                        return (not_take, take)

                                    not_take, take = dfs(root)
                                    return max(not_take, take)
                                """,
                                "返回元组能同时表示节点选与不选两种状态。",
                                "树形 DP 通常遵循后序遍历。"),
                        false,
                        "",
                        "",
                        "",
                        "emerald",
                        60),
                article(
                        DYNAMIC_PROGRAMMING,
                        "algo-state-compression-dp",
                        "状态压缩动态规划",
                        "State Compression Dynamic Programming",
                        "位运算 DP",
                        "状态压缩 DP 用二进制位表示“哪些元素已经被选过”或“当前覆盖了哪些列”，特别适合规模不大但组合状态很多的问题。它把集合状态映射到整数上，从而能高效枚举子集和转移。",
                        "若状态总数为 2^n，则时间复杂度通常在 O(n * 2^n) 到 O(n^2 * 2^n) 之间，空间复杂度为 O(2^n)。虽然指数级，但在 n 不大时仍然是解决 TSP、匹配、棋盘覆盖类问题的标准办法。", 
                        "9 min read",
                        List.of("状态压缩 DP", "位运算", "子集 DP"),
                        List.of("适合元素数量不大但需要枚举选取集合的旅行商、任务分配、覆盖问题。", "当题目规模在 15 到 20 左右且状态带有“已选集合”特征时，状态压缩 DP 非常常见。"),
                        List.of(
                                step("用位掩码表示集合", "例如 mask 的第 i 位为 1 表示第 i 个元素已经被选择。", "编码"),
                                step("定义集合状态 DP", "dp[mask] 或 dp[mask][last] 表示到达当前集合状态时的最优值。", "状态"),
                                step("枚举新增元素转移", "通过位运算判断某元素是否已选，再尝试把它加入当前集合。", "转移")),
                        List.of(
                                caseStudy("旅行商问题 TSP", "dp[mask][i] 表示访问了 mask 集合并停在 i 的最短路。"),
                                caseStudy("棋盘覆盖和课程安排", "当每行或每列状态可以用位图表示时，状态压缩能显著简化转移。")),
                        code("Python", "状态压缩 DP：最短哈密顿路径骨架", """
                                def tsp(dist):
                                    n = len(dist)
                                    inf = float('inf')
                                    dp = [[inf] * n for _ in range(1 << n)]
                                    dp[1][0] = 0
                                    for mask in range(1 << n):
                                        for last in range(n):
                                            if dp[mask][last] == inf:
                                                continue
                                            for nxt in range(n):
                                                if mask & (1 << nxt):
                                                    continue
                                                new_mask = mask | (1 << nxt)
                                                dp[new_mask][nxt] = min(
                                                    dp[new_mask][nxt],
                                                    dp[mask][last] + dist[last][nxt]
                                                )
                                    full = (1 << n) - 1
                                    return min(dp[full][i] + dist[i][0] for i in range(n))
                                """,
                                "位运算能极大提升集合状态的表示效率。",
                                "关注规模上限，超过 20 往往需要换模型。"),
                        false,
                        "",
                        "",
                        "",
                        "cyan",
                        70),
                article(
                        DYNAMIC_PROGRAMMING,
                        "algo-digit-dp",
                        "数位动态规划",
                        "Digit Dynamic Programming",
                        "数位约束",
                        "数位 DP 用“从高位到低位逐位填数”的方式统计满足某类数字性质的整数个数。它的关键是把“前缀是否贴合上界”“前面是否已经开始构造数字”“已经累计了哪些特征”编码进状态。",
                        "复杂度通常为 O(number_of_digits * state_count * digit_choices)，其中 digit_choices 通常是 10。空间复杂度取决于记忆化状态数量，常见为 O(number_of_digits * state_count)。", 
                        "9 min read",
                        List.of("数位 DP", "记忆化搜索", "计数问题"),
                        List.of("适合统计区间内满足数位条件的整数个数，例如不含某个数字、各位和受限、相邻位约束等。", "当题目给出 [0, n] 或 [l, r] 区间并要求按十进制位性质计数时，应优先考虑数位 DP。"),
                        List.of(
                                step("写按位 DFS", "从最高位递归到最低位，每次尝试当前位可以填哪些数字。", "递归"),
                                step("设计限制状态", "常见状态包含 pos、tight、started，以及题目自定义统计量。", "状态"),
                                step("用前缀差处理区间", "统计 [0, r] 与 [0, l-1] 的结果相减，就得到 [l, r] 的答案。", "区间")),
                        List.of(
                                caseStudy("统计不含数字 4 的数", "每一位递归时跳过 4，就能统计区间内所有合法数字。"),
                                caseStudy("统计各位数字和等于目标值的数", "把当前位和累加进状态，即可完成带约束的计数。")),
                        code("Python", "数位 DP：统计不含数字 4 的数量", """
                                from functools import lru_cache

                                def count_without_four(n):
                                    digits = list(map(int, str(n)))

                                    @lru_cache(None)
                                    def dfs(pos, tight, started):
                                        if pos == len(digits):
                                            return 1
                                        limit = digits[pos] if tight else 9
                                        total = 0
                                        for digit in range(limit + 1):
                                            if digit == 4:
                                                continue
                                            total += dfs(
                                                pos + 1,
                                                tight and digit == limit,
                                                started or digit != 0
                                            )
                                        return total

                                    return dfs(0, True, False)
                                """,
                                "tight 表示当前前缀是否仍贴着上界。",
                                "区间统计通常写成 solve(r) - solve(l - 1)。"),
                        true,
                        "计数进阶",
                        "按位构造答案空间",
                        "面对“区间内有多少个数字满足某种十进制性质”时，数位 DP 基本就是最稳的系统解法。",
                        "amber",
                        80)
        );
    }

    private List<KnowledgeBaseService.AdminArticleInput> greedyArticles() {
        return List.of(
                article(
                        GREEDY_ALGORITHMS,
                        "algo-activity-selection",
                        "活动选择问题",
                        "Activity Selection",
                        "贪心经典",
                        "活动选择问题要求在若干区间中选出最多互不重叠的活动。其核心贪心策略是按结束时间从早到晚排序，并优先选择最早结束的活动，因为它为后续留下的可用时间最多。",
                        "排序需要 O(n log n) 时间，扫描选择活动需要 O(n) 时间，因此总复杂度为 O(n log n)。空间复杂度取决于排序实现，通常为 O(1) 到 O(n)。", 
                        "7 min read",
                        List.of("贪心", "区间调度", "排序"),
                        List.of("适合最多不重叠区间、会议室安排、机器时间片分配等场景。", "当目标是“保留尽可能多的后续机会”时，按结束时间贪心很常见。"),
                        List.of(
                                step("按结束时间排序", "把最早结束的活动放在前面，为后续选择留出最多时间。", "排序"),
                                step("记录当前已选末尾", "选中一个活动后，用它的结束时间作为后续活动的起点门槛。", "状态"),
                                step("顺序扫描并挑选", "若下一个活动起始时间不早于当前结束时间，就把它加入答案。", "选择")),
                        List.of(
                                caseStudy("会议室安排", "安排最多场会议时，优先选最早结束的会议能保留更多空闲时段。"),
                                caseStudy("课程时间冲突消解", "在时间冲突的课程中保留尽量多门时，也可以转成活动选择。")),
                        code("Python", "活动选择贪心", """
                                def max_non_overlapping(intervals):
                                    intervals.sort(key=lambda item: item[1])
                                    count = 0
                                    current_end = float('-inf')
                                    for start, end in intervals:
                                        if start >= current_end:
                                            count += 1
                                            current_end = end
                                    return count
                                """,
                                "结束时间越早，后面能接上的活动越多。",
                                "这是贪心正确性最好解释的一类题。"),
                        false,
                        "",
                        "",
                        "",
                        "emerald",
                        10),
                article(
                        GREEDY_ALGORITHMS,
                        "algo-interval-cover",
                        "区间覆盖",
                        "Interval Cover",
                        "覆盖贪心",
                        "区间覆盖问题通常要求用尽量少的区间覆盖目标线段或点集。常见贪心策略是在当前可覆盖范围内，始终选择能把右边界延伸得最远的区间，这样才能保证后续使用区间的数量最少。",
                        "若先按左端点排序，整体复杂度为 O(n log n)；随后一次线性扫描即可完成覆盖判断与区间选择。空间复杂度取决于排序实现。", 
                        "7 min read",
                        List.of("区间覆盖", "贪心", "最少区间"),
                        List.of("适合最少视频拼接、最少雷达覆盖、最少喷灌器覆盖等问题。", "只要目标是用最少段数完成连续覆盖，延伸最远策略就值得优先尝试。"),
                        List.of(
                                step("按起点排序", "先把区间按起点排序，便于在线性扫描中处理所有候选。", "排序"),
                                step("在可接区间里选最远右端点", "只要区间起点不超过当前覆盖边界，就把其右端点纳入候选最大值。", "延伸"),
                                step("完成一次覆盖跳跃", "当扫描到无法继续接的区间时，必须消耗一个选择并把边界推进到最远右端点。", "更新")),
                        List.of(
                                caseStudy("视频拼接", "为了覆盖完整赛事时间轴，每次都应选能把右边界推得最远的片段。"),
                                caseStudy("喷灌器覆盖草坪", "当每个喷头覆盖一段区间时，目标就是用最少喷头覆盖完整区间。")),
                        code("Python", "最少区间覆盖", """
                                def min_cover(intervals, target_end):
                                    intervals.sort()
                                    answer = 0
                                    i = 0
                                    current_end = 0
                                    farthest = 0
                                    while current_end < target_end:
                                        while i < len(intervals) and intervals[i][0] <= current_end:
                                            farthest = max(farthest, intervals[i][1])
                                            i += 1
                                        if farthest == current_end:
                                            return -1
                                        answer += 1
                                        current_end = farthest
                                    return answer
                                """,
                                "每次都要在能接上的区间里选右端点最远者。",
                                "如果最远右端点没有推进，说明覆盖失败。"),
                        false,
                        "",
                        "",
                        "",
                        "cyan",
                        20),
                article(
                        GREEDY_ALGORITHMS,
                        "algo-huffman-coding",
                        "哈夫曼编码",
                        "Huffman Coding",
                        "贪心树",
                        "哈夫曼编码通过反复合并权值最小的两个节点，构造一棵带权路径长度最小的前缀编码树。它体现了贪心算法里非常重要的一类思想：先把最小代价的局部结构固定下来，再把它压缩成新的整体继续处理。",
                        "若使用优先队列维护权值最小节点，时间复杂度为 O(n log n)，因为会进行 n-1 次合并，每次都涉及堆操作。空间复杂度为 O(n)。", 
                        "8 min read",
                        List.of("哈夫曼编码", "贪心", "优先队列"),
                        List.of("适合无损压缩、字符编码优化、最优合并果子等需要最小化带权路径长度的场景。", "凡是“不断合并两个最小代价对象”的问题，都值得联想到哈夫曼思想。"),
                        List.of(
                                step("把频次放入小根堆", "初始化时，每个字符或权值单独作为一个节点入堆。", "初始化"),
                                step("反复取出最小两个节点", "把它们合并成新节点，其权值为二者之和。", "合并"),
                                step("把新节点重新入堆", "直到堆里只剩一个根节点，整棵哈夫曼树构建完成。", "收敛")),
                        List.of(
                                caseStudy("文本压缩", "高频字符得到更短编码，低频字符得到更长编码，总体平均编码长度更短。"),
                                caseStudy("最优合并果子", "每次合并最小两堆果子能保证总合并代价最小。")),
                        code("Python", "哈夫曼合并代价", """
                                import heapq

                                def huffman_cost(weights):
                                    heap = weights[:]
                                    heapq.heapify(heap)
                                    total = 0
                                    while len(heap) > 1:
                                        a = heapq.heappop(heap)
                                        b = heapq.heappop(heap)
                                        merged = a + b
                                        total += merged
                                        heapq.heappush(heap, merged)
                                    return total
                                """,
                                "小根堆负责持续给出当前最小的两个候选。",
                                "最优合并代价和哈夫曼编码是同一个贪心模型。"),
                        false,
                        "",
                        "",
                        "",
                        "amber",
                        30),
                article(
                        GREEDY_ALGORITHMS,
                        "algo-fractional-knapsack",
                        "分数背包",
                        "Fractional Knapsack",
                        "比值贪心",
                        "分数背包允许把物品切分后装入背包，因此最优策略是按单位重量价值从高到低排序，优先装性价比最高的部分。它说明当决策可分割时，很多组合优化问题会从 DP 模型退化为简单贪心。",
                        "排序复杂度为 O(n log n)，扫描装入过程为 O(n)，总复杂度 O(n log n)。空间复杂度主要来自排序。", 
                        "6 min read",
                        List.of("分数背包", "单位价值", "贪心"),
                        List.of("适合原料可切分、资源可按比例配置、连续投资分配等场景。", "如果每个对象可以取一部分，而不是只能整件取舍，应优先考虑单位价值贪心。"),
                        List.of(
                                step("计算单位价值", "先求每件物品的 value / weight，用它衡量装入收益。", "评估"),
                                step("按单位价值排序", "把性价比最高的物品排在前面，保证优先装入。", "排序"),
                                step("能装多少装多少", "若剩余容量装不下一整件，就只取能放得下的那一部分。", "装入")),
                        List.of(
                                caseStudy("资金按比例投资", "每个项目都允许部分投入时，单位收益率最高的项目应先分配资金。"),
                                caseStudy("原料切割配送", "当原料可以拆分发货时，按单位利润装载能获得最大总收益。")),
                        code("Python", "分数背包", """
                                def fractional_knapsack(capacity, items):
                                    # items: (weight, value)
                                    items.sort(key=lambda item: item[1] / item[0], reverse=True)
                                    total = 0.0
                                    for weight, value in items:
                                        if capacity == 0:
                                            break
                                        take = min(capacity, weight)
                                        total += take * value / weight
                                        capacity -= take
                                    return total
                                """,
                                "可分割性是分数背包能贪心的关键前提。",
                                "0/1 背包不能直接用这个策略。"),
                        false,
                        "",
                        "",
                        "",
                        "cyan",
                        40),
                article(
                        GREEDY_ALGORITHMS,
                        "algo-gas-station",
                        "加油站问题",
                        "Gas Station Greedy",
                        "贪心证明",
                        "加油站问题要求找到一个起点，使得汽车沿环路行驶一圈始终油量不为负。关键贪心结论是：如果从起点 s 走到 i 失败，那么 s 到 i 之间任意点都不可能成为合法起点，因此可以一次性跳过整段失败区间。",
                        "只需一次线性扫描即可完成判断，时间复杂度为 O(n)，空间复杂度为 O(1)。它的高明之处在于利用失败区间的整体排除，把看似多起点尝试降成一次遍历。", 
                        "7 min read",
                        List.of("加油站", "贪心", "线性扫描"),
                        List.of("适合环形路径可行起点判定、累积盈亏平衡点查找等场景。", "当局部失败可以整体排除一段候选时，往往就存在贪心优化空间。"),
                        List.of(
                                step("先判断总量可行性", "若总油量小于总消耗，则无论从哪里出发都不可能绕行一圈。", "可行性"),
                                step("维护当前区间余额", "从当前起点开始累积 gas[i] - cost[i]，若中途变负，说明当前起点失败。", "扫描"),
                                step("失败后直接跳到下一点", "因为失败区间内部都不可能作为起点，所以新起点直接设为 i+1。", "跳过")),
                        List.of(
                                caseStudy("物流环线调度", "总资源足够时，寻找可持续完成整圈配送的发车站点。"),
                                caseStudy("能量循环系统", "当每段产生和消耗能量不同，问题可抽象成环形盈亏平衡点判定。")),
                        code("Python", "加油站贪心", """
                                def can_complete_circuit(gas, cost):
                                    if sum(gas) < sum(cost):
                                        return -1
                                    start = 0
                                    tank = 0
                                    for i in range(len(gas)):
                                        tank += gas[i] - cost[i]
                                        if tank < 0:
                                            start = i + 1
                                            tank = 0
                                    return start
                                """,
                                "总量判断是必要前提。",
                                "一旦 tank 变负，旧起点和中间点都可以排除。"),
                        true,
                        "贪心证明",
                        "学会用失败区间排除候选",
                        "加油站题的价值不只在于线性解法，更在于它展示了如何用一次失败证明整段候选无效。",
                        "emerald",
                        50)
        );
    }

    private List<KnowledgeBaseService.AdminArticleInput> backtrackingArticles() {
        return List.of(
                article(
                        BACKTRACKING,
                        "algo-permutations",
                        "全排列回溯",
                        "Backtracking for Permutations",
                        "回溯基础",
                        "全排列问题的核心是按位置逐步放元素，并在每一层只选择尚未使用的候选。回溯算法通过“做选择 -> 递归 -> 撤销选择”的模板把整棵搜索树遍历出来，是理解搜索型算法的第一步。",
                        "如果共有 n 个元素，全排列总数为 n!，因此时间复杂度至少是 O(n * n!)，额外空间主要来自递归栈、路径数组和访问标记，为 O(n)。", 
                        "7 min read",
                        List.of("回溯", "全排列", "搜索树"),
                        List.of("适合排列生成、路径枚举、顺序安排等需要穷举所有次序的场景。", "当答案由若干位置组成，且每个元素最多使用一次时，全排列回溯最常见。"),
                        List.of(
                                step("维护当前路径", "path 存已经选定的元素，路径长度代表递归深度。", "路径"),
                                step("枚举未使用元素", "每一层只从尚未进入路径的元素中继续选择。", "选择"),
                                step("递归后撤销状态", "回溯返回时恢复 visited 和 path，保证下一条分支不受污染。", "撤销")),
                        List.of(
                                caseStudy("密码穷举顺序生成", "当位置顺序本身决定答案时，全排列天然对应所有可能次序。"),
                                caseStudy("旅行计划排列", "枚举景点访问顺序时，排列搜索是最直接的状态树。")),
                        code("Python", "全排列模板", """
                                def permute(nums):
                                    result = []
                                    path = []
                                    used = [False] * len(nums)

                                    def dfs():
                                        if len(path) == len(nums):
                                            result.append(path[:])
                                            return
                                        for i, value in enumerate(nums):
                                            if used[i]:
                                                continue
                                            used[i] = True
                                            path.append(value)
                                            dfs()
                                            path.pop()
                                            used[i] = False

                                    dfs()
                                    return result
                                """,
                                "回溯模板的核心是做选择和撤销选择成对出现。",
                                "一旦答案数本身是 n! 级别，复杂度就不可能更低。"),
                        false,
                        "",
                        "",
                        "",
                        "emerald",
                        10),
                article(
                        BACKTRACKING,
                        "algo-combinations",
                        "组合枚举回溯",
                        "Backtracking for Combinations",
                        "组合搜索",
                        "组合问题只关心选了哪些元素，不关心它们在路径中的排列顺序，因此常用“起点下标 start”避免重复选择。与全排列相比，组合回溯的关键在于控制搜索范围，让每个元素只在某一层之后出现。",
                        "若要从 n 个元素中选 k 个，答案数量是 C(n, k)，典型时间复杂度为 O(k * C(n, k))。额外空间为 O(k)，主要来自路径和递归栈。", 
                        "6 min read",
                        List.of("组合", "回溯", "剪枝"),
                        List.of("适合子集选择、团队组建、候选方案枚举等只关心成员集合的场景。", "当元素顺序不重要时，应优先用 start 下标去重，避免生成重复排列。"),
                        List.of(
                                step("记录当前起点", "start 表示当前层只能从哪些候选开始选，避免回头选择。", "去重"),
                                step("满足长度即收集答案", "当路径长度达到 k 或满足条件时，复制当前路径。", "收集"),
                                step("用剩余可选数做剪枝", "如果后续元素数量不足以补满答案，就可以提前结束。", "剪枝")),
                        List.of(
                                caseStudy("从 n 个学生中选 k 人", "每组成员顺序无关，属于典型组合问题。"),
                                caseStudy("候选药品方案筛选", "从多个候选中选固定数量进行实验时，也可以用组合枚举。")),
                        code("Python", "组合模板", """
                                def combine(n, k):
                                    result = []
                                    path = []

                                    def dfs(start):
                                        if len(path) == k:
                                            result.append(path[:])
                                            return
                                        for value in range(start, n + 1):
                                            if len(path) + (n - value + 1) < k:
                                                break
                                            path.append(value)
                                            dfs(value + 1)
                                            path.pop()

                                    dfs(1)
                                    return result
                                """,
                                "组合问题靠 start 去重，而不是 visited。",
                                "剪枝条件能显著减少无意义搜索。"),
                        false,
                        "",
                        "",
                        "",
                        "cyan",
                        20),
                article(
                        BACKTRACKING,
                        "algo-n-queens",
                        "N 皇后",
                        "N-Queens",
                        "棋盘搜索",
                        "N 皇后要求在 n×n 棋盘上放置 n 个皇后，使其彼此不在同一行、同一列和同一对角线。它是回溯算法里最经典的约束满足问题之一，体现了如何把“合法性判断”融入递归过程形成强剪枝。",
                        "最坏情况下搜索树规模接近 O(n!)，但通过列、主对角线和副对角线约束剪枝，实际搜索会少很多。额外空间为 O(n)，主要用于记录当前放置状态。", 
                        "8 min read",
                        List.of("N 皇后", "回溯", "约束满足"),
                        List.of("适合棋盘放置、约束排布和需要逐行验证合法性的搜索问题。", "当状态是否可扩展可以被快速判断时，回溯加剪枝的效果会很好。"),
                        List.of(
                                step("按行放置皇后", "每一层递归只处理一行，把一维路径转成二维棋盘状态。", "分层"),
                                step("用集合记录冲突", "列、主对角线 row-col、副对角线 row+col 都可用集合快速判断是否合法。", "判定"),
                                step("放置后递归并撤销", "一旦子树搜索结束，必须把占用状态和棋盘字符恢复。", "回溯")),
                        List.of(
                                caseStudy("约束排班问题", "若多种限制条件都能快速判冲突，N 皇后的思路可以迁移到排班搜索。"),
                                caseStudy("棋盘型搜索训练", "它非常适合作为理解回溯剪枝质量的训练题。")),
                        code("Python", "N 皇后回溯", """
                                def solve_n_queens(n):
                                    result = []
                                    cols = set()
                                    diag1 = set()
                                    diag2 = set()
                                    board = [['.'] * n for _ in range(n)]

                                    def dfs(row):
                                        if row == n:
                                            result.append([''.join(line) for line in board])
                                            return
                                        for col in range(n):
                                            if col in cols or row - col in diag1 or row + col in diag2:
                                                continue
                                            cols.add(col)
                                            diag1.add(row - col)
                                            diag2.add(row + col)
                                            board[row][col] = 'Q'
                                            dfs(row + 1)
                                            board[row][col] = '.'
                                            cols.remove(col)
                                            diag1.remove(row - col)
                                            diag2.remove(row + col)

                                    dfs(0)
                                    return result
                                """,
                                "按行搜索能天然保证行不冲突。",
                                "三类约束集合是高效剪枝的关键。"),
                        true,
                        "剪枝训练",
                        "先把合法性判断做到 O(1)",
                        "N 皇后不是单纯暴搜，它真正训练的是如何把冲突判定压缩成常数时间剪枝。",
                        "amber",
                        30),
                article(
                        BACKTRACKING,
                        "algo-subset-sum-backtracking",
                        "子集和回溯",
                        "Subset Sum Backtracking",
                        "可行性搜索",
                        "子集和问题要求判断是否能从若干数字中选出一个子集，使其和等于目标值。回溯解法的关键在于每个元素都面临“选 / 不选”两种分支，并结合排序、剩余和界限做剪枝。",
                        "最坏情况下需要遍历 2^n 种子集，因此时间复杂度为 O(2^n)。额外空间为 O(n)，用于递归栈和当前路径。", 
                        "7 min read",
                        List.of("子集和", "回溯", "选与不选"),
                        List.of("适合中小规模可行性判定、目标和组合、约束选择问题。", "当 n 不大且题目需要真的恢复选择路径时，回溯比单纯 DP 更直观。"),
                        List.of(
                                step("把问题拆成二叉决策树", "每个元素都面临选择与跳过两种分支。", "分支"),
                                step("记录当前和与剩余潜力", "当前和超过目标，或剩余全部加上仍不够目标时，都可以剪枝。", "剪枝"),
                                step("命中目标立即返回", "若只判断存在性，一旦找到答案就可以尽早终止搜索。", "终止")),
                        List.of(
                                caseStudy("预算组合可行性", "判断若干报价中是否存在一组和恰好等于预算。"),
                                caseStudy("目标配方筛选", "从多种原料中选择部分，使其总量满足特定要求。")),
                        code("Python", "子集和回溯", """
                                def subset_sum(nums, target):
                                    nums.sort(reverse=True)

                                    def dfs(index, current):
                                        if current == target:
                                            return True
                                        if current > target or index == len(nums):
                                            return False
                                        if dfs(index + 1, current + nums[index]):
                                            return True
                                        return dfs(index + 1, current)

                                    return dfs(0, 0)
                                """,
                                "先排序再搜索，通常能更早触发剪枝。",
                                "若只判断存在性，可以在找到答案时立即返回。"),
                        false,
                        "",
                        "",
                        "",
                        "emerald",
                        40),
                article(
                        BACKTRACKING,
                        "algo-sudoku-solver",
                        "数独求解",
                        "Sudoku Solver",
                        "约束搜索",
                        "数独求解把空格逐个填入合法数字，本质上是带强约束的回溯搜索。好的实现不会在每一步都重新扫描整行整列，而是借助行、列、宫三个约束集合快速判断某个数字能否落下。",
                        "最坏情况下搜索空间很大，但由于数独约束非常强，配合合法性集合和优先选择候选最少位置的策略，实际求解通常较快。额外空间主要是记录约束和递归栈。", 
                        "9 min read",
                        List.of("数独", "回溯", "约束传播"),
                        List.of("适合填字游戏、约束求解、排列填充这类需要同时满足多维规则的问题。", "当局部合法性检查可以快速完成时，回溯求解约束系统非常有效。"),
                        List.of(
                                step("预处理现有约束", "先扫描棋盘，把每行、每列、每宫已使用的数字记录下来。", "初始化"),
                                step("选择一个空格尝试填数", "通常优先挑候选最少的空格，可以更快触发剪枝。", "选择"),
                                step("合法则递归，否则撤销", "填入数字后同步更新三类约束，回溯时再恢复。", "回溯")),
                        List.of(
                                caseStudy("逻辑谜题求解", "很多谜题都可转成“逐格填值 + 约束检查”的回溯模型。"),
                                caseStudy("约束传播练习", "数独适合练习如何把规则预处理成 O(1) 合法性判断。")),
                        code("Python", "数独回溯框架", """
                                def solve_sudoku(board):
                                    rows = [set() for _ in range(9)]
                                    cols = [set() for _ in range(9)]
                                    boxes = [set() for _ in range(9)]
                                    empties = []

                                    for r in range(9):
                                        for c in range(9):
                                            value = board[r][c]
                                            if value == '.':
                                                empties.append((r, c))
                                            else:
                                                rows[r].add(value)
                                                cols[c].add(value)
                                                boxes[(r // 3) * 3 + c // 3].add(value)

                                    def dfs(index):
                                        if index == len(empties):
                                            return True
                                        r, c = empties[index]
                                        box = (r // 3) * 3 + c // 3
                                        for digit in map(str, range(1, 10)):
                                            if digit in rows[r] or digit in cols[c] or digit in boxes[box]:
                                                continue
                                            board[r][c] = digit
                                            rows[r].add(digit)
                                            cols[c].add(digit)
                                            boxes[box].add(digit)
                                            if dfs(index + 1):
                                                return True
                                            board[r][c] = '.'
                                            rows[r].remove(digit)
                                            cols[c].remove(digit)
                                            boxes[box].remove(digit)
                                        return False

                                    dfs(0)
                                """,
                                "行、列、宫三类约束集合能把合法性判断压到 O(1)。",
                                "复杂回溯题的性能关键往往不在模板，而在剪枝质量。"),
                        false,
                        "",
                        "",
                        "",
                        "cyan",
                        50)
        );
    }

    private List<KnowledgeBaseService.AdminArticleInput> divideAndConquerArticles() {
        return List.of(
                article(
                        DIVIDE_AND_CONQUER,
                        "algo-quickselect",
                        "快速选择",
                        "Quickselect",
                        "分治选择",
                        "快速选择复用了快速排序的 partition 思想，但只递归进入包含第 k 小元素的那一侧区间，因此不必完成完整排序。它说明很多排序问题其实只需要“把目标元素放到正确位置”，无需处理全部区间。",
                        "平均时间复杂度为 O(n)，因为每轮只递归进入一侧；最坏情况下仍可能退化到 O(n^2)。空间复杂度取决于递归深度，平均 O(log n)。", 
                        "7 min read",
                        List.of("快速选择", "partition", "TopK"),
                        List.of("适合第 k 小、第 k 大、中位数和 Top K 边界定位等问题。", "当只关心顺序统计量而非完整排序时，快速选择通常优于完整快排。"),
                        List.of(
                                step("选择基准并分区", "像快排一样，把数组划分成小于基准和大于基准的两边。", "分区"),
                                step("判断目标落在哪边", "比较基准位置与目标下标，只递归进入有可能包含答案的一侧。", "定位"),
                                step("收敛到单点", "当基准位置恰好等于目标下标时，答案已经找到。", "收敛")),
                        List.of(
                                caseStudy("求数组第 k 大元素", "面试里经常要求比 O(n log n) 排序更快的写法，快速选择正是标准答案。"),
                                caseStudy("中位数估计", "中位数本质上就是顺序统计量，可以直接用快速选择定位。")),
                        code("Python", "快速选择模板", """
                                def quickselect(nums, k):
                                    target = len(nums) - k

                                    def select(left, right):
                                        pivot = nums[right]
                                        store = left
                                        for i in range(left, right):
                                            if nums[i] <= pivot:
                                                nums[store], nums[i] = nums[i], nums[store]
                                                store += 1
                                        nums[store], nums[right] = nums[right], nums[store]
                                        if store == target:
                                            return nums[store]
                                        if store < target:
                                            return select(store + 1, right)
                                        return select(left, store - 1)

                                    return select(0, len(nums) - 1)
                                """,
                                "只进入一侧子区间，是它区别于快速排序的关键。",
                                "求第 k 大时，常把目标转成第 len-k 小。"),
                        false,
                        "",
                        "",
                        "",
                        "emerald",
                        10),
                article(
                        DIVIDE_AND_CONQUER,
                        "algo-inversion-count",
                        "逆序对统计",
                        "Inversion Count by Divide and Conquer",
                        "归并应用",
                        "逆序对统计可以通过归并排序在合并阶段顺手完成：当左半部分当前元素大于右半部分当前元素时，左半部分尚未合并的所有元素都与该右侧元素形成逆序对。它是“分治过程中顺便收集统计量”的经典案例。",
                        "归并拆分与合并的总时间复杂度为 O(n log n)，空间复杂度为 O(n)。相比朴素 O(n^2) 枚举，分治把大量重复比较压缩到了合并过程。", 
                        "7 min read",
                        List.of("逆序对", "归并排序", "分治"),
                        List.of("适合数组有序性衡量、最少交换次数分析和序列混乱度统计。", "很多“局部逆序导致的总代价”问题都能在归并过程中统计。"),
                        List.of(
                                step("递归拆成左右两半", "先分别统计左半和右半内部的逆序对数量。", "拆分"),
                                step("在合并时统计跨区间逆序", "若 left[i] > right[j]，则 left[i:] 都与 right[j] 构成逆序对。", "统计"),
                                step("返回有序数组与计数", "让上层既能继续归并，也能累计子问题答案。", "返回")),
                        List.of(
                                caseStudy("数组混乱度评估", "逆序对数量越多，说明序列离完全有序越远。"),
                                caseStudy("最少相邻交换次数", "把一个序列变成目标序列时，逆序对数量常对应最少相邻交换次数。")),
                        code("Python", "归并统计逆序对", """
                                def count_inversions(nums):
                                    def sort(arr):
                                        if len(arr) <= 1:
                                            return arr, 0
                                        mid = len(arr) // 2
                                        left, left_count = sort(arr[:mid])
                                        right, right_count = sort(arr[mid:])
                                        merged = []
                                        i = j = 0
                                        cross = 0
                                        while i < len(left) and j < len(right):
                                            if left[i] <= right[j]:
                                                merged.append(left[i])
                                                i += 1
                                            else:
                                                merged.append(right[j])
                                                cross += len(left) - i
                                                j += 1
                                        merged.extend(left[i:])
                                        merged.extend(right[j:])
                                        return merged, left_count + right_count + cross

                                    return sort(nums)[1]
                                """,
                                "跨区间逆序只在 merge 阶段统计。",
                                "统计与排序可以在一次分治中同时完成。"),
                        false,
                        "",
                        "",
                        "",
                        "cyan",
                        20),
                article(
                        DIVIDE_AND_CONQUER,
                        "algo-fast-power",
                        "快速幂",
                        "Fast Exponentiation",
                        "指数分治",
                        "快速幂利用指数的二进制拆分，把 a^n 转成更小幂次的组合。当 n 为偶数时，a^n = (a^(n/2))^2；当 n 为奇数时，再额外乘一个 a。它把线性次幂运算压缩到了对数级。", 
                        "每次都把指数折半，因此时间复杂度为 O(log n)，空间复杂度递归写法为 O(log n)，迭代写法可以做到 O(1)。", 
                        "6 min read",
                        List.of("快速幂", "分治", "位运算"),
                        List.of("适合大指数幂运算、模幂运算、矩阵快速幂和组合数学中的快速重复乘法。", "当运算满足结合律时，快速幂思想还能推广到矩阵、函数复合和变换迭代。"),
                        List.of(
                                step("把指数写成二进制", "指数每右移一位，相当于把当前基数平方一次。", "拆分"),
                                step("按位决定是否乘入答案", "若当前最低位是 1，就把当前基数乘到结果里。", "累积"),
                                step("不断平方并折半指数", "循环直到指数归零，完成所有有效位的贡献累积。", "收敛")),
                        List.of(
                                caseStudy("模幂计算", "在大整数取模场景下，快速幂是求 a^b mod m 的标准方法。"),
                                caseStudy("矩阵快速幂", "线性递推数列常把状态转移矩阵做快速幂来降复杂度。")),
                        code("Python", "迭代快速幂", """
                                def fast_pow(base, exponent, mod=None):
                                    result = 1
                                    while exponent > 0:
                                        if exponent & 1:
                                            result *= base
                                            if mod is not None:
                                                result %= mod
                                        base *= base
                                        if mod is not None:
                                            base %= mod
                                        exponent >>= 1
                                    return result
                                """,
                                "指数每次折半，所以复杂度是对数级。",
                                "模运算通常在每次乘法后同步取模。"),
                        false,
                        "",
                        "",
                        "",
                        "amber",
                        30),
                article(
                        DIVIDE_AND_CONQUER,
                        "algo-majority-element",
                        "多数元素分治解法",
                        "Majority Element by Divide and Conquer",
                        "分治统计",
                        "多数元素是出现次数超过一半的元素。分治解法把数组拆成左右两半，各自求出候选多数元素后，再统计两个候选在当前区间内的真实次数，最终返回频次更高者。这个思路体现了“子问题给出候选，父问题负责验证”的分治套路。",
                        "递归深度为 O(log n)，每层统计当前区间的候选次数需要 O(n)，所以总时间复杂度为 O(n log n)。空间复杂度主要来自递归栈，为 O(log n)。", 
                        "6 min read",
                        List.of("多数元素", "分治", "候选验证"),
                        List.of("适合主元素、众数候选和需要从子问题候选中做全局验证的场景。", "当子问题很容易产出候选，但候选是否有效还需在大问题范围内确认时，可考虑此套路。"),
                        List.of(
                                step("递归到单元素区间", "长度为 1 的区间，多数元素显然就是它自己。", "基线"),
                                step("分别求左右候选", "左右子数组各自返回一个候选多数元素。", "候选"),
                                step("在当前区间计数验证", "若左右候选不同，就统计它们在当前区间的出现次数，返回更大者。", "验证")),
                        List.of(
                                caseStudy("日志主来源识别", "在大区间里判断哪个来源占据绝对多数时，可借鉴候选合并思路。"),
                                caseStudy("投票数据分析", "若要在分段统计后汇总全局多数候选，分治验证思路很直观。")),
                        code("Python", "多数元素分治", """
                                def majority_element(nums):
                                    def solve(left, right):
                                        if left == right:
                                            return nums[left]
                                        mid = (left + right) // 2
                                        left_major = solve(left, mid)
                                        right_major = solve(mid + 1, right)
                                        if left_major == right_major:
                                            return left_major
                                        left_count = sum(1 for i in range(left, right + 1) if nums[i] == left_major)
                                        right_count = sum(1 for i in range(left, right + 1) if nums[i] == right_major)
                                        return left_major if left_count > right_count else right_major

                                    return solve(0, len(nums) - 1)
                                """,
                                "子问题给的是候选，不是最终结论。",
                                "同类问题里，Boyer-Moore 投票法往往还能进一步降复杂度。"),
                        false,
                        "",
                        "",
                        "",
                        "emerald",
                        40)
        );
    }

    private List<KnowledgeBaseService.AdminArticleInput> graphArticles() {
        return List.of(
                article(
                        GRAPH_ALGORITHMS,
                        "algo-bfs-shortest-path",
                        "无权图最短路径 BFS",
                        "BFS Shortest Path on Unweighted Graph",
                        "图论入门",
                        "在无权图里，边的代价完全相同，因此按层扩展的 BFS 天然保证第一次到达某个节点时路径长度最短。这个结论来自队列维护的层次顺序，而不是 BFS 名字本身。",
                        "若图有 n 个点、m 条边，时间复杂度为 O(n + m)，因为每个点最多入队一次、每条边最多被检查两次。空间复杂度为 O(n)，用于队列和 visited / dist 数组。", 
                        "7 min read",
                        List.of("BFS", "最短路径", "无权图"),
                        List.of("适合迷宫最短步数、社交图最少关系层数、棋盘移动最少次数等无权图问题。", "只要每次移动成本都相同，就应优先考虑 BFS 而不是 Dijkstra。"),
                        List.of(
                                step("初始化起点入队", "把起点放入队列，并把 dist[start] 设为 0。", "起点"),
                                step("按层扩展相邻节点", "弹出当前节点后，把所有未访问的邻居入队，并更新距离为 dist[cur] + 1。", "扩展"),
                                step("首次到达即最短", "因为队列保证先处理距离更短的状态，所以第一次到达目标节点时就可以返回。", "结论")),
                        List.of(
                                caseStudy("迷宫最少步数", "网格每走一步代价相同，本质上就是无权图最短路径。"),
                                caseStudy("社交网络六度关系", "求两个人之间最少需要经过多少层好友，可以用 BFS 层次扩展。")),
                        code("Python", "无权图 BFS 最短路", """
                                from collections import deque

                                def bfs_shortest_path(graph, start):
                                    dist = {start: 0}
                                    queue = deque([start])
                                    while queue:
                                        node = queue.popleft()
                                        for nxt in graph[node]:
                                            if nxt in dist:
                                                continue
                                            dist[nxt] = dist[node] + 1
                                            queue.append(nxt)
                                    return dist
                                """,
                                "dist 是否存在既能代表访问标记，也能记录最短距离。",
                                "无权图最短路不需要优先队列。"),
                        false,
                        "",
                        "",
                        "",
                        "emerald",
                        10),
                article(
                        GRAPH_ALGORITHMS,
                        "algo-dijkstra",
                        "Dijkstra 最短路径",
                        "Dijkstra Shortest Path",
                        "最短路核心",
                        "Dijkstra 适用于边权非负的图。它每次从尚未确定的节点中选出当前距离最小者，并以此更新邻居，依赖的核心事实是：非负边权保证已经取出的最小距离不会再被后续路径改写。",
                        "若使用邻接表加优先队列，时间复杂度通常为 O((n + m) log n)。空间复杂度为 O(n + m)，用于图存储、距离数组和堆。", 
                        "8 min read",
                        List.of("Dijkstra", "最短路径", "优先队列"),
                        List.of("适合导航、网络路由、任务流转等边权非负的单源最短路问题。", "当每条边代表非负代价或耗时，Dijkstra 往往是图论首选算法。"),
                        List.of(
                                step("初始化距离数组", "起点距离设为 0，其余节点设为无穷大。", "初始化"),
                                step("用堆取当前最短节点", "每轮从小根堆弹出距离最小的节点作为当前确定点。", "选点"),
                                step("松弛相邻边", "若经过当前节点能让邻居距离更短，就更新 dist 并重新入堆。", "松弛")),
                        List.of(
                                caseStudy("地图导航", "道路长度或时间通常非负，完全符合 Dijkstra 的应用前提。"),
                                caseStudy("服务链延迟估计", "在非负延迟网络里，从源服务到其他节点的最短延迟可用 Dijkstra 计算。")),
                        code("Python", "Dijkstra 模板", """
                                import heapq

                                def dijkstra(graph, start):
                                    dist = {node: float('inf') for node in graph}
                                    dist[start] = 0
                                    heap = [(0, start)]
                                    while heap:
                                        current_dist, node = heapq.heappop(heap)
                                        if current_dist > dist[node]:
                                            continue
                                        for nxt, weight in graph[node]:
                                            new_dist = current_dist + weight
                                            if new_dist < dist[nxt]:
                                                dist[nxt] = new_dist
                                                heapq.heappush(heap, (new_dist, nxt))
                                    return dist
                                """,
                                "重复入堆是正常现象，弹出时要判断是否过期。",
                                "一旦存在负权边，就不能直接使用 Dijkstra。"),
                        true,
                        "图论主力",
                        "先判断边权是否非负",
                        "做最短路题时，第一反应应当是先看图是否无权、非负权还是存在负权边，这直接决定算法选型。",
                        "cyan",
                        20),
                article(
                        GRAPH_ALGORITHMS,
                        "algo-floyd-warshall",
                        "Floyd-Warshall 全源最短路",
                        "Floyd-Warshall",
                        "全源最短路",
                        "Floyd-Warshall 用三重循环逐步尝试把每个节点 k 当作中转点，更新任意 i 到 j 的最短距离。它本质上是一种动态规划：dist[i][j] 在考虑完前 k 个中转点后能达到的最优值。",
                        "时间复杂度为 O(n^3)，空间复杂度为 O(n^2)。因此它更适合点数不大但需要任意两点最短路的稠密图。", 
                        "7 min read",
                        List.of("Floyd", "全源最短路", "动态规划"),
                        List.of("适合点数较小的全源最短路、传递闭包、任意两点关系分析。", "当要回答大量任意两点查询时，先做 Floyd 预处理通常更划算。"),
                        List.of(
                                step("初始化邻接矩阵", "已知边权写入矩阵，自己到自己的距离设为 0，不连通设为无穷大。", "初始化"),
                                step("逐个开放中转点", "外层循环枚举中转点 k，表示当前允许路径经过的新增节点。", "开放"),
                                step("尝试更新所有点对", "若 dist[i][k] + dist[k][j] 更短，就更新 dist[i][j]。", "更新")),
                        List.of(
                                caseStudy("城市间任意最短路查询", "城市数量不大但查询次数很多时，Floyd 可一次预处理后快速回答。"),
                                caseStudy("传递闭包判定", "把距离更新逻辑换成布尔逻辑，就能得到任意点对可达性。")),
                        code("Python", "Floyd-Warshall 模板", """
                                def floyd(dist):
                                    n = len(dist)
                                    for k in range(n):
                                        for i in range(n):
                                            for j in range(n):
                                                dist[i][j] = min(dist[i][j], dist[i][k] + dist[k][j])
                                    return dist
                                """,
                                "外层必须先枚举中转点 k，才能满足状态依赖顺序。",
                                "适合点数小、查询多的场景。"),
                        false,
                        "",
                        "",
                        "",
                        "amber",
                        30),
                article(
                        GRAPH_ALGORITHMS,
                        "algo-kruskal",
                        "Kruskal 最小生成树",
                        "Kruskal Minimum Spanning Tree",
                        "最小生成树",
                        "Kruskal 先按边权从小到大排序，再依次尝试加入边；若一条边连接的是两个不同连通块，就把它选入生成树。它把 MST 问题转成“从便宜边开始，避免成环”的贪心过程，并查集是它的标准配套结构。",
                        "排序边的复杂度为 O(m log m)，之后每条边的并查集操作接近 O(1) 均摊，因此总复杂度为 O(m log m)。空间复杂度为 O(n + m)。", 
                        "8 min read",
                        List.of("Kruskal", "最小生成树", "并查集"),
                        List.of("适合边集天然可枚举、稀疏图或需要按边权贪心构造 MST 的场景。", "当图边很多但点数适中时，Kruskal 往往实现更直接。"),
                        List.of(
                                step("按边权排序", "让最便宜的边优先进入候选。", "排序"),
                                step("用并查集判环", "若边的两个端点属于不同集合，说明加入后不会形成环。", "判环"),
                                step("累计边权直到选满 n-1 条边", "生成树有且仅有 n-1 条边，达到该数量就可结束。", "收敛")),
                        List.of(
                                caseStudy("网络布线最省成本", "设备之间布线成本不同，Kruskal 可以求得最小总连接成本。"),
                                caseStudy("道路修建规划", "在保证所有村庄连通的前提下，优先修建更便宜的道路。")),
                        code("Python", "Kruskal 模板", """
                                def kruskal(n, edges, union_find):
                                    edges.sort(key=lambda item: item[2])
                                    mst_weight = 0
                                    used_edges = 0
                                    for u, v, weight in edges:
                                        if union_find.union(u, v):
                                            mst_weight += weight
                                            used_edges += 1
                                            if used_edges == n - 1:
                                                break
                                    return mst_weight if used_edges == n - 1 else -1
                                """,
                                "并查集判环是 Kruskal 的关键基础设施。",
                                "稀疏图里，Kruskal 往往比 Prim 更容易写。"),
                        false,
                        "",
                        "",
                        "",
                        "emerald",
                        40),
                article(
                        GRAPH_ALGORITHMS,
                        "algo-prim",
                        "Prim 最小生成树",
                        "Prim Minimum Spanning Tree",
                        "最小生成树",
                        "Prim 从某个起点开始，不断把与当前生成树相连且边权最小的边加入树中。它像是“树上的 Dijkstra”：每次都从边界上挑最便宜的一条扩展新节点。",
                        "若使用邻接表加优先队列，时间复杂度通常为 O(m log n)；若是邻接矩阵实现，则为 O(n^2)。空间复杂度为 O(n + m)。", 
                        "7 min read",
                        List.of("Prim", "最小生成树", "优先队列"),
                        List.of("适合稠密图、邻接矩阵图以及从一个起点逐步扩展的最小连接问题。", "当图较稠密或已是邻接矩阵表示时，Prim 往往更顺手。"),
                        List.of(
                                step("任选起点入树", "先把一个节点加入生成树，并把它相邻边加入候选堆。", "起点"),
                                step("取最小边扩展新节点", "每次弹出连接生成树与树外节点的最小边。", "扩展"),
                                step("跳过已在树中的节点", "若堆顶边指向的节点已经被加入生成树，就直接丢弃。", "去重")),
                        List.of(
                                caseStudy("园区网络建设", "从一个核心交换机出发，逐步以最小成本把其他节点接入网络。"),
                                caseStudy("地图区域连通", "在地图点数不多但边连接密集时，Prim 的邻接矩阵实现很合适。")),
                        code("Python", "Prim 模板", """
                                import heapq

                                def prim(graph, start=0):
                                    visited = set()
                                    heap = [(0, start)]
                                    total = 0
                                    while heap:
                                        weight, node = heapq.heappop(heap)
                                        if node in visited:
                                            continue
                                        visited.add(node)
                                        total += weight
                                        for nxt, edge_weight in graph[node]:
                                            if nxt not in visited:
                                                heapq.heappush(heap, (edge_weight, nxt))
                                    return total if len(visited) == len(graph) else -1
                                """,
                                "Prim 维护的是“树到树外”的最小边界。",
                                "它扩展的是节点，不是像 Kruskal 那样直接处理全局边集。"),
                        false,
                        "",
                        "",
                        "",
                        "cyan",
                        50),
                article(
                        GRAPH_ALGORITHMS,
                        "algo-topological-sort",
                        "拓扑排序",
                        "Topological Sort",
                        "DAG 顺序",
                        "拓扑排序用于有向无环图，输出一种满足所有依赖方向的线性顺序。只要图中存在环，就无法得到合法拓扑序，因此它既能求依赖顺序，也能顺便判环。",
                        "无论是 Kahn 入度算法还是 DFS 后序写法，时间复杂度都为 O(n + m)，因为每个节点和每条边都只处理常数次。空间复杂度为 O(n + m)。", 
                        "7 min read",
                        List.of("拓扑排序", "DAG", "入度"),
                        List.of("适合课程安排、任务依赖、编译顺序和流水线构建等有先后约束的问题。", "当题目明确存在“必须先做 A 再做 B”的依赖时，应优先判断是否是 DAG。"),
                        List.of(
                                step("统计每个节点入度", "入度表示当前仍有多少前置依赖没有完成。", "统计"),
                                step("把入度为 0 的节点入队", "这些节点当前已经没有前置约束，可以立即输出。", "入队"),
                                step("弹出节点并削减后继入度", "若某个后继节点入度减到 0，就把它加入队列。", "更新")),
                        List.of(
                                caseStudy("课程安排", "一门课程的先修课都满足后，才能把它加入学习顺序。"),
                                caseStudy("构建系统编译依赖", "模块依赖关系若成环，就意味着构建顺序无法完成。")),
                        code("Python", "Kahn 拓扑排序", """
                                from collections import deque

                                def topo_sort(graph, indegree):
                                    queue = deque([node for node, degree in indegree.items() if degree == 0])
                                    order = []
                                    while queue:
                                        node = queue.popleft()
                                        order.append(node)
                                        for nxt in graph[node]:
                                            indegree[nxt] -= 1
                                            if indegree[nxt] == 0:
                                                queue.append(nxt)
                                    return order
                                """,
                                "输出节点数少于总节点数时，通常说明图中存在环。",
                                "拓扑排序的本质是不断释放当前没有依赖的节点。"),
                        true,
                        "依赖管理",
                        "先看图里有没有环",
                        "课程安排、构建系统、工作流编排，本质上都在求一条满足依赖关系的 DAG 线性序。",
                        "amber",
                        60)
        );
    }

    private List<KnowledgeBaseService.AdminArticleInput> stringArticles() {
        return List.of(
                article(
                        STRING_ALGORITHMS,
                        "algo-kmp",
                        "KMP 字符串匹配",
                        "Knuth-Morris-Pratt",
                        "字符串经典",
                        "KMP 通过为模式串预处理失配函数 next / lps，在匹配失败时直接跳到下一个仍可能匹配的位置，而不必回退主串指针。它本质上是在模式串内部复用“前缀 = 后缀”的信息，避免重复比较。",
                        "构建 next 数组时间复杂度为 O(m)，匹配过程为 O(n)，总复杂度为 O(n + m)。空间复杂度为 O(m)，用于保存模式串的失配信息。", 
                        "8 min read",
                        List.of("KMP", "字符串匹配", "失配函数"),
                        List.of("适合大文本中的单模式串精确匹配、DNA 片段检索、日志关键字扫描等场景。", "当需要在线性时间完成模式匹配时，KMP 是最经典的确定性算法之一。"),
                        List.of(
                                step("预处理最长相等前后缀", "为模式串每个位置计算 lps，记录失配后模式串应该跳到哪里。", "预处理"),
                                step("同步扫描主串和模式串", "字符相等则双指针同时右移，不相等则根据 lps 回退模式串。", "匹配"),
                                step("命中后继续复用状态", "若需要找全部匹配位置，命中一次后仍可按 lps 继续匹配。", "复用")),
                        List.of(
                                caseStudy("代码编辑器查找", "在长文本中查找固定关键字时，KMP 能避免大量重复回退。"),
                                caseStudy("DNA 序列搜索", "模式串较长且需要确定性线性匹配时，KMP 十分适合。")),
                        code("Python", "KMP 模板", """
                                def build_lps(pattern):
                                    lps = [0] * len(pattern)
                                    length = 0
                                    for i in range(1, len(pattern)):
                                        while length > 0 and pattern[i] != pattern[length]:
                                            length = lps[length - 1]
                                        if pattern[i] == pattern[length]:
                                            length += 1
                                            lps[i] = length
                                    return lps

                                def kmp_search(text, pattern):
                                    lps = build_lps(pattern)
                                    j = 0
                                    for i, ch in enumerate(text):
                                        while j > 0 and ch != pattern[j]:
                                            j = lps[j - 1]
                                        if ch == pattern[j]:
                                            j += 1
                                            if j == len(pattern):
                                                return i - len(pattern) + 1
                                                j = lps[j - 1]
                                    return -1
                                """,
                                "主串指针 i 从不回退，是 KMP 的性能核心。",
                                "lps 数组记录的是最长相等前后缀长度。"),
                        true,
                        "字符串经典",
                        "失配时别回退主串",
                        "KMP 的真正价值不是背模板，而是理解为什么模式串内部的信息能消除重复比较。",
                        "emerald",
                        10),
                article(
                        STRING_ALGORITHMS,
                        "algo-rabin-karp",
                        "Rabin-Karp 字符串哈希",
                        "Rabin-Karp",
                        "哈希匹配",
                        "Rabin-Karp 先把字符串片段映射成数值哈希，再滑动窗口比较哈希值，只有哈希命中时才做必要的二次校验。它把字符串匹配转成整数比较，因此非常适合多模式或重复子串判断问题。",
                        "预处理哈希和幂数组为 O(n)，每个窗口的哈希更新为 O(1)，总匹配复杂度平均为 O(n + m)。若哈希冲突较多，最坏需要退化到额外字符比较。空间复杂度为 O(n)。", 
                        "7 min read",
                        List.of("Rabin-Karp", "字符串哈希", "滚动哈希"),
                        List.of("适合多次子串比较、重复子串查找、字符串集合检索等场景。", "当问题能容忍极低概率冲突，滚动哈希往往非常高效。"),
                        List.of(
                                step("预处理前缀哈希", "把字符串每个前缀映射成整数，便于 O(1) 取出任意子串哈希。", "预处理"),
                                step("滑动窗口更新哈希", "窗口右移时利用前一窗口结果和幂值快速得到新区间哈希。", "滑动"),
                                step("哈希命中再校验", "为规避碰撞，哈希相等后可选择再做一次字符串比较。", "校验")),
                        List.of(
                                caseStudy("重复 DNA 序列", "对固定长度片段做滚动哈希，可以快速识别重复子串。"),
                                caseStudy("文档去重", "利用哈希比较文档片段相似性，能够快速过滤大量候选。")),
                        code("Python", "滚动哈希匹配", """
                                def rabin_karp(text, pattern):
                                    base = 131
                                    mod = 10**9 + 7
                                    m = len(pattern)
                                    target_hash = 0
                                    window_hash = 0
                                    power = 1
                                    for i in range(m):
                                        target_hash = (target_hash * base + ord(pattern[i])) % mod
                                        window_hash = (window_hash * base + ord(text[i])) % mod
                                        if i < m - 1:
                                            power = (power * base) % mod
                                    if window_hash == target_hash and text[:m] == pattern:
                                        return 0
                                    for i in range(m, len(text)):
                                        window_hash = (window_hash - ord(text[i - m]) * power) % mod
                                        window_hash = (window_hash * base + ord(text[i])) % mod
                                        if window_hash == target_hash and text[i - m + 1:i + 1] == pattern:
                                            return i - m + 1
                                    return -1
                                """,
                                "哈希能把子串比较降成整数比较。",
                                "工程里常用双哈希进一步降低冲突概率。"),
                        false,
                        "",
                        "",
                        "",
                        "cyan",
                        20),
                article(
                        STRING_ALGORITHMS,
                        "algo-z-algorithm",
                        "Z Algorithm",
                        "Z Algorithm",
                        "前缀匹配",
                        "Z 算法用 z[i] 表示从位置 i 开始的后缀与整个字符串前缀的最长公共前缀长度。它通过维护一个当前最右匹配区间 [l, r]，把大量重复比较复用起来，在很多前缀匹配题里比 KMP 更直观。",
                        "Z 数组的构建时间复杂度为 O(n)，空间复杂度为 O(n)。复杂度优势来自对已有匹配区间信息的复用，而不是暴力从每个位置重新匹配。", 
                        "7 min read",
                        List.of("Z 算法", "最长公共前缀", "线性匹配"),
                        List.of("适合模式串拼接匹配、周期串判断、前缀覆盖统计等场景。", "当题目围绕“某位置开始与整体前缀能匹配多长”展开时，Z 算法往往更顺手。"),
                        List.of(
                                step("维护最右匹配区间", "区间 [l, r] 记录当前已知最右的前缀匹配窗口。", "窗口"),
                                step("用镜像位置复用信息", "若 i 落在窗口内，可以先利用 z[i-l] 给出一个下界。", "复用"),
                                step("必要时继续暴力延伸", "只有超出已知窗口的部分才需要真正逐字符比较。", "延伸")),
                        List.of(
                                caseStudy("模式串在文本中的全部出现位置", "把 pattern + '#' + text 拼起来，z 值等于模式串长度的位置就是匹配点。"),
                                caseStudy("字符串周期检测", "若某个前缀长度能整除字符串长度且对应 z 值覆盖剩余部分，就能判断周期。")),
                        code("Python", "Z 数组模板", """
                                def z_algorithm(s):
                                    z = [0] * len(s)
                                    left = right = 0
                                    for i in range(1, len(s)):
                                        if i <= right:
                                            z[i] = min(right - i + 1, z[i - left])
                                        while i + z[i] < len(s) and s[z[i]] == s[i + z[i]]:
                                            z[i] += 1
                                        if i + z[i] - 1 > right:
                                            left, right = i, i + z[i] - 1
                                    z[0] = len(s)
                                    return z
                                """,
                                "窗口 [left, right] 保存的是最右前缀匹配段。",
                                "Z 算法和 KMP 都在复用前缀匹配信息，只是表达方式不同。"),
                        false,
                        "",
                        "",
                        "",
                        "amber",
                        30),
                article(
                        STRING_ALGORITHMS,
                        "algo-manacher",
                        "Manacher 最长回文子串",
                        "Manacher",
                        "回文专题",
                        "Manacher 通过在字符间插入分隔符，把奇偶回文统一成同一模型，并利用当前最右回文边界复用半径信息，从而在线性时间求出每个中心的最大回文半径。它是最长回文子串问题的最优经典解法。",
                        "时间复杂度为 O(n)，空间复杂度为 O(n)。线性复杂度的关键在于：每个字符扩展失败后，不会被无意义地重复扩展太多次。", 
                        "8 min read",
                        List.of("Manacher", "回文串", "线性算法"),
                        List.of("适合最长回文子串、回文半径统计、每个中心回文范围分析等场景。", "当需要在线性时间解决大规模回文匹配问题时，Manacher 是标准工具。"),
                        List.of(
                                step("做分隔符预处理", "把原串改写成 #a#b#c# 形式，统一奇偶回文。", "预处理"),
                                step("维护当前最右回文", "记录当前最右回文中心和右边界，作为后续位置的镜像参考。", "窗口"),
                                step("在必要时继续扩展", "若镜像信息不足以覆盖当前位置，就从当前半径继续向两侧扩展。", "扩展")),
                        List.of(
                                caseStudy("社交昵称回文检测", "批量判断昵称里最长回文片段长度时，Manacher 比中心扩展法更高效。"),
                                caseStudy("DNA 回文片段分析", "需要快速找出长回文结构时，线性复杂度优势明显。")),
                        code("Python", "Manacher 模板", """
                                def manacher(s):
                                    transformed = '#' + '#'.join(s) + '#'
                                    radius = [0] * len(transformed)
                                    center = right = 0
                                    for i in range(len(transformed)):
                                        mirror = 2 * center - i
                                        if i < right:
                                            radius[i] = min(right - i, radius[mirror])
                                        while (i - radius[i] - 1 >= 0 and
                                               i + radius[i] + 1 < len(transformed) and
                                               transformed[i - radius[i] - 1] == transformed[i + radius[i] + 1]):
                                            radius[i] += 1
                                        if i + radius[i] > right:
                                            center, right = i, i + radius[i]
                                    return radius
                                """,
                                "插入分隔符后，奇偶回文都能用同一种半径定义处理。",
                                "mirror 位置提供的是可以直接复用的最小保证。"),
                        false,
                        "",
                        "",
                        "",
                        "emerald",
                        40),
                article(
                        STRING_ALGORITHMS,
                        "algo-aho-corasick",
                        "AC 自动机",
                        "Aho-Corasick Automaton",
                        "多模式匹配",
                        "AC 自动机把 Trie 树与失配指针结合起来，实现对多个模式串的同时匹配。它可以看作 KMP 在多模式场景下的推广：主串只需扫描一遍，就能找出所有词典模式的命中位置。",
                        "构建 Trie 和失配指针总复杂度与模式串总长度成正比，匹配主串复杂度为 O(text_length + matches)。空间复杂度取决于 Trie 节点总数。", 
                        "9 min read",
                        List.of("AC 自动机", "Trie", "多模式匹配"),
                        List.of("适合敏感词过滤、日志规则扫描、字典批量匹配、病毒特征检测等多模式查询。", "当需要同时匹配成百上千个关键词时，AC 自动机远优于逐个模式串重复 KMP。"),
                        List.of(
                                step("先构建 Trie", "把所有模式串插入前缀树，共享相同前缀结构。", "Trie"),
                                step("BFS 构建失配指针", "让每个节点在失配后跳转到当前最长可复用后缀位置。", "失配"),
                                step("扫描主串并沿失配链统计", "主串指针始终前进，匹配失败时沿 fail 指针跳转。", "匹配")),
                        List.of(
                                caseStudy("敏感词过滤", "把大量敏感词一次构建成 AC 自动机，扫描文本时即可同时命中。"),
                                caseStudy("入侵检测规则引擎", "网络流量里同时匹配多条特征规则时，AC 自动机是经典基础结构。")),
                        code("Python", "AC 自动机构建骨架", """
                                from collections import deque, defaultdict

                                def build_ac(patterns):
                                    trie = [{'next': defaultdict(int), 'fail': 0, 'output': []}]
                                    for pattern in patterns:
                                        node = 0
                                        for ch in pattern:
                                            nxt = trie[node]['next'].get(ch)
                                            if not nxt:
                                                trie.append({'next': defaultdict(int), 'fail': 0, 'output': []})
                                                nxt = len(trie) - 1
                                                trie[node]['next'][ch] = nxt
                                            node = nxt
                                        trie[node]['output'].append(pattern)

                                    queue = deque(trie[0]['next'].values())
                                    while queue:
                                        node = queue.popleft()
                                        for ch, nxt in trie[node]['next'].items():
                                            fail = trie[node]['fail']
                                            while fail and ch not in trie[fail]['next']:
                                                fail = trie[fail]['fail']
                                            trie[nxt]['fail'] = trie[fail]['next'].get(ch, 0)
                                            trie[nxt]['output'].extend(trie[trie[nxt]['fail']]['output'])
                                            queue.append(nxt)
                                    return trie
                                """,
                                "Trie 解决共享前缀，fail 指针解决失配跳转。",
                                "AC 自动机的威力体现在一次扫描同时匹配多个模式。"),
                        false,
                        "",
                        "",
                        "",
                        "cyan",
                        50),
                article(
                        STRING_ALGORITHMS,
                        "algo-rolling-hash",
                        "滚动哈希与子串比较",
                        "Rolling Hash",
                        "子串比较",
                        "滚动哈希把字符串前缀映射成整数，并利用幂数组支持任意子串 O(1) 取哈希。它的强项不是单次精确匹配，而是高频子串相等性比较、重复子串判重和二分答案中的快速判定。",
                        "预处理时间复杂度为 O(n)，之后每次子串比较为 O(1) 平均时间。空间复杂度为 O(n)，用于保存前缀哈希和幂数组。", 
                        "7 min read",
                        List.of("滚动哈希", "子串比较", "字符串"),
                        List.of("适合重复子串判重、最长重复子串二分判定、字符串相等性高频比较。", "当题目要大量比较不同区间子串是否相同时，滚动哈希很有优势。"),
                        List.of(
                                step("预处理前缀哈希和幂", "prefix[i] 记录前 i 个字符的哈希值，power[i] 记录基数幂。", "预处理"),
                                step("O(1) 获取子串哈希", "借助前缀差和幂值，把任意区间哈希快速取出。", "查询"),
                                step("需要时做双哈希", "为了降低碰撞概率，可以使用两个模数并行计算。", "抗冲突")),
                        List.of(
                                caseStudy("最长重复子串", "常把长度二分，再用滚动哈希判定某长度子串是否重复。"),
                                caseStudy("文档片段去重", "比较大量片段是否相同时，滚动哈希比逐字符比较快得多。")),
                        code("Python", "前缀滚动哈希", """
                                def build_hash(s, base=131, mod=10**9 + 7):
                                    prefix = [0] * (len(s) + 1)
                                    power = [1] * (len(s) + 1)
                                    for i, ch in enumerate(s, start=1):
                                        prefix[i] = (prefix[i - 1] * base + ord(ch)) % mod
                                        power[i] = (power[i - 1] * base) % mod
                                    return prefix, power

                                def substring_hash(prefix, power, left, right, mod=10**9 + 7):
                                    return (prefix[right + 1] - prefix[left] * power[right - left + 1]) % mod
                                """,
                                "前缀哈希把任意区间比较压成常数时间。",
                                "双哈希是工程里常见的抗碰撞手段。"),
                        true,
                        "字符串工具箱",
                        "把子串比较降成 O(1)",
                        "很多看起来复杂的字符串判重与二分题，真正的底层加速器其实就是滚动哈希。",
                        "amber",
                        60)
        );
    }

    private KnowledgeBaseService.AdminArticleInput article(
            Section section,
            String slug,
            String title,
            String englishTitle,
            String badge,
            String principle,
            String complexity,
            String readTime,
            List<String> tags,
            List<String> scenarios,
            List<Step> steps,
            List<CaseStudy> cases,
            CodeSample codeSample,
            boolean spotlight,
            String spotlightEyebrow,
            String spotlightTitle,
            String spotlightDescription,
            String spotlightAccent,
            int sortOrder
    ) {
        List<KnowledgeBaseService.KnowledgeStep> strategySteps = new ArrayList<>();
        for (int index = 0; index < steps.size(); index++) {
            Step step = steps.get(index);
            strategySteps.add(new KnowledgeBaseService.KnowledgeStep(
                    String.format("%02d", index + 1),
                    step.title(),
                    step.description(),
                    step.badge()));
        }

        List<KnowledgeBaseService.KnowledgeInsight> insights = new ArrayList<>();
        String[] accents = {"emerald", "cyan", "amber", "rose"};
        for (int index = 0; index < cases.size(); index++) {
            CaseStudy caseStudy = cases.get(index);
            insights.add(new KnowledgeBaseService.KnowledgeInsight(
                    caseStudy.title(),
                    caseStudy.description(),
                    accents[index % accents.length]));
        }

        List<String> checklist = List.of(
                "能口头说明核心不变量或状态含义",
                "能按步骤手写主流程并处理边界条件",
                "能独立分析时间复杂度和空间复杂度",
                "能说出至少一个典型应用场景");

        String lead = "推荐实现顺序：" + steps.stream().map(Step::title).reduce((left, right) -> left + " -> " + right).orElse("先建模再编码")
                + "。写代码时先保证核心状态正确，再优化边界和常数。";

        return new KnowledgeBaseService.AdminArticleInput(
                slug,
                title,
                englishTitle,
                section.id(),
                section.title(),
                section.description(),
                badge,
                principle,
                lead,
                complexity,
                readTime,
                tags,
                scenarios,
                strategySteps,
                insights,
                List.of(new KnowledgeBaseService.KnowledgeCodeBlock(
                        codeSample.language(),
                        codeSample.title(),
                        codeSample.code(),
                        codeSample.callouts())),
                checklist,
                List.of(),
                spotlight,
                spotlight ? spotlightEyebrow : "",
                spotlight ? spotlightTitle : "",
                spotlight ? spotlightDescription : "",
                spotlightAccent,
                true,
                sortOrder
        );
    }

    private Step step(String title, String description, String badge) {
        return new Step(title, description, badge);
    }

    private CaseStudy caseStudy(String title, String description) {
        return new CaseStudy(title, description);
    }

    private CodeSample code(String language, String title, String snippet, String... callouts) {
        return new CodeSample(language, title, snippet, List.of(callouts));
    }

    private record Section(String id, String title, String description) {
    }

    private record Step(String title, String description, String badge) {
    }

    private record CaseStudy(String title, String description) {
    }

    private record CodeSample(String language, String title, String code, List<String> callouts) {
    }
}
