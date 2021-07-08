/**
 * 
 */
package br.com.policena.trydecrypt;

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

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.ArrayUtils;
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
	private static final boolean RUN_OLD = Boolean.FALSE;
	private static final boolean RUN_NEW = Boolean.TRUE;
	private ExecutorService pool;
	LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();

	@Override
	public void run(String... args) throws Exception {
		if (RUN) {
			if (RUN_OLD) {
				pool = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
						Runtime.getRuntime().availableProcessors(), 0L, TimeUnit.MILLISECONDS, queue);

				// BalloonHash bh = new
				// BalloonHash(MessageDigest.getInstance("SHA-256"), 1 << 6, 1
				// << 9, 1);
				// SecretKey key = new SecretKeySpec(bh.hash("1234".getBytes(),
				// "2341".getBytes()), "AES");

				String path = "../backups/07-bk_20210707_094729_dAk7WAiaK4658hd8k5k.bin";
				byte[] file = Files.readAllBytes(Paths.get(path));
				// byte[] file = AESUtil.loadFileBytesNormal();
				byte[] blockAes = Arrays.copyOfRange(file, 0x110, 0x200);

				byte[] ivByte = Arrays.copyOfRange(file, 0x100, 0x110);
				IvParameterSpec iv = new IvParameterSpec(ivByte);

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

				SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
				records.forEach(record -> {
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
						
						String encoding = guessEncoding(cipher.doFinal(toDecrypt));
						String decoded = new String(toDecrypt, encoding);
						System.out.println(String.format("%s;%s;%d;%s;%s", new String(pass), new String(pin), interactions, encoding, decoded));

					} catch (Exception e) {
					}
				});
			}
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

		for (int i = 0; i < 100_000; ++i) {
			final int iInner = i;
			pool.submit(() -> {
				try {
					KeySpec spec = new PBEKeySpec(password.toCharArray(), pin.getBytes(), iInner, 256);
					SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

					Cipher cipher;
					cipher = Cipher.getInstance(AESUtil.CIPHER_INSTANCE);
					cipher.init(Cipher.DECRYPT_MODE, key, iv);

					byte[] decryptedBytes = cipher.doFinal(blockAes);
					// String decrypted = new String(decryptedBytes);

					boolean twice = Boolean.FALSE, base58 = Boolean.FALSE, base64 = Boolean.FALSE;

					try {
						cipher.doFinal(decryptedBytes);
						twice = Boolean.TRUE;
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

					// System.out.println(
					// String.format("Pass = %s, Pin = %s, I = %d, Reversed? %s,
					// Twice? %s\nDecrypted: %s",
					// password, pin, iInner, reversed, twice, decrypted));

					System.out.println(String.format(
							"Pass = %s, Pin = %s, I = %d, Reversed? %s, Twice? %s, base58? %s, base64? %s, length = %d",
							password, pin, iInner, reversed, twice, base58, base64, decryptedBytes.length));

				} catch (Exception e) {
				}
			});

			while (queue.size() > 100_000) {
				TimeUnit.MILLISECONDS.sleep(100);
			}
		}
	}

}
