/**
 * 
 */
package br.org.policena.trydecrypt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.crypto.generators.BCrypt;
import org.bouncycastle.crypto.generators.SCrypt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @since Jul 7, 2021
 *
 */
@SpringBootApplication
public class TesteAleatorio implements CommandLineRunner {

	private static final boolean RUN = Boolean.TRUE;
	private static final boolean RUN_OLD = Boolean.TRUE;
	private static final boolean RUN_NEW = Boolean.FALSE;
	
	// 0 for CBC/PBKDF2WithHmacSHA256, 1 for BCrypt, 2 for Scrypt, 3 for PCBC, 4 for CBC no padding, 5 for ECB, 6 for CTR
	private static int MODE = 0; 
	
	private ExecutorService pool;
	LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();

	@Override
	public void run(String... args) throws Exception {
		if (RUN) {
			pool = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
					Runtime.getRuntime().availableProcessors(), 0L, TimeUnit.MILLISECONDS, queue);
			if (RUN_OLD) {

				// BalloonHash bh = new
				// BalloonHash(MessageDigest.getInstance("SHA-256"), 1 << 6, 1
				// << 9, 1);
				// SecretKey key = new SecretKeySpec(bh.hash("1234".getBytes(),
				// "2341".getBytes()), "AES");

//				String path = "../backups/07-bk_20210707_094729_dAk7WAiaK4658hd8k5k.bin";
//				String path = "../backups/27-bk_20210707_115052_3ke1HhjtW2gdPEzJVB9Ze.bin";
//				String path = "../backups/31-bk_20210713_084629_3ke1HhjtW2gdPEzJVB9dz.bin";
				String path = "../backups/35-bk_20210713_123153_3ke1HhjtW2gdPEzJVB9e4.bin";
				
				byte[] file = Files.readAllBytes(Paths.get(path));
				// byte[] file = AESUtil.loadFileBytesNormal();
				byte[] blockAes = Arrays.copyOfRange(file, 0x110, 0x200);

				byte[] ivByte = Arrays.copyOfRange(file, 0x100, 0x110);
				IvParameterSpec iv = new IvParameterSpec(ivByte);
				
				System.out.println("Pass; Pin; Interactions; Reversed; Twice; base58; base64; length");

				tryDecrypt("1234", "2341", iv, blockAes, Boolean.FALSE);
				tryDecrypt("2341", "1234", iv, blockAes, Boolean.FALSE);

				ArrayUtils.reverse(blockAes);
				tryDecrypt("1234", "2341", iv, blockAes, Boolean.TRUE);
				tryDecrypt("2341", "1234", iv, blockAes, Boolean.TRUE);
			}

			if (RUN_NEW) {
				List<List<String>> records = new ArrayList<>();
				String path = "../output/results.csv";
				try (BufferedReader br = new BufferedReader(new FileReader(path))) {
					String line;
					int i = 0;
					while ((line = br.readLine()) != null) {
						String[] values = line.split(";");
						if (i > 0) {
							records.add(Arrays.asList(values));
						}
						++i;
					}
				}

				path = "../backups/07-bk_20210707_094729_dAk7WAiaK4658hd8k5k.bin";
				byte[] file = Files.readAllBytes(Paths.get(path));

				byte[] blockAes = Arrays.copyOfRange(file, 0x110, 0x200);

				byte[] ivByte = Arrays.copyOfRange(file, 0x100, 0x110);
				IvParameterSpec iv = new IvParameterSpec(ivByte);

				AtomicLong counter = new AtomicLong(0);
				SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
				records.forEach(record -> {
					pool.submit(() -> {
						char[] pass = record.get(0).trim().toCharArray();
						byte[] pin = record.get(1).trim().getBytes();
						int interactions = Integer.valueOf(record.get(2).trim());
						boolean reverse = Boolean.valueOf(record.get(3).trim());
						KeySpec spec = new PBEKeySpec(pass, pin, interactions, 256);
						try {
							SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
							
							Cipher cipher = getCipher();
							initCipher(cipher, key, iv);
							
							byte[] toDecrypt = blockAes;
							if (reverse) {
								byte[] localBytes = Arrays.copyOf(blockAes, blockAes.length);
								ArrayUtils.reverse(localBytes);
								toDecrypt = localBytes;
							}
							
							byte[] decrypted = cipher.doFinal(toDecrypt);
							
							byte[] header = Arrays.copyOfRange(decrypted, 0, decrypted.length - 224);
//							ByteArrayInputStream bais = new ByteArrayInputStream(header);
//							byte[] decompressed = new ZipInputStream(bais).readAllBytes();
							
							String encoding = Utils.guessEncoding(header);
							String decoded = new String(header, encoding);
							
//							try {
//								File f = new File("target/" + encoding + ".txt");
//								if ( !f.exists() ) {
//									f.createNewFile();
//								}
//							    Files.write(Paths.get(f.getPath()), header, StandardOpenOption.APPEND);
//							}catch (IOException e) {
//							    //exception handling left as an exercise for the reader
//							}
							
//							System.out.println(String.format("%s;%s;%d;%s;%s", new String(pass), new String(pin), interactions, encoding, decoded));
							
//							OutputStream os = new FileOutputStream("/tmp/hex" + counter.get() + ".hex");
//						    os.write(decrypted);
//						    os.close();
						    
						    counter.incrementAndGet();
							
							// lines above print the inner block decrypted string
//							  int offset = decrypted.length - 224 + 96;
						     // int offset = 159;
//							 byte[] newBlock = Arrays.copyOfRange(decrypted, offset, decrypted.length);
							
//							 byte[] newDecrypted = cipher.doFinal(newBlock);
//							 String innerString = new String(newDecrypted, guessEncoding(newDecrypted));
//							 System.out.println(String.format("Pass \"%s\" + PIN \"%s\" reverse \"%s\" with %d interactions with offset %d decrypted the inner block! ",
//									 record.get(0).trim(), record.get(1).trim(), record.get(3).trim(), interactions, offset));
//							 System.out.println(innerString);
							
						} catch (Exception e) {
						}
					});
				});
			}
			
			pool.shutdown();
			pool.awaitTermination(1, TimeUnit.DAYS);
		}
	}

	/**
	 * @param string
	 * @param string2
	 * @param iv
	 * @param blockAes
	 * @param true1
	 */
	private void tryDecrypt(String password, String pin, IvParameterSpec iv, byte[] blockAes, Boolean reversed)
			throws Exception {
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		
		AtomicLong counter = new AtomicLong();

		int interactions = 17;
		if ( MODE == 0 ) {
			interactions = 100_000;
		}
		for (int i = 0; i <= interactions; ++i) {
			final int iInner = i;
			pool.submit(() -> {
				try {
					
					byte[] bytes;
					if ( MODE == 1 ) {
						bytes = BCrypt.generate(
								password.getBytes(), // byte array of user supplied, low entropy passw 
								Arrays.copyOf(pin.getBytes(), 16), // 128 bit(= 16 bytes), CSPRNG generated salt
					            iInner // cost factor, performs 2^14 iterations.
					            );
					}  else if ( MODE == 2 ) {
						bytes = SCrypt.generate(
								password.getBytes(), // user supplied password, converted into byte array
								Arrays.copyOf(pin.getBytes(), 16), // salt of size 32 bytes
					            (int)Math.pow(2, iInner), // CPU/Memory cost
					            16, // block size
					            1, // Parallelization parameter:
					            32 // (256 bits) Length of output key size
					            );
					} else {
						KeySpec spec = new PBEKeySpec(password.toCharArray(), pin.getBytes(), iInner, 256);
						bytes = factory.generateSecret(spec).getEncoded();
					}
					
					SecretKey key = new SecretKeySpec(bytes, "AES");

					Cipher cipher = getCipher();
					initCipher(cipher, key, iv);

					byte[] decryptedBytes = cipher.doFinal(blockAes);
					// String decrypted = new String(decryptedBytes);

					boolean twice = Boolean.FALSE, base58 = Boolean.FALSE, base64 = Boolean.FALSE;

					try {
						byte[] innerString = Arrays.copyOfRange(decryptedBytes, 0, decryptedBytes.length - 224);
//						System.out.println(String.format("Inner string (length: %d): %s", innerString.length, new String(innerString, Utils.guessEncoding(innerString))));
						
						@SuppressWarnings("unused")
						byte[] innerBlockBytes = cipher.doFinal(Arrays.copyOfRange(decryptedBytes, decryptedBytes.length - 224, decryptedBytes.length));
						twice = Boolean.TRUE;
						if ( MODE != 0 ) {
							
							// trying to decrypt inner block
							//System.out.println(String.format("Inner bytes (length: %d): \"%s\"", innerBlockBytes.length, new String(innerBytes, guessEncoding(innerBlockBytes))));
							
//							try {
//								System.out.println("Base58: " + Base58.decode(new String(innerBlockBytes)));
//							} catch (Exception ex) {
//							}
//							try {
//								System.out.println("Base64: " + Base64.getDecoder().decode(new String(innerBlockBytes)));
//							} catch (Exception ex) {
//							}
						}
					} catch (Exception ex) {
					}
					try {
						Base58.decode(new String(decryptedBytes));
						base58 = Boolean.TRUE;
					} catch (Exception ex) {
					}
					try {
						Base64.getDecoder().decode(new String(decryptedBytes));
						base64 = Boolean.TRUE;
					} catch (Exception ex) {
					}

					// System.out.println(String.format("Pass = %s, Pin = %s, I = %d, Reversed? %s,
					// Twice? %s\nDecrypted: %s",
					// password, pin, iInner, reversed, twice, decrypted));

					try {
						System.out.println(String.format("%s;%s;%d;%s;%s;%s;%s;%d", password, pin,
							iInner, reversed, twice, base58, base64, decryptedBytes.length));
//						if ( MODE != 0 ) {
//							System.out.println(String.format("Decrypted (length: %d): \"%s\"", decryptedBytes.length,
//									new String(decryptedBytes, guessEncoding(decryptedBytes))));
//						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}

					
//					OutputStream os = new FileOutputStream("/tmp/hex" + counter.get() + ".hex");
//				    os.write(decryptedBytes);
//				    os.close();
				    
				    counter.incrementAndGet();
				} catch (Exception ex) {
//					ex.printStackTrace();
				}
			});

			while (queue.size() > interactions) {
				TimeUnit.MILLISECONDS.sleep(100);
			}
		}
	}

	/**
	 * @return 
	 * @throws Exception 
	 */
	private Cipher getCipher() throws Exception {
		Cipher cipher;
		if ( MODE == 3 ) {
			cipher = Cipher.getInstance(AESUtil.CIPHER_INSTANCE_PCBC);
		} else if ( MODE == 4 ) {
			cipher = Cipher.getInstance(AESUtil.CIPHER_INSTANCE_CBC_NOPADDING);
		} else if ( MODE == 5 ) {
			cipher = Cipher.getInstance(AESUtil.CIPHER_INSTANCE_ECB);
		} else if ( MODE == 6 ) {
			cipher = Cipher.getInstance(AESUtil.CIPHER_INSTANCE_CTR);
		} else {
			cipher = Cipher.getInstance(AESUtil.CIPHER_INSTANCE);
		}
		return cipher;
	}
	
	/**
	 * @param cipher
	 * @param key
	 * @param iv
	 */
	private void initCipher(Cipher cipher, SecretKey key, IvParameterSpec iv) throws Exception {
		if ( MODE == 5 ) {
			cipher.init(Cipher.DECRYPT_MODE, key);
		} else {
			cipher.init(Cipher.DECRYPT_MODE, key, iv);
		}
	}

}
