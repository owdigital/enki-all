--
-- Name: services; Type: TABLE;
--

CREATE TABLE services (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v1(),
    url TEXT NOT NULL,
    name TEXT NOT NULL
);

--
-- Name: service; Type: TABLE;
--

CREATE TABLE service (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v1(),
    user_id UUID REFERENCES "user" (id) NOT NULL,
    service_id UUID REFERENCES services (id) NOT NULL,
    proof_url TEXT NOT NULL
);

CREATE UNIQUE INDEX user_id_service_user_id_service_user_id_idx ON service (user_id, service_id);

insert into services (name, url) VALUES
    ('facebook', 'https://www.facebook.com/'),
    ('twitter', 'https://www.twitter.com/');
    