
SET client_encoding = 'UTF8';

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

--
-- Name: user; Type: TABLE;
--

CREATE TABLE "user" (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v1(),
    name text NOT NULL UNIQUE,
    password text NOT NULL
);

--
-- Name: bank; Type: TABLE;
--

CREATE TABLE bank (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v1(),
    user_id UUID REFERENCES "user" (id) NOT NULL,
    pub_key TEXT NOT NULL,
    consus_user TEXT NOT NULL,
    agent_url TEXT NOT NULL
);

--
-- Name: pii_type; Type: TABLE;
--

CREATE TABLE pii_type (
  id VARCHAR(256) PRIMARY KEY,
  description TEXT NOT NULL
);

--
-- Name: metadata_assertion; Type: TABLE;
--

CREATE TABLE metadata_assertion (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v1(),
    bank_id UUID REFERENCES bank (id) NOT NULL,
    subject TEXT NOT NULL,
    pii_type_id VARCHAR(256) REFERENCES pii_type (id) NOT NULL,
    location TEXT NOT NULL,
    created timestamp without time zone NOT NULL,
    signature TEXT NOT NULL
);

CREATE INDEX metadata_assertion_subject_idx ON metadata_assertion (subject);

--
-- Name: sharing_purpose; Type: TABLE;
--

CREATE TABLE sharing_purpose (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v1(),
  description TEXT NOT NULL
);

--
-- Name: share_assertion; Type: TABLE;
--

CREATE TABLE share_assertion (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v1(),
    metadata_assertion_id UUID REFERENCES metadata_assertion (id) NOT NULL,
    sharing_bank_id UUID REFERENCES bank (id) NOT NULL,
    signature TEXT NOT NULL,
    purpose_id UUID REFERENCES sharing_purpose (id) NOT NULL,
    created timestamp without time zone NOT NULL,
    start_date timestamp without time zone NOT NULL,
    end_date timestamp without time zone NOT NULL
);

--
-- Name: user_association; Type: TABLE;
--

CREATE TABLE user_association (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v1(),
    user_id UUID REFERENCES "user" (id) NOT NULL,
    bank_id UUID REFERENCES bank (id) NOT NULL,
    bank_user_id text NOT NULL
);

CREATE UNIQUE INDEX user_association_user_id_bank_user_id_idx ON user_association (user_id, bank_user_id);

--
-- Name: metadata_assertion_revocation; Type: TABLE;
--

CREATE TABLE metadata_assertion_revocation (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v1(),
    created timestamp without time zone NOT NULL,
    valid_from timestamp without time zone NOT NULL,
    user_id UUID REFERENCES "user" (id),
    metadata_assertion_id UUID REFERENCES metadata_assertion (id) NOT NULL
);

--
-- Name: share_assertion_revocation; Type: TABLE;
--

CREATE TABLE share_assertion_revocation (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v1(),
    created timestamp without time zone NOT NULL,
    valid_from timestamp without time zone NOT NULL,
    user_id UUID REFERENCES "user" (id),
    share_assertion_id UUID REFERENCES share_assertion (id) NOT NULL
);


