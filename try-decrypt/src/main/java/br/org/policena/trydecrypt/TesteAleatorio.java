/**
 * 
 */
package br.org.policena.trydecrypt;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
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
import org.mozilla.universalchardet.UniversalDetector;
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
	
	private static int MODE = 1; // 0 for PBKDF2WithHmacSHA256, 1 for BCrypt, 2 for Scrypt
	
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

				String path = "../backups/27-bk_20210707_115052_3ke1HhjtW2gdPEzJVB9Ze.bin";
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
							
							Cipher cipher;
							cipher = Cipher.getInstance(AESUtil.CIPHER_INSTANCE);
							cipher.init(Cipher.DECRYPT_MODE, key, iv);
							
							byte[] toDecrypt = blockAes;
							if (reverse) {
								byte[] localBytes = Arrays.copyOf(blockAes, blockAes.length);
								ArrayUtils.reverse(localBytes);
								toDecrypt = localBytes;
							}
							
							byte[] decrypted = cipher.doFinal(toDecrypt);
							String encoding = guessEncoding(decrypted);
							String decoded = new String(toDecrypt, encoding);
							
							System.out.println(String.format("%s;%s;%d;%s;%s", new String(pass), new String(pin), interactions, encoding, decoded));
							
							OutputStream os = new FileOutputStream("/tmp/hex" + counter.get() + ".hex");
						    os.write(decrypted);
						    os.close();
						    
						    counter.incrementAndGet();
							
							// lines above print the inner block decrypted string
//							 int offset = decrypted.length - 224;
//							 byte[] newBlock = Arrays.copyOfRange(decrypted, offset, decrypted.length);
							
//							 byte[] newDecrypted = cipher.doFinal(newBlock);
//							 String innerString = new String(newDecrypted, guessEncoding(newDecrypted));
//							 System.out.println(String.format("Pass \"%s\" + PIN \"%s\" reverse \"%s\" decrypted the inner block! ",
//									 record.get(0).trim(), record.get(1).trim(), record.get(3).trim()));
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

	public static String guessEncoding(byte[] bytes) {
		String DEFAULT_ENCODING = "UTF-8";
		UniversalDetector detector = new UniversalDetector(null);
		detector.handleData(bytes, 0, bytes.length);
		detector.dataEnd();
		String encoding = detector.getDetectedCharset();
		detector.reset();
		if (encoding == null) {
			encoding = DEFAULT_ENCODING;
		}
		return encoding;
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
		for (int i = 0; i < interactions; ++i) {
			final int iInner = i;
			pool.submit(() -> {
				try {
					
					byte[] bytes;
					if ( MODE == 0 ) {
						KeySpec spec = new PBEKeySpec(password.toCharArray(), pin.getBytes(), iInner, 256);
						bytes = factory.generateSecret(spec).getEncoded();
					} else if ( MODE == 1 ) {
						bytes = BCrypt.generate(
								password.getBytes(), // byte array of user supplied, low entropy passw 
								Arrays.copyOf(pin.getBytes(), 16), // 128 bit(= 16 bytes), CSPRNG generated salt
					            iInner // cost factor, performs 2^14 iterations.
					            );
					}  else {
						bytes = SCrypt.generate(
								password.getBytes(), // user supplied password, converted into byte array
								Arrays.copyOf(pin.getBytes(), 16), // salt of size 32 bytes
					            (int)Math.pow(2, iInner), // CPU/Memory cost
					            16, // block size
					            1, // Parallelization parameter:
					            32 // (256 bits) Length of output key size
					            );
					}
					
					SecretKey key = new SecretKeySpec(bytes, "AES");

					Cipher cipher;
					cipher = Cipher.getInstance(AESUtil.CIPHER_INSTANCE);
					cipher.init(Cipher.DECRYPT_MODE, key, iv);

					byte[] decryptedBytes = cipher.doFinal(blockAes);
					// String decrypted = new String(decryptedBytes);

					boolean twice = Boolean.FALSE, base58 = Boolean.FALSE, base64 = Boolean.FALSE;

					try {
						byte[] innerString = Arrays.copyOfRange(decryptedBytes, 0, decryptedBytes.length - 224);
						System.out.println(String.format("Inner string (length: %d): %s", innerString.length, new String(innerString, guessEncoding(innerString))));
						
						@SuppressWarnings("unused")
						byte[] innerBytes = cipher.doFinal(Arrays.copyOfRange(decryptedBytes, decryptedBytes.length - 224 + 16, decryptedBytes.length));
						twice = Boolean.TRUE;
						if ( MODE != 0 ) {
							
							// trying to decrypt inner block
							//System.out.println(String.format("Inner bytes (length: %d): \"%s\"", innerBytes.length, new String(innerBytes, guessEncoding(innerBytes))));
							
//							try {
//								System.out.println("Base58: " + Base58.decode(new String(decryptedBytes)));
//							} catch (Exception ex) {
//							}
//							try {
//								System.out.println("Base64: " + Base64.getDecoder().decode(new String(decryptedBytes)));
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

					
					OutputStream os = new FileOutputStream("/tmp/hex" + counter.get() + ".hex");
				    os.write(decryptedBytes);
				    os.close();
				    
				    counter.incrementAndGet();
				} catch (Exception ex) {
					//ex.printStackTrace();
				}
			});

			while (queue.size() > interactions) {
				TimeUnit.MILLISECONDS.sleep(100);
			}
		}
	}

}
