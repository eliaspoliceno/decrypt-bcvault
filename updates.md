Added BCrypt and Scrypt key specs to *TesteAleatorio* class, with a new *MODE* variable to control which one to use.  
Comes to me that the decrypted block have 239 bytes, and this number should be multiple of 16 to be another inner AES-256 CBC block.  
Then, i added an 14 bytes length offset from the beginning of the result bytes block to try to decrypt the inner block. This 14 bytes i think they should store 32 character wallet name plus 34 character wallet address.  
No SCrypt combinations decrypted the outer bytes block.  
Only two BCrypt combinations decrypted the block but not the inner block.  

#### 2021-07-12
BCVault security model article means that there is two security layers, one maded by your global pin and pass, and another made by each wallet pin and password.  
Then comes to me that, after first layer decrypt, each block in memory is made by wallet public address (34-char long), wallet name (32-char long), wallet type (btc, eth, ltc...), and a flag that mark which wallets have them private keys revealed.  
How many bytes are needed for storing these informations? Is it someway compressed? We can not forget that the remaining block must be larger enough to store the private key (65-char long).  
So i revealed all private keys (wallets folder in this repo) and tried some kind of decompression algorithms (new classes added).  
From the 1646 successful pin, pass and interactions combinations in first decrypt step, only 5 of them could decrypt inner block (output/results-inner-block.csv). All of them could decrypt the inner block by varying the offset which would say to us were this block starts.

#### 2021-07-13
The uppercase wallet names change, and also one single letter increase, caused the whole 240-byte block to change. Reverting the name cause the block to maintain mostly equal to how it were previously.  
When modifying the name, the combination of pass, pin and interactions didn't works anymore, so i had to regenerate the file with these combinations in [output folder](output).  
Now i am thinking if CBC mode can cause this, or if is this file were encrypted in another mode of operation (maybe PCBC or CTR or even ECB)?  