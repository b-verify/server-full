package crpyto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class CryptographicSignature {
	
	// we use ECDSA on the curve secp256k1
	public static final String SIGNATURE_ALGO = "NONEwithECDSA";
	public static final String TYPE = "EC";
	public static final String CURVE = "secp256k1";
	
	public static KeyPair generateNewKeyPair() {
		try {
			// secp256k1 curve ECDSA
			KeyPairGenerator kpg = KeyPairGenerator.getInstance(TYPE);
			ECGenParameterSpec kpgparams = new ECGenParameterSpec(CURVE);
			kpg.initialize(kpgparams);
			KeyPair key = kpg.generateKeyPair();
			return key;
		} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public static void savePublicKey(String file, PublicKey pubKey) {
		// we save the pubkey using x.509 encoding 
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
				pubKey.getEncoded());
		try {
			File f = new File(file);
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(x509EncodedKeySpec.getEncoded());
			fos.close();
		}catch(Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public static void savePrivateKey(String file, PrivateKey privKey) {
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
				privKey.getEncoded());
		try{
			File f = new File(file);
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(pkcs8EncodedKeySpec.getEncoded());
			fos.close();
		}catch(Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public static PublicKey loadPublicKey(String file) {
		try {
			File f = new File(file);
			FileInputStream fis = new FileInputStream(f);
			byte[] encoded = new byte[(int) f.length()];
			fis.read(encoded);
			fis.close();
			KeyFactory keyFactory = KeyFactory.getInstance(TYPE);
			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
					encoded);
			PublicKey pubKey = keyFactory.generatePublic(publicKeySpec);
			return pubKey;
		}catch(Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public static PrivateKey loadPrivateKey(String file) {
		try {
			File f = new File(file);
			FileInputStream fis = new FileInputStream(f);
			byte[] encoded = new byte[(int) f.length()];
			fis.read(encoded);
			fis.close();
			KeyFactory keyFactory = KeyFactory.getInstance(TYPE);
			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
					encoded);
			PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
			return privateKey;
		}catch(Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public static byte[] sign(byte[] message, PrivateKey privKey) {
		try {
			Signature dsa = Signature.getInstance(SIGNATURE_ALGO);
			dsa.initSign(privKey);
			dsa.update(message);
			byte[] output = dsa.sign();
			return output;
			
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public static boolean verify(byte[] message, byte[] signature, PublicKey pubKey) {
		try {
			Signature dsa = Signature.getInstance(SIGNATURE_ALGO);
			dsa.initVerify(pubKey);
			dsa.update(message);
			return dsa.verify(signature);
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
			return false;
		}
		
	}
	
}



