package src.util;

import io.restassured.response.Response;
import org.apache.commons.io.FileUtils;
import org.zeroturnaround.zip.ZipUtil;
import src.service.QueryEntity;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileUtil {

    private static String pathFolder;

    public static void createFolder(QueryEntity queryEntity) {
        try {
            String[] path = queryEntity.getPathFolder().split("/");
            try {
                Files.createDirectory(Paths.get(String.format("/%s", path[1])));
            } catch (FileAlreadyExistsException ignored) {
            }
            try {
                Files.createDirectory(Paths.get(String.format("/%s/%s", path[1], path[2])));
            } catch (FileAlreadyExistsException ignored) {
            }
            try {
                Files.createDirectory(Paths.get(String.format("/%s/%s/%s", path[1], path[2], path[3])));
            } catch (FileAlreadyExistsException ignored) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Возвращает дерево папок и файлов List<String>
     *
     * @param path
     * @return
     */

    public static List<String> pathsTree(String path) {
        try {
            Files.walkFileTree(Paths.get(path), new FileVisitor());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(FileVisitor.getAllPaths());
        return FileVisitor.getAllPaths();
    }

    /**
     * Создает директории pathStr в случае их отсутствия
     *
     * @param pathStr
     */
    public static void createFolderFromString(String pathStr) {
        String[] path = pathStr.split("/");
        if (Files.notExists(Paths.get(pathStr))) {
            String pathFull = path[0] + "/";
            for (int i = 1; i < path.length; i++) {
                pathFull = pathFull + path[i] + "/";
                try {
                    Files.createDirectory(Paths.get(pathFull));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Создает файл с именем fileName по пути pathStr с содержанием fileText
     *
     * @param pathStr
     * @param fileName
     * @param fileText
     */
    public static void createFileFromListString(String pathStr, String fileName, List<String> fileText) {
        if (Files.notExists(Paths.get(pathStr + "/" + fileName))) {
            try {
                Files.createFile(Paths.get(pathStr + "/" + fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Path file = Paths.get(pathStr + "/" + fileName);
        try {
            Files.write(file, fileText, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Создает файл с именем fileName по пути pathStr с содержанием fileText
     *
     * @param pathStr
     * @param fileName
     * @param fileText
     */
    public static void createFileFromString(String pathStr, String fileName, String fileText) {
        ArrayList<String> fileTextList = new ArrayList<>();
        fileTextList.add(fileText);
        createFileFromListString(pathStr, fileName, fileTextList);
    }

    /**
     * Архивирует папку path в архив с именем fileName
     *
     * @param path
     * @param fileName
     */
    public static void createZipFromPath(String path, String fileName) {
        String pathToArchive = "data/archive/";
        createFolderFromString(pathToArchive);
        ZipUtil.pack(new File(path), new File(pathToArchive + fileName));
    }

    /**
     * удаляет все по пути path
     *
     * @param path
     */
    public static void delPath(String path) {
        try {
            File file = new File(path);
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Возвращает содержимое файла path в списке List<String>
     *
     * @param path
     * @return
     */
    public static String fileReader(String path) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(path));
            StringBuilder stringBuilder = new StringBuilder();
            for (String line : lines) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Перемещает папку со всем содержимым
     *
     * @param from
     * @param to
     */
    public static void move(String from, String to) {
        Path fromPath = Paths.get(from);
        Path toPath = Paths.get(to);
        createFolderFromString(to);
        validate(fromPath);
        try {
            Files.walkFileTree(fromPath, new CopyDirVisitor(fromPath, toPath));
            Files.walkFileTree(fromPath, new DeleteDirVisitor());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void validate(Path... paths) {
        for (Path path : paths) {
            Objects.requireNonNull(path);
            if (!Files.isDirectory(path)) {
                throw new IllegalArgumentException(String.format("%s is not a directory", path.toString()));
            }
        }
    }


    public static void writeResponse(QueryEntity queryEntity, Response responseSravni, Response responseMock) throws IOException {
        File fileResponseSravni = new File(queryEntity.getPathFolder() + "responseSravni.txt");
        fileResponseSravni.createNewFile();
        try (FileWriter writer = new FileWriter(fileResponseSravni, false)) {
            writer.write(responseSravni.asString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        File fileResponseMock = new File(queryEntity.getPathFolder() + "responseMock.txt");
        fileResponseMock.createNewFile();
        try (FileWriter writer = new FileWriter(fileResponseMock, false)) {
            writer.write(responseMock.asString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
