CREATE TABLE project_members
(
    project_id BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    CONSTRAINT pk_project_members PRIMARY KEY (project_id, user_id)
);

ALTER TABLE project_members
    ADD CONSTRAINT fk_promem_on_project FOREIGN KEY (project_id) REFERENCES projects (id);

ALTER TABLE project_members
    ADD CONSTRAINT fk_promem_on_user FOREIGN KEY (user_id) REFERENCES users (id);