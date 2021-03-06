/*
 * Copyright © 2018 Coda Hale (coda.hale@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.org.policena.trydecrypt;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * An implementation of the {@link BalloonHash} algorithm.
 *
 * @see <a href="https://eprint.iacr.org/2016/027.pdf">Balloon Hashing: A Memory-Hard Function
 *     Providing Provable Protection Against Sequential Attacks</a>
 */
public class BalloonHash {

  private static final byte[] NULL = new byte[0];
  private static final int DELTA = 3; // number of dependencies per block / graph depth

  private final MessageDigest h;
  private final int n, r, p, l;

  /**
   * Create a new {@link BalloonHash} instance with the given parameters.
   *
   * @param h the hash algorithm to use
   * @param n the space cost (in blocks)
   * @param r the time cost (in rounds)
   * @param p the parallelism cost (in threads)
   */
  public BalloonHash(MessageDigest h, int n, int r, int p) {
    this.h = h;
    h.reset();
    this.l = h.getDigestLength();

    if (n < 1) {
      throw new IllegalArgumentException("n must be greater than the digest length");
    }
    this.n = n + (n % 2);

    if (r < 1) {
      throw new IllegalArgumentException("r must be greater than or equal to 1");
    }
    this.r = r;

    if (p < 1) {
      throw new IllegalArgumentException("p must be greater than or equal to 1");
    }
    this.p = p;
  }

  /**
   * Returns the length of the resulting digest (in bytes).
   *
   * @return the length of the resulting digest (in bytes)
   */
  public int digestLength() {
    return l;
  }

  /**
   * Return the number of bytes required to hash a password.
   *
   * @return the number of bytes required to hash a password
   */
  public int memoryUsage() {
    return l * n;
  }

  /**
   * Returns the space cost (in hash blocks).
   *
   * @return the space cost (in hash blocks)
   */
  public int n() {
    return n;
  }

  /**
   * Returns the time cost (in iterations).
   *
   * @return the time cost (in iterations)
   */
  public int r() {
    return r;
  }

  /**
   * Returns the parallelism cost (in threads).
   *
   * @return the parallelism cost (in threads)
   */
  public int p() {
    return p;
  }

  /**
   * Hashes the given password and salt.
   *
   * @param password a password of arbitrary length
   * @param salt a salt of at least 4 bytes
   * @return the hash balloon digest
   */
  public byte[] hash(byte[] password, byte[] salt) {
    if (salt.length < 4) {
      throw new IllegalArgumentException("salt must be at least 4 bytes long");
    }

    if (p == 1) {
      return singleHash(h, password, salt, 1);
    }

    return IntStream.rangeClosed(1, p)
        .parallel()
        .mapToObj(id -> singleHash(newHash(), password, salt, id))
        .reduce(
            new byte[l],
            (a, b) -> {
              // combine all hashes by XORing them together
              var c = new byte[a.length];
              for (var i = 0; i < a.length; i++) {
                c[i] = (byte) (a[i] ^ b[i]);
              }
              return c;
            });
  }

  private byte[] singleHash(MessageDigest h, byte[] password, byte[] salt, int id) {
    // encode worker ID and params in with the salt
    var seed = seed(salt, id);
    var cnt = 0; // the counter used in the security proof
    var cntBlock = new byte[4];
    var v = new byte[l];
    var idxBlock = new byte[12];
    var buf = new byte[n][l];

    // Step 1. Expand input into buffer.
    hash(h, cnt++, cntBlock, password, seed, buf[0]);
    for (var m = 1; m < buf.length; m++) {
      hash(h, cnt++, cntBlock, buf[m - 1], NULL, buf[m]);
    }

    // Step 2. Mix buffer contents.
    for (var t = 0; t < r; t++) {
      for (var m = 0; m < buf.length; m++) {
        // Step 2a. Hash last and current blocks.
        var prev = buf[(int) ((m - 1 & 0xffffffffL) % (buf.length & 0xffffffffL))];
        hash(h, cnt++, cntBlock, prev, buf[m], buf[m]);

        // Step 2b. Hash in pseudorandomly chosen blocks.
        for (var i = 0; i < DELTA; i++) {
          idxBlock[0] = (byte) t;
          idxBlock[1] = (byte) (t >>> 8);
          idxBlock[2] = (byte) (t >>> 16);
          idxBlock[3] = (byte) (t >>> 24);
          idxBlock[4] = (byte) m;
          idxBlock[5] = (byte) (m >>> 8);
          idxBlock[6] = (byte) (m >>> 16);
          idxBlock[7] = (byte) (m >>> 24);
          idxBlock[8] = (byte) i;
          idxBlock[9] = (byte) (i >>> 8);
          idxBlock[10] = (byte) (i >>> 16);
          idxBlock[11] = (byte) (i >>> 24);

          hash(h, cnt++, cntBlock, seed, idxBlock, v);
          var other = (v[0] & 0xff);
          other |= (v[1] & 0xff) << 8;
          other |= (v[2] & 0xff) << 16;
          other |= (v[3] & 0xff) << 24;
          other = (int) ((other & 0xffffffffL) % (buf.length & 0xffffffffL));

          hash(h, cnt++, cntBlock, buf[m], buf[other], buf[m]);
        }
      }
    }

    // Step 3. Extract output from buffer.
    return buf[buf.length - 1];
  }

  private byte[] seed(byte[] salt, int id) {
    var seed = Arrays.copyOfRange(salt, 0, salt.length + 16);

    // add parameters
    var idx = salt.length;
    seed[idx++] = (byte) n;
    seed[idx++] = (byte) (n >>> 8);
    seed[idx++] = (byte) (n >>> 16);
    seed[idx++] = (byte) (n >>> 24);
    seed[idx++] = (byte) r;
    seed[idx++] = (byte) (r >>> 8);
    seed[idx++] = (byte) (r >>> 16);
    seed[idx++] = (byte) (r >>> 24);
    seed[idx++] = (byte) p;
    seed[idx++] = (byte) (p >>> 8);
    seed[idx++] = (byte) (p >>> 16);
    seed[idx++] = (byte) (p >>> 24);
    seed[idx++] = (byte) id;
    seed[idx++] = (byte) (id >>> 8);
    seed[idx++] = (byte) (id >>> 16);
    seed[idx] = (byte) (id >>> 24);
    return seed;
  }

  private void hash(MessageDigest h, int cnt, byte[] cntBlock, byte[] a, byte[] b, byte[] out) {
    cntBlock[0] = (byte) cnt;
    cntBlock[1] = (byte) (cnt >>> 8);
    cntBlock[2] = (byte) (cnt >>> 16);
    cntBlock[3] = (byte) (cnt >>> 24);

    try {
      h.update(cntBlock);
      h.update(a);
      h.update(b);
      h.digest(out, 0, out.length);
    } catch (DigestException e) {
      throw new RuntimeException(e);
    }
  }

  private MessageDigest newHash() {
    try {
      return MessageDigest.getInstance(h.getAlgorithm(), h.getProvider());
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}
