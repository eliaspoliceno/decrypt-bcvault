/**
 * 
 */
package br.org.policena.trydecrypt;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @since Jul 6, 2021
 *
 */
public class AESUtil {

	private static final String path = "../backups/05-bk_20210707_094509_dAk7WAiaK4658hd8k5i.bin";
	public static final String CIPHER_INSTANCE = "AES/CBC/PKCS5PADDING";
	public static final String CIPHER_INSTANCE_PCBC = "AES/PCBC/PKCS5PADDING";
	public static final String CIPHER_INSTANCE_CBC_NOPADDING = "AES/CBC/NoPadding";
	public static final String CIPHER_INSTANCE_ECB = "AES/ECB/PKCS5PADDING";
	public static final String CIPHER_INSTANCE_CTR = "AES/CTR/PKCS5PADDING";
	public static final String CIPHER_INSTANCE_GCM = "AES/GCM/NoPadding";

	public static SecretKey getKeyFromPassword(String password, String salt)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
		// KeySpec spec = new PBEKeySpec(password.toCharArray(),
		// Base64.getEncoder().encode(salt.getBytes()), 65536, 256);
		SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
		return secret;
	}

	public static IvParameterSpec generateIv() {
		byte[] iv = new byte[16];
		new SecureRandom().nextBytes(iv);
		return new IvParameterSpec(iv);
	}

	public static byte[] loadFileBytesLittleEndian() throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		byte[] normalOrder = new byte[bytes.length];
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		int i = 0;
		while (bb.hasRemaining()) {
			normalOrder[i] = bb.get();
			++i;
		}
		return normalOrder;
	}

	public static byte[] loadFileBytesBigEndian() throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		byte[] normalOrder = new byte[bytes.length];
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.order(ByteOrder.BIG_ENDIAN);
		int i = 0;
		while (bb.hasRemaining()) {
			normalOrder[i] = bb.get();
			++i;
		}
		return normalOrder;
	}

	public static byte[] loadFileBytesNormal() throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		return bytes;
	}

	public static byte[] loadFileBytesReverse() throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		ArrayUtils.reverse(bytes);
		return bytes;
	}

}
