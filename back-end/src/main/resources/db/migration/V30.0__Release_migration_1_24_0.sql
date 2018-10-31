SET foreign_key_checks = 0;

INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (1, 'USER_MANUAL_TAB', 'Kasutusjuhendid');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (3, 'USER_MANUAL_TAB', 'User guide');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (2, 'USER_MANUAL_TAB', 'Kasutusjuhendid');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (1, 'USER_MANUALS_HEADING', 'Kasutusjuhendid');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (3, 'USER_MANUALS_HEADING', 'User guide');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (2, 'USER_MANUALS_HEADING', 'Kasutusjuhendid');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (1, 'USER_MANUALS_ADD_NEW_MANUAL', 'Lisa uus juhend');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (3, 'USER_MANUALS_ADD_NEW_MANUAL', 'Add new guide');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (2, 'USER_MANUALS_ADD_NEW_MANUAL', 'Lisa uus juhend');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (1, 'USER_MANUALS_HIDE_NEW_ROW', 'Peida uus rida');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (3, 'USER_MANUALS_HIDE_NEW_ROW', 'Hide new row');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (2, 'USER_MANUALS_HIDE_NEW_ROW', 'Peida uus rida');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (1, 'USER_MANUALS_TITLE', 'Pealkiri');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (3, 'USER_MANUALS_TITLE', 'Title');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (2, 'USER_MANUALS_TITLE', 'Pealkiri');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (1, 'USER_MANUALS_LINK', 'Video link');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (3, 'USER_MANUALS_LINK', 'Video link');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (2, 'USER_MANUALS_LINK', 'Video link');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (1, 'USER_MANUALS_TEXT_LINK', 'Link tekstile');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (3, 'USER_MANUALS_TEXT_LINK', 'Link to text');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (2, 'USER_MANUALS_TEXT_LINK', 'Link tekstile');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (1, 'USER_MANUALS_ADD', 'Lisa');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (3, 'USER_MANUALS_ADD', 'Add');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (2, 'USER_MANUALS_ADD', 'Lisa');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (1, 'USER_MANUALS_DELETE', 'Kustuta');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (3, 'USER_MANUALS_DELETE', 'Delete');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (2, 'USER_MANUALS_DELETE', 'Kustuta');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (1, 'USER_MANUALS_DELETE_DIALOG_TITLE', 'Kustuta kasutusjuhend');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (3, 'USER_MANUALS_DELETE_DIALOG_TITLE', 'Delete user guide');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (2, 'USER_MANUALS_DELETE_DIALOG_TITLE', 'Kustuta kasutusjuhend');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (1, 'USER_MANUALS_DELETE_DIALOG_CONTENT', 'Kas oled kindel, et soovid selle kasutusjuhendi kustutada?');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (3, 'USER_MANUALS_DELETE_DIALOG_CONTENT', 'Are you sure you want to delete this user guide?');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (2, 'USER_MANUALS_DELETE_DIALOG_CONTENT', 'Kas oled kindel, et soovid selle kasutusjuhendi kustutada?');
UPDATE Translation SET translation = 'Eelmise külastuse järje meelespidamine' WHERE translationKey = 'LOCATION_DIALOG_HEADER' AND translationGroup = 1;
UPDATE Translation SET translation = 'Remember last visit' WHERE translationKey = 'LOCATION_DIALOG_HEADER' AND translationGroup = 2;
UPDATE Translation SET translation = 'Eelmise külastuse järje meelespidamine' WHERE translationKey = 'LOCATION_DIALOG_HEADER' AND translationGroup = 3;

SET foreign_key_checks = 1;