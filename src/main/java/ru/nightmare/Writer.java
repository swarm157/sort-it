package ru.nightmare;

import org.apache.commons.io.FileSystem;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

import static java.nio.file.StandardOpenOption.WRITE;


/**
 * Writer - класс предназначенный для удобной записи данных в указанный файл.
 */
public class Writer {

    // Имя файла
    protected String fileName;

    // Файл
    private File file;

    // Поток для записи в файл
    private FileChannel out;

    // Логгер для удобного логгирования
    private static final Logger logger = LoggerFactory.getLogger(Writer.class);


    /**
     *
     * @param fileName имя файла поток записи для которого программа должна открыть.
     * @throws IOException Не должно вылететь известным способом
     */
    public Writer(String fileName) throws IOException {
        this.fileName = fileName;
        file = new File(fileName);
        if (!file.canWrite()) {
            logger.error("Ошибка " + fileName + " недоступно для записи");
            System.exit(-8);
        }
        if (file.exists()&&!file.isDirectory()) file.delete();
        if (!file.exists()) {
            if (file.isDirectory()) {
                logger.error("Ошибка, c именем " + fileName + " уже существует директория");
                close();
            }
            try {
                FileUtils.touch(file);
                logger.debug("Файл "+fileName+" успешно создан");
            } catch (IOException e) {
                logger.error("Не могу создать файл, программа вынуждена завершиться из-за: " + e.getMessage());
                close();
            }
        }
        FileOutputStream outputStream = new FileOutputStream(String.valueOf(file.toPath()), true);
        out = outputStream.getChannel();
        //out = FileChannel.open(file.toPath(), WRITE);
    }

    /**
     *
     * @param line Строка которая будет добавлена в самый конец файла
     * @throws IOException Ошибка, может вылететь в случае отсутствия доступного места на жестком диске
     */
    public void append(String line) throws IOException {
        if (!isClosed()) {
            out.position(out.size() - 1 >= 0 ? out.size() - 1 : 0);
            try {
                out.write(ByteBuffer.wrap(line.getBytes(StandardCharsets.UTF_8)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Метод вызываемый для закрытия файла в потоке.
     * После этого добавлять новые строки в экземпляр Writer невозможно
     * @throws IOException
     */
    public void close() throws IOException {
        out.close();
    }

    /**
     * Метод вызываемый для проверки закрытости потока
     */
    public boolean isClosed() {
        return !out.isOpen();
    }

    /**
     * Возвращает имя указанного для записи файла
     * @return
     */
    public String getFileName() {
        return fileName;
    }
}
