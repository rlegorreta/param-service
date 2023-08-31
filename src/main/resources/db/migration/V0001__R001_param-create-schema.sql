DROP EXTENSION IF EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS sys_dates (
    id UUID DEFAULT uuid_generate_v4(),
    name varchar(100) NOT NULL,
    day date NOT NULL,
    PRIMARY KEY(id)
    );

CREATE TABLE IF NOT EXISTS sys_rates (
    id UUID DEFAULT uuid_generate_v4(),
    name varchar(100) NOT NULL,
    rate numeric NOT NULL,
    PRIMARY KEY(id)
    );

CREATE TABLE IF NOT EXISTS document_types (
    id UUID DEFAULT uuid_generate_v4(),
    name varchar(100) NOT NULL,
    expiration varchar(40) NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS templates (
    id UUID DEFAULT uuid_generate_v4(),
    name varchar(100) NOT NULL,
    file_repo varchar(256),
    channel varchar(20),
    json_code varchar(1024),
    blockly_blocks varchar(5120),
    creation_Date date NOT NULL,
    modification_Date date NOT NULL,
    author varchar(40) NOT NULL,
    active boolean NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS template_fields (
    id UUID DEFAULT uuid_generate_v4(),
    id_template UUID NOT NULL,
    name varchar(20) NOT NULL,
    type varchar(12) NOT NULL,
    default_value varchar(1024),
    PRIMARY KEY(id),
    FOREIGN KEY (id_template) REFERENCES templates(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS data_sources (
     id UUID DEFAULT uuid_generate_v4(),
     name varchar(100) NOT NULL,
     json_code varchar(3072),
     blockly_block varchar(20480),
     config_code varchar(2048),
     config_blockly_block varchar(5120),
     mapping varchar(1024),
     creation_Date date NOT NULL,
     modification_Date date NOT NULL,
     author varchar(40) NOT NULL,
     active boolean NOT NULL,
     PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS data_source_fields (
       id UUID DEFAULT uuid_generate_v4(),
       id_data_source UUID NOT NULL,
       name varchar(20) NOT NULL,
       type varchar(12) NOT NULL,
       validations VARCHAR(512),
       PRIMARY KEY(id),
       FOREIGN KEY (id_data_source) REFERENCES data_sources(id) ON DELETE CASCADE
);
