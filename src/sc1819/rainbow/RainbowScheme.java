package sc1819.rainbow;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import org.apache.commons.cli.*;

import sc1819.rainbow.util.GF16;

/**
 * This class contains all methods needed to perform the Rainbow signature scheme on a file.
 * It provides:
 * <ul>
 * <li>
 * a method for generating and writing a key pair onto a file;
 * </li>
 * <li>
 * a method for signing a file, that is, for generating a signature given the secret key;
 * </li>
 * <li>
 * a method for verifying the validity of a signature given the public key.
 * </li>
 * </ul>
 */
public class RainbowScheme {
	
	/**
	 * Generates a key pair. Writes both keys on files.
	 * @param pkPath the path of the file on which the public key is written
	 * @param skPath the path of the file on which the private key is written
	 */
	public static void keygen(String pkPath, String skPath) {
		RainbowKeyPair keys = new RainbowKeyPair(new SecureRandom());
		keys.saveKeys(pkPath, skPath);
	}
	
	/**
	 * Loads a private key from file and produces a signature for a given file.
	 * @param skPath the path of the file containing the secret key
	 * @param filePath the path of the file that is to be signed
	 * @param signaturePath the path of the signature generated
	 * @exception Exception
	 * @exception IOException
	 */
	public static void sign(String skPath, String filePath, String signaturePath) {
		RainbowSecKey sk = RainbowSecKey.loadKey(skPath);
		
		byte[] h = hashFile(filePath);
		h = sk.getS().evalInv(h);
		h = sk.getF().invF(h);
		h = sk.getT().evalInv(h);
		
		System.out.print("Rainbow(16,32,32,32) = ");
		System.out.println(GF16.toHex(h));
		
		FileOutputStream fout = null;
		ByteArrayOutputStream baos = null;

		try {
			fout = new FileOutputStream(signaturePath);
			baos = new ByteArrayOutputStream();
			baos.write(h, 0, h.length);
			baos.writeTo(fout);

			//System.out.println("Done");

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {

			if (fout != null) {
				try {
					fout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (baos != null) {
				try {
					baos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}
	
	/**
	 * Verifies that a file signature is valid.
	 * @param pkPath path to the file containing the public key
	 * @param filePath path to the signed file
	 * @param signaturePath path to the signature
	 * @return true if the signature is valid, false otherwise
	 * @exception FileNotFoundException if the signature path doesn't lead to a file
	 * @exception IOException 
	 */
	public static boolean verify(String pkPath, String filePath, String signaturePath) {
		RainbowPubKey pk = RainbowPubKey.loadKey(pkPath);
		
		byte[] h = hashFile(filePath);
		
		File signatureFile = new File(signaturePath);
		byte[] signature = new byte[(int)signatureFile.length()];
		DataInputStream dataIs = null;
		try {
			dataIs = new DataInputStream(new FileInputStream(signatureFile));
			dataIs.readFully(signature);
		} catch (FileNotFoundException ex) {
			System.out.println(signatureFile+" not found!");
			System.exit(1);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (dataIs != null) {
				try {
					dataIs.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		if (signature.length != 96) {
			System.out.println(signatureFile+" is not a valid signature!");
			System.exit(1);
		}
		byte[] k = pk.eval(signature);
		
		return Arrays.equals(h, k);
	}
	
	/**
	 * This methods computes the hash of a file using SHA-256. (this is needed to generate a file signature)
	 * @param fileName the path of the file to be hashed
	 * @return the hash of the file
	 */
	public static byte[] hashFile(String fileName) {
		byte[] buffer= new byte[8192];
	    int count;
	    MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	    BufferedInputStream bis;
		try {
			bis = new BufferedInputStream(new FileInputStream(fileName));
			while ((count = bis.read(buffer)) > 0) {
		        digest.update(buffer, 0, count);
		    }
		    bis.close();
		} catch (FileNotFoundException ex) {
			System.out.println(fileName+" not found!");
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		byte[] res = digest.digest();
		byte[] resHalf = new byte[64];
		
		for (int i = 0; i < 32; i++) {
			resHalf[i] = (byte)((res[i] & 240) >> 4);
			resHalf[i+32] = (byte)(res[i] & 15);
		}

	    return resHalf;
	}

	public static void main(String[] args) {
		//args = new String[] {"-keygen", "pk.txt", "sk.txt"};
		//args = new String[] {"-sign", "sk.txt", "pk.txt", "sign.txt"};
		//args = new String[] {"-verify", "pk.txt", "pk.txt", "sign.txt"};
		//args = new String[] {"-verify"};
		Options options = new Options();

		Option keygen = Option.builder("keygen")
				.argName("pk sk")
				.hasArg()
				.numberOfArgs(2)
				.valueSeparator(' ')
				.desc("Generate a rainbow key pair and stores it in <pk> and <sk>")
				.build();
		options.addOption(keygen);
		
		Option sign = Option.builder("sign")
				.argName("sk file signatureFile")
				.hasArgs()
				.numberOfArgs(3)
				.valueSeparator(' ')
				.desc("")
				.build();
		options.addOption(sign);
		
		Option verify = Option.builder("verify")
				.argName("pk file signatureFile")
				.hasArgs()
				.numberOfArgs(3)
				.valueSeparator(' ')
				.desc("")
				.build();
		options.addOption(verify);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("RainbowScheme", options, true);

			System.exit(1);
			return;
		}

		if (cmd.hasOption("keygen") && !cmd.hasOption("sign") && !cmd.hasOption("verify")) {
			String pkFileName = cmd.getOptionValues("keygen")[0];
			String skFileName = cmd.getOptionValues("keygen")[1];
			
			if (pkFileName.equals(skFileName)) {
				System.out.println("Enter different filenames for the keys!");
				System.exit(1);
			}
			
			System.out.println("Rainbow(16,32,32,32)");
			System.out.println("Hash size: 32 bytes");
			System.out.println("Signature size: 48 bytes");
			System.out.println("Generating keys...");
			RainbowKeyPair keys = new RainbowKeyPair(new SecureRandom());
			System.out.println("Keys generated");
			keys.saveKeys(pkFileName, skFileName);
			File pkFile = new File(pkFileName);
			System.out.println("Private key size: "+pkFile.length()+" bytes");
			File skFile = new File(skFileName);
			System.out.println("Public key size: "+skFile.length()+" bytes");
		} else if (!cmd.hasOption("keygen") && cmd.hasOption("sign") && !cmd.hasOption("verify")) {
			String skPath = cmd.getOptionValues("sign")[0];
			String filePath = cmd.getOptionValues("sign")[1];
			String signaturePath = cmd.getOptionValues("sign")[2];
			
			File sigFile = new File(signaturePath);
			if (sigFile.isFile()) {
				System.out.println(signaturePath+" is an existing file!");
				System.exit(1);
			}
			
			RainbowScheme.sign(skPath, filePath, signaturePath);
		} else if (!cmd.hasOption("keygen") && !cmd.hasOption("sign") && cmd.hasOption("verify")) {
			String pkPath = cmd.getOptionValues("verify")[0];
			String filePath = cmd.getOptionValues("verify")[1];
			String signaturePath = cmd.getOptionValues("verify")[2];
			
			if (RainbowScheme.verify(pkPath, filePath, signaturePath)) System.out.println("Rainbow(16,32,32,32) verification success");
			else System.out.println("Rainbow(16,32,32,32) verification fail");
		} else {
			formatter.printHelp("RainbowScheme", options, true);
		}
	}

}
