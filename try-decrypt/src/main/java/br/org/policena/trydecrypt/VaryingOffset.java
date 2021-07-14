/**
 * 
 */
package br.org.policena.trydecrypt;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.DeflaterInputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @since Jul 12, 2021
 *
 */
@SpringBootApplication
public class VaryingOffset implements CommandLineRunner {

	private static final boolean RUN = Boolean.FALSE;
	
	private ExecutorService pool;
	LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();

	@Override
	public void run(String... args) throws Exception {
		if ( RUN ) {
			pool = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
					Runtime.getRuntime().availableProcessors(), 0L, TimeUnit.MILLISECONDS, queue);
			
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
						
						byte[] header = Arrays.copyOfRange(decrypted, 0, decrypted.length - 224);
//						ByteArrayInputStream bais = new ByteArrayInputStream(header);
//						byte[] decompressed = new ZipInputStream(bais).readAllBytes();
						
						String encoding = Utils.guessEncoding(header);
						String decoded = new String(header, encoding);
						
//						System.out.println(String.format("%s;%s;%d;%s;%s", new String(pass), new String(pin), interactions, encoding, decoded));
						
					    counter.incrementAndGet();
						
						// lines above print the inner block decrypted string
					    for ( int offset = decrypted.length - 224; offset < 16; offset += 16) {
					    	byte[] newBlock = Arrays.copyOfRange(decrypted, offset, decrypted.length);
					    	
						 	byte[] newDecrypted = cipher.doFinal(newBlock);
						 	String innerString = new String(newDecrypted, Utils.guessEncoding(newDecrypted));
//						 System.out.println(String.format("Pass \"%s\" + PIN \"%s\" reverse \"%s\" with %d interactions with offset %d decrypted the inner block! ",
//								 record.get(0).trim(), record.get(1).trim(), record.get(3).trim(), interactions, offset));
						 	try {
//						 		System.out.println(innerString);
						 		
						 		ByteArrayInputStream bais = new ByteArrayInputStream(newDecrypted);
						 		byte[] decompressed = new DeflaterInputStream(bais).readAllBytes();
//						 		byte[] b64 = Base64.getDecoder().decode(decompressed);
						 		System.out.println(Arrays.toString(decompressed));
						 	} catch (Exception ex) {
						 		
						 	}
					    	
					    }
//						  int offset = decrypted.length - 224 + 96;
					     // int offset = 159;
						
					} catch (Exception e) {
					}
				});
			});
			
			pool.shutdown();
			pool.awaitTermination(1, TimeUnit.DAYS);
		}
	}

}
