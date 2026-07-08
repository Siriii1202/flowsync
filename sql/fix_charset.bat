@echo off
chcp 65001 >nul
mysql -u root -proot --default-character-set=utf8mb4 flowsync_simple < "D:\AICODING\new_project\sql\fix_charset.sql"
echo Done!
