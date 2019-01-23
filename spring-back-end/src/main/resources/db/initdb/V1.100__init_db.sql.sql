-- Create tables

CREATE TABLE Author (
  id      BIGINT AUTO_INCREMENT PRIMARY KEY,
  name    VARCHAR(255) NOT NULL,
  surname VARCHAR(255) NOT NULL,

  UNIQUE KEY (name, surname)
);

CREATE TABLE ResourceType (
  id   BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE Tag (
  id   BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE Publisher (
  id      BIGINT AUTO_INCREMENT PRIMARY KEY,
  name    VARCHAR(255) NOT NULL UNIQUE,
  website VARCHAR(255)
);

CREATE TABLE IssueDate (
  id    BIGINT AUTO_INCREMENT PRIMARY KEY,
  day   SMALLINT,
  month SMALLINT,
  year  INTEGER
);

CREATE TABLE LanguageTable (
  id   BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  code VARCHAR(255) NOT NULL
);

CREATE TABLE LicenseType (
  id   BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE Taxon (
  id    BIGINT AUTO_INCREMENT PRIMARY KEY,
  name  VARCHAR(255) NOT NULL,
  level VARCHAR(255) NOT NULL
);

CREATE TABLE EducationalContext (
  id BIGINT PRIMARY KEY,

  FOREIGN KEY (id)
  REFERENCES Taxon (id)
    ON DELETE RESTRICT
);

CREATE TABLE Domain (
  id                 BIGINT PRIMARY KEY,
  educationalContext BIGINT NOT NULL,

  FOREIGN KEY (id)
  REFERENCES Taxon (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (educationalContext)
  REFERENCES EducationalContext (id)
    ON DELETE RESTRICT
);

CREATE TABLE Subject (
  id     BIGINT PRIMARY KEY,
  domain BIGINT NOT NULL,

  FOREIGN KEY (id)
  REFERENCES Taxon (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (domain)
  REFERENCES Domain (id)
    ON DELETE RESTRICT
);

CREATE TABLE Specialization (
  id     BIGINT PRIMARY KEY,
  domain BIGINT NOT NULL,

  FOREIGN KEY (id)
  REFERENCES Taxon (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (domain)
  REFERENCES Domain (id)
    ON DELETE RESTRICT
);

CREATE TABLE Module (
  id             BIGINT PRIMARY KEY,
  specialization BIGINT NOT NULL,

  FOREIGN KEY (id)
  REFERENCES Taxon (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (specialization)
  REFERENCES Specialization (id)
    ON DELETE RESTRICT
);

CREATE TABLE Topic (
  id      BIGINT PRIMARY KEY,
  subject BIGINT,
  domain  BIGINT,

  module  BIGINT,

  FOREIGN KEY (id)
  REFERENCES Taxon (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (subject)
  REFERENCES Subject (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (domain)
  REFERENCES Domain (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (module)
  REFERENCES Module (id)
    ON DELETE RESTRICT
);

CREATE TABLE Subtopic (
  id    BIGINT PRIMARY KEY,
  topic BIGINT NOT NULL,

  FOREIGN KEY (id)
  REFERENCES Taxon (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (topic)
  REFERENCES Topic (id)
    ON DELETE RESTRICT
);

CREATE TABLE CrossCurricularTheme (
  id   BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE KeyCompetence (
  id   BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE Repository (
  id                  BIGINT                   AUTO_INCREMENT PRIMARY KEY,
  baseURL             VARCHAR(255) UNIQUE NOT NULL,
  lastSynchronization TIMESTAMP           NULL DEFAULT NULL,
  schemaName          VARCHAR(255) UNIQUE NOT NULL,
  isEstonianPublisher BOOLEAN
);

CREATE TABLE User (
  id       BIGINT AUTO_INCREMENT PRIMARY KEY,
  userName VARCHAR(255) UNIQUE NOT NULL,
  name     VARCHAR(255)        NOT NULL,
  surName  VARCHAR(255)        NOT NULL,
  idCode   VARCHAR(11) UNIQUE  NOT NULL,
  role     VARCHAR(255)        NOT NULL
);

CREATE TABLE Person (
  id BIGINT AUTO_INCREMENT PRIMARY KEY
);

CREATE TABLE Institution (
  id     BIGINT AUTO_INCREMENT PRIMARY KEY,
  ehisId VARCHAR(255) NOT NULL,
  person BIGINT,

  FOREIGN KEY (person)
  REFERENCES Person (id)
    ON DELETE CASCADE
);

CREATE TABLE Institution_Roles (
  institution       BIGINT       NOT NULL,
  institutionalRole VARCHAR(255) NOT NULL,
  schoolClass       VARCHAR(255),
  schoolYear        VARCHAR(255),

  FOREIGN KEY (institution)
  REFERENCES Institution (id)
    ON DELETE CASCADE
);

CREATE TABLE AuthenticatedUser (
  id                 BIGINT  AUTO_INCREMENT PRIMARY KEY,
  user_id            BIGINT              NOT NULL,
  token              VARCHAR(255) UNIQUE NOT NULL,
  firstLogin         BOOLEAN DEFAULT FALSE,
  homeOrganization   VARCHAR(255),
  mails              VARCHAR(255),
  affiliations       VARCHAR(255),
  scopedAffiliations VARCHAR(255),
  person             BIGINT,
  loginDate          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (user_id)
  REFERENCES User (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (person)
  REFERENCES Person (id)
    ON DELETE CASCADE
);

CREATE TABLE AuthenticationState (
  id          BIGINT    AUTO_INCREMENT PRIMARY KEY,
  token       VARCHAR(255) UNIQUE NOT NULL,
  created     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  name        VARCHAR(255),
  surname     VARCHAR(255),
  idCode      VARCHAR(11),
  sessionCode VARCHAR(255)
);

CREATE TABLE Recommendation (
  id        BIGINT     AUTO_INCREMENT PRIMARY KEY,
  creator   BIGINT     NOT NULL,
  added     TIMESTAMP  DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (creator)
  REFERENCES User (id)
    ON DELETE RESTRICT
);

CREATE TABLE LearningObject (
  id                   BIGINT             AUTO_INCREMENT PRIMARY KEY,
  added                TIMESTAMP          DEFAULT CURRENT_TIMESTAMP,
  deleted              BOOLEAN,
  picture              LONGBLOB           DEFAULT NULL,
  updated              TIMESTAMP NULL     DEFAULT NULL,
  views                BIGINT    NOT NULL DEFAULT 0,
  creator              BIGINT,
  recommendation       BIGINT,

  FOREIGN KEY (creator)
  REFERENCES User (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (recommendation)
  REFERENCES Recommendation (id)
    ON DELETE RESTRICT
);

CREATE TABLE Material (
  id                   BIGINT             PRIMARY KEY,
  lang                 BIGINT,
  issueDate            BIGINT,
  licenseType          BIGINT,
  source               TEXT      NOT NULL,
  repositoryIdentifier VARCHAR(255),
  repository           BIGINT,
  paid                 BOOLEAN            DEFAULT FALSE,
  isSpecialEducation   BOOLEAN            DEFAULT FALSE,
  embeddable           BOOLEAN            DEFAULT FALSE,
  curriculumLiterature BOOLEAN            DEFAULT FALSE,

  UNIQUE KEY (repositoryIdentifier, repository),

  FOREIGN KEY (id)
  REFERENCES LearningObject (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (lang)
  REFERENCES LanguageTable (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (issueDate)
  REFERENCES IssueDate (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (repository)
  REFERENCES Repository (id)
    ON DELETE RESTRICT
);

CREATE TABLE LanguageKeyCodes (
  lang BIGINT       NOT NULL,
  code VARCHAR(255) NOT NULL,

  FOREIGN KEY (lang)
  REFERENCES LanguageTable (id)
    ON DELETE RESTRICT
);

CREATE TABLE LanguageString (
  id        BIGINT AUTO_INCREMENT PRIMARY KEY,
  lang      BIGINT,
  textValue TEXT NOT NULL,

  FOREIGN KEY (lang)
  REFERENCES LanguageTable (id)
    ON DELETE RESTRICT
);

CREATE TABLE Material_Author (
  material BIGINT NOT NULL,
  author   BIGINT NOT NULL,

  PRIMARY KEY (material, author),

  FOREIGN KEY (material)
  REFERENCES Material (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (author)
  REFERENCES Author (id)
    ON DELETE RESTRICT
);

CREATE TABLE Material_Publisher (
  material  BIGINT NOT NULL,
  publisher BIGINT NOT NULL,

  PRIMARY KEY (material, publisher),

  FOREIGN KEY (publisher)
  REFERENCES Publisher (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (material)
  REFERENCES Material (id)
    ON DELETE RESTRICT
);

CREATE TABLE Material_Title (
  material BIGINT NOT NULL,
  title    BIGINT NOT NULL,

  PRIMARY KEY (material, title),

  FOREIGN KEY (material)
  REFERENCES Material (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (title)
  REFERENCES LanguageString (id)
    ON DELETE RESTRICT
);

CREATE TABLE Material_Description (
  material    BIGINT NOT NULL,
  description BIGINT NOT NULL,

  PRIMARY KEY (material, description),

  FOREIGN KEY (material)
  REFERENCES Material (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (description)
  REFERENCES LanguageString (id)
    ON DELETE RESTRICT
);

CREATE TABLE Material_ResourceType (
  material     BIGINT NOT NULL,
  resourceType BIGINT NOT NULL,

  PRIMARY KEY (material, resourceType),

  FOREIGN KEY (material)
  REFERENCES Material (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (resourceType)
  REFERENCES ResourceType (id)
    ON DELETE RESTRICT
);

CREATE TABLE Material_Taxon (
  material BIGINT NOT NULL,
  taxon    BIGINT NOT NULL,

  PRIMARY KEY (material, taxon),

  FOREIGN KEY (material)
  REFERENCES Material (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (taxon)
  REFERENCES Taxon (id)
    ON DELETE RESTRICT
);

CREATE TABLE LearningObject_Tag (
  learningObject BIGINT NOT NULL,
  tag      BIGINT NOT NULL,

  PRIMARY KEY (learningObject, tag),

  FOREIGN KEY (learningObject)
  REFERENCES LearningObject (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (tag)
  REFERENCES Tag (id)
    ON DELETE RESTRICT
);

CREATE TABLE LearningObject_CrossCurricularTheme (
  learningObject             BIGINT NOT NULL,
  crossCurricularTheme BIGINT NOT NULL,

  PRIMARY KEY (learningObject, crossCurricularTheme),

  FOREIGN KEY (learningObject)
  REFERENCES LearningObject (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (crossCurricularTheme)
  REFERENCES CrossCurricularTheme (id)
    ON DELETE RESTRICT
);

CREATE TABLE LearningObject_KeyCompetence (
  learningObject      BIGINT NOT NULL,
  keyCompetence BIGINT NOT NULL,

  PRIMARY KEY (learningObject, keyCompetence),

  FOREIGN KEY (learningObject)
  REFERENCES LearningObject (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (keyCompetence)
  REFERENCES KeyCompetence (id)
    ON DELETE RESTRICT
);

CREATE TABLE LearningObject_TargetGroup (
  learningObject    BIGINT NOT NULL,
  targetGroup VARCHAR(255),

  PRIMARY KEY (learningObject, targetGroup),

  FOREIGN KEY (learningObject)
  REFERENCES LearningObject (id)
    ON DELETE RESTRICT
);

CREATE TABLE TranslationGroup (
  id   BIGINT AUTO_INCREMENT PRIMARY KEY,
  lang BIGINT NOT NULL,

  FOREIGN KEY (lang)
  REFERENCES LanguageTable (id)
    ON DELETE RESTRICT
);

CREATE TABLE Translation (
  translationGroup BIGINT,
  translationKey   VARCHAR(255),
  translation      TEXT NOT NULL,

  PRIMARY KEY (translationGroup, translationKey),

  FOREIGN KEY (translationGroup)
  REFERENCES TranslationGroup (id)
    ON DELETE RESTRICT
);

CREATE TABLE Page (
  id       BIGINT AUTO_INCREMENT PRIMARY KEY,
  name     VARCHAR(255) NOT NULL,
  content  TEXT         NOT NULL,
  language BIGINT       NOT NULL,

  UNIQUE KEY (name, language),

  FOREIGN KEY (language)
  REFERENCES LanguageTable (id)
    ON DELETE RESTRICT
);

CREATE TABLE Portfolio (
  id              BIGINT       PRIMARY KEY,
  title           VARCHAR(255) NOT NULL,
  taxon           BIGINT,
  originalCreator BIGINT       NOT NULL,
  summary         TEXT,
  visibility      VARCHAR(255) NOT NULL,

  FOREIGN KEY (id)
  REFERENCES LearningObject (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (originalCreator)
  REFERENCES User (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (taxon)
  REFERENCES Taxon (id)
    ON DELETE RESTRICT
);

CREATE TABLE Chapter (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  title         VARCHAR(255) NOT NULL,
  textValue     TEXT,
  parentChapter BIGINT,
  portfolio     BIGINT,
  chapterOrder  INTEGER,

  FOREIGN KEY (portfolio)
  REFERENCES Portfolio (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (parentChapter)
  REFERENCES Chapter (id)
    ON DELETE RESTRICT
);

CREATE TABLE Chapter_Material (
  chapter       BIGINT  NOT NULL,
  material      BIGINT  NOT NULL,
  materialOrder INTEGER NOT NULL,

  PRIMARY KEY (chapter, material),

  FOREIGN KEY (chapter)
  REFERENCES Chapter (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (material)
  REFERENCES Material (id)
    ON DELETE RESTRICT
);

CREATE TABLE WaramuTaxonMapping (
  id    BIGINT PRIMARY KEY,
  name  VARCHAR(255) NOT NULL,
  taxon BIGINT,

  FOREIGN KEY (taxon)
  REFERENCES Taxon (id)
    ON DELETE RESTRICT
);

CREATE TABLE EstCoreTaxonMapping (
  id    BIGINT PRIMARY KEY,
  name  VARCHAR(255) NOT NULL,
  taxon BIGINT,

  FOREIGN KEY (taxon)
  REFERENCES Taxon (id)
    ON DELETE RESTRICT
);

CREATE TABLE Comment (
  id                BIGINT    AUTO_INCREMENT PRIMARY KEY,
  text              TEXT   NOT NULL,
  creator           BIGINT NOT NULL,
  learningObject    BIGINT,
  added             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (creator)
  REFERENCES User (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (learningObject)
  REFERENCES LearningObject (id)
    ON DELETE RESTRICT
);

CREATE TABLE UserLike (
  id                BIGINT    AUTO_INCREMENT PRIMARY KEY,
  creator           BIGINT  NOT NULL,
  learningObject    BIGINT,
  isLiked           BOOLEAN NOT NULL,
  added             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  UNIQUE KEY (learningObject, creator),

  FOREIGN KEY (creator)
  REFERENCES User (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (learningObject)
  REFERENCES LearningObject (id)
    ON DELETE RESTRICT
);


CREATE TABLE ImproperContent (
  id                BIGINT    AUTO_INCREMENT PRIMARY KEY,
  creator           BIGINT NOT NULL,
  learningObject    BIGINT,
  added             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted           BOOLEAN   DEFAULT FALSE,
  reason            VARCHAR(255),

  FOREIGN KEY (creator)
  REFERENCES User (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (learningObject)
  REFERENCES LearningObject (id)
    ON DELETE RESTRICT
);

CREATE TABLE BrokenContent (
  id       BIGINT    AUTO_INCREMENT PRIMARY KEY,
  creator  BIGINT NOT NULL,
  material BIGINT,
  added    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted  BOOLEAN   DEFAULT FALSE,

  FOREIGN KEY (creator)
  REFERENCES User (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (material)
  REFERENCES Material (id)
    ON DELETE RESTRICT
);

CREATE TABLE TagUpVote (
  id        BIGINT  AUTO_INCREMENT PRIMARY KEY,
  user      BIGINT NOT NULL,
  material  BIGINT,
  portfolio BIGINT,
  tag       BIGINT NOT NULL,
  deleted   BOOLEAN DEFAULT FALSE,

  FOREIGN KEY (user)
  REFERENCES User (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (material)
  REFERENCES Material (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (portfolio)
  REFERENCES Portfolio (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (tag)
  REFERENCES Tag (id)
    ON DELETE RESTRICT
);
