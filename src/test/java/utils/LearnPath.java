package utils;

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import static java.nio.file.StandardOpenOption.*;

import static java.nio.file.StandardCopyOption.*;

/**
 * Created by swqsh on 2017/6/20.
 */
public class LearnPath {
    private static Logger logger=Logger.getLogger(LearnPath.class);

    public static void main(String[] args) throws IOException {
        String purgeroot="purgeroot";
        Path purgerootPath= Paths.get(System.getProperty("user.dir"),purgeroot);
        boolean existed= Files.exists(purgerootPath);
        if(!existed)
            logger.info(purgerootPath.toString()+" existed:"+existed);
        try {
            if(!Files.isDirectory(purgerootPath))
                Files.createDirectory(purgerootPath);
        } catch (IOException e) {
            e.printStackTrace();
            logger.warn("can't create purgeroot");
        }
        Path data=Paths.get(System.getProperty("user.dir"),purgeroot,"data");
        /*Path dataTemp=Paths.get(System.getProperty("user.dir"),purgeroot,"dataTemp");
        if(!Files.exists(data))
            Files.createFile(data);
        if(!Files.exists(dataTemp))
            Files.createFile(dataTemp);
        BufferedWriter writer=Files.newBufferedWriter(dataTemp);
        writer.write("hello");
        writer.flush();
        Files.move(dataTemp,data,REPLACE_EXISTING);*/
        if(Files.exists(data)){
            SeekableByteChannel channel=Files.newByteChannel(data,APPEND);
            ByteBuffer buffer=ByteBuffer.wrap("are you ok".getBytes());
            channel.write(buffer);

        }
    }

}
