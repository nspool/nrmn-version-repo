CREATE TABLE nrmn.diver_ref_aud (
    diver_id integer NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    full_name varchar(255),
    full_name_mod boolean,
    initials varchar(255),
    initials_mod boolean,
    CONSTRAINT diver_ref_aud_pkey PRIMARY KEY (diver_id, rev)
);

CREATE TABLE nrmn.error_check (
    job_id varchar(255) NOT NULL,
    message varchar(255) NOT NULL,
    column_target varchar(255),
    error_level varchar(255),
    row_id bigint NOT NULL,
    CONSTRAINT error_check_pkey PRIMARY KEY (job_id, message, row_id)
);

CREATE TABLE nrmn.revinfo (
    id integer NOT NULL,
    timestamp bigint NOT NULL,
    api_request_id varchar(255),
    username varchar(255),
    CONSTRAINT revinfo_pkey PRIMARY KEY (id)
);

CREATE TABLE nrmn.sec_user_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    email_address varchar(255),
    email_mod boolean,
    full_name varchar(255),
    full_name_mod boolean,
    hashed_password varchar(255),
    hashed_password_mod boolean,
    status varchar(255),
    status_mod boolean,
    CONSTRAINT sec_user_aud_pkey PRIMARY KEY (id, rev)
);

CREATE TABLE nrmn.staged_job (
    file_id varchar(255) NOT NULL,
    job_attributes jsonb,
    source varchar(255),
    status varchar(255),
    CONSTRAINT staged_job_pkey PRIMARY KEY (file_id)
);

CREATE TABLE nrmn.staged_survey (
    id bigserial NOT NULL,
    common_name varchar(255),
    l5 integer,
    l95 integer,
    pqs integer,
    block integer,
    buddy varchar(255),
    code varchar(255),
    date date,
    depth float8,
    direction varchar(255),
    diver varchar(255),
    inverts integer,
    is_invert_sizing boolean,
    latitude float8,
    longitude float8,
    m2_invert_sizing_species boolean,
    measure_value json,
    method integer,
    site_name varchar(255),
    site_no varchar(255),
    species varchar(255),
    time float8,
    total integer,
    vis integer,
    staged_job_file_id varchar(255),
    CONSTRAINT staged_survey_pkey PRIMARY KEY (id)
);

CREATE TABLE nrmn.observation_aud (
    observation_id integer NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    measure_value integer,
    measure_value_mod boolean,
    observation_attribute jsonb,
    observation_attribute_mod boolean,
    CONSTRAINT observation_aud_pkey PRIMARY KEY (observation_id, rev)
);

CREATE TABLE nrmn.survey_method_aud (
    survey_method_id integer NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    block_num integer,
    block_num_mod boolean,
    survey_not_done boolean,
    survey_not_done_mod boolean,
    CONSTRAINT survey_method_aud_pkey PRIMARY KEY (survey_method_id, rev)
);

CREATE TABLE nrmn.survey_aud (
    survey_id integer NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    depth integer,
    depth_mod boolean,
    direction varchar(255),
    direction_mod boolean,
    survey_attribute jsonb,
    survey_attribute_mod boolean,
    survey_date date,
    survey_date_mod boolean,
    survey_num integer,
    survey_num_mod boolean,
    survey_time time without time zone,
    survey_time_mod boolean,
    visibility integer,
    visibility_mod boolean,
    CONSTRAINT survey_aud_pkey PRIMARY KEY (survey_id, rev)
);

CREATE TABLE nrmn.site_ref_aud (
    site_id integer NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    is_active boolean,
    is_active_mod boolean,
    latitude float8,
    latitude_mod boolean,
    longitude float8,
    longitude_mod boolean,
    site_attribute jsonb,
    site_attribute_mod boolean,
    site_code varchar(255),
    site_code_mod boolean,
    site_name varchar(255),
    site_name_mod boolean,
    CONSTRAINT site_ref_aud_pkey PRIMARY KEY (site_id, rev)
);

CREATE TABLE nrmn.location_ref_aud (
    location_id integer NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    is_active boolean,
    is_active_mod boolean,
    location_name varchar(255),
    location_name_mod boolean,
    CONSTRAINT location_ref_aud_pkey PRIMARY KEY (location_id, rev)
);

CREATE TABLE nrmn.sec_role (
    name varchar(255) NOT NULL,
    version integer NOT NULL,
    CONSTRAINT sec_role_pkey PRIMARY KEY (name)
);

