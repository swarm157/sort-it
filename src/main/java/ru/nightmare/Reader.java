package ru.nightmare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * Reader служит для последовательного считывания файла с одного конца к другому построчно.
 */
public class Reader {

    // Имя файла указанного для считывания
    protected String fileName;
    private File file;

    private static final Logger logger = LoggerFactory.getLogger(Reader.class);


    // Канал через который происходит считывание
    FileChannel in;

    // текущая позиция в файле
    private long position;

    /**
     * Буфферизованная версия данных из метода read
     * Если файл закончился, программа выдаст null на попытку считывания из буфера более одного раза
     * чтобы избежать зависания в некоторых случаях.
     * Если буфер пуст, также возвращает null
     * @return
     * @throws IOException
     */

    public String getBuffer() throws IOException {
        String temp = buffer;
        if (isEnded()||isClosed()) {
            buffer = null;
        }
        return temp;
    }

    private String buffer;


    /**
     * Конструктор считывателя, принимает вкачестве аргумента имя файла для чтения
     * Проверяет файл на возможность чтения, если это невозможно, выдает ошибку и закрывает поток.
     * @param fileName
     * @throws IOException
     */
    public Reader(String fileName) throws IOException {
        this.fileName = fileName;
        file = new File(fileName);
        if (!file.canRead()) {
            logger.error("Ошибка " + fileName + " недоступен для чтения");
            close();
        }
        in = FileChannel.open(file.toPath(), READ);
        position = Boot.forwardSort?0:in.size()-1;
    }


    /**
     * Считывает каждую строку файла
     * С начала к концу или наоборот
     * Алгоритм извлекает данные посимвольно, выдает в результате целую строку
     * Результат сохраняется в локальный буфер для дальнейшего использования
     * Чистит строки от спецсиволом \r и \n
     * @return
     * @throws IOException
     */
    public String read() throws IOException {
        if (!isClosed()) {
            String line = "";
            while (!line.contains("\n") && !isEnded()) {
                in.position(position);
                ByteBuffer result = ByteBuffer.allocate(1);
                in.read(result);
                if (Boot.forwardSort)
                    position++;
                else position--;
                result.flip();
                byte[] data = new byte[result.remaining()]; // Создаем массив для хранения считанных данных
                result.get(data); // Заполняем массив данными из ByteBuffer
                String resultString = new String(data); // Преобразуем массив байтов в строку
                line += resultString;
            }
            if (!Boot.forwardSort) {
                StringBuilder reversedString = new StringBuilder(line).reverse();
                line = reversedString.toString();
            }
            line = line.replace("\n", "");
            line = line.replace("\r", "");
            buffer = line;
            return line;
        }
        return "";
    }

    /**
     * Проверяет позицию и сообщает дошел поток до конца файла или нет, согласно направлению считывания.
     * @return
     * @throws IOException
     */

    public boolean isEnded() throws IOException {
        //System.out.println(position);
        //System.out.println(in.size()-1);
        return Boot.forwardSort?position>=in.size()-1:position<=0;
    }

    /**
     * Закрывает поток, более использовать этот обьект для дальнейшего чтения невозможно
     * @throws IOException
     */
    public void close() throws IOException {
        in.close();
    }
    /**
     * Метод вызываемый для проверки закрытости потока
     */
    public boolean isClosed() {
        return !in.isOpen();
    }

    /**
     * Возвращает имя файла с которого читаются данные
     * @return
     */
    public String getFileName() {
        return fileName;
    }
}
