# decrypt-bcvault
My personal effort trying to decrypt BCVault device.

# Briefing
I bought BC Vault hardware wallet, then i created several wallets and backed them up. So i have kept storing my funds in one wallet which now i know i don't wrote the password correctly. Sad but true.  
As my property, i can assure it will never go through BCVault's support no matter which secure way they provide.  
By knowing Global Pass, Global PIN, and another wallets passwords and PINs, i can brute-force this only wallet by myself, as their support will try to do.  
Offcourse, this depends on discovering which encryption method these backup uses, which lead me to asking for help.  

# Firmware analysis
The firmware is available for public download at these official urls:  
[https://update.bc-vault.com/downloads/firmware_1.5.6.bin](https://update.bc-vault.com/downloads/firmware_1.5.6.bin)  
[https://update.bc-vault.com/downloads/firmware_1.5.7.bin](https://update.bc-vault.com/downloads/firmware_1.5.7.bin)  
[https://update.bc-vault.com/downloads/firmware_1.5.9.bin](https://update.bc-vault.com/downloads/firmware_1.5.9.bin)  

They can be loaded into ghidra as a Cortex little endian (the device is an Atmel ATSAM4SD32B).  
Base address is 0x00800000, you will need the backup files in this repo in address 0x20000000 (SRAM) and 0x60000000 (external SRAM). I have not tryed to load it in IDA Pro as i have not an license.  

# Testing it
To start testing, i bought another device, which one i can do whatever i want. This device came in its 1.6 hardware version (previous device is 1.2). So i started from scratch and followed these steps:  

1. Reseted the device to its initial state  
2. Set global pass to 1234 and global PIN to 2341 (up, right, down, left)  
3. Backup [file backups/03-bk_20210707_094313_dAk7WAiaK4658hd8k5g.bin](backups/03-bk_20210707_094313_dAk7WAiaK4658hd8k5g.bin)  
4. Created new btc wallet with pass 1234, and pin 2341 (named first, address 1LgCWyPd2B4tJf25cDHwRS2QS3Kd79nezg)  
5. Backup [file backups/05-bk_20210707_094509_dAk7WAiaK4658hd8k5i.bin](backups/05-bk_20210707_094509_dAk7WAiaK4658hd8k5i.bin)  
6. Created a btc wallet with pass 1234, pin 3412 (named second, address 19Fh8qK7HCuQfwHEmujUD4VEmiwXgkwL2U)  
7. Backup [file backups/07-bk_20210707_094729_dAk7WAiaK4658hd8k5k.bin](backups/07-bk_20210707_094729_dAk7WAiaK4658hd8k5k.bin)  
8. Changed global pass to 2341 (no name)  
9. Backup [file backups/09-bk_20210707_094959_dAk7WAiaK4658hd8k5n.bin](backups/09-bk_20210707_094959_dAk7WAiaK4658hd8k5n.bin)  
10. Created btc wallet with pass 1234, pin 2341 (no name)  
11. Backup [file backups/11-bk_20210707_095136_3ke1HhjtW2gdPEzJVB9VA.bin](backups/11-bk_20210707_095136_3ke1HhjtW2gdPEzJVB9VA.bin)  
12. Restored backup from step 7  
13. Changed global pin to 3412  
14. Backup [file backups/14-bk_20210707_095531_3ke1HhjtW2gdPEzJVB9VD.bin](backups/14-bk_20210707_095531_3ke1HhjtW2gdPEzJVB9VD.bin)  
15. Restored backup from step 7  
16. Created btc wallet with pass 1234, pin 2341 (named third, address 1MUcwbdLfF9YfvgFoXbss1pyPGDJkXBK4U)  
17. Backup [file backups/17-bk_20210707_114523_3ke1HhjtW2gdPEzJVB9VG.bin](backups/17-bk_20210707_114523_3ke1HhjtW2gdPEzJVB9VG.bin)  
18. Created btc wallet with pass 1234, pin 2341 (named fourth, address 1MTZPDAL3u7YguEM94PAGxzTzBsXV6ia4r)  
19. Backup [file backups/19-bk_20210707_114648_3ke1HhjtW2gdPEzJVB9VJ.bin](backups/19-bk_20210707_114648_3ke1HhjtW2gdPEzJVB9VJ.bin)  
20. Created btc wallet with pass 1234, pin 2341 (named fifth, address 1NG7ZZwzHFw1ddarX5ZmBSTTk5hQ74i6AN)  
21. Backup [file backups/21-bk_20210707_114750_3ke1HhjtW2gdPEzJVB9Za.bin](backups/21-bk_20210707_114750_3ke1HhjtW2gdPEzJVB9Za.bin)  
22. Created btc wallet with pass 1234, pin 2341 (named sixth, address 14rGAunjHeDWz9oCHTnjT3ym8aJ6ek1hgW)  
23. Backup [file backups/23-bk_20210707_114849_3ke1HhjtW2gdPEzJVB9Zc.bin](backups/23-bk_20210707_114849_3ke1HhjtW2gdPEzJVB9Zc.bin)  
24. Created btc wallet with pass 1234, pin 2341 (named seventh, address 1JPCCy2J9uipUhncJjWfUf6zTp16t7MvrD)  
25. Backup [file backups/25-bk_20210707_114957_3ke1HhjtW2gdPEzJVB9Ze.bin](backups/25-bk_20210707_114957_3ke1HhjtW2gdPEzJVB9Ze.bin)  
26. Created btc wallet with pass 1234, pin 2341 (named eighth, address 12tRBaEk33y74KWQPGwDUufWTdo2oUEdpB)  
27. Backup [file backups/27-bk_20210707_115052_3ke1HhjtW2gdPEzJVB9Ze.bin](backups/27-bk_20210707_115052_3ke1HhjtW2gdPEzJVB9Ze.bin)  
28. Reveal all private keys
29. Backup [file backups/29-29-bk_20210712_181433_3ke1HhjtW2gdPEzJVB9Zi.bin](backups/29-29-bk_20210712_181433_3ke1HhjtW2gdPEzJVB9Zi.bin)  
30. Changed all wallet names to uppercase  
31. Backup [file 31-bk_20210713_084629_3ke1HhjtW2gdPEzJVB9dz.bin](backups/31-bk_20210713_084629_3ke1HhjtW2gdPEzJVB9dz.bin)  

After that, i compared the bin backup files, with these results:  
1. First 0x100 bytes are apparently reserved for device name.  
2. After creating each wallet, and comparing the file with previous, the changes were increasing from an offset of 0x100 (256 bytes), starting from offset 0x101 (first wallet created in step 5, as in step 5 there i have not created an wallet yet).  
2. First 0x10 (16) bytes of each 0x100 (256) bytes block apparently are the IV and the encryption appears to be an AES-256 in CBC mode, so it can(?) be ignored.  
3. Sometimes it changed the byte in the 'B' position (0x10B, 0x20B, 0x30B and so on)  
4. After changing the global PIN or pass, in steps 9 and 14, file changed completely, starting, again, from the offset 0x101.  

# Coding
I know the password and PIN of this test wallet, and i think it is an AES-256 in CBC mode, so i tried to decrypt it in a simple springboot application.  
So, i created the try-decrypt java app with several simple command line runners investigating the backup file.  
Code can be compiled and started from simply:  
```console
cd decrypt-bcvault/try-decrypt
mvn clean install
java -jar target/try-decrypt-0.0.1-SNAPSHOT.jar
```

or, if you want to save results locally:  
```console
cd decrypt-bcvault/try-decrypt
mvn clean install
java -jar target/try-decrypt-0.0.1-SNAPSHOT.jar |tee target/results.txt
```

1. CommandLineRunner: First try, its a garbage, just ignore.  
2. FindIVRunner: another mess, just ignore it too.  
3. AnotherCommandLineRunner: I tought the private key were someway just encrypted in these 256-byte blocks, so i revealed one of them and tryed to encrypt, then compared with the binary file content. Too much slow, won't run in a amount of time i can wait for. After it, i minded that private keys are not simplely encrypted, because how could the desktop application know these names we give and generated addresses of the wallets?  
4. *TesteAleatorio: ok, here things becomes interesting*. You can control it by RUN, RUN_OLD and RUN_NEW static booleans. In RUN_OLD i created a PBKDF2WithHmacSHA256 key with pass and pin (used as a salt), varying interactions from 0 to 100000 and tryed to decrypt the block. I do several tests, like, decrypted twice, exchaging pass and pin, tried base58 and base64 reading, reversed the block byte array, etc. **There were 1646 combinations that decrypted the file!** After that, i compiled the results (output/results.csv), which are: several interaction amounts can decrypt the block; the block is not twice encrypted; the block is not a base58 nor base64 encoded.  
5. Again in TesteAleatorio, now with RUN_NEW mode: now i got these interaction numbers in a csv file, i read the block and tryed to guess which encoding the decrypted block uses. Different interaction numbers produces same UTF-8 result (visually equals, i cannot guarantee), not human readable.  

# Updates
Added BCrypt and Scrypt key specs to *TesteAleatorio* class, with a new *MODE* variable to control which one to use.  
Comes to me that the decrypted block have 239 bytes, and this number should be multiple of 16 to be another inner AES-256 CBC block.  
Then, i added an 14 bytes length offset from the beginning of the result bytes block to try to decrypt the inner block. This 14 bytes i think they should store 32 character wallet name plus 34 character wallet address.  
No SCrypt combinations decrypted the outer bytes block.  
Only two BCrypt combinations decrypted the block but not the inner block and.  

#### 2021-07-12
BCVault security model article means that there is two security layers, one maded by your global pin and pass, and another made by each wallet pin and password.  
Then comes to me that, after first layer decrypt, each block in memory is made by wallet public address (34-char long), wallet name (32-char long), wallet type (btc, eth, ltc...), and a flag that mark which wallets have them private keys revealed.  
How many bytes are needed for storing these informations? Is it someway compressed? We can not forget that the remaining block must be larger enough to store the private key (65-char long).  
So i revealed all private keys (wallets folder in this repo) and tried some kind of decompression algorithms (new classes added).  
From the 1646 successful pin, pass and interactions combinations in first decrypt step, only 5 of them could decrypt inner block (output/results-inner-block.csv). All of them could decrypt the inner block by varying the offset which would say to us were this block starts.

# Help need
Ok guys, now i am asking for help of you experts. Get in touch if interested.  

