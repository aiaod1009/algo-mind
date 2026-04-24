package com.example.demo.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class LevelOptionsMigrationRunner implements CommandLineRunner {

  private final DataSource dataSource;
  private final ObjectMapper objectMapper;

  private static final Map<Long, List<String>> DEFAULT_OPTIONS = Map.of(
      5L, List.of("O(n)", "O(n²)", "O(n*m)", "O(n+m)")
  );

  @Override
  @Transactional
  public void run(String... args) {
    try {
      if (!tableExists("t_level_options")) {
        return;
      }

      Map<Long, List<String>> rowsByLevel = loadOptionsRows();

      int migratedLevels = 0;
      int migratedRows = 0;

      try (Connection connection = dataSource.getConnection()) {
        for (Map.Entry<Long, List<String>> entry : rowsByLevel.entrySet()) {
          Long levelId = entry.getKey();
          List<String> rawRows = entry.getValue();
          MigrationResult parsed = parseRows(rawRows);

          if (!parsed.needsMigration) {
            continue;
          }

          try (var deleteStmt = connection.prepareStatement("DELETE FROM t_level_options WHERE level_id = ?")) {
            deleteStmt.setLong(1, levelId);
            deleteStmt.executeUpdate();
          }

          if (!parsed.options.isEmpty()) {
            try (var insertStmt = connection
                .prepareStatement("INSERT INTO t_level_options(level_id, options) VALUES (?, ?)")) {
              for (String option : parsed.options) {
                insertStmt.setLong(1, levelId);
                insertStmt.setString(2, option);
                insertStmt.addBatch();
              }
              insertStmt.executeBatch();
            }
          }

          migratedLevels++;
          migratedRows += parsed.options.size();
        }

        Set<Long> levelsWithOptions = rowsByLevel.keySet();
        for (Map.Entry<Long, List<String>> entry : DEFAULT_OPTIONS.entrySet()) {
          Long levelId = entry.getKey();
          if (levelsWithOptions.contains(levelId)) {
            continue;
          }

          List<String> options = entry.getValue();
          try (var insertStmt = connection
              .prepareStatement("INSERT INTO t_level_options(level_id, options) VALUES (?, ?)")) {
            for (String option : options) {
              insertStmt.setLong(1, levelId);
              insertStmt.setString(2, option);
              insertStmt.addBatch();
            }
            insertStmt.executeBatch();
          }

          migratedLevels++;
          migratedRows += options.size();
          log.info("已为关卡 {} 添加缺失的 {} 个选项", levelId, options.size());
        }
      }

      if (migratedLevels > 0) {
        log.info("关卡选项迁移完成：修复 {} 个关卡，共 {} 条选项", migratedLevels, migratedRows);
      }
    } catch (Exception ex) {
      log.error("关卡选项迁移失败，已跳过迁移", ex);
    }
  }

  private boolean tableExists(String tableName) {
    try (Connection connection = dataSource.getConnection()) {
      var metadata = connection.getMetaData();
      try (ResultSet rs = metadata.getTables(connection.getCatalog(), null, tableName, new String[] { "TABLE" })) {
        return rs.next();
      }
    } catch (Exception ex) {
      return false;
    }
  }

  private Map<Long, List<String>> loadOptionsRows() throws Exception {
    Map<Long, List<String>> grouped = new LinkedHashMap<>();
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT level_id, options FROM t_level_options ORDER BY level_id")) {
      while (rs.next()) {
        long levelId = rs.getLong("level_id");
        String optionValue = rs.getString("options");
        grouped.computeIfAbsent(levelId, key -> new ArrayList<>()).add(optionValue == null ? "" : optionValue);
      }
    }
    return grouped;
  }

  private MigrationResult parseRows(List<String> rows) {
    LinkedHashSet<String> options = new LinkedHashSet<>();
    boolean hasJsonRow = false;

    for (String row : rows) {
      String trimmed = row == null ? "" : row.trim();
      if (trimmed.isEmpty()) {
        continue;
      }

      if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
        hasJsonRow = true;
        List<String> parsed = parseJsonArray(trimmed);
        if (!parsed.isEmpty()) {
          parsed.stream().map(String::trim).filter(s -> !s.isEmpty()).forEach(options::add);
          continue;
        }
      }

      options.add(trimmed);
    }

    boolean needsMigration = hasJsonRow;
    return new MigrationResult(new ArrayList<>(options), needsMigration);
  }

  private List<String> parseJsonArray(String jsonArrayText) {
    try {
      return objectMapper.readValue(jsonArrayText, new TypeReference<List<String>>() {
      });
    } catch (Exception ignore) {
      String normalized = jsonArrayText.substring(1, jsonArrayText.length() - 1).trim();
      if (normalized.isEmpty()) {
        return List.of();
      }
      String[] parts = normalized.split(",");
      List<String> list = new ArrayList<>(parts.length);
      for (String part : parts) {
        String value = part.trim();
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
          value = value.substring(1, value.length() - 1);
        }
        if (!value.isBlank()) {
          list.add(value);
        }
      }
      return list;
    }
  }

  private record MigrationResult(List<String> options, boolean needsMigration) {
  }
}
