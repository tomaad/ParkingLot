package services.command;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileCommandReader implements Closeable {
    private BufferedReader bufferedReader;

    public FileCommandReader(String fileLocation) throws FileNotFoundException {
        this.bufferedReader = new BufferedReader(new FileReader(new File(fileLocation)));
    }

    public FileCommandReader(InputStream inputStream) {
        this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    }

    public String read() throws IOException {
        return bufferedReader.readLine();
    }

    @Override
    public void close() throws IOException {
        bufferedReader.close();
    }
}
