CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    role_id BIGINT,
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_name ON sys_user(user_name);
CREATE INDEX IF NOT EXISTS idx_role_id ON sys_user(role_id);

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) UNIQUE,
    description VARCHAR(200),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_role_code ON sys_role(role_code);

CREATE TABLE IF NOT EXISTS production_trace (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sn_code VARCHAR(100) UNIQUE NOT NULL,
    product_model VARCHAR(100),
    work_order_no VARCHAR(50),
    batch_no VARCHAR(50),
    production_date TIMESTAMP,
    status VARCHAR(20),
    material_list TEXT,
    production_history TEXT,
    quality_records TEXT,
    test_data TEXT,
    delivery_info TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sn_code ON production_trace(sn_code);
CREATE INDEX IF NOT EXISTS idx_batch_no ON production_trace(batch_no);

CREATE TABLE IF NOT EXISTS exception_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sn_code VARCHAR(100),
    exception_type VARCHAR(50),
    exception_level VARCHAR(20),
    description TEXT,
    images TEXT,
    location VARCHAR(100),
    operator VARCHAR(50),
    status VARCHAR(20) DEFAULT 'pending',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS station_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    station_code VARCHAR(50) UNIQUE NOT NULL,
    station_name VARCHAR(100),
    process VARCHAR(100),
    operator VARCHAR(50),
    sop_content TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_station_code ON station_info(station_code);

CREATE TABLE IF NOT EXISTS sop_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    station_id BIGINT,
    step_title VARCHAR(200),
    step_description TEXT,
    step_image VARCHAR(500),
    step_video VARCHAR(500),
    step_poster VARCHAR(500),
    sort_order INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_station_id ON sop_detail(station_id);

CREATE TABLE IF NOT EXISTS ai_document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255),
    file_path VARCHAR(500),
    file_type VARCHAR(50),
    content TEXT,
    summary TEXT,
    status INT DEFAULT 1,
    uploaded_by VARCHAR(50),
    upload_time TIMESTAMP,
    sync_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ai_document_chunk (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT,
    content TEXT,
    keywords VARCHAR(500),
    chunk_index INT,
    total_chunks INT,
    embedding TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_document_id ON ai_document_chunk(document_id);