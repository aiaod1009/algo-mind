package com.example.demo.config;

import com.example.demo.entity.ForumComment;
import com.example.demo.entity.ForumPost;
import com.example.demo.entity.Level;
import com.example.demo.entity.User;
import com.example.demo.repository.ForumCommentRepository;
import com.example.demo.repository.ForumPostRepository;
import com.example.demo.repository.LevelRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

        private static final long RANDOM_SEED = 42L;
        private static final String DEFAULT_PASSWORD = "123456";

        private static final List<String> AVATAR_BACKGROUND_COLORS = List.of(
                        "#2563EB", "#0F766E", "#DC2626", "#7C3AED", "#EA580C", "#059669",
                        "#1D4ED8", "#9333EA", "#BE123C", "#0891B2", "#4F46E5", "#CA8A04");

        private final LevelRepository levelRepository;
        private final UserRepository userRepository;
        private final ForumPostRepository forumPostRepository;
        private final ForumCommentRepository forumCommentRepository;
        private final PasswordEncoder passwordEncoder;
        private final TransactionTemplate transactionTemplate;

        @Override
        public void run(String... args) {
                OffsetDateTime now = OffsetDateTime.now().withSecond(0).withNano(0);

                log.info("开始初始化开发环境演示数据");
                executeStage("关卡", this::initLevels);
                executeStage("用户", () -> initUsers(now));
                executeStage("帖子", () -> initPosts(now));
                executeStage("评论", () -> initComments(now));
                log.info("开发环境演示数据初始化完成");
        }

        public void initLevels() {
                List<LevelSeed> seeds = levelSeeds();
                List<Level> levelsToSave = new ArrayList<>(seeds.size());
                int inserted = 0;
                int updated = 0;

                for (LevelSeed seed : seeds) {
                        Optional<Level> existing = levelRepository.findByTrackAndOrder(seed.track(), seed.order());
                        Level level = existing.orElseGet(Level::new);
                        applyLevel(level, seed);
                        levelsToSave.add(level);
                        if (existing.isPresent()) {
                                updated++;
                        } else {
                                inserted++;
                        }
                }

                levelRepository.saveAll(levelsToSave);
                log.info("关卡初始化完成：新增 {} 条，更新 {} 条，总计 {} 条", inserted, updated, levelsToSave.size());
        }

        public void initUsers(OffsetDateTime now) {
                List<UserSeed> seeds = userSeeds();
                List<User> usersToSave = new ArrayList<>(seeds.size());
                Random random = new Random(RANDOM_SEED);
                int inserted = 0;
                int updated = 0;

                for (int i = 0; i < seeds.size(); i++) {
                        UserSeed seed = seeds.get(i);
                        Optional<User> existing = userRepository.findByEmail(seed.email());

                        if (existing.isPresent()) {
                                if ("admin@example.com".equalsIgnoreCase(seed.email())) {
                                        User user = existing.get();
                                        applyUser(user, seed, now, random, i, false);
                                        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
                                        usersToSave.add(user);
                                }
                                updated++;
                                continue;
                        }

                        User user = new User();
                        applyUser(user, seed, now, random, i, true);
                        usersToSave.add(user);
                        inserted++;
                }

                if (!usersToSave.isEmpty()) {
                        userRepository.saveAll(usersToSave);
                }
                log.info("用户初始化完成：新增 {} 条，更新 {} 条，总计 {} 条", inserted, updated, usersToSave.size());
        }

        public void initPosts(OffsetDateTime now) {
                List<PostSeed> seeds = postSeeds();
                List<ForumPost> postsToSave = new ArrayList<>(seeds.size());
                Random random = new Random(RANDOM_SEED);
                int inserted = 0;
                int updated = 0;

                for (PostSeed seed : seeds) {
                        User author = loadRequiredUser(seed.authorEmail());
                        Optional<ForumPost> existing = forumPostRepository.findByTopic(seed.topic());

                        if (existing.isPresent()) {
                                ForumPost post = existing.get();
                                if (post.getUserId() == null) {
                                        post.setUserId(author.getId());
                                        postsToSave.add(post);
                                }
                                updated++;
                                continue;
                        }

                        ForumPost post = new ForumPost();
                        applyPost(post, seed, author, now, random);
                        postsToSave.add(post);
                        inserted++;
                }

                if (!postsToSave.isEmpty()) {
                        forumPostRepository.saveAll(postsToSave);
                }
                log.info("帖子初始化完成：新增 {} 条，更新 {} 条，总计 {} 条", inserted, updated, postsToSave.size());
        }

        public void initComments(OffsetDateTime now) {
                List<CommentSeed> seeds = commentSeeds();
                List<ForumComment> commentsToSave = new ArrayList<>(seeds.size());
                int inserted = 0;
                int updated = 0;

                for (CommentSeed seed : seeds) {
                        ForumPost post = loadRequiredPost(seed.postTopic());
                        User author = loadRequiredUser(seed.userEmail());
                        Optional<ForumComment> existing = forumCommentRepository
                                        .findFirstByPostIdAndUserIdAndContent(post.getId(), author.getId(),
                                                        seed.content());

                        if (existing.isPresent()) {
                                updated++;
                                continue;
                        }

                        ForumComment comment = new ForumComment();
                        applyComment(comment, seed, post, author, now);
                        commentsToSave.add(comment);
                        inserted++;
                }

                if (!commentsToSave.isEmpty()) {
                        forumCommentRepository.saveAll(commentsToSave);
                }
                syncPostCommentCounts();
                log.info("评论初始化完成：新增 {} 条，更新 {} 条，总计 {} 条", inserted, updated, commentsToSave.size());
        }

        private void executeStage(String stageName, Runnable action) {
                transactionTemplate.executeWithoutResult(status -> {
                        try {
                                log.info("开始初始化{}数据", stageName);
                                action.run();
                                log.info("{}数据初始化成功", stageName);
                        } catch (RuntimeException ex) {
                                log.error("{}数据初始化失败，当前阶段事务已回滚", stageName, ex);
                                throw ex;
                        }
                });
        }

        private void applyLevel(Level level, LevelSeed seed) {
                level.setTrack(seed.track());
                level.setOrder(seed.order());
                level.setName(seed.name());
                level.setIsUnlocked(seed.unlocked());
                level.setRewardPoints(seed.rewardPoints());
                level.setType(seed.type());
                level.setQuestion(seed.question());
                level.setOptions(resolveOptions(seed));
                level.setAnswer(seed.answer());
                level.setDescription(seed.description());
        }

        private List<String> resolveOptions(LevelSeed seed) {
                if (seed.options() != null && !seed.options().isEmpty()) {
                        return seed.options();
                }

                if ("code".equals(seed.type()) || "fill".equals(seed.type())) {
                        return List.of();
                }

                if ("judge".equals(seed.type())) {
                        return List.of("正确", "错误");
                }

                return defaultSingleOptions(seed.answer());
        }

        private List<String> defaultSingleOptions(String answer) {
                String safeAnswer = answer == null ? "" : answer.trim();
                if (safeAnswer.isEmpty()) {
                        return List.of("选项A", "选项B", "选项C", "选项D");
                }

                if (safeAnswer.startsWith("O(")) {
                        LinkedHashSet<String> options = new LinkedHashSet<>();
                        options.add(safeAnswer);
                        options.add("O(1)");
                        options.add("O(log n)");
                        options.add("O(n)");
                        options.add("O(n log n)");
                        options.add("O(n²)");
                        options.add("O(nW)");
                        options.add("O(2^n)");
                        return options.stream().limit(4).toList();
                }

                return switch (safeAnswer) {
                        case "哈希表" -> List.of("哈希表", "双指针", "动态规划", "回溯法");
                        case "斐波那契数列" -> List.of("斐波那契数列", "贪心算法", "并查集", "拓扑排序");
                        case "后进先出" -> List.of("后进先出", "先进先出", "随机访问", "层序遍历");
                        case "先进先出" -> List.of("先进先出", "后进先出", "随机插入", "双端回退");
                        case "根左右" -> List.of("根左右", "左右根", "左根右", "右左根");
                        case "是当前堆中的最大值" -> List.of("是当前堆中的最大值", "是当前堆中的最小值", "一定等于中位数", "始终等于平均值");
                        case "连通性" -> List.of("连通性", "字符串匹配", "区间最值", "背包优化");
                        case "区间查询与区间更新" -> List.of("区间查询与区间更新", "图最短路", "全排列枚举", "矩阵快速幂");
                        case "字符串前缀查询" -> List.of("字符串前缀查询", "最短路径", "拓扑排序", "集合合并");
                        case "二进制位掩码" -> List.of("二进制位掩码", "哈希冲突链", "并查集路径压缩", "差分数组");
                        case "满足条件的数字统计" -> List.of("满足条件的数字统计", "树的直径", "最小生成树", "字符串哈希");
                        case "连续子数组或子串问题" -> List.of("连续子数组或子串问题", "图的连通块划分", "线段树懒标记", "拓扑层次遍历");
                        case "区间求和" -> List.of("区间求和", "子集枚举", "图染色", "最小割");
                        case "寻找下一个更大元素" -> List.of("寻找下一个更大元素", "求连通分量", "求最短路径", "统计逆序对");
                        case "左右子树高度差不超过 1" -> List.of("左右子树高度差不超过 1", "每层节点数相同", "根节点必须最小", "叶子节点高度相等");
                        default -> List.of(safeAnswer, "以上都不正确", "需要更多信息", "无法确定");
                };
        }

        private void applyUser(User user, UserSeed seed, OffsetDateTime now, Random random, int colorIndex,
                        boolean isNewUser) {
                user.setName(seed.name());
                user.setEmail(seed.email());
                if (isNewUser) {
                        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
                }
                user.setPoints(seed.points());
                user.setBio(seed.bio());
                user.setGender(seed.gender());
                user.setAvatar(buildAvatar(seed.name(), colorIndex));
                user.setTargetTrack(seed.targetTrack());
                user.setWeeklyGoal(seed.weeklyGoal());

                OffsetDateTime createdAt = now.minusDays(seed.joinedDaysAgo())
                                .minusHours(seed.joinedHourOffset())
                                .minusMinutes(random.nextInt(30));
                OffsetDateTime updatedAt = now.minusHours(seed.lastActiveHoursAgo()).minusMinutes(random.nextInt(20));

                user.setCreatedAt(createdAt);
                user.setUpdatedAt(updatedAt.isBefore(createdAt) ? createdAt.plusHours(1) : updatedAt);
        }

        private void applyPost(ForumPost post, PostSeed seed, User author, OffsetDateTime now, Random random) {
                post.setUserId(author.getId());
                post.setAuthor(author.getName());
                post.setAuthorLevel(resolveAuthorLevel(author.getPoints()));
                post.setAvatar(author.getAvatar());
                post.setTopic(seed.topic());
                post.setContent(seed.content());
                post.setQuote(seed.quote());
                post.setTag(seed.tag());
                post.setLikes(buildPostLikes(seed.hotness(), random));
                post.setCreatedAt(now.minusDays(seed.daysAgo()).minusHours(seed.hourOffset())
                                .minusMinutes(random.nextInt(45)));
                post.setComments(0);
        }

        private void applyComment(ForumComment comment, CommentSeed seed, ForumPost post, User author,
                        OffsetDateTime now) {
                comment.setPostId(post.getId());
                comment.setUserId(author.getId());
                comment.setAuthor(author.getName());
                comment.setAuthorLevel(resolveAuthorLevel(author.getPoints()));
                comment.setAvatar(author.getAvatar());
                comment.setContent(seed.content());
                comment.setLikes(buildCommentLikes(seed.hotness(), seed.minutesAfterPost()));
                comment.setParentId(null);

                OffsetDateTime createdAt = post.getCreatedAt().plusMinutes(seed.minutesAfterPost());
                comment.setCreatedAt(createdAt.isAfter(now) ? now.minusMinutes(5) : createdAt);
        }

        private void syncPostCommentCounts() {
                List<ForumPost> postsToSave = new ArrayList<>();

                for (PostSeed seed : postSeeds()) {
                        forumPostRepository.findByTopic(seed.topic()).ifPresent(post -> {
                                post.setComments(Math.toIntExact(forumCommentRepository.countByPostId(post.getId())));
                                postsToSave.add(post);
                        });
                }

                if (!postsToSave.isEmpty()) {
                        forumPostRepository.saveAll(postsToSave);
                }
        }

        private User loadRequiredUser(String email) {
                return userRepository.findByEmail(email)
                                .orElseThrow(() -> new IllegalStateException("未找到初始化用户：" + email));
        }

        private ForumPost loadRequiredPost(String topic) {
                return forumPostRepository.findByTopic(topic)
                                .orElseThrow(() -> new IllegalStateException("未找到初始化帖子：" + topic));
        }

        private int buildPostLikes(int hotness, Random random) {
                return switch (hotness) {
                        case 3 -> 68 + random.nextInt(70);
                        case 2 -> 22 + random.nextInt(36);
                        default -> 4 + random.nextInt(14);
                };
        }

        private int buildCommentLikes(int hotness, int minutesAfterPost) {
                int offset = Math.floorMod(minutesAfterPost, 7);
                return switch (hotness) {
                        case 3 -> 10 + offset + 4;
                        case 2 -> 3 + offset;
                        default -> Math.min(3, offset / 2);
                };
        }

        private String resolveAuthorLevel(Integer points) {
                int safePoints = points == null ? 0 : points;
                if (safePoints >= 800) {
                        return "Lv.5";
                }
                if (safePoints >= 600) {
                        return "Lv.4";
                }
                if (safePoints >= 400) {
                        return "Lv.3";
                }
                if (safePoints >= 200) {
                        return "Lv.2";
                }
                return "Lv.1";
        }

        private String buildAvatar(String name, int colorIndex) {
                String displayName = shortDisplayName(name);
                String background = AVATAR_BACKGROUND_COLORS
                                .get(Math.floorMod(colorIndex, AVATAR_BACKGROUND_COLORS.size()));
                String svg = """
                                <svg xmlns='http://www.w3.org/2000/svg' width='128' height='128' viewBox='0 0 128 128'>
                                  <rect width='128' height='128' rx='30' fill='%s'/>
                                  <circle cx='96' cy='30' r='12' fill='rgba(255,255,255,0.18)'/>
                                  <text x='64' y='76' font-size='34' text-anchor='middle' fill='#ffffff'
                                        font-family='Arial, PingFang SC, Microsoft YaHei, sans-serif'>%s</text>
                                </svg>
                                """.formatted(background, displayName);
                return "data:image/svg+xml;charset=UTF-8,"
                                + URLEncoder.encode(svg, StandardCharsets.UTF_8).replace("+", "%20");
        }

        private String shortDisplayName(String name) {
                if (name == null || name.isBlank()) {
                        return "用户";
                }
                return name.length() <= 2 ? name : name.substring(name.length() - 2);
        }

        private List<LevelSeed> levelSeeds() {
                return List.of(
                                level("algo", 1, "两数之和", true, 10, "single",
                                                "给定整数数组 nums 和目标值 target，哪种思路能在 O(n) 时间内找到答案？",
                                                "哈希表",
                                                "经典哈希表入门题。"),
                                level("algo", 2, "二分查找", true, 10, "single",
                                                "在有序数组中查找目标值，二分查找的时间复杂度是？",
                                                "O(log n)",
                                                "掌握边界写法比背模板更重要。"),
                                level("algo", 3, "快速排序", false, 15, "single",
                                                "快速排序的平均时间复杂度是？",
                                                "O(n log n)",
                                                "分治思想的典型代表。"),
                                level("algo", 4, "归并排序", false, 15, "single",
                                                "归并排序的额外空间复杂度通常是？",
                                                "O(n)",
                                                "稳定排序，适合讲清楚分治流程。"),
                                level("algo", 5, "动态规划入门", false, 20, "single",
                                                "求斐波那契数列时，使用标准 DP 写法的时间复杂度是？",
                                                "O(n)",
                                                "先定义状态，再找转移。"),
                                level("algo", 6, "爬楼梯问题", false, 20, "single",
                                                "一次可以爬 1 或 2 阶，爬到第 n 阶的方案数，本质上接近哪类问题？",
                                                "斐波那契数列",
                                                "动态规划最常见的转化之一。"),
                                level("algo", 7, "最大子数组和", false, 25, "single",
                                                "Kadane 算法求最大子数组和的时间复杂度是？",
                                                "O(n)",
                                                "理解“以当前位置结尾”的状态定义很关键。"),
                                level("algo", 8, "0-1 背包", false, 30, "single",
                                                "0-1 背包的经典 DP 状态转移，时间复杂度通常写作？",
                                                "O(nW)",
                                                "n 是物品数，W 是背包容量。"),
                                level("algo", 9, "实现两数之和", false, 30, "code",
                                                "给定 nums 和 target，返回两数之和对应的下标。要求时间复杂度 O(n)。",
                                                "用哈希表记录已经遍历过的数字与下标，边遍历边判断 target - nums[i] 是否出现过。",
                                                "把思路写成清晰的代码实现。"),
                                level("algo", 10, "实现二分查找", false, 25, "code",
                                                "在有序数组 nums 中查找 target，不存在返回 -1。要求时间复杂度 O(log n)。",
                                                "使用 while (left <= right) 的写法，注意 mid 和左右边界更新。",
                                                "重点练边界，不只是背模板。"),
                                level("algo", 11, "实现斐波那契数列", false, 25, "code",
                                                "实现一个函数，返回斐波那契数列第 n 项。要求使用迭代或 DP 思路。",
                                                "维护前两项或 DP 数组，自底向上迭代计算即可。",
                                                "适合作为 DP 代码题热身。"),

                                level("ds", 1, "数组基础", true, 10, "single",
                                                "数组支持按下标随机访问，平均时间复杂度是？",
                                                "O(1)",
                                                "数据结构基础题。"),
                                level("ds", 2, "链表插入", true, 10, "single",
                                                "在单链表头部插入一个节点，时间复杂度通常是？",
                                                "O(1)",
                                                "理解指针改动即可。"),
                                level("ds", 3, "栈的特性", false, 15, "single",
                                                "栈最典型的访问顺序是？",
                                                "后进先出",
                                                "LIFO 是核心关键词。"),
                                level("ds", 4, "队列的特性", false, 15, "single",
                                                "队列最典型的访问顺序是？",
                                                "先进先出",
                                                "FIFO 是核心关键词。"),
                                level("ds", 5, "二叉树遍历", false, 20, "single",
                                                "二叉树前序遍历的顺序是？",
                                                "根左右",
                                                "先访问根，再递归左右子树。"),
                                level("ds", 6, "堆的性质", false, 25, "single",
                                                "最大堆的堆顶元素满足什么性质？",
                                                "是当前堆中的最大值",
                                                "优先队列的常见底层结构。"),
                                level("ds", 7, "哈希表查询", false, 20, "single",
                                                "哈希表平均查询时间复杂度通常是？",
                                                "O(1)",
                                                "别忘了这是平均复杂度。"),
                                level("ds", 8, "平衡二叉树", false, 30, "single",
                                                "AVL 树保持平衡的关键条件通常描述为？",
                                                "左右子树高度差不超过 1",
                                                "理解“平衡”比记名词更重要。"),
                                level("ds", 9, "实现链表反转", false, 30, "code",
                                                "反转单链表并返回新的头节点。要求额外空间复杂度 O(1)。",
                                                "使用 pre、cur、next 三个指针原地迭代反转。",
                                                "基础但很考察指针细节。"),
                                level("ds", 10, "实现前序遍历", false, 25, "code",
                                                "返回二叉树的前序遍历结果，可以使用递归或显式栈。",
                                                "前序顺序为根、左、右；递归或栈模拟都可以。",
                                                "适合巩固树和栈的结合。"),

                                level("contest", 1, "滑动窗口", true, 15, "single",
                                                "滑动窗口最适合解决哪类问题？",
                                                "连续子数组或子串问题",
                                                "看到“连续”通常就要敏感起来。"),
                                level("contest", 2, "前缀和", false, 15, "single",
                                                "前缀和最常用于优化哪类计算？",
                                                "区间求和",
                                                "预处理后快速回答查询。"),
                                level("contest", 3, "单调栈", false, 20, "single",
                                                "单调栈常见应用是？",
                                                "寻找下一个更大元素",
                                                "理解“维护单调性”比背题名更重要。"),
                                level("contest", 4, "并查集", false, 25, "single",
                                                "并查集最擅长处理哪类问题？",
                                                "连通性",
                                                "动态合并集合时非常好用。"),
                                level("contest", 5, "线段树", false, 30, "single",
                                                "线段树最常用于支持什么操作？",
                                                "区间查询与区间更新",
                                                "竞赛中非常常见的区间数据结构。"),
                                level("contest", 6, "字典树", false, 25, "single",
                                                "字典树最适合处理什么场景？",
                                                "字符串前缀查询",
                                                "尤其适合批量单词前缀检索。"),
                                level("contest", 7, "状态压缩 DP", false, 35, "single",
                                                "状态压缩 DP 通常使用什么表示状态？",
                                                "二进制位掩码",
                                                "当状态数量不大但组合很多时很常见。"),
                                level("contest", 8, "数位 DP", false, 35, "single",
                                                "数位 DP 常用于解决哪类问题？",
                                                "满足条件的数字统计",
                                                "通常会和“按位枚举”一起出现。"),
                                level("contest", 9, "实现最长无重复子串", false, 35, "code",
                                                "给定字符串 s，返回不含重复字符的最长子串长度。要求时间复杂度 O(n)。",
                                                "用滑动窗口维护当前无重复区间，并记录字符最近出现位置。",
                                                "竞赛与面试都很高频。"),
                                level("contest", 10, "实现三数之和", false, 40, "code",
                                                "给定数组 nums，返回所有和为 0 的不重复三元组。",
                                                "先排序，再固定一个数，剩余部分使用双指针去重查找。",
                                                "双指针综合题。"));
        }

        private List<UserSeed> userSeeds() {
                return List.of(
                                user("admin@example.com", "Admin", "演示账号，用于体验完整学习流程。", "unknown", "algo", 999, 10, 180,
                                                1, 1),
                                user("linan@example.com", "林安", "最近在补动态规划和图论，喜欢把题解写成自己的模板。", "female", "algo", 860, 12,
                                                120, 2, 4),
                                user("zhoumu@example.com", "周沐", "刷题节奏不快，但会认真复盘每一道错题。", "male", "algo", 540, 8, 96, 5,
                                                9),
                                user("chenxi@example.com", "陈汐", "偏爱单调栈、双指针这类能一眼看出结构感的题。", "female", "contest", 620, 10,
                                                88, 4, 6),
                                user("qiaomo@example.com", "乔墨", "最近在练周赛复盘，希望把失误总结得更系统。", "male", "contest", 470, 9, 70,
                                                3, 5),
                                user("songhe@example.com", "宋河", "数据结构基础打得差不多了，正在补图论。", "male", "ds", 380, 7, 65, 6, 7),
                                user("heqing@example.com", "何清", "喜欢把链表和二叉树题整理成小抄，复习效率会高很多。", "female", "ds", 310, 6,
                                                54, 2, 10),
                                user("xiaoyu@example.com", "夏语", "刚开始系统学算法，目标是先把基础题做扎实。", "female", "algo", 180, 5, 42,
                                                4, 16),
                                user("nuannuan@example.com", "暖暖", "每天坚持做两三题，慢一点也想走得稳一点。", "female", "contest", 260, 6,
                                                38, 3, 12),
                                user("lichuan@example.com", "黎川", "最近在补前缀和和差分，准备把区间问题系统过一遍。", "male", "contest", 430, 8,
                                                30, 5, 8),
                                user("ziye@example.com", "子夜", "会把典型题拆成题型卡片，方便之后复习。", "male", "algo", 710, 11, 26, 1,
                                                3),
                                user("tanyue@example.com", "谭越", "周赛状态起伏有点大，最近在练比赛中的节奏控制。", "male", "contest", 590, 10,
                                                22, 2, 6),
                                user("ache@example.com", "阿澈", "更喜欢看别人的题解对比不同写法，再整理成自己的版本。", "female", "ds", 240, 5, 18,
                                                3, 11));
        }

        private List<PostSeed> postSeeds() {
                return List.of(
                                post(
                                                "刷题三个月后，我开始用错题笔记复盘了",
                                                "以前我做完题就直接看下一道，短期会有成就感，但过几天再看还是会忘。最近改成每晚花二十分钟整理错题笔记：先写错因，再写正确思路，最后补一句“下次看到什么信号应该想到这类做法”。\n\n坚持两周之后，明显感觉二分、前缀和这类基础题不容易反复踩坑了。想问问大家平时会怎么做复盘？",
                                                "经验分享",
                                                "先把暴力思路写出来，再看能省掉哪层循环。",
                                                "linan@example.com",
                                                2,
                                                4,
                                                3),
                                post(
                                                "求助：单调栈到底什么时候该想到？",
                                                "我最近刷题经常在题解里看到单调栈，但自己做题时很难第一时间想到它。尤其是“下一个更大元素”“能看到的楼”这类题，题解一看就明白，自己做又想不到。\n\n大家平时是靠关键词触发，还是靠题型归纳？有没有比较顺手的判断方法？",
                                                "求助",
                                                null,
                                                "chenxi@example.com",
                                                1,
                                                8,
                                                3),
                                post(
                                                "二分模板总写错边界，这里整理一个稳一点的版本",
                                                "这周把以前写过的二分题重新过了一遍，发现自己出错最多的地方不是思路，而是边界。后来我统一成一套写法：区间闭合、循环条件固定、返回值逻辑分情况处理。\n\n我把常见的三种模板都记成了“找值 / 找左边界 / 找右边界”，感觉稳定很多。贴出来给大家参考，也欢迎补充更顺手的写法。",
                                                "题解",
                                                "left + (right - left) / 2 比直接写 (l + r) / 2 更稳。",
                                                "zhoumu@example.com",
                                                5,
                                                6,
                                                2),
                                post(
                                                "图论入门路线，想听听大家都怎么学",
                                                "最近准备系统补图论，手里有 BFS / DFS 的基础，但一到最短路、并查集和最小生成树就有点乱。我现在打算先补遍历和建图，再刷模板题，最后做综合题。\n\n如果你们是从零开始学图论，会怎么安排顺序？有没有比较适合循序渐进的题单？",
                                                "学习路线",
                                                null,
                                                "xiaoyu@example.com",
                                                12,
                                                5,
                                                1),
                                post(
                                                "分享一下我最近的周赛复盘模板",
                                                "最近周赛结束后我会固定做三件事：第一，回看每道题卡住的那一刻，记录自己当时为什么没想到；第二，把赛后补题分成“知识点不会”和“赛时状态问题”；第三，给下周定一个非常具体的小目标。\n\n这样复盘几次之后，感觉比赛时的心态会稳一些，也更容易知道自己接下来该补什么。",
                                                "比赛复盘",
                                                "复盘不是记答案，而是记自己为什么会错。",
                                                "qiaomo@example.com",
                                                7,
                                                10,
                                                2),
                                post(
                                                "动态规划状态设计总卡住，有没有通用拆解方法？",
                                                "我最近在刷 DP，最常卡住的不是转移方程，而是第一步“状态到底该怎么定义”。看题解时会觉得作者定义得很自然，但自己上手就容易把状态设计得又长又绕。\n\n如果要给一个入门同学讲“怎么从题意里抽状态”，你们会怎么解释？有没有自己总结出来的顺序或 checklist？",
                                                "算法讨论",
                                                null,
                                                "songhe@example.com",
                                                3,
                                                11,
                                                3),
                                post(
                                                "写了一个链表题小抄，欢迎拍砖",
                                                "最近把链表常见操作整理成了一页小抄，包括反转、删除倒数第 k 个节点、快慢指针找中点这些。整理的时候发现其实很多题都只是“画图 + 换指针顺序”的组合。\n\n我想继续把树和堆也按这个方式整理出来，大家如果有觉得特别值得记住的链表坑点，也欢迎补充。",
                                                "数据结构",
                                                null,
                                                "heqing@example.com",
                                                18,
                                                7,
                                                1),
                                post(
                                                "第一次 AK 模拟赛后的复盘",
                                                "这周第一次在模拟赛里把会做的题都稳定做出来，虽然题目整体不算难，但最大的收获是节奏终于没有乱。以前我总会在第二题卡住太久，这次强迫自己控制在十五分钟内做出取舍，反而整场更顺。\n\n感觉比赛里“知道什么时候先放下”这件事，可能和会不会做题一样重要。",
                                                "成长记录",
                                                "状态不稳时，先保住会做的题。",
                                                "nuannuan@example.com",
                                                26,
                                                9,
                                                2));
        }

        private List<CommentSeed> commentSeeds() {
                return List.of(
                                comment("刷题三个月后，我开始用错题笔记复盘了", "zhoumu@example.com",
                                                "我最近也在这么做，尤其是把“为什么会想歪”写下来之后，复习时帮助特别大。", 38, 2),
                                comment("刷题三个月后，我开始用错题笔记复盘了", "nuannuan@example.com",
                                                "我以前只记答案，不记错因，后来发现复盘效果差很多。你这个方法很实用。", 190, 1),

                                comment("求助：单调栈到底什么时候该想到？", "linan@example.com",
                                                "我自己的触发词是“最近更大/更小”和“每个元素只进出一次”。看到这两类描述就会往单调栈靠。", 12, 3),
                                comment("求助：单调栈到底什么时候该想到？", "qiaomo@example.com",
                                                "可以先把暴力写出来，如果你发现每次都在向左或向右找第一个更优元素，就很像单调栈题。", 46, 2),
                                comment("求助：单调栈到底什么时候该想到？", "songhe@example.com",
                                                "我会先问自己：这个元素被处理完之后，未来还会不会再回来？如果不会，单调栈就很有戏。", 95, 2),
                                comment("求助：单调栈到底什么时候该想到？", "heqing@example.com",
                                                "建议把“下一个更大元素”“每日温度”“柱状图最大矩形”放一起刷，体感会明显。", 260, 1),
                                comment("求助：单调栈到底什么时候该想到？", "tanyue@example.com",
                                                "竞赛里我经常先观察是否能把 O(n^2) 的枚举压成“每个元素只被弹一次”，这时就会想到单调栈。", 520, 2),

                                comment("图论入门路线，想听听大家都怎么学", "linan@example.com",
                                                "我会按“建图与遍历 -> 最短路 -> 并查集 -> 最小生成树”这个顺序来，前面先把图的表达方式练熟。", 85, 2),

                                comment("分享一下我最近的周赛复盘模板", "chenxi@example.com",
                                                "把“知识点不会”和“赛时状态问题”拆开这点特别好，不然很容易把问题归因混在一起。", 25, 2),
                                comment("分享一下我最近的周赛复盘模板", "nuannuan@example.com",
                                                "我之前复盘总在抄题解，你这个模板更像是在复盘自己当时的决策，思路很对。", 140, 1),
                                comment("分享一下我最近的周赛复盘模板", "lichuan@example.com",
                                                "我现在会在复盘最后加一行“下次比赛想刻意练什么”，这样下一周更容易执行。", 355, 1),

                                comment("动态规划状态设计总卡住，有没有通用拆解方法？", "linan@example.com",
                                                "我会先问自己：题目最终要的是什么量，它能不能拆成“前 i 个元素时”的子问题。很多时候状态就从这里冒出来。", 15, 3),
                                comment("动态规划状态设计总卡住，有没有通用拆解方法？", "ziye@example.com",
                                                "可以先只写一句自然语言状态定义，比如“到第 i 天结束时的最优值”，别急着上公式。", 58, 2),
                                comment("动态规划状态设计总卡住，有没有通用拆解方法？", "qiaomo@example.com",
                                                "我会先画一个很小的数据样例，看答案是由哪几个更小的答案拼出来的，状态会更好找。", 180, 2),
                                comment("动态规划状态设计总卡住，有没有通用拆解方法？", "tanyue@example.com",
                                                "如果状态一上来就写得很长，通常说明拆得还不够干净。先试着少一个维度再看。", 420, 1),

                                comment("第一次 AK 模拟赛后的复盘", "songhe@example.com",
                                                "“先保住会做的题”这句太真实了，我最近也在练这个，不然很容易被一题带崩整场。", 75, 2),
                                comment("第一次 AK 模拟赛后的复盘", "chenxi@example.com",
                                                "能在赛时主动止损其实很难，这说明你已经开始有比赛节奏感了。继续保持。", 260, 1));
        }

        private LevelSeed level(String track, int order, String name, boolean unlocked, int rewardPoints,
                        String type, String question, String answer, String description) {
                return new LevelSeed(track, order, name, unlocked, rewardPoints, type, question, answer, description,
                                List.of());
        }

        private UserSeed user(String email, String name, String bio, String gender, String targetTrack,
                        int points, int weeklyGoal, int joinedDaysAgo, int joinedHourOffset, int lastActiveHoursAgo) {
                return new UserSeed(email, name, bio, gender, targetTrack, points, weeklyGoal,
                                joinedDaysAgo, joinedHourOffset, lastActiveHoursAgo);
        }

        private PostSeed post(String topic, String content, String tag, String quote,
                        String authorEmail, int daysAgo, int hourOffset, int hotness) {
                return new PostSeed(topic, content, tag, quote, authorEmail, daysAgo, hourOffset, hotness);
        }

        private CommentSeed comment(String postTopic, String userEmail, String content, int minutesAfterPost,
                        int hotness) {
                return new CommentSeed(postTopic, userEmail, content, minutesAfterPost, hotness);
        }

        private record LevelSeed(
                        String track,
                        Integer order,
                        String name,
                        boolean unlocked,
                        Integer rewardPoints,
                        String type,
                        String question,
                        String answer,
                        String description,
                        List<String> options) {
        }

        private record UserSeed(
                        String email,
                        String name,
                        String bio,
                        String gender,
                        String targetTrack,
                        Integer points,
                        Integer weeklyGoal,
                        int joinedDaysAgo,
                        int joinedHourOffset,
                        int lastActiveHoursAgo) {
        }

        private record PostSeed(
                        String topic,
                        String content,
                        String tag,
                        String quote,
                        String authorEmail,
                        int daysAgo,
                        int hourOffset,
                        int hotness) {
        }

        private record CommentSeed(
                        String postTopic,
                        String userEmail,
                        String content,
                        int minutesAfterPost,
                        int hotness) {
        }
}
