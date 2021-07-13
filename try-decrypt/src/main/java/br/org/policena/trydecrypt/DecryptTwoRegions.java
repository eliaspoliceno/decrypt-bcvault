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
//			String path = "../backups/31-bk_20210713_084629_3ke1HhjtW2gdPEzJVB9dz.bin";
//			String path = "../backups/35-bk_20210713_123153_3ke1HhjtW2gdPEzJVB9e4.bin";
			byte[] file = Files.readAllBytes(Paths.get(path));
			
			byte[] block = Arrays.copyOfRange(file, 0x110, 0x200);
//			byte[] block700 = Arrays.copyOfRange(file, 0x710, 0x900);
			byte[] ivByte = Arrays.copyOfRange(file, 0x100, 0x110);

			IvParameterSpec iv = new IvParameterSpec(ivByte);
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			
			KeySpec spec = new PBEKeySpec("1234".toCharArray(), "2341".getBytes(), 60450, 256);
//			KeySpec spec = new PBEKeySpec("2341".toCharArray(), "1234".getBytes(), 78093, 256);
//			KeySpec spec = new PBEKeySpec("2341".toCharArray(), "1234".getBytes(), 77682, 256);
//			KeySpec spec = new PBEKeySpec("1234".toCharArray(), "2341".getBytes(), 60526, 256);
			
			SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
			
//			System.out.println(Arrays.toString(factory.generateSecret(spec).getEncoded()));
			
			Cipher cipher;
			cipher = Cipher.getInstance(AESUtil.CIPHER_INSTANCE);
			cipher.init(Cipher.DECRYPT_MODE, key, iv);
//			cipher.init(Cipher.DECRYPT_MODE, key, iv);
			
//			ArrayUtils.reverse(block);
			
			byte[] decrypted700 = cipher.doFinal(block);
//			Files.write(Paths.get("/tmp/backup_27_lowercase.bin"), decrypted700);
//			Files.write(Paths.get("/tmp/backup_31_uppercase.bin"), decrypted700);
//			Files.write(Paths.get("/tmp/backup_35_lowercase0.bin"), decrypted700);
			System.out.println(Utils.bytesToHex(decrypted700));
//			System.out.println(new String(decrypted700, Utils.guessEncoding(decrypted700)));
			
//			byte[] block800 = Arrays.copyOfRange(file, 0x810, 0x900);
//			ivByte = Arrays.copyOfRange(file, 0x800, 0x810);
//			iv = new IvParameterSpec(ivByte);
//			
//			cipher.init(Cipher.DECRYPT_MODE, key, iv);
//			
//			byte[] decrypted800 = cipher.doFinal(block800);
//			Files.write(Paths.get("/tmp/0x800.bin"), decrypted800);
			
			byte[] again = cipher.doFinal(decrypted700);
			System.out.println(Utils.bytesToHex(again));
			
			// 15FACC59ADD07AD8B4EBC6B1968D5E97A238ED43A14970D2D9ACABCFADE951AF
//			System.out.println(Utils.bytesToHex(again).indexOf("15") + " of " + Utils.bytesToHex(again).length());
		}
	}

}
