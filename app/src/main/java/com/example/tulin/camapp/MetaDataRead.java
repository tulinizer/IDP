package com.example.tulin.camapp;


import android.os.Environment;
import android.util.Log;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.boxes.apple.AppleNameBox;
import com.googlecode.mp4parser.util.Path;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Change metadata and make sure chunkoffsets are corrected.
 */
public class MetaDataRead {

    //mediaStorageDir.getPath() + File.separator + fileName;

    public MetaDataRead() throws IOException {

//      MetaDataRead cmd = new MetaDataRead();
      //  read(path);
     //   System.err.println(xml);

    }

    public String read(String videoFilePath) throws IOException {

        File videoFile = new File(videoFilePath);
        if (!videoFile.exists()) {
            throw new FileNotFoundException("File " + videoFilePath + " not exists");
        }

        if (!videoFile.canRead()) {
            throw new IllegalStateException("No read permissions to file " + videoFilePath);
        }

     //   FileInputStream fileInputStream = new FileInputStream("/home/sannies2/Mission_Impossible_Ghost_Protocol_Feature_SDUV_480p_16avg192max.uvu");
      //  IsoFile isoFile = new IsoFile(path);
      //  IsoFile isoFile = new IsoFile(path);

    //    AppleNameBox nam = Path.getPath(isoFile, "/moov[0]/udta[0]/meta[0]/ilst/©nam");
     //   String xml = nam.getValue();
      //  isoFile.close();


      //  FileInputStream fileInputStream = new FileInputStream("/home/sannies2/Mission_Impossible_Ghost_Protocol_Feature_SDUV_480p_16avg192max.uvu");
       // IsoFile isoFile = new IsoFile(fileInputStream.getChannel());

        IsoFile isoFile = new IsoFile(videoFilePath);

        AppleNameBox nam = Path.getPath(isoFile, "moov[0]/udta[0]/meta[0]/ilst/©nam");
        if(isoFile == null)
            Log.d("iso file", "null");

        String xml = nam.getValue();
        isoFile.close();

        System.err.println(xml);
        return xml;
    }
}