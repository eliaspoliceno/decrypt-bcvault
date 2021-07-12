/**
 * 
 */
package br.org.policena.trydecrypt;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @since Jul 12, 2021
 *
 */
@SpringBootApplication
public class DecryptTwoRegions implements CommandLineRunner {

	private static final boolean RUN = Boolean.FALSE;

	@Override
	public void run(String... args) throws Exception {
		if ( RUN ) {
			String path = "../backups/27-bk_20210707_115052_3ke1HhjtW2gdPEzJVB9Ze.bin";
			byte[] file = Files.readAllBytes(Paths.get(path));

			byte[] block = Arrays.copyOfRange(file, 0x110, 0x200);
//			byte[] block700 = Arrays.copyOfRange(file, 0x710, 0x900);
			byte[] ivByte = Arrays.copyOfRange(file, 0x100, 0x110);

			IvParameterSpec iv = new IvParameterSpec(ivByte);
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			
			KeySpec spec = new PBEKeySpec("1234".toCharArray(), "2341".getBytes(), 60450, 256);
			SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
			
			Cipher cipher;
			cipher = Cipher.getInstance(AESUtil.CIPHER_INSTANCE);
			cipher.init(Cipher.DECRYPT_MODE, key, iv);
			
			byte[] decrypted700 = cipher.doFinal(block);
			Files.write(Paths.get("/tmp/0x100.bin"), decrypted700);
			
//			byte[] block800 = Arrays.copyOfRange(file, 0x810, 0x900);
//			ivByte = Arrays.copyOfRange(file, 0x800, 0x810);
//			iv = new IvParameterSpec(ivByte);
//			
//			cipher.init(Cipher.DECRYPT_MODE, key, iv);
//			
//			byte[] decrypted800 = cipher.doFinal(block800);
//			Files.write(Paths.get("/tmp/0x800.bin"), decrypted800);
		}
	}

}
