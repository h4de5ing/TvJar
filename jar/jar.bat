cp "../app/build/intermediates/dex/release/mergeDexRelease/classes.dex" "spider.jar"
certUtil -hashfile "../app/build/intermediates/dex/release/mergeDexRelease/classes.dex" MD5 | find /i /v "md5" | find /i /v "certutil" > "spider.jar.md5"