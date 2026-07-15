USE smart_bracelet_mes;
UPDATE sys_user SET password = '$2a$10$JBzpcrYP9pM6I8JQfugf3O4cakC/Az4Plr.clQ7H4brvqB9oCBfQ2' WHERE username = 'zhangsan';
UPDATE sys_user SET password = '$2a$10$JBzpcrYP9pM6I8JQfugf3O4cakC/Az4Plr.clQ7H4brvqB9oCBfQ2' WHERE username = 'lisi';
UPDATE sys_user SET password = '$2a$10$JBzpcrYP9pM6I8JQfugf3O4cakC/Az4Plr.clQ7H4brvqB9oCBfQ2' WHERE username = 'wangwu';
UPDATE sys_user SET password = '$2a$10$JBzpcrYP9pM6I8JQfugf3O4cakC/Az4Plr.clQ7H4brvqB9oCBfQ2' WHERE username = 'zhaoliu';
UPDATE sys_user SET password = '$2a$10$JBzpcrYP9pM6I8JQfugf3O4cakC/Az4Plr.clQ7H4brvqB9oCBfQ2' WHERE username = 'sunqi';
SELECT '密码更新完成' as result;