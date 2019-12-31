SET foreign_key_checks = 0;

call insert_translation('ADD_GDPR_TERM', 'Lisa GDPR tingimus', 'Add GDPR term');

call update_translations('LICENSE_AGREEMENT_DIALOG_HEADER', '<strong>Nõusolek minu materjalide ja kogumike litsentsi muutmiseks</strong>',
                         '<strong>Consent to change the licenses of my materials and portfolios</strong>',
                         '<strong>Согласие на изменение лицензии моих материалов и сборников (модальное окно)</strong>');

call update_translations('LICENSE_AGREEMENT_DIALOG_TEXT', 'Kogumike avalikustamiseks ühtlustame E-koolikotis kasutatavat litsentsi, milleks on <strong>CC BY-SA 3.0.</strong> Litsentsi kokkuvõte asub <a href="https://creativecommons.org/licenses/by-sa/3.0/ee/" target="_blank">SIIN</a> ja täistekst <a href="https://creativecommons.org/licenses/by-sa/3.0/ee/legalcode" target="_blank">SIIN</a>.<br><br><strong>Nõustumisel muudame automaatselt kõigi Sinu materjalide ja kogumike litsentsiks CC BY-SA 3.0.</strong><br><strong>Mittenõustumisel</strong> muutuvad kõik Sinu materjalid ja kogumikud, millel ei ole CC BY-SA 3.0 litsentsi, privaatseks (st mitte avalikuks). Privaatsetele materjalidele ja kogumikele saad uue litsentsi hiljem ükshaaval käsitsi määrata ja need taas avalikuks muuta.<br><br><strong>NB! Litsents ei laiene sisule, mis asub E-koolikotist väljaspool (nt lingitud sisu). Litsents kehtib otse E-koolikotti laaditud või kirjutatud (sh kopeeritud) sisule.</strong>',
                         'We are applying changes to the current system of licenses in E-schoolbag and moving over to the use of only one license - <strong>CC BY-SA 3.0.</strong> You can find the summary of the license <a href="https://creativecommons.org/licenses/by-sa/3.0/ee/deed.en" target="_blank">HERE</a> and read the full version <a href="https://creativecommons.org/licenses/by-sa/3.0/legalcode" target="_blank">HERE</a>.<br><br> We would like to help make this process easier and faster for you and ask for your consent to apply the change to all of your materials and portfolios at once.<br><strong>If you accept, then we will automatically change the licenses of all of your materials and portfolios to CC BY-SA 3.0.</strong><br><strong>If you do not accept</strong>, all of your materials which do not already have the CC BY-SA 3.0 license will be made private (visible only to you). These materials can later be published only after you have accepted the new license on each of them one by one.<br><br><strong>NB! The license does not extend to content which is located outside of E-schoolbag (e.g. linked content). The license applies to content which is uploaded or written (also copied) directly into E-schoolbag.</strong>',
                         'Для публикации сборников в E-koolikott мы используем единую лицензию <strong>CC BY-SA 3.0.</strong> Обзор лицензии представлен <a href="https://creativecommons.org/licenses/by-sa/3.0/ee/deed.ru" target="_blank">ЗДЕСЬ</a>, с полным текстом можно ознакомиться <a href="https://creativecommons.org/licenses/by-sa/3.0/legalcode" target="_blank">ЗДЕСЬ</a>.<br><br><strong>В случае Вашего согласия мы автоматически изменим лицензии всех Ваших материалов и сборников на CC BY-SA 3.0.</strong><br><strong>Если Вы не согласны,</strong> то все Ваши материалы и сборники, не имеющие лицензии CC BY-SA 3.0, станут закрытыми для публичного использования (приватными). Такие сборники и материалы можно снова опубликовать позже, самостоятельно добавив к ним лицензию CC BY-SA 3.0. В этом случае лицензия для каждого материала добавляется отдельно.<br><br><strong>NB! Лицензия не распространяется на материалы, которые находятся вне E-koolikott (например, доступные по внешней ссылке). Лицензия действительна только для материалов, которые загружены или созданы (в т.ч. скопированы) в E-koolikott.</strong>');

call update_translations('ATTENTION', 'Tähelepanu!', 'Attention!', 'Внимание!');

call update_translations('PORTFOLIOS_SET_TO_PRIVATE_TEXT', 'Osad Sinu kogumikud (loetelu allpool) on muudetud privaatseks, sest need sisaldavad teiste autorite materjale, mille litsents või autoriõigused ei luba neid kogumikus avalikustada.<br><br>Selliseid kogumikke saad avalikustada alles siis, kui neis olevad materjalid on litsentsiga CC BY-SA 3.0 (mida saab lisada materjali omanik) või kui eemaldad sobimatu litsentsiga materjalid oma kogumikust.',
                         'Some of your portfolios (see the list below) have been made private because these portfolios include materials from other authors without the new CC BY-SA 3.0 license. Portfolios containing materials without the correct license can not be made public in E-schoolbag.<br><br>You can publish these portfolios after the CC BY-SA 3.0 license has been added to all materials included in them (licenses can be changed by the material’s owner) or you have removed all materials without the correct license from the portfolio.',
                         'Некоторые Ваши сборники (список приведен ниже) стали закрытыми для публичного использования (приватными). Это произошло потому, что эти сборники содержат созданные другими авторами материалы, лицензии или авторские права на которые не позволяют опубликовать их в составе сборника.<br><br>Вы сможете снова опубликовать эти сборники, когда все входящие в них материалы будут обладать лицензией CC BY-SA 3.0 (ее может добавить владелец материала), или если удалите из своего сборника все нелицензированные материалы.');

call update_translations('MATERIAL_PUBLICATION_TITLE', 'Materjali avalikustamine', 'Material publication', 'Публикация материала');

call update_translations('MATERIAL_LICENSE_WARNING', 'Ühtlustame E-koolikotis kasutatavat litsentsi. Kuna materjalil puudub <strong>CC BY-SA 3.0 litsents</strong> (või sellega kokku sobiv CC BY 3.0 litsents), ei saa materjali hetkel avalikustada.<br><br>Materjali avalikustamine on võimalik vaid juhul, kui nõustud nimetatud litsentsiga (litsents ei kehti lingitud sisule, mis asub E-koolikotist väljas). Selleks ava materjali muutmise vaade ja kinnita litsentsiga nõusolek.',
                         'We are applying changes to the license system in E-schoolbag. Please review and add the <strong>CC BY-SA 3.0</strong> (or a compatible CC BY 3.0) license to the material if you wish to publish it.<br><br>Publishing the material is possible only after the required license has been added to it (the license does not apply to materials which are located outside E-Schoolbag). To do that, open edit mode and fill the checkbox to accept the new license.',
                         'Для публикации материалов в E-koolikott мы используем единую лицензию <strong>CC BY-SA 3.0</strong>. У вашего материала нет лицензии <strong>CC BY-SA 3.0</strong> (или совместимой с ней CC BY 3.0), поэтому в настоящий момент мы не можем опубликовать этот материал.<br><br>Вы сможете снова опубликовать этот материал, если согласитесь добавить к нему лицензию.  ** Лицензия не распространяется на материалы, которые находятся вне E-koolikott (например, доступные по внешней ссылке). Чтобы добавить лицензию, откройте окно изменения материала и дайте свое согласие на лицензию.');

SET foreign_key_checks = 1;