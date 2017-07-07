package  com.setl.ed25519;
public class Ed25519DonnaJNI {
   static {
      // hello.dll on Windows or libhello.so on Linux or libhello.dylib on Mac
      System.loadLibrary("ed25519-donna"); 
   }
   public native void testCryptoLibrary();
   
   public byte[] createPrivateKey() {
     return createRandomBytes(32);
   }

   public native byte[] createRandomBytes(int size);

   /** create a public key for a given private key */
   public native byte[] createPublicKey(byte[] privatekey);
   
   /** sign a message 
    * @param message
    * @param privateKey
    * @param publicKey
    * @return the 64-byte signature
    */
   public native byte[] createSignature(byte[] message,
                                        byte[] privateKey,
                                        byte[] publicKey);

   /**
    * Verify a signature
    * 
    * @param message the message
    * @param publicKey the public key
    * @param signature the 64-byte signature
    */
   public native boolean verifySignature(byte[] message,
                                         byte[] publicKey,
                                         byte[] signature);
   

   public native boolean batchVerifySignature(byte[][] message,
                                              int[] messageLen,
                                              byte[][] publicKey,
                                              byte [][]signature);


}