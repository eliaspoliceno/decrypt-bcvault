/**
 * 
 */
package br.org.policena.trydecrypt;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @since Jul 6, 2021
 *
 */
@SpringBootApplication
public class FindIVRunner implements CommandLineRunner {

	private static final boolean RUN = false;

	// @Override
	public void runOld(String... args) throws Exception {
		byte[] blockAes = Arrays.copyOfRange(AESUtil.loadFileBytesLittleEndian(), 0x600, 0x700);
		// ArrayUtils.reverse(blockAes);

		byte[] ivByte = Arrays.copyOfRange(blockAes, 0x0, 0x10);
		IvParameterSpec iv = new IvParameterSpec(ivByte);

		SecretKey keyNormal = AESUtil.getKeyFromPassword("1234", "2341");
		// SecretKey keyNormal = AESUtil.getKeyFromPassword("2341", "1234");

		Cipher cipher = Cipher.getInstance(AESUtil.CIPHER_INSTANCE);
		cipher.init(Cipher.DECRYPT_MODE, keyNormal, iv);

		byte[] encoded = Arrays.copyOfRange(blockAes, 0x10, 0x20);
		// byte[] encoded = blockAes;

		// byte[] decodedString = Base64.getDecoder().decode(new
		// String(encoded));
		// String decrypted = new String(cipher.doFinal(decodedString));
		String decrypted = new String(cipher.doFinal(encoded));
		System.out.println("Decrypted: " + decrypted);
	}

	// @Override
	public void runlol(String... args) throws Exception {
		byte[] blockAes = Arrays.copyOfRange(AESUtil.loadFileBytesNormal(), 0x601, 0x700);
		// byte[] blockAes =
		// Arrays.copyOfRange(AESUtil.loadFileBytesLittleEndian(), 0x600,
		// 0x700);
		ArrayUtils.reverse(blockAes);

		byte[] ivByte = Arrays.copyOfRange(blockAes, 0x0, 0x10);
		IvParameterSpec iv = new IvParameterSpec(ivByte);

		// SecretKey keyNormal = AESUtil.getKeyFromPassword("1234", "2341");
		SecretKey keyNormal = AESUtil.getKeyFromPassword("2341", "1234");

		Cipher cipher = Cipher.getInstance(AESUtil.CIPHER_INSTANCE);
		cipher.init(Cipher.ENCRYPT_MODE, keyNormal, iv);

		String privateKey = "6265327CB72B80F368FA736D3ED10A11B4D039432B3D558070144F20E00D76A41";

		String encrypted = new String(cipher.doFinal(privateKey.getBytes()));

		System.out.println("Encrypted contains? " + (new String(blockAes).contains(encrypted)));
	}

	// @Override
	public void runAnother(String... args) throws Exception {
		byte[] blockAes = Arrays.copyOfRange(AESUtil.loadFileBytesNormal(), 0x600, 0x700);
		ArrayUtils.reverse(blockAes);

		System.out.println("Is Base64: " + org.apache.commons.codec.binary.Base64.isBase64(blockAes));
		int counter = 0;
		for (int i = 0; i < blockAes.length; ++i) {
			byte octet = blockAes[i];
			// if (org.apache.commons.codec.binary.Base64.isBase64(octet)) {
			if (octet == (byte) '=') {
				System.out.println("i=" + i + " is base64");
				counter++;
			}
		}
		System.out.println("Total: " + counter);

		// ArrayUtils.reverse(blockAes);
		// System.out.println(new String(Base64.getDecoder().decode(blockAes)));
	}

	@Override
	public void run(String... args) throws Exception {
		if (RUN) {
			byte[] file = AESUtil.loadFileBytesNormal();

			// byte[] blockAes = Arrays.copyOfRange(file, 0x110, 0x200);
			// byte[] blockAes = Arrays.copyOfRange(file, 0x100, 0x1f0);
			byte[] blockAes = Arrays.copyOfRange(file, 0x100, 0x200);
			ArrayUtils.reverse(blockAes);

			byte[] ivByte = Arrays.copyOfRange(file, 0x100, 0x110);
			// byte[] ivByte = Arrays.copyOfRange(file, 0x1f0, 0x200);
			// ArrayUtils.reverse(ivByte);
			IvParameterSpec iv = new IvParameterSpec(ivByte);

			SecretKey key = AESUtil.getKeyFromPassword("1234", "2341");
			// SecretKey key = AESUtil.getKeyFromPassword("2341", "1234");

			Cipher cipher = Cipher.getInstance(AESUtil.CIPHER_INSTANCE);
			cipher.init(Cipher.DECRYPT_MODE, key, iv);
			// cipher.init(Cipher.DECRYPT_MODE, key, null);

			// String decrypted = new String(cipher.doFinal(blockAes));
			// System.out.println("Decrypted: " + decrypted);

			// byte[] decoded = Base64.getDecoder().decode(new
			// String(blockAes));
		}
	}

}
