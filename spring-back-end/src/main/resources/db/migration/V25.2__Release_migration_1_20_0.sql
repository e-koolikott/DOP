SET foreign_key_checks = 0;

INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (1, 'SEARCH_RESULT_NO_RESULT_EXACT_PHRASE', 'Õppevaras ei leidu <strong>täpseid</strong> vasteid otsingule "<strong>${query}</strong>"');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (3, 'SEARCH_RESULT_NO_RESULT_EXACT_PHRASE', 'No learning objects contain the <strong>exact</strong> query "<strong>${query}</strong>"');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (2, 'SEARCH_RESULT_NO_RESULT_EXACT_PHRASE', 'Õppevaras ei leidu <strong>täpseid</strong> vasteid otsingule "<strong>${query}</strong>"');

INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (1, 'SEARCH_RESULT_1_RESULT_EXACT_PHRASE', '<strong>1 õppevaras</strong> leidub <strong>täpne</strong> vaste otsingule "<strong>${query}</strong>"');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (3, 'SEARCH_RESULT_1_RESULT_EXACT_PHRASE', '<strong>1 learning object</strong> contains the <strong>exact</strong> query "<strong>${query}</strong>"');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (2, 'SEARCH_RESULT_1_RESULT_EXACT_PHRASE', '<strong>1 õppevaras</strong> leidub <strong>täpne</strong> vaste otsingule "<strong>${query}</strong>"');

INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (1, 'SEARCH_RESULT_MULTIPLE_RESULT_EXACT_PHRASE', '<strong>${count} õppevaras</strong> leidub <strong>täpseid</strong> vasteid otsingule "<strong>${query}</strong>"');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (3, 'SEARCH_RESULT_MULTIPLE_RESULT_EXACT_PHRASE', '<strong>${count} learning objects</strong> contain the <strong>exact</strong> query "<strong>${query}</strong>"');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (2, 'SEARCH_RESULT_MULTIPLE_RESULT_EXACT_PHRASE', '<strong>${count} õppevaras</strong> leidub <strong>täpseid</strong> vasteid otsingule "<strong>${query}</strong>"');

INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (1, 'SEARCH_RESULT_NO_RESULT_SIMILAR_PHRASE', 'Õppevaras ei leidu <strong>osalisi</strong> vasteid otsingule "<strong>${query}</strong>"');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (3, 'SEARCH_RESULT_NO_RESULT_SIMILAR_PHRASE', 'No learning objects contain queries <strong>similar</strong> to "<strong>${query}</strong>"');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (2, 'SEARCH_RESULT_NO_RESULT_SIMILAR_PHRASE', 'Õppevaras ei leidu <strong>osalisi</strong> vasteid otsingule "<strong>${query}</strong>"');

INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (1, 'SEARCH_RESULT_1_RESULT_SIMILAR_PHRASE', '<strong>1 õppevaras</strong> leidub <strong>osaline</strong> vaste otsingule "<strong>${query}</strong>"');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (3, 'SEARCH_RESULT_1_RESULT_SIMILAR_PHRASE', '<strong>1 learning object</strong> contains a query <strong>similar</strong> to "<strong>${query}</strong>"');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (2, 'SEARCH_RESULT_1_RESULT_SIMILAR_PHRASE', '<strong>1 õppevaras</strong> leidub <strong>osaline</strong> vaste otsingule "<strong>${query}</strong>"');

INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (1, 'SEARCH_RESULT_MULTIPLE_RESULT_SIMILAR_PHRASE', '<strong>${count} õppevaras</strong> leidub <strong>osalisi</strong> vasteid otsingule "<strong>${query}</strong>"');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (3, 'SEARCH_RESULT_MULTIPLE_RESULT_SIMILAR_PHRASE', '<strong>${count} learning objects</strong> contain queries <strong>similar</strong> to "<strong>${query}</strong>"');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (2, 'SEARCH_RESULT_MULTIPLE_RESULT_SIMILAR_PHRASE', '<strong>${count} õppevaras</strong> leidub <strong>osalisi</strong> vasteid otsingule "<strong>${query}</strong>"');

INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (1, 'SEARCH_RESULT_NO_RESULT_WORD', 'Õppevaras ei leidu vasteid otsingule "<strong>${query}</strong>"');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (3, 'SEARCH_RESULT_NO_RESULT_WORD', 'No learning objects contain the query "<strong>${query}</strong>"');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (2, 'SEARCH_RESULT_NO_RESULT_WORD', 'Õppevaras ei leidu vasteid otsingule "<strong>${query}</strong>"');

INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (1, 'SEARCH_RESULT_1_WORD', '<strong>1 õppevaras</strong> leidub vaste otsingule "<strong>${query}</strong>"');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (3, 'SEARCH_RESULT_1_WORD', '<strong>1 learning object</strong> contains the query "<strong>${query}</strong>"');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (2, 'SEARCH_RESULT_1_WORD', '<strong>1 õppevaras</strong> leidub vaste otsingule "<strong>${query}</strong>"');

INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (1, 'SEARCH_RESULT_MULTIPLE_WORD', '<strong>${count} õppevaras</strong> leidub vasteid otsingule "<strong>${query}</strong>"');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (3, 'SEARCH_RESULT_MULTIPLE_WORD', '<strong>${count} learning objects</strong> contain the query "<strong>${query}</strong>"');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (2, 'SEARCH_RESULT_MULTIPLE_WORD', '<strong>${count} õppevaras</strong> leidub vasteid otsingule "<strong>${query}</strong>"');

INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (1, 'HEADER_DEFAULT_SEARCH_PLACEHOLDER', 'Otsi pealkirja, kirjelduse, võtmesõna, autori, väljaandja järgi');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (3, 'HEADER_DEFAULT_SEARCH_PLACEHOLDER', 'Search by title, description, tag, author, publisher');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (2, 'HEADER_DEFAULT_SEARCH_PLACEHOLDER', 'Otsi pealkirja, kirjelduse, võtmesõna, autori, väljaandja järgi');

INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (1, 'HEADER_PORTFOLIO_SEARCH_PLACEHOLDER', 'Otsi siit materjale sellesse kogumikku pealkirja, kirjelduse, võtmesõna, autori, väljaandja järgi');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (3, 'HEADER_PORTFOLIO_SEARCH_PLACEHOLDER', 'Search for new materials by title, description, tag, author, publisher');
INSERT INTO Translation (translationGroup, translationKey, translation) VALUES (2, 'HEADER_PORTFOLIO_SEARCH_PLACEHOLDER', 'Otsi siit materjale sellesse kogumikku pealkirja, kirjelduse, võtmesõna, autori, väljaandja järgi');

SET foreign_key_checks = 1;

