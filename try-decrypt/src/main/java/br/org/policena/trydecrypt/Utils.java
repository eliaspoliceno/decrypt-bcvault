/**
 * 
 */
package br.org.policena.trydecrypt;

import org.mozilla.universalchardet.UniversalDetector;

/**
 * @since Jul 12, 2021
 *
 */
public class Utils {
	
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

}
