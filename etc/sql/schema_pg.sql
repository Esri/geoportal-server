-- See the NOTICE file distributed with
-- this work for additional information regarding copyright ownership.
-- Esri Inc. licenses this file to You under the Apache License, Version 2.0
-- (the "License"); you may not use this file except in compliance with
-- the License.  You may obtain a copy of the License at
-- 
--      http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.

-- User table -------------------------------------------------------------------

-- First drop table GPT_USER in order to drop sequence gpt_user_seq -------
DROP TABLE gpt_user;

DROP SEQUENCE gpt_user_seq;
CREATE SEQUENCE gpt_user_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;


CREATE TABLE gpt_user
(
  userid integer NOT NULL DEFAULT nextval('gpt_user_seq'::regclass),
  dn character varying(900),
  username character varying(64),
  CONSTRAINT gpt_user_pk PRIMARY KEY (userid)
) 
WITHOUT OIDS;


DROP INDEX gpt_user_idx1;
CREATE INDEX gpt_user_idx1
  ON gpt_user
  USING btree
  (dn);


DROP INDEX gpt_user_idx2;
CREATE INDEX gpt_user_idx2
  ON gpt_user
  USING btree
  (username); 

-- Saved search table ----------------------------------------------------------

DROP TABLE gpt_search;
CREATE TABLE gpt_search
(
  uuid character varying(38) NOT NULL,
  name character varying(255),
  userid numeric(32),
  criteria text,
  CONSTRAINT gpt_search_pk PRIMARY KEY (uuid)
) 
WITHOUT OIDS;


-- Index: gpt_search_idx1 --------

DROP INDEX gpt_search_idx1;
CREATE INDEX gpt_search_idx1
  ON gpt_search
  USING btree
  (userid);

-- Create Pending Harvesting Jobs table ------------------------------------------------

DROP TABLE gpt_harvesting_jobs_pending;
CREATE TABLE gpt_harvesting_jobs_pending
(
  uuid character varying(38) NOT NULL,
  harvest_id character varying(38) NOT NULL,
  input_date timestamp without time zone DEFAULT now(),
  harvest_date timestamp without time zone DEFAULT now(),
  job_status character varying(10),
  job_type character varying(10),
  criteria character varying(1024) NULL,
  service_id character varying(128),
  CONSTRAINT gpt_harvjobspndg_pk PRIMARY KEY (harvest_id)
) 
WITHOUT OIDS;


-- Index: fki_harvestjobspndg_harvesting ---------

DROP INDEX fki_harvestjobspndg_harvesting;
CREATE INDEX fki_harvestjobspndg_harvesting
  ON gpt_harvesting_jobs_pending
  USING btree
  (harvest_id);

-- Index: gpt_hjobspndg_idx1 ----------

DROP INDEX gpt_hjobspndg_idx1;
CREATE INDEX gpt_hjobspndg_idx1
  ON gpt_harvesting_jobs_pending
  USING btree
  (uuid);

-- Index: gpt_hjobspndg_idx2 -------

DROP INDEX gpt_hjobspndg_idx2;
CREATE INDEX gpt_hjobspndg_idx2
  ON gpt_harvesting_jobs_pending
  USING btree
  (harvest_date);

-- Index: gpt_hjobspndg_idx3 -------- 

DROP INDEX gpt_hjobspndg_idx3;
CREATE INDEX gpt_hjobspndg_idx3
  ON gpt_harvesting_jobs_pending
  USING btree
  (input_date);



-- Create Completed Harvesting Jobs table ------------------------------------------------

-- Table: gpt_harvesting_jobs_completed ---------------

DROP TABLE gpt_harvesting_jobs_completed;
CREATE TABLE gpt_harvesting_jobs_completed
(
  uuid character varying(38) NOT NULL,
  harvest_id character varying(38) NOT NULL,
  input_date timestamp without time zone DEFAULT now(),
  harvest_date character varying DEFAULT now(),
  job_type character varying(10),
  service_id character varying(128),
  CONSTRAINT gpt_harvestjobscmpltd_pk PRIMARY KEY (uuid)
) 
WITHOUT OIDS;


-- Index: fki_gpt_harvjobscmpltd_harvesting ------

DROP INDEX fki_gpt_harvjobscmpltd_harvesting;
CREATE INDEX fki_gpt_harvjobscmpltd_harvesting
  ON gpt_harvesting_jobs_completed
  USING btree
  (harvest_id);

