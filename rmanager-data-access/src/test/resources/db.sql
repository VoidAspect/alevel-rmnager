create table managed_resources
(
    id             identity not null primary key,
    name           varchar  not null unique,
    description    varchar,
    total_capacity integer  not null,
    capacity       integer  not null,
    check (total_capacity > 0),
    check (capacity >= 0 and capacity <= total_capacity)
);

create sequence alloc_request_id_seq increment by 10;

create table alloc_requests
(
    id                         bigint  not null primary key,
    resource_id                bigint  not null references managed_resources (id),
    previous_resource_capacity bigint  not null,
    capacity                   bigint  not null,
    issued_at                  timestamp not null default current_timestamp,
    status                     tinyint not null,
    reason                     varchar,
    check (previous_resource_capacity >= 0)
);

create index alloc_requests_issued_at_idx on alloc_requests(issued_at)