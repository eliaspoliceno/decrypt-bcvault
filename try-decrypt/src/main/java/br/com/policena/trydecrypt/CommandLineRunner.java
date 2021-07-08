/**
 * 
 */
package br.com.policena.trydecrypt;

import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @since Jul 5, 2021
 *
 */
@SpringBootApplication
public class CommandLineRunner implements org.springframework.boot.CommandLineRunner {

	private static final Boolean RUN = Boolean.FALSE;

	@Override
	public void run(String... args) throws Exception {
		if (RUN) {
			byte[] fileData = AESUtil.loadFileBytesLittleEndian();

			SecretKey key = AESUtil.getKeyFromPassword("1234", "2341");
			IvParameterSpec iv = AESUtil.generateIv();

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, key, iv);

			for (int size = 16; size < 17; ++size) {

				for (int offset = 0; offset + size < fileData.length; ++offset) {
					iv = new IvParameterSpec(fileData, offset, size);

					for (int z = offset + size + 1; z + offset + size < fileData.length; ++z) {
						byte[] cipherText = Arrays.copyOfRange(fileData, z, fileData.length);

						try {
							String decrypted = new String(cipher.doFinal(Base64.getDecoder().decode(cipherText)));
							System.out.println(String.format("\n\n\n\n\nDecodificou! No offset=[%d], size=[%d], z=[%d]",
									offset, size, z));
							System.out.println("Valor decodificado: \"" + decrypted + "\"");
							break;
						} catch (Exception ex) {
							continue;
						}
					}
				}
			}

			System.out.println("Arquivo carregado");
		}
	}

}
