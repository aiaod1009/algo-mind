CREATE TABLE IF NOT EXISTS t_completed_error_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    level_id BIGINT NULL,
    title VARCHAR(200) NULL,
    level_type VARCHAR(50) NULL,
    question VARCHAR(1000) NULL,
    user_answer VARCHAR(500) NULL,
    description VARCHAR(500) NULL,
    correct_answer VARCHAR(500) NULL,
    analysis_status VARCHAR(50) NULL,
    analysis VARCHAR(2000) NULL,
    source_error_id BIGINT NULL,
    created_at DATETIME NULL,
    last_error_at DATETIME NULL,
    completed_at DATETIME NOT NULL,
    INDEX idx_completed_error_user_completed (user_id, completed_at),
    INDEX idx_completed_error_user_level (user_id, level_id)
);
