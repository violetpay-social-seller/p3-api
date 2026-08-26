CREATE TABLE operator_action_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    operator_user_id UUID NOT NULL,
    action_type VARCHAR(60) NOT NULL,
    target_type VARCHAR(60) NOT NULL,
    target_id UUID NOT NULL,
    reason TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_operator_action_logs_operator_user_id
        FOREIGN KEY (operator_user_id) REFERENCES users (id)
);

CREATE TABLE reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_user_id UUID NOT NULL,
    target_type VARCHAR(60) NOT NULL,
    target_id UUID NOT NULL,
    reason TEXT NOT NULL,
    evidence TEXT,
    status VARCHAR(30) NOT NULL,
    assigned_operator_id UUID,
    resolution TEXT,
    resolved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_reports_reporter_user_id FOREIGN KEY (reporter_user_id) REFERENCES users (id),
    CONSTRAINT fk_reports_assigned_operator_id
        FOREIGN KEY (assigned_operator_id) REFERENCES users (id)
);

CREATE TABLE service_inquiries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requester_user_id UUID,
    title VARCHAR(150) NOT NULL,
    body TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    assignee_operator_id UUID,
    answer TEXT,
    answered_at TIMESTAMPTZ,
    closed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_service_inquiries_requester_user_id
        FOREIGN KEY (requester_user_id) REFERENCES users (id),
    CONSTRAINT fk_service_inquiries_assignee_operator_id
        FOREIGN KEY (assignee_operator_id) REFERENCES users (id)
);

CREATE TABLE order_status_histories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    previous_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    changed_by UUID,
    reason TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_order_status_histories_order_id
        FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_status_histories_changed_by
        FOREIGN KEY (changed_by) REFERENCES users (id)
);

CREATE INDEX ix_operator_action_logs_target ON operator_action_logs (target_type, target_id);
CREATE INDEX ix_operator_action_logs_operator_created_at
    ON operator_action_logs (operator_user_id, created_at DESC);
CREATE INDEX ix_reports_status_created_at ON reports (status, created_at DESC);
CREATE INDEX ix_reports_target ON reports (target_type, target_id);
CREATE INDEX ix_service_inquiries_status_created_at ON service_inquiries (status, created_at DESC);
CREATE INDEX ix_order_status_histories_order_created_at
    ON order_status_histories (order_id, created_at DESC);
