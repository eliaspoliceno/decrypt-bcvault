1. changed data in offset 905 from 0x01 to 0x02 -> did not loaded ethereum wallet  
2. changed data in offset 906 from 0xB3 to 0xB4 -> did not loaded any wallet and reported a corrupted file([screenshot](debugging-screenshots/error-corrupted-wallet.png))   
3. changed data in offset 907 from 0xB4 to 0xB5 -> did not loaded any wallet also did not showed any error  
4. changed data in offset 908 from 0xB0 to 0xB1 -> dit not loaded any wallet also did not showed any error  
5. changed data in offset 909 from 0xC7 to 0xC8 -> dit not loaded any wallet also did not showed any error  
6. changed data in offset 90A from 0x02 to 0x03 -> loaded bitcoin wallets and did not showed ethereum wallet (detect that this change mades wallet as "hidden")  
7. changed data in offset 90E from 0x28 to 0x29 -> loaded bitcoin and ethereum wallets normally  
8. changed data in offset 80A from 0x04 to 0x02 -> loaded bitcoin and and ethereum wallets (the last bitcoin wallet I created is now marked as secured)  
9. changed data in offset 905 from 0x01 to 0x00 -> showed only bitcoin wallets, and the ethereum wallet "a" as it were a bitcoin wallet  
10. changed data in offset 905 from 0x01 to 0x10 -> showed only bitcoin wallets, could not find ethereum wallet "a"  
11. changed data in offset 906 from 0xB3 to 0xC3 -> reported a corrupted wallet file as in step 2  
12. changed data in offset 907 from 0xB4 to 0xC4 -> did not loaded any wallet also did not showed any error  
13. changed data in offset 908 from 0xB0 to 0xC0 -> did not loaded any wallet also did not showed any error  
14. changed data in offset 909 from 0xC7 to 0xD7 -> did not loaded any wallet also did not showed any error  
15. changed data in offset 90E from 0x28 to 0x38 -> loaded bitcoin and ethereum wallets normally  