SET foreign_key_checks = 0;

-- TagUpVote start

CREATE TABLE TagUpVoteAux (
  id                BIGINT  AUTO_INCREMENT PRIMARY KEY,
  user              BIGINT NOT NULL,
  learningObject    BIGINT NOT NULL,
  tag               BIGINT NOT NULL,
  deleted           BOOLEAN DEFAULT FALSE,

  FOREIGN KEY (user)
  REFERENCES User (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (learningObject)
  REFERENCES LearningObject (id)
    ON DELETE RESTRICT,

  FOREIGN KEY (tag)
  REFERENCES Tag (id)
    ON DELETE RESTRICT
);

INSERT INTO TagUpVoteAux (id, user, tag, deleted, learningObject) SELECT id, user, tag, deleted, if (tuv.material is not null, material, portfolio) FROM TagUpVote tuv;
DROP TABLE TagUpVote;
RENAME TABLE TagUpVoteAux TO TagUpVote;

-- TagUpVote end


-- Remove Waramu taxon mappings, as they should be est-core now
DROP TABLE IF EXISTS WaramuTaxonMapping;

-- Add new translations
INSERT INTO Translation(translationGroup, translationKey, translation) VALUES (3,'ADD_TO_NEW_PORTFOLIO','Add materials to new portfolio');
INSERT INTO Translation(translationGroup, translationKey, translation) VALUES (1,'ADD_TO_NEW_PORTFOLIO','Lisa materjalid uude kogumikku');
INSERT INTO Translation(translationGroup, translationKey, translation) VALUES (2,'ADD_TO_NEW_PORTFOLIO','Добавить материалы в новый портфель');

INSERT INTO Translation(translationGroup, translationKey, translation) VALUES (1,'ADD_TO_EXISTING_PORTFOLIO','Lisa materjal olemasolevasse kogumikku');
INSERT INTO Translation(translationGroup, translationKey, translation) VALUES (2,'ADD_TO_EXISTING_PORTFOLIO','Добавить материалы к существующему портфелю');
INSERT INTO Translation(translationGroup, translationKey, translation) VALUES (3,'ADD_TO_EXISTING_PORTFOLIO','Add materials to existing portfolio');


INSERT INTO Translation(translationGroup, translationKey, translation) VALUES (3,'CHOOSE_PORTFOLIO','Choose portfolio');
INSERT INTO Translation(translationGroup, translationKey, translation) VALUES (1,'CHOOSE_PORTFOLIO','Vali kogumik');
INSERT INTO Translation(translationGroup, translationKey, translation) VALUES (2,'CHOOSE_PORTFOLIO','Выберите портфель');

INSERT INTO Translation(translationGroup, translationKey, translation) VALUES (3,'CHOOSE_PORTFOLIO_CHAPTER','Choose porfolio chapter');
INSERT INTO Translation(translationGroup, translationKey, translation) VALUES (1,'CHOOSE_PORTFOLIO_CHAPTER','Vali kogumiku peatükk');
INSERT INTO Translation(translationGroup, translationKey, translation) VALUES (2,'CHOOSE_PORTFOLIO_CHAPTER','Выбрать главу портфеля');

INSERT INTO Translation(translationGroup, translationKey, translation) VALUES (3,'PORTFOLIO_ADD_MATERIAL_FAIL','Adding material(s) to portfolio failed');
INSERT INTO Translation(translationGroup, translationKey, translation) VALUES (1,'PORTFOLIO_ADD_MATERIAL_FAIL','Materjali(de) lisamine kogumikku ebaõnnestus');
INSERT INTO Translation(translationGroup, translationKey, translation) VALUES (2,'PORTFOLIO_ADD_MATERIAL_FAIL','Добавление материалов в портфель не удалось.');

-- Changing Publisher role structure

ALTER TABLE User
    ADD publisher BIGINT,
    ADD CONSTRAINT FOREIGN KEY(publisher) REFERENCES Publisher(id) ON DELETE RESTRICT;

UPDATE User SET role = 'USER' where role = 'PUBLISHER';

-- Adding a way to track changes in the LearningObject table

ALTER TABLE LearningObject ADD lastInteraction TIMESTAMP NULL DEFAULT NULL;

SET foreign_key_checks = 1;
