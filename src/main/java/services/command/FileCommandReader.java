package services.command;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FileCommandReader implements Closeable {
    private String fileLocation;
    private BufferedReader bufferedReader;

    public FileCommandReader(String fileLocation) throws FileNotFoundException {
        this.fileLocation = fileLocation;
        this.bufferedReader = new BufferedReader(new FileReader(new File(fileLocation)));
    }

    public String read() throws IOException {
        return bufferedReader.readLine();
    }

    @Override
    public void close() throws IOException {
        bufferedReader.close();
    }
}
