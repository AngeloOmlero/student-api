
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
    delivered BOOLEAN DEFAULT false NOT NULL,
    read BOOLEAN DEFAULT false NOT NULL
);

CREATE INDEX idx_messages_sender_receiver_created_at ON messages(sender_id, receiver_id, created_at);
CREATE INDEX idx_messages_receiver_unread ON messages(receiver_id, read);