-- Index: gpt_hjobscmpltd_idx1 ---------

DROP INDEX gpt_hjobscmpltd_idx1;
CREATE INDEX gpt_hjobscmpltd_idx1
  ON gpt_harvesting_jobs_completed
  USING btree
  (harvest_id);


-- Create Harvesting History table ---------------------------------------------

-- Table: gpt_harvesting_history --------

DROP TABLE gpt_harvesting_history;
CREATE TABLE gpt_harvesting_history
(
  uuid character varying(38) NOT NULL,
  harvest_id character varying(38) NOT NULL,
  harvest_date timestamp without time zone DEFAULT now(),
  harvested_count numeric(32) DEFAULT 0,
  validated_count numeric(32) DEFAULT 0,
  published_count numeric(32) DEFAULT 0,
  harvest_report text,
  CONSTRAINT gpt_harvhist_pk PRIMARY KEY (uuid)
) 
WITHOUT OIDS;


-- Index: fki_gpt_harvhist_harvesting_fk ------------

DROP INDEX fki_gpt_harvhist_harvesting_fk;
CREATE INDEX fki_gpt_harvhist_harvesting_fk
  ON gpt_harvesting_history
  USING btree
  (harvest_id);  

-- Create Resources table ---------------------------------------------------------  

CREATE SEQUENCE gpt_resource_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE GPT_RESOURCE (
  
  DOCUUID           character varying(38) NOT NULL,
  TITLE             character varying(4000),
  OWNER             numeric NOT NULL,
  INPUTDATE         timestamp without time zone DEFAULT now(),
  UPDATEDATE        timestamp without time zone DEFAULT now(),
  ID                numeric(32) NOT NULL DEFAULT nextval('gpt_resource_seq'::regclass),
  APPROVALSTATUS    character varying(64),
  PUBMETHOD         character varying(64),
  SITEUUID          character varying(38),
  SOURCEURI         character varying(4000),
  FILEIDENTIFIER    character varying(4000),
  ACL               character varying(4000),
  HOST_URL          character varying(255), 
  PROTOCOL_TYPE     character varying(20), 
  PROTOCOL          character varying(1000),
  FREQUENCY         character varying(10),
  SEND_NOTIFICATION character varying(10),
  FINDABLE          character varying(6),
  SEARCHABLE        character varying(6),
  SYNCHRONIZABLE    character varying(6),
  LASTSYNCDATE      timestamp without time zone,
  
  CONSTRAINT GPT_RESOURCE_PK PRIMARY KEY (DOCUUID)
)
WITHOUT OIDS;

CREATE INDEX GPT_RESOURCE_IDX1  ON GPT_RESOURCE USING btree(SITEUUID);
CREATE INDEX GPT_RESOURCE_IDX2  ON GPT_RESOURCE USING btree(FILEIDENTIFIER);
CREATE INDEX GPT_RESOURCE_IDX3  ON GPT_RESOURCE USING btree(SOURCEURI);
CREATE INDEX GPT_RESOURCE_IDX4  ON GPT_RESOURCE USING btree(UPDATEDATE);
CREATE INDEX GPT_RESOURCE_IDX5  ON GPT_RESOURCE USING btree(TITLE);
CREATE INDEX GPT_RESOURCE_IDX6  ON GPT_RESOURCE USING btree(OWNER);
CREATE INDEX GPT_RESOURCE_IDX8  ON GPT_RESOURCE USING btree(APPROVALSTATUS);
CREATE INDEX GPT_RESOURCE_IDX9  ON GPT_RESOURCE USING btree(PUBMETHOD);
CREATE INDEX GPT_RESOURCE_IDX11 ON GPT_RESOURCE USING btree(ACL);
CREATE INDEX GPT_RESOURCE_IDX12 ON GPT_RESOURCE USING btree(PROTOCOL_TYPE);
CREATE INDEX GPT_RESOURCE_IDX13 ON GPT_RESOURCE USING btree(LASTSYNCDATE);

CREATE TABLE GPT_RESOURCE_DATA (
  DOCUUID           character varying(38) NOT NULL,
  ID                numeric(32) UNIQUE NOT NULL,
  XML               text,
  THUMBNAIL         bytea,
  CONSTRAINT GPT_RESOURCE_DATA_PK PRIMARY KEY (DOCUUID)
);

CREATE INDEX GPT_RESOURCE_DATA_IDX1  ON GPT_RESOURCE_DATA USING btree(ID);


