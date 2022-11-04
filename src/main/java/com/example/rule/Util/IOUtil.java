package com.example.rule.Util;

import com.example.rule.Model.PO.RuleStructureResPO;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class IOUtil {
    public static Object readObject(File file) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(Files.newInputStream(file.toPath()));
        Object object = objectInputStream.readObject();
        objectInputStream.close();
        return object;
    }

    public static void writeObject(File file, Object object) throws IOException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(Files.newOutputStream(file.toPath()));
        objectOutputStream.writeObject(object);
        objectOutputStream.close();
    }

    public static void writeLines(File file, List<String> lines) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
        for (String line : lines) {
            bufferedWriter.write(line);
        }
        bufferedWriter.close();
    }
}
