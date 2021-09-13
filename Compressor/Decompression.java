package Compressor;

import Compressor.includes.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class Decompression extends IO {

    // Value Constructor
    Decompression(String i_inputPath, String i_outputPath) {
        this.inputPath = i_inputPath; // Assign file input path as a variable.
        this.outputPath = i_outputPath; // Assign file output path as a variable.

        // Initialize input stream.
        try {
            inputStream = new FileInputStream(inputPath);
        } catch (FileNotFoundException e) {
            System.out.printf("\nFile not found under the path:%s.\n", inputPath);
        }

        // Initialize output stream.
        try {
            outputStream = new FileOutputStream(outputPath);
        } catch (FileNotFoundException e) {
            System.out.printf("\nFile not found under the path:%s.\n", inputPath);
        }


        // Create bit readers and writers.
        bitInputStream = new BitInputStream(inputStream);
        bitOutputStream = new BitOutputStream(outputStream);

    }

//    // Public functions
//    public void decompressFile_old() {
//
//        // Variables
//        final char EOF = '\uFFFF';
//        String bytesRead = "";
//        String originalBytesString = "";
//        int compressionKey = 256;
//        String knownPartOfTheCompression = "";
//        HashMap<Integer, String> dictionary = new HashMap<>();
//
//        // Read compression size from file
//        try {
//            this.bitsToRead = (short) (((short) bitInputStream.readBits(8)) + 1);
//            System.out.printf("\nNumber of bits to read: %d",this.bitsToRead);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        final int BITS_TO_WRITE = this.bitsToRead - 1;
//
//        while(true) {
//            try {
//                // Read byte, check if we reached EOF.
//                bytesRead = "" + (char) bitInputStream.readBits(bitsToRead);
//
//                System.out.printf("\n[DEBUG] Bytes from file: \"%s\"",bytesRead);
//
//                if(bytesRead.charAt(0) == EOF) // End of file check
//                    break;
//
//
//                // Concat bytes
//                originalBytesString = originalBytesString.concat(bytesRead);
//
//                int hashKey = originalBytesString.charAt(0);
//
//                System.out.printf("\n[DEBUG] Concated bytes: %s | HashKey: %d",originalBytesString
//                        , hashKey);
//
//                if(dictionary.containsKey(hashKey) && knownPartOfTheCompression.length() == 0){ // We known this byte
//                    knownPartOfTheCompression = dictionary.get(hashKey);
//
//                    System.out.printf("\n[DEBUG] I already know those bytes! Their hash is: %s",knownPartOfTheCompression);
//
//                } else { // We don't know this byte
//                    dictionary.put(compressionKey, knownPartOfTheCompression + bytesRead);
//
//                    System.out.printf("\n[DEBUG] I have added to the dictionary: [%d] \"%s\"",compressionKey, knownPartOfTheCompression + bytesRead);
//
//                    String outputString = dictionary.get(compressionKey);
//
//
//                   for(int i = 0; i < outputString.length(); i++){
//                       bitOutputStream.writeBits(BITS_TO_WRITE, (int) outputString.charAt(i));
//                   }
//
//                    System.out.printf("\n[DEBUG] I have written to the file: %s",dictionary.get(compressionKey));
//
//
//                    compressionKey++; // Increment the next compression hash.
//                    originalBytesString = "";
//                    knownPartOfTheCompression = "";
//                }
//
//                // Reset reader.
//                bytesRead = "";
//                System.out.println();
//            }
//
//            catch (IOException e) {
//                System.out.println("[ERROR] Unable to read/write bytes from/to file.");
//            }
//        }
//
//        // Check for the last byte
//        if(knownPartOfTheCompression.length() > 0){
//
//            for(int i = 0; i < knownPartOfTheCompression.length(); i++){
//                bitOutputStream.writeBits(BITS_TO_WRITE, (int) knownPartOfTheCompression.charAt(i));
//            }
//        }
//
//        bitOutputStream.flush();
//        System.out.println("\n~ [DEBUG] Finished decompressing file!");
//
//    }

    public void decompressFile() {
        // Declarations & Variables
        final char EOF = '\uFFFF';
        final int EOF_INT = -1;
        final int LOAD_OPERATION = 1;
        final int SAVE_OPERATION = 0;

        int currentDictionaryKey = 0;
        HashMap<Integer, String> dictionary = new HashMap<>();
        String knownPartOfTheCompression = "";

        String readByte = "";
        int readByte_Int = 0;
        String dataFromFile = "";
        int dataFromFile_Int = 0;

        System.out.println("~ Decompression started.");

        // Read the compression mode from file
        try {
            this.userSelectedCompressionMode = bitInputStream.readBits(2);
            this.compressionSize = COMPRESSION_MODE[userSelectedCompressionMode];
            this.READ_WRITE_CYCLES = this.compressionSize / 8;

        } catch (IOException e) {
            System.out.println("[ERROR] Cannot read file.");
        }

        System.out.printf("[DEBUG] File encoded in mode %d [%d Bits].\n", this.userSelectedCompressionMode, this.compressionSize);

        while (true) { // Read file loop
            try {
                System.out.println();

                final int OPERATION = bitInputStream.readBits(1);

                if(OPERATION == EOF_INT)
                    break;

                System.out.printf("[DEBUG] Operation [%d - %s operation].\n",OPERATION, getOperationName(OPERATION));

                if (OPERATION == SAVE_OPERATION) {
                    for (int i = 0; i < READ_WRITE_CYCLES; i++) {
                        readByte = "" + (char) bitInputStream.readBits(BYTE);

                        if (readByte.charAt(0) == EOF) // End of file check
                            break;

                        dataFromFile = dataFromFile.concat(readByte);
                    }

                    System.out.printf("[DEBUG] Read \"%s\".\n",dataFromFile);

                    if (dataFromFile.charAt(0) == EOF) // End of file check
                        break;


                    System.out.printf("[DEBUG] Writing \"%s\" to decompressed file.\n",dataFromFile);
                    // Write N amount of bytes to decompressed file
                    // Variables
                    String dataByte = "";

                    for (int i = 0; i < READ_WRITE_CYCLES; i++) {
                        try {
                            dataByte = dataFromFile.substring(0, 8);
                        } catch (java.lang.StringIndexOutOfBoundsException e) {
                            dataByte = dataFromFile.substring(0, dataFromFile.length());
                        }

                        for(int j = 0; j < dataByte.length(); j++){
                            bitOutputStream.writeBits(BYTE, (int) dataByte.charAt(j));
                        }

                        try {
                            dataFromFile = dataFromFile.substring(8, dataFromFile.length());
                        } catch (java.lang.StringIndexOutOfBoundsException e) {
                            break;
                        }

                    }
                } else if (OPERATION == LOAD_OPERATION) {
//                    for (int i = 0; i < READ_WRITE_CYCLES; i++) {
//                        readByte = "" + (char) bitInputStream.readBits(BYTE);
//
//                        if (readByte.charAt(0) == EOF) // End of file check
//                            break;
//
//                        dataFromFile = dataFromFile.concat(readByte);
//                    }
                    // Reset variable
                    readByte = "";

                    for(int i = 0; i < READ_WRITE_CYCLES; i++){
                        readByte_Int = bitInputStream.readBits(BYTE);

                        if(readByte_Int == EOF_INT)
                            break;

                        readByte = readByte.concat(Integer.toBinaryString(readByte_Int));

                        System.out.printf("[DEBUG] Reading %d, Binary %s.\n", readByte_Int, readByte);
                    }



                    if (readByte.charAt(0) == EOF || readByte_Int == EOF_INT) // End of file check
                        break;

                    dataFromFile_Int = Integer.parseInt(readByte,2);

                    System.out.printf("[DEBUG] Loading from position [%d].\n",dataFromFile_Int);

                    // Load data from dictionary
                    knownPartOfTheCompression = dictionary.get(dataFromFile_Int);

                    System.out.printf("[DEBUG] Loaded \"%s\" from dictionary.\n", knownPartOfTheCompression);
                    dataFromFile = ""; // Reset variable

                    for (int i = 0; i < READ_WRITE_CYCLES; i++) {
                        readByte = "" + (char) bitInputStream.readBits(BYTE);

                        if (readByte.charAt(0) == EOF) // End of file check
                            break;

                        dataFromFile = dataFromFile.concat(readByte);
                    }

                    System.out.printf("[DEBUG] Read \"%s\", entire string %s.\n", dataFromFile, knownPartOfTheCompression + dataFromFile);
                    // Save the entire value to dictionary
                    dataFromFile = knownPartOfTheCompression + dataFromFile;


                    System.out.printf("[DEBUG] Writing \"%s\" to decompressed file.\n",dataFromFile);
                    // Write N amount of bytes to decompressed file
                    // Variables
                    String dataByte = "";

                    for(int i = 0; i < dataFromFile.length(); i++){
                        bitOutputStream.writeBits(BYTE, (int) dataFromFile.charAt(i));
                    }
//
//                    for (int i = 0; i < READ_WRITE_CYCLES; i++) {
//                        try {
//                            dataByte = dataFromFile.substring(0, 7);
//                        } catch (java.lang.StringIndexOutOfBoundsException e) {
//                            dataByte = dataFromFile.substring(0, dataFromFile.length());
//                        }
//
//
//
//                        bitOutputStream.writeBits(BYTE, (int) dataByte.charAt(0));
//
//                        try {
//                            dataFromFile = dataFromFile.substring(8, dataFromFile.length());
//                        } catch (java.lang.StringIndexOutOfBoundsException e) {
//                            break;
//                        }
//
//                    }

                }

                // Save bytes to dictionary
                System.out.printf("[DEBUG] Saving data \"%s\" [%d].\n ",dataFromFile,currentDictionaryKey);
                dictionary.put(currentDictionaryKey, dataFromFile);

                // Adjust variables
                currentDictionaryKey++;
                dataFromFile = "";
                knownPartOfTheCompression = "";
                if (readByte.charAt(0) == EOF) // End of file check
                    break;

            } // End of while loop
            catch (IOException e) {
                System.out.println("[ERROR] Cannot read file.");
                break;
            }
        }

        System.out.println("\n ~ [DEBUG] Reached EOF.\n");
        bitOutputStream.flush();
        bitOutputStream.close();
        bitInputStream.close();
    }

    private String getOperationName(final int i_operation) {
        if(i_operation == 0)
            return "Save";
        else
            return "Load";
    }
}
