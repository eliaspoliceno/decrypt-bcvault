# decrypt-bcvault
My efforts trying to decrypt BCVault device.

# Briefing
I bought BC Vault hardware wallet, then i created several wallets and backed them up. So i have kept storing my funds in one wallet which now i know i don't wrote the password correctly. Sad but true.  
As my property, i can assure it will never go through BCVault's support no matter which secure way would be provided.  
By knowing Global Pass, Global PIN, and another wallets passwords and PINs, i can brute-force this only wallet by myself, as i think their support will try to do.  
Offcourse, this depends on discovering which encryption method these backup uses, which lead me to asking for help.  

# Firmware analysis
By man-in-the-middle-ing the desktop application, I could find that all the files in [firmware folder](firmware/) are available for public download at these official urls:  
[https://update.bc-vault.com/downloads/firmware_1.5.6.bin](https://update.bc-vault.com/downloads/firmware_1.5.6.bin)  
[https://update.bc-vault.com/downloads/firmware_1.5.7.bin](https://update.bc-vault.com/downloads/firmware_1.5.7.bin)  
[https://update.bc-vault.com/downloads/firmware_1.5.9.bin](https://update.bc-vault.com/downloads/firmware_1.5.9.bin)  

They can be loaded into ghidra as a Cortex little endian (the device is an Atmel ATSAM4SD32B).  
Base address is 0x00400000, you will need the backup files in this repo in address 0x20000000 (SRAM) and 0x60000000 (external SRAM). I have not tryed to load it in IDA Pro as i have not an license.  

# Testing it
To start testing, i bought another device, which one i can do whatever i want. This device came in its 1.6 hardware version (previous device is 1.2). So i started from scratch and followed these steps defined in [steps file](test-steps.md).  

After that, i compared the bin backup files, with these results:  
1. First 0x100 bytes are apparently reserved for device name.  
2. After creating each wallet, and comparing the file with previous, the changes were increasing from an offset of 0x100 (256 bytes), starting from offset 0x101 (first wallet created in step 5, as in step 5 there i have not created an wallet yet).  
2. First 0x10 (16) bytes of each 0x100 (256) bytes block apparently are the IV and the encryption appears to be an AES-256 in CBC mode (firmware analys shown that rsbox array is defined in memory), so it can(?) be ignored.  
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
I have moved older updates to [THIS FILE](updates.md)  

#### 2021-07-14
Added ghidra repo folder. Imported [firmware/firmware_1.5.6.bin](firmware/firmware_1.5.6.bin) and added file [backups/09-bk_20210707_094959_dAk7WAiaK4658hd8k5n.bin](backups/09-bk_20210707_094959_dAk7WAiaK4658hd8k5n.bin) to memory.  
Then i played [leveldown svd file loader](https://github.com/leveldown-security/SVD-Loader-Ghidra) with [svdfile/ATSAM4SD32B.svd](svdfile/ATSAM4SD32B.svd), and analyzed it selecting "ARM Agressive Instruction Finder" and "Scalar Operand References".  
I searched **and found the AES-256 vectors in firmware** as pointed in [this article](https://www.pentestpartners.com/security-blog/reverse-engineering-keys-from-firmware-a-how-to/).  

#### 2021-07-26  
When searching for defined strings in Ghidra, I have found some references to [FreeRTOS](https://www.freertos.org/) and Visual Studio. The firmware is apparently compiled using Atmel Studio.  
I have also did some tests with factory restore function. I am thinking that these backup files may have the RNG seed on it. You have to shake again the device when you factory reset it, but you don't have to shake if you restore a backup.  
If it is correct, and if we can find the RNG, we can regenerate the private keys by applying the RNG and time-slicing the timestamp. So, that way, we can access the private keys without knowing the wallet PIN and pass.  
I could also find that the backup files have also some code on it. That piece of code is stored in that part I was thinking it were the IV. I can not guarantee that Ghidra did it job correctly, I am a bit confused about it now. Why would these backups have code on it?  
So, excluding RNG for now, the backup files must contains: number of wallets (or, if so, we can detect if there is exactly 2000 wallets as they mention); coin type of each wallet; public address; name. This must be under first protection layer.  

#### 2021-08-06
I am now doing some modifications in the file, trying to understand how data is stored. I could find that this first 16-byte region I was thinking it were the IV is kind of a flag and data region.  
After doing some modifications, I could determine some byte positions that when changed, marks wallets as hidden or not, secured or not. These changes I am reporting in [THIS OTHER FILE](file-test-steps.md)  
If you are planning to restore these backup files in your device, please remove the numbers I have added in the very beginning of it's names. The device does not detect if they are not in the regular pattern.  

# Help need
Ok guys, now i am asking for help of you experts. Get in touch if interested.  

