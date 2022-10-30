package com.example.rule.Util;

import com.example.rule.Model.PO.RuleStructureResPO;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
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
}