CREATE TABLE nrmn.sec_user_roles (
    sec_user_id bigint NOT NULL,
    sec_role_id varchar(255) NOT NULL,
    CONSTRAINT sec_user_roles_pkey PRIMARY KEY (sec_user_id, sec_role_id)
);

CREATE TABLE nrmn.observable_item_ref_aud (
    observable_item_id integer NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    obs_item_attribute jsonb,
    obs_item_attribute_mod boolean,
    observable_item_name varchar(255),
    observable_item_name_mod boolean,
    CONSTRAINT observable_item_ref_aud_pkey PRIMARY KEY (observable_item_id, rev)
);

CREATE TABLE nrmn.public_data_exclusion (
    program_id integer NOT NULL,
    site_id integer NOT NULL,
    CONSTRAINT public_data_exclusion_pkey PRIMARY KEY (program_id, site_id)
);

CREATE TABLE nrmn.sec_user (
    id bigint NOT NULL,
    email_address varchar(255) NOT NULL,
    full_name varchar(255),
    hashed_password varchar(255),
    status varchar(255) NOT NULL,
    version integer NOT NULL,
    CONSTRAINT sec_user_pkey PRIMARY KEY (id)
);

CREATE TABLE nrmn.user_action_aud (
    id bigint NOT NULL,
    audit_time timestamp with time zone,
    details text,
    operation varchar(255),
    request_id varchar(255),
    username varchar(255),
    CONSTRAINT user_action_aud_pkey PRIMARY KEY (id)
);

ALTER TABLE nrmn.sec_user_aud
    ADD CONSTRAINT fk1tqqojx2q75iy64166aehon7p FOREIGN KEY (rev) REFERENCES nrmn.revinfo (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE nrmn.staged_survey
    ADD CONSTRAINT fkc53smpawan288nu3oqugn8b06 FOREIGN KEY (staged_job_file_id) REFERENCES nrmn.staged_job (file_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE nrmn.observation_aud
    ADD CONSTRAINT fkctpj5torreec5ut7jcsxjwxtd FOREIGN KEY (rev) REFERENCES nrmn.revinfo (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE nrmn.survey_method_aud
    ADD CONSTRAINT fkk0pl3e2pnxqsx8schxcqakf4p FOREIGN KEY (rev) REFERENCES nrmn.revinfo (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE nrmn.survey_aud
    ADD CONSTRAINT fklqcbssyix1l4orhbnrvd9khta FOREIGN KEY (rev) REFERENCES nrmn.revinfo (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE nrmn.site_ref_aud
    ADD CONSTRAINT fkoj8hgo02f1vvoas72bogiv97t FOREIGN KEY (rev) REFERENCES nrmn.revinfo (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE nrmn.location_ref_aud
    ADD CONSTRAINT fkqcdhb4kma1glcjulq39i8hofn FOREIGN KEY (rev) REFERENCES nrmn.revinfo (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE nrmn.sec_user_roles
    ADD CONSTRAINT fk_role_user_sec FOREIGN KEY (sec_role_id) REFERENCES nrmn.sec_role (name) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE nrmn.observable_item_ref_aud
    ADD CONSTRAINT fksehkdmw8opm6n0ytxsmtcjx9l FOREIGN KEY (rev) REFERENCES nrmn.revinfo (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE nrmn.public_data_exclusion
    ADD CONSTRAINT fksq3vap0t8ruo7d5ghdt5imphh FOREIGN KEY (program_id) REFERENCES nrmn.program_ref (program_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

CREATE UNIQUE INDEX unique_email ON nrmn.sec_user (email_address);

ALTER TABLE nrmn.diver_ref_aud
    ADD CONSTRAINT fk1nahs3dov9lbpxnmeafoyl82i FOREIGN KEY (rev) REFERENCES nrmn.revinfo (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE nrmn.error_check
    ADD CONSTRAINT fkhmycainhljtnhm0ywwutb308w FOREIGN KEY (row_id) REFERENCES nrmn.staged_survey (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE nrmn.sec_user_roles
    ADD CONSTRAINT fk_user_sec_role FOREIGN KEY (sec_user_id) REFERENCES nrmn.sec_user (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE nrmn.public_data_exclusion
    ADD CONSTRAINT fksya8iyraotp866qvhggmwgmkp FOREIGN KEY (site_id) REFERENCES nrmn.site_ref (site_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

CREATE SEQUENCE IF NOT EXISTS nrmn.hibernate_sequence;

CREATE SEQUENCE IF NOT EXISTS nrmn.user_id_seq;
