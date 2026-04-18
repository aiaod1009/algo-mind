package com.example.demo.service;

import com.example.demo.config.ProhibitedWordConfig;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProhibitedWordService {

    private static final Logger log = LoggerFactory.getLogger(ProhibitedWordService.class);

    private final ProhibitedWordConfig config;

    private final Map<Character, Object> dfaRoot = new HashMap<>();
    private final Set<String> wordSet = new HashSet<>();

    public ProhibitedWordService(ProhibitedWordConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        if (!config.isEnabled()) {
            log.info("违禁词检测已禁用");
            return;
        }
        loadWords();
        buildDfa();
        log.info("违禁词检测服务初始化完成，共加载 {} 个违禁词", wordSet.size());
    }

    public ProhibitedWordCheckResult check(String text) {
        if (!config.isEnabled() || !StringUtils.hasText(text)) {
            return ProhibitedWordCheckResult.clean();
        }

        List<String> foundWords = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            int end = matchDfa(text, i);
            if (end > i) {
                foundWords.add(text.substring(i, end));
                i = end - 1;
            }
        }

        if (foundWords.isEmpty()) {
            return ProhibitedWordCheckResult.clean();
        }
        return ProhibitedWordCheckResult.violation(foundWords);
    }

    @SuppressWarnings("unchecked")
    private int matchDfa(String text, int startIndex) {
        Map<Character, Object> current = dfaRoot;
        int matchEnd = -1;
        for (int i = startIndex; i < text.length(); i++) {
            char ch = text.charAt(i);
            Object next = current.get(ch);
            if (next == null) {
                break;
            }
            matchEnd = i + 1;
            if (next instanceof Map) {
                current = (Map<Character, Object>) next;
            } else {
                break;
            }
        }
        return matchEnd;
    }

    @SuppressWarnings("unchecked")
    private void buildDfa() {
        dfaRoot.clear();
        for (String word : wordSet) {
            if (!StringUtils.hasText(word)) {
                continue;
            }
            Map<Character, Object> current = dfaRoot;
            for (int i = 0; i < word.length(); i++) {
                char ch = word.charAt(i);
                Object next = current.get(ch);
                if (next == null) {
                    Map<Character, Object> newNode = new HashMap<>();
                    current.put(ch, newNode);
                    current = newNode;
                } else if (next instanceof Map) {
                    current = (Map<Character, Object>) next;
                }
            }
            current.put('\0', Boolean.TRUE);
        }
    }

    private void loadWords() {
        String filePath = config.getFilePath();
        try {
            List<String> lines;
            if (filePath.startsWith("classpath:")) {
                String resourcePath = filePath.substring("classpath:".length());
                Resource resource = new ClassPathResource(resourcePath);
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                    lines = reader.lines().toList();
                }
            } else {
                Path path = Paths.get(filePath);
                lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            }

            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    wordSet.add(trimmed);
                }
            }
        } catch (IOException e) {
            log.error("加载违禁词文件失败: {}", filePath, e);
        }
    }

    public static class ProhibitedWordCheckResult {
        private final boolean hasViolation;
        private final List<String> matchedWords;

        private ProhibitedWordCheckResult(boolean hasViolation, List<String> matchedWords) {
            this.hasViolation = hasViolation;
            this.matchedWords = matchedWords;
        }

        public static ProhibitedWordCheckResult clean() {
            return new ProhibitedWordCheckResult(false, List.of());
        }

        public static ProhibitedWordCheckResult violation(List<String> matchedWords) {
            return new ProhibitedWordCheckResult(true, matchedWords);
        }

        public boolean hasViolation() {
            return hasViolation;
        }

        public List<String> getMatchedWords() {
            return matchedWords;
        }
    }
}
