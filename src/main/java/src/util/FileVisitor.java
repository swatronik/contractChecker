package src.util;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FileVisitor extends SimpleFileVisitor<Path> {

    public static List<String> allPaths = new ArrayList<>();

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        allPaths.add(dir.toString());
        return super.postVisitDirectory(dir, exc);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        allPaths.add(file.getParent().toString() + "/" + file.getFileName().toString());
        return FileVisitResult.CONTINUE;
    }

    public static List<String> getAllPaths() {
        return Lists.reverse(allPaths);
    }
}
