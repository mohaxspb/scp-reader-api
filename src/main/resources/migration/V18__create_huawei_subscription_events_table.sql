create table if not exists huawei_subscription_event_handle_attempt
(
    id                         bigserial not null,

    status_update_notification text      not null,
    notification_signature     text      not null,

    error_class                text,
    error_message              text,
    stacktrace                 text,
    cause_error_class          text,
    cause_error_message        text,
    cause_stacktrace           text,

    created                    timestamp,
    updated                    timestamp,
    primary key (id)
);