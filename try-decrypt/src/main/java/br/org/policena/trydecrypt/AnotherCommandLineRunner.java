/**
 * 
 */
package br.org.policena.trydecrypt;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @since Jul 6, 2021
 *
 */
@SpringBootApplication
public class AnotherCommandLineRunner implements CommandLineRunner {
	
	private ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	private static final Boolean RUN = Boolean.FALSE;

	@Override
	public void run(String... args) throws Exception {
		if (RUN) {
			LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
			pool = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
					Runtime.getRuntime().availableProcessors(), 0L, TimeUnit.MILLISECONDS, queue);

			BigInteger max = BigInteger.valueOf(256).pow(16);
			byte[] zero = new byte[] { 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0 };

			SecretKey keyNormal = AESUtil.getKeyFromPassword("1234", "2341");
			SecretKey keyReverse = AESUtil.getKeyFromPassword("2341", "1234");

			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");

			String privateKey = "6265327CB72B80F368FA736D3ED10A11B4D039432B3D558070144F20E00D76A41";

			byte[] fileDataLittle = Arrays.copyOfRange(AESUtil.loadFileBytesLittleEndian(), 0x99, 0x1000);
			byte[] fileDataBig = Arrays.copyOfRange(AESUtil.loadFileBytesBigEndian(), 0x99, 0x1000);
			byte[] fileDataNormal = Arrays.copyOfRange(AESUtil.loadFileBytesNormal(), 0x99, 0x1000);
			byte[] fileDataReverse = Arrays.copyOfRange(AESUtil.loadFileBytesReverse(), 0x99, 0x1000);

			BigInteger counter = BigInteger.ZERO;
			for (;;) {
				if (counter.compareTo(max) > 0) {
					break;
				}

				if (counter.toString().endsWith("000")) {
					System.out.println(sdf.format(new Date()) + ": " + counter.toString() + " ivs tentados.");
				}

				byte[] counterByteArray = counter.toByteArray();
				byte[] copy = zero.clone();
				System.arraycopy(counterByteArray, 0, copy, copy.length - counterByteArray.length,
						counterByteArray.length);

				IvParameterSpec iv = new IvParameterSpec(copy);

				submit(keyNormal, iv, fileDataLittle, privateKey, Boolean.TRUE);
				submit(keyNormal, iv, fileDataBig, privateKey, Boolean.TRUE);
				submit(keyNormal, iv, fileDataNormal, privateKey, Boolean.TRUE);
				submit(keyNormal, iv, fileDataReverse, privateKey, Boolean.TRUE);

				submit(keyReverse, iv, fileDataLittle, privateKey, Boolean.FALSE);
				submit(keyReverse, iv, fileDataBig, privateKey, Boolean.FALSE);
				submit(keyReverse, iv, fileDataNormal, privateKey, Boolean.FALSE);
				submit(keyReverse, iv, fileDataReverse, privateKey, Boolean.FALSE);

				while (queue.size() > 100_000) {
					try {
						TimeUnit.MILLISECONDS.sleep(100);
					} catch (Exception ex) {
					}
				}
				// System.out.println(Arrays.toString(counterByteArray));

				counter = counter.add(BigInteger.ONE);
			}

			pool.awaitTermination(365, TimeUnit.DAYS);
		}
	}

	/**
	 * @param keyNormal
	 * @param iv
	 * @param fileData 
	 * @param input 
	 * @param wasNormal 
	 */
	private void submit(SecretKey pKey, IvParameterSpec iv, byte[] fileData, String input, Boolean wasNormal) {
		pool.submit(() -> {
			try {
				Cipher cipher = Cipher.getInstance(AESUtil.CIPHER_INSTANCE);
				cipher.init(Cipher.ENCRYPT_MODE, pKey, iv);
				byte[] cipherText = cipher.doFinal(input.getBytes());
				
				//System.out.println("Vai procurar a senha.");
				String sFileData = new String(fileData);
				String sCipher = new String(cipherText);
				
				int index = sFileData.indexOf(sCipher);
				//System.out.println("Boyer-Moore terminou");
				if ( index > 0) {
					System.out.println("Achou a senha no arquivo! Posicao " + index + ", WasNormal: " + wasNormal + ", iv=" + Arrays.toString(iv.getIV()));
					System.exit(0);
				/*} else {
					System.out.println("Nao achou a senha");*/
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

}
