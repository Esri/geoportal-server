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

if exists (select * from dbo.sysobjects where id = object_id(N'[GPT_USER]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
  drop table [GPT_USER]
GO

CREATE TABLE [GPT_USER] ( 
  [USERID] [numeric](32, 0) IDENTITY(1,1) NOT NULL,
  [DN] [nvarchar](900) ,
  [USERNAME] [nvarchar](64) ,
  CONSTRAINT GPT_USER_PK PRIMARY KEY (USERID)
)
GO

CREATE INDEX GPT_USER_IDX1 ON GPT_USER(DN)
GO

CREATE INDEX GPT_USER_IDX2 ON GPT_USER(USERNAME)
GO

-- Saved search table ----------------------------------------------------------

if exists (select * from dbo.sysobjects where id = object_id(N'[GPT_SEARCH]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
  drop table [GPT_SEARCH]
GO

CREATE TABLE GPT_SEARCH (
  [UUID] [nvarchar](38) NOT NULL ,
  [NAME] [nvarchar] (255) NULL,
  [USERID] [numeric](32, 0)  ,
  [CRITERIA] [ntext] NULL, 
  CONSTRAINT GPT_SEARCH_PK PRIMARY KEY (UUID)
);

CREATE INDEX GPT_SEARCH_IDX1 ON GPT_SEARCH(USERID);


-- Create Harvesting Jobs table ------------------------------------------------

CREATE TABLE [GPT_HARVESTING_JOBS_PENDING] (
  [UUID] [nvarchar](38) NOT NULL ,
  [HARVEST_ID] [nvarchar](38) NOT NULL ,
  [INPUT_DATE] [datetime] NULL DEFAULT (getdate()),
  [HARVEST_DATE] [datetime] NULL DEFAULT (getdate()),
  [JOB_STATUS] [nvarchar](10) NOT NULL ,
  [JOB_TYPE] [nvarchar](10) NOT NULL ,
  [CRITERIA] [nvarchar](1024) NULL,
  [SERVICE_ID] [nvarchar](128) ,
  CONSTRAINT [GPT_HARVJOBSPNDG_PK] PRIMARY KEY (
    [HARVEST_ID]
  )
)
GO

CREATE INDEX GPT_HJOBSPNDG_IDX1 ON GPT_HARVESTING_JOBS_PENDING(UUID)
GO

CREATE INDEX GPT_HJOBSPNDG_IDX2 ON GPT_HARVESTING_JOBS_PENDING(HARVEST_DATE)
GO

CREATE INDEX GPT_HJOBSPNDG_IDX3 ON GPT_HARVESTING_JOBS_PENDING(INPUT_DATE)
GO

CREATE TABLE [GPT_HARVESTING_JOBS_COMPLETED] (
  [UUID] [nvarchar](38) NOT NULL ,
  [HARVEST_ID] [nvarchar](38) NOT NULL ,
  [INPUT_DATE] [datetime] NULL DEFAULT (getdate()),
  [HARVEST_DATE] [datetime] NULL DEFAULT (getdate()),
  [JOB_TYPE] [nvarchar](10) NOT NULL ,
  [SERVICE_ID] [nvarchar](128) ,
  CONSTRAINT [GPT_HARVJOBSCMPLTD_PK] PRIMARY KEY (
    [UUID]
  )
)
GO

CREATE INDEX GPT_HJOBSCMPLTD_IDX1 ON GPT_HARVESTING_JOBS_COMPLETED(HARVEST_ID)
GO

-- Create Harvesting History table ---------------------------------------------

CREATE TABLE [GPT_HARVESTING_HISTORY] (
  [UUID] [nvarchar](38) NOT NULL ,
  [HARVEST_ID] [nvarchar](38) NOT NULL ,
  [HARVEST_DATE] [datetime] NULL DEFAULT (getdate()),
  [HARVESTED_COUNT] [numeric](10, 0) DEFAULT 0,
  [VALIDATED_COUNT] [numeric](10, 0) DEFAULT 0,
  [PUBLISHED_COUNT] [numeric](10, 0) DEFAULT 0,
  [HARVEST_REPORT] [ntext] NULL, 
  CONSTRAINT [GPT_HARVHIST_PK] PRIMARY KEY (
    [UUID]
  )
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

-- Create resource table ---------------------------------------------------------
if exists (select * from dbo.sysobjects where id = object_id(N'[GPT_RESOURCE_DATA]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
  drop table [GPT_RESOURCE_DATA]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[GPT_RESOURCE]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
  drop table [GPT_RESOURCE]
GO

CREATE TABLE [GPT_RESOURCE] (
   [DOCUUID]           [nvarchar](38) PRIMARY KEY,
   [TITLE]             [nvarchar](4000),
   [OWNER]             [numeric](32,0),
   [INPUTDATE]         [datetime] DEFAULT (getdate()),
   [UPDATEDATE]        [datetime] DEFAULT (getdate()),
   [ID]                [numeric](10, 0) IDENTITY(1,1) NOT NULL,
   [APPROVALSTATUS]    [nvarchar](64),
   [PUBMETHOD]         [nvarchar](64),
   [SITEUUID]          [nvarchar](38),
   [SOURCEURI]         [nvarchar](4000),
   [FILEIDENTIFIER]    [nvarchar](4000),
   [ACL]               [nvarchar](4000),
   [HOST_URL]          [nvarchar](255) NULL,
   [PROTOCOL_TYPE]     [nvarchar](20) NULL, 
   [PROTOCOL]          [nvarchar](1000) NULL, 
   [FREQUENCY]         [nvarchar](10),
   [SEND_NOTIFICATION] [nvarchar](10),
   [FINDABLE]          [nvarchar](6),
   [SEARCHABLE]        [nvarchar](6),
   [SYNCHRONIZABLE]    [nvarchar](6),
   [LASTSYNCDATE]      [datetime]
)
GO

CREATE INDEX GPT_RESOURCE_IDX1  ON GPT_RESOURCE(SITEUUID)
GO

CREATE INDEX GPT_RESOURCE_IDX2  ON GPT_RESOURCE(FILEIDENTIFIER)
GO

CREATE INDEX GPT_RESOURCE_IDX3  ON GPT_RESOURCE(SOURCEURI)
GO

CREATE INDEX GPT_RESOURCE_IDX4  ON GPT_RESOURCE(UPDATEDATE)
GO

CREATE INDEX GPT_RESOURCE_IDX5  ON GPT_RESOURCE(TITLE)
GO

CREATE INDEX GPT_RESOURCE_IDX6  ON GPT_RESOURCE(OWNER)
GO

CREATE INDEX GPT_RESOURCE_IDX8  ON GPT_RESOURCE(APPROVALSTATUS)
GO

CREATE INDEX GPT_RESOURCE_IDX9  ON GPT_RESOURCE(PUBMETHOD)
GO

CREATE INDEX GPT_RESOURCE_IDX11 ON GPT_RESOURCE(ACL)
GO

CREATE INDEX GPT_RESOURCE_IDX12 ON GPT_RESOURCE(PROTOCOL_TYPE)
GO

CREATE INDEX GPT_RESOURCE_IDX13 ON GPT_RESOURCE(ID)
GO

CREATE INDEX GPT_RESOURCE_IDX14 ON GPT_RESOURCE(LASTSYNCDATE)
GO

CREATE TABLE [GPT_RESOURCE_DATA] (
  [DOCUUID]   [nvarchar](38) PRIMARY KEY,
  [ID]        [numeric](10, 0) UNIQUE NOT NULL,
  [XML]       [ntext] NULL,
  [THUMBNAIL] [varBinary](MAX)
)
GO

CREATE INDEX GPT_RESOURCE_DATA_IDX1 ON GPT_RESOURCE_DATA(ID)
GO
