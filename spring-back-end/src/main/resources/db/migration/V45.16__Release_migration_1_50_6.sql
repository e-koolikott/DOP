SET foreign_key_checks = 0;

call update_translations('EMAIL_VALIDATION_DIALOG_TEXT', 'Sinu e-posti aadressile <strong>${email}</strong> on saadetud 4-kohaline kinnituskood. Kui sa kinnituskoodi oma postkastist ei leia, kontrolli ka rämpsposti kausta.<br><br>Kui Sa ei ole <strong>15 minuti jooksul</strong> oma e-posti aadressile kinnituskoodi saanud või kui avastad, et ülaltoodud e-postiaadress ei ole korrektne, siis tee brauserile refresh ja alusta autentimisega algusest!<br><br>Trüki kinnituskood siia:', 'We have sent a 4 digit code to your e-mail address <strong>${email}</strong>. If you can''t find the code be sure to check your spam folder.<br><br>If you have not received the confirmation code on your e-mail in <strong>15 minutes</strong>, or you notice your e-mail address is incorrect, please refresh the browser tab and start the authentication process from the beginning.<br><br>Insert code below:', 'Sinu e-posti aadressile <strong>${email}</strong> on saadetud 4-kohaline kinnituskood. Kui sa kinnituskoodi oma postkastist ei leia, kontrolli ka rämpsposti kausta.<br><br>Trüki kinnituskood siia: (RU)');

call update_translations('LICENSE_AGREEMENT_DIALOG_TEXT', 'Et saaksime edaspidi õppematerjale kasutada, kohandada ning kogumikesse lisada, ühtlustame E-koolikotis kasutatavat litsentsi, milleks on <strong>CC BY-SA 3.0</strong> (Creative Commons''i litsentsiga „Autorile viitamine + jagamine samadel tingimustel 3.0 Eesti" ehk CC BY-SA 3.0). Litsentsi kokkuvõte asub <a href="https://creativecommons.org/licenses/by-sa/3.0/ee/" target="_blank">SIIN</a> ja täistekst <a href="https://creativecommons.org/licenses/by-sa/3.0/ee/legalcode" target="_blank">SIIN</a>.<br><br><strong>Nõustumisel muudame automaatselt kõigi Sinu materjalide ja kogumike litsentsiks CC BY-SA 3.0.</strong><br><strong>Mittenõustumisel</strong> muutuvad kõik Sinu materjalid ja kogumikud, millel ei ole CC BY-SA 3.0 litsentsi, privaatseks (st mitte avalikuks). Privaatsetele materjalidele ja kogumikele saad uue litsentsi hiljem ükshaaval käsitsi määrata ja need taas avalikuks muuta.<br><br><strong>NB! Litsents ei laiene sisule, mis asub E-koolikotist väljaspool (nt lingitud sisu). Litsents kehtib otse E-koolikotti laaditud või kirjutatud (sh kopeeritud) sisule.</strong>',
                         'We are applying changes to the current system of licenses in E-schoolbag in order to ensure the materials used, edited and added to portfolios comply with legal requirements. The new systems sees the use of only one license - <strong>CC BY-SA 3.0</strong> (Attribution-ShareAlike 3.0 Unported). You can find the summary of the license <a href="https://creativecommons.org/licenses/by-sa/3.0/ee/deed.en" target="_blank">HERE</a> and read the full version <a href="https://creativecommons.org/licenses/by-sa/3.0/legalcode" target="_blank">HERE</a>.<br><br> We would like to help make this process easier and faster for you and ask for your consent to apply the change to all of your materials and portfolios at once.<br><strong>If you accept, then we will automatically change the licenses of all of your materials and portfolios to CC BY-SA 3.0.</strong><br><strong>If you do not accept</strong>, all of your materials which do not already have the CC BY-SA 3.0 license will be made private (visible only to you). These materials can later be published only after you have accepted the new license on each of them one by one.<br><br><strong>NB! The license does not extend to content which is located outside of E-schoolbag (e.g. linked content). The license applies to content which is uploaded or written (also copied) directly into E-schoolbag.</strong>',
                         'Для публикации сборников в E-koolikott мы используем единую лицензию <strong>CC BY-SA 3.0.</strong> Обзор лицензии представлен <a href="https://creativecommons.org/licenses/by-sa/3.0/ee/deed.ru" target="_blank">ЗДЕСЬ</a>, с полным текстом можно ознакомиться <a href="https://creativecommons.org/licenses/by-sa/3.0/legalcode" target="_blank">ЗДЕСЬ</a>.<br><br><strong>В случае Вашего согласия мы автоматически изменим лицензии всех Ваших материалов и сборников на CC BY-SA 3.0.</strong><br><strong>Если Вы не согласны,</strong> то все Ваши материалы и сборники, не имеющие лицензии CC BY-SA 3.0, станут закрытыми для публичного использования (приватными). Такие сборники и материалы можно снова опубликовать позже, самостоятельно добавив к ним лицензию CC BY-SA 3.0. В этом случае лицензия для каждого материала добавляется отдельно.<br><br><strong>NB! Лицензия не распространяется на материалы, которые находятся вне E-koolikott (например, доступные по внешней ссылке). Лицензия действительна только для материалов, которые загружены или созданы (в т.ч. скопированы) в E-koolikott.</strong>');

call update_translations('AGREEMENT_DIALOG_TEXT', 'Näed käesolevat teadet, sest registreerisid end esmakordselt E-koolikoti kasutajaks või on kasutajatingimused pärast Sinu viimast sisse logimist uuenenud. Palun loe tingimusi põhjalikult ja kinnita oma nõusolek.',
                         'You are seeing this notification as a first time user or because some terms have changed since your last login. Please read the following terms and conditions carefully and mark your acceptance below.',
                         'Näed käesolevat teadet, sest registreerisid end esmakordselt E-koolikoti kasutajaks või on kasutajatingimused pärast Sinu viimast sisse logimist uuenenud. Palun loe tingimusi põhjalikult ja kinnita oma nõusolek. (RU)');

SET foreign_key_checks = 1;
